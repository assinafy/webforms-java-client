# Assinafy Webforms Java Client SDK

*[Leia em português](README.md) · English*

Java 25 client for all 147 operations in the [Assinafy API](https://api.assinafy.com.br/v1/docs). The
`webforms` name is historical; new integrations should also consider `com.assinafy:assinafy-sdk`.

Detailed operation, request, and response documentation lives in [API_REFERENCE.md](docs/API_REFERENCE.md),
with runnable endpoint examples in [EXAMPLES.md](docs/EXAMPLES.md) and repository setup in
[INSTALLATION.md](docs/INSTALLATION.md).

## Install

The SDK requires Java 25+, Maven 3.9.16+ when using Maven, and TLS 1.2+.

```xml
<dependency>
    <groupId>com.assinafy</groupId>
    <artifactId>webforms-java-client-sdk</artifactId>
    <version>2.10.0</version>
</dependency>
```

```groovy
implementation 'com.assinafy:webforms-java-client-sdk:2.10.0'
```

Maven Central needs no repository credentials. GitHub Packages and Gradle Kotlin DSL are covered in the
installation guide.

## Configure the client

Create one thread-safe client per credential/account and reuse it. Keep credentials in environment variables
or secret storage; the SDK does not read `.env` files.

```java
AssinafyClient client = new AssinafyClient(new AssinafyClientOptions()
    .setApiKey(System.getenv("ASSINAFY_API_KEY"))
    .setAccountId(System.getenv("ASSINAFY_ACCOUNT_ID"))
    .setTimeoutMs(30_000)
    .setMaxRetries(2));
```

API keys are preferred over bearer tokens when both are set. `maxRetries` applies only to safe reads that
receive 429/503; uploads and every mutation are sent once. The production base URL is
`https://api.assinafy.com.br/v1`. Binary endpoints return `byte[]`; list endpoints return
`PaginatedResult<T>`; other JSON endpoints unwrap the API envelope into typed models. `ApiException` exposes
API, OAuth, retry, and rate-limit details.

## End-to-end signing flow

Upload a PDF (maximum 25 MB/2,000 pages), wait for metadata, and optionally attach tracked contract terms.

```java
DocumentDetails document = client.documents.upload(new File("contract.pdf"));
document = client.documents.waitUntilReady(document.getId(), 60_000, 2_000);

DocumentContractResult terms = client.contracts.set(document.getId(), new DocumentContractInput()
    .setStatus("active").setCounterpartyName("Example Company")
    .setStartDate("2026-11-01").setEndDate("2027-10-31"));
```

Create or reuse an account signer, then estimate the exact assignment before creating it.

```java
Signer signer = client.signers.findOrCreate(
    new CreateSignerPayload("Example Signer", "signer@example.com")
        .setWhatsappPhoneNumber("+5548999990000")
        .setGovernmentId("39053344705"));

CreateAssignmentPayload request = new CreateAssignmentPayload().setMethod("virtual").setSigners(List.of(
    SignerRef.of(signer.getId()).setVerificationMethod("Email")
        .setNotificationMethods(List.of("Email")).setStep(1)));
CostEstimate estimate = client.assignments.estimateCost(document.getId(), request);
if (!Boolean.TRUE.equals(estimate.getHasSufficientResources())) {
    throw new IllegalStateException(estimate.getBlockingReason());
}
Assignment assignment = client.assignments.create(document.getId(), request);
```

Supported verification/notification pairs are:

| Verification | Notification | Requirements |
|---|---|---|
| `Email` | `Email` | Default, one-time code |
| `Whatsapp` | `Whatsapp` | Paid-plan notification channel |
| `IdDocumentFaceMatchBasic` | `Email` or `Whatsapp` | Feature enabled, signer CPF, explicit biometric consent |
| `DigitalCertificate` | `Email` or `Whatsapp` | Feature enabled, CPF/CNPJ, `virtual`, signer alone in its step |

DigitalCertificate supports the signer's ICP-Brasil A1/A3 certificate through the browser Web PKI flow and
produces a qualified PAdES artifact. Biometric `InReview` allows signing but holds certification until the
workspace approves or rejects the private evidence. Estimate first because plan features and credits vary.

Signer-facing calls use only `signer-access-code`; the client deliberately withholds workspace credentials.

```java
DocumentDetails signingView = client.signerSelf.getSign(signerAccessCode);
client.signerSelf.acceptTerms(signerAccessCode);
client.signerSelf.verifyEmail("123456", signerAccessCode); // Whatsapp uses verifyWhatsapp
client.signerSelf.confirmSignerData(document.getId(), signerAccessCode,
    new ConfirmSignerDataPayload().setEmail("signer@example.com").setHasAcceptedTerms(true));

// Only for IdDocumentFaceMatchBasic, after the signer explicitly consents:
if (biometricRequired && signerConsented) {
    IdentityVerificationStatus identity = client.signerSelf.getIdentityVerification(signerAccessCode);
    if (identity.canStart()) redirectSignerTo(
        client.signerSelf.startIdentityVerification(signerAccessCode, true).url());
}

client.signerSelf.uploadSignature(signerAccessCode, signaturePng, "signature", true);
// Submit the non-empty AssignmentSignEntry values represented by signingView.getAssignment().getItems().
```

For certificate signers, confirm data and accept terms before `getSign`.
Complete ordinary assignments with `assignments.signEntries`; every entry carries the returned item, field, and
page IDs plus its string value. Certificate signing is completed in the browser, not by fabricating certificate
material in the SDK. Poll status or consume signed webhooks idempotently, then download the final artifact.

```java
DocumentDetails current = client.documents.details(document.getId());
byte[] signedPdf = client.documents.download(document.getId(), "certificated");
byte[] qualifiedPdf = client.documents.download(document.getId(), "pades"); // DigitalCertificate only
```

## Envelopes and embedded signing

`client.envelopes` sends up to 20 documents to up to 20 signers with one invitation per signer. Estimate first,
set `max_total_credits`, and use a stable idempotency key for recovery. Each document keeps its own certificate.
For signing inside your app, register exact HTTPS origins through `client.embed`, set `embedded=true` on the
signer, and mint a fresh five-minute, single-use iframe session. Never log its URL or exchange the token yourself.
After signer access closes, `requestCertifiedCopy` delivers a new link to the original signing contact.
See [complete payloads and flows](docs/EXAMPLES.md#envelopes-and-embedded-signing).

## Signed webhooks

New or changed endpoint URLs must use HTTPS; legacy stored HTTP URLs remain available for exact lookup/deletion.
Store a creation secret immediately because list/view/update responses omit it.

```java
WebhookEndpoint endpoint = client.webhooks.createEndpoint(new WebhookEndpointPayload()
    .setUrl("https://example.com/webhooks/assinafy").setEmail("ops@example.com")
    .setEvents(List.of("document_ready", "signer_signed_document")).setSigningEnabled(true));
WebhookVerifier verifier = new WebhookVerifier(endpoint.secret());
WebhookEvent event = verifier.verify(webhookId, webhookTimestamp, webhookSignature, rawBody);
```

Verify the raw body before acknowledging, deduplicate by `webhook-id`, answer 2xx quickly, and process later.
API keys/user sessions can later read the secret. OAuth apps can rotate it with `webhooks:write`;
`rotateEndpointSecret(id, true)` retains the old key for 24 hours while deliveries carry both signatures.

## OAuth 2.1 marketplace connections

Use API keys for your own workspace. For customer workspaces, register exact HTTPS callbacks and use state plus
S256 PKCE. Validate callback `state` and issuer before exchanging the single-use code.

```java
String verifier = OAuthResource.generateCodeVerifier();
String state = OAuthResource.generateState();
String authorizeUrl = client.oauth.authorizationUrl(new OAuthAuthorizationRequest(
    "client-id", "https://example.com/oauth/callback")
    .setScopes(List.of("documents:read", "documents:write", "offline_access"))
    .setState(state).setCodeVerifier(verifier));

OAuthTokens tokens = client.oauth.exchangeAuthorizationCode(
    "client-id", clientSecret, code, "https://example.com/oauth/callback", verifier);
AssinafyClient asUser = new AssinafyClient(
    new AssinafyClientOptions().setToken(tokens.getAccessToken()));
```

Store tokens atomically per workspace. Serialize refreshes: every successful refresh rotates the refresh token
and grants a fresh 30 days. Save the replacement before use; never replay a token after an uncertain outcome.
Revoke the latest token on disconnect. OAuth tokens are restricted to the approved scopes and one workspace.

```java
OAuthTokens renewed = client.oauth.refreshToken("client-id", clientSecret, storedRefreshToken);
tokenStore.replaceAtomically(renewed);
client.oauth.revoke("client-id", clientSecret, renewed.getRefreshToken(), "refresh_token");
```

## Build and support

```bash
./mvnw verify
docker compose run --rm test
```

The build enforces Java 25, compiler warnings as errors, unit tests, and release-profile Javadoc doclint. Live
sandbox tests require the environment variables in [Installation](docs/INSTALLATION.md#building-and-sandbox-tests);
they never target production.

- [API reference](docs/API_REFERENCE.md) — every operation and complete payload catalog
- [Examples](docs/EXAMPLES.md) — endpoint-by-endpoint Java and JSON
- [Installation](docs/INSTALLATION.md) — repositories and build tools
- [Changelog](CHANGELOG.md) — released behavior
- [License](LICENSE) — MIT
