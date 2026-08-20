# Assinafy Webforms Java Client SDK

Java client SDK for the [Assinafy Webforms API](https://api.assinafy.com.br/v1/docs).

Covers all 89 operations in the published API contract: accounts, users, authentication, documents, signers,
assignments, fields, templates, tags, webhooks, signer-facing flows, and the high-level
`uploadAndRequestSignatures` workflow.

## Requirements

- Java 25+ (the SDK is compiled and verified on the current Java 25 LTS)
- Maven 3.9+; for Gradle builds, use a Gradle release that supports JDK 25

## Installation

### Maven

```xml
<dependency>
    <groupId>com.assinafy</groupId>
    <artifactId>webforms-java-client-sdk</artifactId>
    <version>2.0.2</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.assinafy:webforms-java-client-sdk:2.0.2'
```

See [docs/INSTALLATION.md](docs/INSTALLATION.md) for full setup instructions.

## Quick Start

```java
import com.assinafy.sdk.AssinafyClient;
import com.assinafy.sdk.AssinafyClientOptions;
import com.assinafy.sdk.models.*;
import java.io.File;
import java.util.List;

AssinafyClient client = new AssinafyClient(new AssinafyClientOptions()
    .setApiKey(System.getenv("ASSINAFY_API_KEY"))
    .setAccountId(System.getenv("ASSINAFY_ACCOUNT_ID")));

UploadAndRequestSignaturesResult result = client.uploadAndRequestSignatures(
    new UploadAndRequestSignaturesOptions(
        new File("contract.pdf"),
        List.of(
            new UploadAndRequestSignaturesSigner("John Doe", "john@example.com"),
            new UploadAndRequestSignaturesSigner("Jane Smith", "jane@example.com")
                .setWhatsappPhoneNumber("+5548999990000")
        )
    ).setMessage("Please sign this contract")
);

System.out.println("Document ID: " + result.getDocument().getId());
```

See the [complete API reference](docs/API_REFERENCE.md) for the 89-operation matrix, request/response schemas,
status codes, and webhook payload contract. [Worked examples](docs/EXAMPLES.md) cover the common end-to-end flows.

## Response envelope

JSON responses are wrapped as `{ "status": <int>, "message": "<string>", "data": <payload> }`. The SDK unwraps
`data` into the typed model and raises an `ApiException` for a non-success HTTP status or `status >= 400` in the
envelope. Binary download endpoints return their raw bytes and are not JSON envelopes. List endpoints also read
the `X-Pagination-*` response headers into `PaginatedResult.getMeta()`.

## Authentication

```java
// Preferred: X-Api-Key header
new AssinafyClient(new AssinafyClientOptions()
    .setApiKey("k_xxx")
    .setAccountId("acc_xxx"));

// Legacy: Authorization: Bearer <token>
new AssinafyClient(new AssinafyClientOptions()
    .setToken("jwt_xxx")
    .setAccountId("acc_xxx"));

// Unauthenticated endpoints such as login and public signer flows
new AssinafyClient(new AssinafyClientOptions());
```

### Authentication API

```java
AuthenticationResult session = client.auth.login("user@example.com", "password");
String accessToken = session.getAccessToken();

AuthenticationResult googleSession = client.auth.socialLogin(
    new SocialLoginPayload("google", googleToken, true));

// Use the bearer session for key rotation so the client does not retain a key it just revoked.
AssinafyClient tokenClient = new AssinafyClient(
    new AssinafyClientOptions().setToken(accessToken));
ApiKeyResponse masked = tokenClient.auth.getApiKey();
ApiKeyResponse created = tokenClient.auth.createApiKey("password");
tokenClient.auth.deleteApiKey();

// Link a social-login provider (e.g. Google) to the authenticated user.
tokenClient.auth.linkSocialLogin("google", googleToken);
tokenClient.auth.changePassword("user@example.com", "old-password", "new-password");

// Password reset routes are public and do not reuse a rotated/revoked key.
AssinafyClient publicClient = new AssinafyClient(new AssinafyClientOptions());
publicClient.auth.requestPasswordReset("user@example.com");
publicClient.auth.resetPassword("user@example.com", resetToken, "new-password");
```

## Configuration

| Option           | Type    | Default                            | Description                               |
|------------------|---------|------------------------------------|-------------------------------------------|
| `apiKey`         | String  | —                                  | Preferred credential (`X-Api-Key` header) |
| `token`          | String  | —                                  | Legacy access token (`Bearer` header)     |
| `accountId`      | String  | —                                  | Default account/workspace ID              |
| `baseUrl`        | String  | `https://api.assinafy.com.br/v1`   | HTTPS API base URL (loopback HTTP is test-only) |
| `timeoutMs`      | int     | `30000`                            | Request timeout in milliseconds           |
| `maxRetries`     | int     | `0`                                | Retries safe reads only on HTTP 429/503; mutating requests are never replayed |

### Factory Methods

```java
// Positional factory
AssinafyClient configured = AssinafyClient.create("api-key", "account-id",
    opts -> opts.setTimeoutMs(60_000));

// From a map (snake_case or camelCase keys)
AssinafyClient fromMap = AssinafyClient.fromConfig(Map.of(
    "api_key", System.getenv("ASSINAFY_API_KEY"),
    "account_id", System.getenv("ASSINAFY_ACCOUNT_ID")
));
```

## Resources

### Accounts and Users

```java
List<WorkspaceAccount> workspaces = client.accounts.list();
WorkspaceAccount workspace = client.accounts.get();
WorkspaceAccount created = client.accounts.create(new AccountPayload("Legal Operations")
    .setNotificationSenderType("Account"));
client.accounts.update(new AccountPayload().setName("Legal"));

AccountTheme theme = client.accounts.getTheme();
byte[] logo = client.accounts.downloadLogo();
client.accounts.uploadLogo(pngBytes, "logo.png");
client.accounts.deleteLogo();
List<DocumentStatsRow> accountStats = client.accounts.stats(Map.of("granularity", "monthly"));

User me = client.users.getSelf();
NotificationPreferences preferences = client.users.getNotificationPreferences();
client.users.updateNotificationPreferences(
    new NotificationPreferences().setDocumentCompleted(true).setSignerDeclined(true));
List<DocumentStatsRow> allAccountStats = client.users.stats(Map.of("granularity", "monthly"));

// Permanent; force=true also cancels an active paid subscription.
client.accounts.delete(false, created.getId());
```

### Documents

```java
// Upload from file
DocumentDetails doc = client.documents.upload(new File("contract.pdf"));

// Upload from bytes
DocumentDetails uploadedBytes = client.documents.upload(pdfBytes, "contract.pdf");

// List documents
PaginatedResult<DocumentListItem> page = client.documents.list(Map.of("page", "1", "per_page", "20"));

// Lightweight search (compact results — no expanded assignment/pages, cheaper for typeahead)
PaginatedResult<DocumentListItem> hits = client.documents.search(Map.of("search", "invoice"));

// Rename a document (PATCH /documents/{id}; allowed before an assignment exists)
DocumentDetails renamed = client.documents.rename(doc.getId(), "Signed contract.pdf");

// Get document details
DocumentDetails details = client.documents.details(doc.getId());

// Verify a signed document by its signature hash (public, no auth) — returns a typed result
DocumentVerification verification = client.documents.verify(signatureHash);
boolean valid = verification.getIsValid();

// Wait until ready for signing
DocumentDetails ready = client.documents.waitUntilReady(doc.getId());

// After an assignment is completed and status is certificated, download the final signed PDF:
// byte[] pdf = client.documents.download(doc.getId());
byte[] original = client.documents.download(doc.getId(), "original");
byte[] thumbnail = client.documents.thumbnail(doc.getId());
byte[] pageImage = client.documents.downloadPage(doc.getId(), pageId);

// Audit trail
List<DocumentActivity> activities = client.documents.activities(doc.getId());

// Check signing progress
boolean done = client.documents.isFullySigned(doc.getId());
SigningProgress progress = client.documents.getSigningProgress(doc.getId());

// Public (unauthenticated) — minimal info for signer landing pages
DocumentDetails publicInfo = client.documents.getPublic(doc.getId());
// After an assignment puts the document in pending_signature:
// client.documents.sendToken(doc.getId(), "signer@example.com", "email");

// Document tags
List<Tag> tags = client.documents.listTags(doc.getId());
client.documents.appendTags(doc.getId(), List.of("Urgent"));
client.documents.replaceTags(doc.getId(), List.of("Contracts", "2026-Q1"));
boolean detached = client.documents.detachTag(doc.getId(), tagId);

// Final cleanup after every operation below that uses doc:
// client.documents.delete(doc.getId());
```

### Signers

```java
Signer signer = client.signers.create(
    new CreateSignerPayload("John Doe", "john@example.com")
        .setWhatsappPhoneNumber("+5548999990000")
);

// Idempotent by email — reuses an existing signer instead of creating a duplicate (does not update fields)
Signer existing = client.signers.findByEmail("john@example.com");

Signer fetched = client.signers.get(signer.getId());
PaginatedResult<Signer> list = client.signers.list(Map.of("search", "john"));
client.signers.update(signer.getId(), new UpdateSignerPayload()
    .setFullName("Johnny Doe")
    .setGovernmentId("39053344705"));
client.signers.delete(signer.getId());
```

### Assignments

```java
Assignment assignment = client.assignments.create(doc.getId(),
    new CreateAssignmentPayload()
        .setMethod("virtual")
        .setSignerStrings(signer1.getId(), signer2.getId())
        .setMessage("Please review and sign")
        .setExpiresAt("2030-12-31T23:59:00Z"));

// List assignments across the account (the SDK sends the account context as the accountId query param)
PaginatedResult<Assignment> assignments = client.assignments.list(Map.of("per_page", "20"));

// Typed cost estimates
CostEstimate cost = client.assignments.estimateCost(doc.getId(), new CreateAssignmentPayload()
    .setSignerStrings(signer1.getId()));
if (cost.getHasSufficientResources()) { /* ... */ }

ResendResult resent = client.assignments.resendNotification(doc.getId(), assignment.getId(), signer1.getId());
client.assignments.resetExpiration(doc.getId(), assignment.getId(), "2027-06-30T00:00:00Z");
client.assignments.clearExpiration(doc.getId(), assignment.getId());  // remove expiration (sends expires_at: null)
ResendCostEstimate resendCost = client.assignments.estimateResendCost(doc.getId(), assignment.getId(), signer1.getId());
client.assignments.whatsappNotifications(doc.getId(), assignment.getId());
```

`SignerRef.verificationMethod` accepts `Email`, `Whatsapp`, or `DigitalCertificate`. Digital-certificate
signers need a CPF/CNPJ in `government_id`, the account feature enabled, and a signing step containing no
other signer. The API charges two credits per digital-certificate signer in addition to notification cost.
Download the resulting qualified PAdES PDF with `client.documents.download(documentId, "pades")`.

### Assignments (signer-facing)

Endpoints authorised via a short-lived `signer-access-code`. These are typically called from a
signer landing page rather than from the account-holder's server.

```java
// Fetch the assignment the signer is being asked to complete (GET /sign returns the document view,
// whose getAssignment()/getCurrentSigner() carry the signer-facing assignment data).
DocumentDetails signingView = client.signerSelf.getSign(signerAccessCode);

// Submit collect-method field values
client.assignments.signEntries(doc.getId(), assignmentId, signerAccessCode, List.of(
    new AssignmentSignEntry("item-1", "field-1", "page-1", "John Doe")
));

// Mutually exclusive alternative (do not run after signEntries):
// client.assignments.decline(doc.getId(), assignmentId, signerAccessCode, "Not happy with clause 3");
```

### Signer Self-Service

```java
// Profile and terms
Signer self = client.signerSelf.getSelf(signerAccessCode);
client.signerSelf.acceptTerms(signerAccessCode);

// Email/WhatsApp verification flow (the access code is sent as a query parameter)
client.signerSelf.verifyEmail("123456", signerAccessCode);
Signer confirmed = client.signerSelf.confirmSignerData(doc.getId(), signerAccessCode,
    new ConfirmSignerDataPayload().setFullName("John Doe").setEmail("a@b.com")
        .setGovernmentId("15774136604"));

// Signature image upload (PNG is the published contract; JPEG detection is retained for compatibility).
// Pass reuse=true to allow the saved signature to be reused across documents (sets is_signature_reusable).
client.signerSelf.uploadSignature(signerAccessCode, signatureBytes, "signature");
client.signerSelf.uploadSignature(signerAccessCode, signatureBytes, "signature", true);
byte[] saved = client.signerSelf.downloadSignature(signerAccessCode, "signature");

// Multi-document signer flows
DocumentDetails signingView = client.signerSelf.getSign(signerAccessCode);
DocumentDetails current = client.signerSelf.getCurrentDocument(signerId, signerAccessCode);
PaginatedResult<DocumentDetails> mine = client.signerSelf.listDocuments(
    signerId, signerAccessCode, Map.of("page", "1", "per_page", "20"));
PaginatedResult<DocumentDetails> found = client.signerSelf.searchDocuments(signerId, signerAccessCode, "invoice");
// The artifact route is public in the current API contract. The access-code overload remains for compatibility.
byte[] signerCopy = client.signerSelf.downloadDocument(signerId, doc.getId(), "pades");
byte[] legacyCopy = client.signerSelf.downloadDocument(
    signerId, doc.getId(), "original", signerAccessCode);
client.signerSelf.signMultiple(signerAccessCode, List.of(doc1.getId(), doc2.getId()));
// Mutually exclusive alternative (do not run for doc1/doc2 after signMultiple):
// client.signerSelf.declineMultiple(signerAccessCode, List.of(doc1.getId()), "Not interested");
```

### Webhooks

```java
WebhookSubscription sub = client.webhooks.register(
    new RegisterWebhookPayload("https://example.com/webhooks", "admin@example.com")
        .setEvents(List.of("document_ready", "signer_signed_document"))
        .setActive(true)
);

client.webhooks.getSubscription();
client.webhooks.update(new RegisterWebhookPayload(sub.getUrl(), sub.getEmail())
    .setEvents(sub.getEvents()).setActive(sub.isActive())); // PUT is create-or-replace
client.webhooks.inactivate();         // stop deliveries but keep the subscription (there is no hard-delete endpoint)
client.webhooks.listEventTypes();
client.webhooks.listDispatches();
client.webhooks.retryDispatch(dispatchId);
```

### Tags

```java
PaginatedResult<Tag> tags = client.tags.list(Map.of("search", "contract"));
Tag created = client.tags.create(new CreateTagPayload("Contracts").setColor("ff8800"));
Tag updated = client.tags.update(created.getId(),
    new UpdateTagPayload().setName("Sales Contracts").clearColor());
boolean deleted = client.tags.delete(updated.getId(), true);
```

### Field Definitions

```java
FieldDefinition field = client.fields.create(new CreateFieldPayload("text", "Reference")
    .setRequired(true));

PaginatedResult<FieldDefinition> fields = client.fields.list(
    Map.of("include_standard", "true"));

FieldDefinition one = client.fields.get(field.getId());
client.fields.update(field.getId(), new UpdateFieldPayload().setName("Internal Reference"));

FieldValidationResult validation = client.fields.validate(field.getId(), "ABC-123");
List<FieldTypeInfo> fieldTypes = client.fields.listTypes();
client.fields.delete(field.getId());
```

### Templates

```java
PaginatedResult<TemplateListItem> templates = client.templates.list(Map.of("search", "NDA"));
TemplateDetails template = client.templates.get(templateId);

// Create a document from a template
DocumentDetails doc = client.documents.createFromTemplate(
    templateId,
    List.of(new TemplateSigner(template.getRoles().get(0).getId(), signerId)
        .setVerificationMethod("Email")
        .setNotificationMethods(List.of("Email"))
        .setStep(1)),
    new CreateDocumentFromTemplateOptions()
        .setTags(List.of("Generated"))
);
```

## Errors

```java
import com.assinafy.sdk.exceptions.*;

try {
    client.documents.upload(new File("contract.pdf"));
} catch (ValidationException e) {
    System.err.println("Validation: " + e.getMessage() + " " + e.getErrors());
} catch (ApiException e) {
    System.err.println("API error " + e.getStatusCode() + ": " + e.getMessage());
} catch (NetworkException e) {
    System.err.println("Network: " + e.getMessage());
} catch (AssinafyException e) {
    System.err.println("SDK error: " + e.getMessage());
}
```

## Development

```bash
# Run tests in Docker (recommended)
docker compose run --rm test

# Or run locally with the Maven Wrapper (requires JDK 25+)
./mvnw test

# Live smoke tests against the sandbox (skipped unless credentials are set; defaults to the sandbox base URL)
ASSINAFY_API_KEY=... ASSINAFY_ACCOUNT_ID=... ./mvnw test -Dtest=LiveSmokeTest

# Explicit opt-in for the test that dispatches real signing-request email
ASSINAFY_API_KEY=... ASSINAFY_ACCOUNT_ID=... ASSINAFY_LIVE_EMAILS=true \
  ASSINAFY_TEST_EMAIL=... ASSINAFY_SECOND_TEST_EMAIL=... ./mvnw test -Dtest=LiveSmokeTest
```

`LiveSmokeTest` refuses non-sandbox base URLs. Keep live credentials in environment/CI secrets, never Maven
properties or source files.

CI runs `./mvnw verify` on the current JDK 25 LTS. GitLab is the source of truth and mirrors to GitHub, where
the equivalent Actions workflows run. Releases publish to GitHub Packages on a `v*` tag via the `release` profile
(`-Prelease`, which also builds `-sources` and `-javadoc` jars).

## License

MIT
