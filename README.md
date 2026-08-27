# Assinafy Webforms Java Client SDK

Java client SDK for the [Assinafy Webforms API](https://api.assinafy.com.br/v1/docs).

Covers all 89 operations in the official API contract: accounts, users, authentication, documents, signers,
assignments, fields, templates, tags, webhooks, and the signer-facing signing flows.

This README follows one document from installation to a signed, downloaded PDF. Each section is the next step
of that journey, so reading top to bottom gives you the whole integration; jumping to a heading gives you one
stage of it. The [complete API reference](docs/API_REFERENCE.md) is the per-operation lookup table,
[worked examples](docs/EXAMPLES.md) hold longer runnable programs, and
[docs/INSTALLATION.md](docs/INSTALLATION.md) covers build-tool setup in depth.

---

## 1. Requirements and installation

- Java 25+ (the SDK is compiled and verified on the current Java 25 LTS)
- Maven 3.9.16 or newer 3.x (the wrapper pins 3.9.16); for Gradle, use a release that supports JDK 25

**Maven**

```xml
<dependency>
    <groupId>com.assinafy</groupId>
    <artifactId>webforms-java-client-sdk</artifactId>
    <version>2.1.0</version>
</dependency>
```

**Gradle**

```groovy
implementation 'com.assinafy:webforms-java-client-sdk:2.1.0'
```

The artifact is published to GitHub Packages, so the repository must be declared once in your build. See
[docs/INSTALLATION.md](docs/INSTALLATION.md) for that and for the sources/Javadoc jars.

---

## 2. Building the client

One `AssinafyClient` per credential and account. It is thread-safe, holds a shared connection pool, and is
meant to be created once and reused for the life of the application.

Keep credentials in environment variables or a deployment secret manager. The SDK does not read `.env` files,
so applications pass secret values explicitly. Never put credentials in source, logs, exception messages, Maven
properties, or browser code.

```bash
export ASSINAFY_API_KEY=your_api_key
export ASSINAFY_ACCOUNT_ID=your_account_id
```

```java
import com.assinafy.sdk.AssinafyClient;
import com.assinafy.sdk.AssinafyClientOptions;

AssinafyClient client = new AssinafyClient(new AssinafyClientOptions()
    .setApiKey(System.getenv("ASSINAFY_API_KEY"))
    .setAccountId(System.getenv("ASSINAFY_ACCOUNT_ID"))
    .setTimeoutMs(30_000)
    .setMaxRetries(2)); // safe reads only; mutations are never replayed
```

| Option       | Type   | Default                          | Description                                                                   |
|--------------|--------|----------------------------------|-------------------------------------------------------------------------------|
| `apiKey`     | String | —                                | Preferred credential, sent as the `X-Api-Key` header                          |
| `token`      | String | —                                | Bearer access token, used only when no API key is set                         |
| `accountId`  | String | —                                | Default workspace for every account-scoped method                             |
| `baseUrl`    | String | `https://api.assinafy.com.br/v1` | HTTPS API root; loopback HTTP is accepted only for local tests                |
| `timeoutMs`  | int    | `30000`                          | Connect, read, and write timeout; must be positive                            |
| `maxRetries` | int    | `0`                              | Extra attempts for safe reads on HTTP 429/503; mutating requests are never replayed |

Two shortcuts exist for the common cases:

```java
// Positional factory with an optional customizer.
AssinafyClient configured = AssinafyClient.create("api-key", "account-id",
    opts -> opts.setTimeoutMs(60_000));

// From string configuration (snake_case or camelCase keys).
AssinafyClient fromMap = AssinafyClient.fromConfig(Map.of(
    "api_key", System.getenv("ASSINAFY_API_KEY"),
    "account_id", System.getenv("ASSINAFY_ACCOUNT_ID")
));
```

An API key is the right credential for a back-end integration. A bearer token works too, and a client with no
credential at all is valid — it is what you use for the public endpoints (login, password reset, document
verification, and the public document views).

```java
// Bearer session: Authorization: Bearer <token>
new AssinafyClient(new AssinafyClientOptions().setToken("jwt_xxx").setAccountId("acc_xxx"));

// Unauthenticated: login and public signer flows only.
new AssinafyClient(new AssinafyClientOptions());
```

Section 10 covers obtaining a token and managing API keys.

---

## 3. What every call returns

JSON responses are wrapped as `{ "status": <int>, "message": "<string>", "data": <payload> }`. The SDK unwraps
`data` into the typed model, so your code never handles the envelope. It raises `ApiException` for a non-success
HTTP status **or** for `status >= 400` inside the envelope — the API sometimes returns an error envelope under
HTTP 200, and the SDK treats that as the error it is.

Three response shapes follow from that:

- **Typed models** for ordinary JSON endpoints — `DocumentDetails`, `Assignment`, `Signer`, and so on.
- **`PaginatedResult<T>`** for list endpoints. `getData()` is the array; `getMeta()` carries `currentPage`,
  `perPage`, `total`, and `lastPage`, read from the `X-Pagination-*` response headers. Query maps accept
  `per-page`, `per_page`, or `perPage`; the SDK always sends `per-page`.
- **`byte[]`** for binary downloads (logos, document artifacts, page images, thumbnails, signature images).
  These are not envelopes; when the server answers one with an error envelope instead, the SDK detects the JSON
  body and raises `ApiException` rather than handing you the error as if it were a PDF.

Retries are opt-in and deliberately narrow. With `maxRetries` above zero the client retries only `GET`, `HEAD`,
and `OPTIONS` that receive 429 or 503. It honors a numeric `Retry-After` or `X-Rate-Limit-Reset`, caps the wait
at 30 seconds, and preserves thread interruption. It never replays an upload, create, update, delete,
notification, or signature.

---

## 4. Preparing the workspace

Everything below is account-scoped. Methods take an optional trailing `accountId` that overrides the client
default, so one client can serve several workspaces.

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

User me = client.users.getSelf();
NotificationPreferences preferences = client.users.getNotificationPreferences();
client.users.updateNotificationPreferences(
    new NotificationPreferences().setDocumentCompleted(true).setSignerDeclined(true));

// Permanent; force=true also cancels an active paid subscription.
client.accounts.delete(false, created.getId());
```

**Tags** organise documents, and **field definitions** describe the inputs a `collect` assignment can place on
a page. Both are workspace-level and are usually created once, before any document exists.

```java
Tag contracts = client.tags.create(new CreateTagPayload("Contracts").setColor("ff8800"));
Tag renamed = client.tags.update(contracts.getId(),
    new UpdateTagPayload().setName("Sales Contracts").clearColor());
PaginatedResult<Tag> tags = client.tags.list(Map.of("search", "contract"));
boolean deleted = client.tags.delete(renamed.getId(), true); // force detaches it from documents first

FieldDefinition reference = client.fields.create(
    new CreateFieldPayload("text", "Reference").setRequired(true));
PaginatedResult<FieldDefinition> fields = client.fields.list(Map.of("include_standard", "true"));
client.fields.update(reference.getId(), new UpdateFieldPayload().setName("Internal Reference"));
List<FieldTypeInfo> fieldTypes = client.fields.listTypes();
client.fields.delete(reference.getId());

// Validate a value against a field's type/regex rules before submitting it.
FieldValidationResult check = client.fields.validate(reference.getId(), "ABC-123");
List<FieldValidationResult> checks = client.fields.validateMultiple(List.of(
    new FieldValidationPayload(reference.getId(), "ABC-123")));
```

---

## 5. Creating the document

A document starts either from an uploaded PDF or from a template. Uploads are `multipart/form-data` with one
`file` part, at most 25 MB and 2,000 pages.

```java
// From a file or from bytes already in memory.
DocumentDetails doc = client.documents.upload(new File("contract.pdf"));
DocumentDetails fromBytes = client.documents.upload(pdfBytes, "contract.pdf");

// Rename is allowed only before an assignment exists.
DocumentDetails renamed = client.documents.rename(doc.getId(), "Signed contract.pdf");

// Full listing, and a lightweight search for typeahead (no expanded assignment/pages).
PaginatedResult<DocumentListItem> page = client.documents.list(Map.of("page", "1", "per_page", "20"));
PaginatedResult<DocumentListItem> hits = client.documents.search(Map.of("search", "invoice"));

// Tags on this document.
List<Tag> attached = client.documents.listTags(doc.getId());
client.documents.appendTags(doc.getId(), List.of(urgentTagId));
client.documents.replaceTags(doc.getId(), List.of(contractTagId, quarterTagId));
boolean detached = client.documents.detachTag(doc.getId(), tagId);
```

Templates produce a document and its assignment in a single call. Provide one signer entry per template role;
the signers must already exist in the account.

```java
PaginatedResult<TemplateListItem> templates = client.templates.list(Map.of("search", "NDA"));
TemplateDetails template = client.templates.get(templateId);

CostEstimate templateCost = client.documents.estimateCostFromTemplate(templateId,
    List.of(new TemplateSigner(template.getRoles().get(0).getId()).setVerificationMethod("Email")));

DocumentDetails generated = client.documents.createFromTemplate(
    templateId,
    List.of(new TemplateSigner(template.getRoles().get(0).getId(), signerId)
        .setVerificationMethod("Email")
        .setNotificationMethods(List.of("Email"))
        .setStep(1)),
    new CreateDocumentFromTemplateOptions().setTags(List.of("Generated")));
```

An uploaded PDF is not immediately assignable: the API extracts page metadata first. `waitUntilReady` polls
`GET /documents/{id}` until the status reaches `metadata_ready`, `pending_signature`, or `certificated`,
raising `ValidationException` if the document fails, expires, is rejected, or the wait budget elapses.

```java
DocumentDetails ready = client.documents.waitUntilReady(doc.getId());          // 30s budget, 2s interval
DocumentDetails patient = client.documents.waitUntilReady(doc.getId(), 120_000, 5_000);
```

A `virtual` assignment can be created while metadata is still processing; a `collect` assignment cannot,
because its field placements reference specific page IDs.

---

## 6. Identifying the signers

Signers live at the account level and are reused across documents, so the same person is one record no matter
how many contracts they sign.

```java
// Strict create: always sends POST and reports a duplicate email as an ApiException.
Signer signer = client.signers.create(
    new CreateSignerPayload("John Doe", "john@example.com")
        .setWhatsappPhoneNumber("+5548999990000"));

// Explicit reuse policy: search by exact case-insensitive email, POST only when absent.
// It does not update an existing signer's fields.
Signer reusable = client.signers.findOrCreate(
    new CreateSignerPayload("John Doe", "john@example.com"));

Signer existing = client.signers.findByEmail("john@example.com");
Signer fetched = client.signers.get(signer.getId());
PaginatedResult<Signer> list = client.signers.list(Map.of("search", "john"));
client.signers.update(signer.getId(), new UpdateSignerPayload()
    .setFullName("Johnny Doe")
    .setGovernmentId("39053344705"));
client.signers.delete(signer.getId());
```

Pick `create` when the signer is genuinely new and a duplicate should be an error; pick `findOrCreate` when
"this person, whether or not we have met them before" is what you mean. Changing a signer's email or WhatsApp
number is refused while that channel is verified on an in-flight document, and rotates the access codes of any
unverified in-flight requests — resend after such a change.

---

## 7. Pricing and requesting signatures

Estimate first. The estimate endpoint takes the same payload shape as the create call, charges nothing, and
tells you whether the account can fund the request.

```java
CreateAssignmentPayload request = new CreateAssignmentPayload()
    .setMethod("virtual")
    .setSignerStrings(signer.getId())
    .setMessage("Please review and sign")
    .setExpiresAt("2030-12-31T23:59:00Z");

CostEstimate estimate = client.assignments.estimateCost(doc.getId(), request);
if (!Boolean.TRUE.equals(estimate.getHasSufficientResources())) {
    throw new IllegalStateException("Assignment cannot be funded: " + estimate.getBlockingReason());
}

Assignment assignment = client.assignments.create(doc.getId(), request);
```

`blocking_reason` is `PendingPayment`, `InsufficientDocuments`, or `InsufficientCredits`;
`getBreakdown()` itemises what drove the number.

**`virtual` versus `collect`.** A virtual assignment asks for a signature and nothing else. A collect
assignment additionally places input fields at coordinates on specific pages, so it needs a `metadata_ready`
document and one entry per page:

```java
CreateAssignmentPayload collect = new CreateAssignmentPayload()
    .setMethod("collect")
    .setSignerStrings(signer.getId())
    .setCollectEntries(List.of(new CollectAssignmentEntry(pageId, List.of(
        new CollectFieldPlacement(signer.getId(), fieldId,
            new DisplaySettings(100, 100, 240, 40, 12))))));
```

**Verification and notification.** `SignerRef.verificationMethod` accepts `Email`, `Whatsapp`, or
`DigitalCertificate`, and `notificationMethods` accepts `Email` or `Whatsapp`. If neither is supplied both
default to `Email`; supplying one lets the API infer the other. Only one notification channel per signer.
Digital-certificate signers need a CPF/CNPJ in `government_id`, the account feature enabled, and a signing step
containing no other signer; the resulting qualified PAdES PDF is
`client.documents.download(documentId, "pades")`.

**Signing order.** `step` sequences the signers: everyone sharing a step signs in parallel, and the next step
is notified only after the previous one completes. If you use it, every signer needs one, and the values must
be contiguous from 1.

Once an assignment exists you can list, re-notify, and re-schedule it:

```java
PaginatedResult<Assignment> assignments = client.assignments.list(Map.of("page", "1", "per-page", "20"));

ResendResult resent = client.assignments.resendNotification(doc.getId(), assignment.getId(), signer.getId());
ResendCostEstimate resendCost = client.assignments.estimateResendCost(
    doc.getId(), assignment.getId(), signer.getId());

client.assignments.resetExpiration(doc.getId(), assignment.getId(), "2027-06-30T00:00:00Z");
client.assignments.clearExpiration(doc.getId(), assignment.getId()); // sends expires_at: null

List<WhatsappNotification> whatsapp =
    client.assignments.whatsappNotifications(doc.getId(), assignment.getId());
```

### The one-call shortcut

For the plain virtual path, `uploadAndRequestSignatures` composes upload, optional readiness polling,
`findOrCreate` per signer, and assignment creation, returning the document, the assignment, and the signer IDs.

```java
UploadAndRequestSignaturesResult result = client.uploadAndRequestSignatures(
    new UploadAndRequestSignaturesOptions(new File("contract.pdf"), List.of(
            new UploadAndRequestSignaturesSigner("John Doe", "john@example.com")))
        .setMessage("Please review and sign"));
```

The API has no transaction spanning those calls. If a later stage fails, the helper makes a best-effort attempt
to delete the uploaded document and attaches any cleanup failure to the original exception as a suppressed
exception. Account-scoped signers are never deleted automatically, because another workflow may already
reference them.

---

## 8. The signer's side

These endpoints are authorised by a short-lived `signer-access-code` sent as a query parameter, not by the
account API key. They are normally called from a signer landing page rather than from your back end, and the
SDK exposes them so you can build that page or simulate the flow in tests.

```java
// The document the signer was invited to sign. Returns HTTP 409 while it is still being prepared —
// surfaced as ApiException with getStatusCode() == 409; retry with backoff.
DocumentDetails signingView = client.signerSelf.getSign(signerAccessCode);

// Identity: profile, terms, one-time code, and confirmed personal data.
Signer self = client.signerSelf.getSelf(signerAccessCode);
client.signerSelf.acceptTerms(signerAccessCode);
client.signerSelf.verifyEmail("123456", signerAccessCode);
Signer confirmed = client.signerSelf.confirmSignerData(doc.getId(), signerAccessCode,
    new ConfirmSignerDataPayload().setFullName("John Doe").setEmail("signer@example.com")
        .setGovernmentId("15774136604"));

// Signature image. PNG is the published media type; the SDK also detects JPEG bytes.
// reuse=true lets the saved signature be reused across documents (sets is_signature_reusable).
client.signerSelf.uploadSignature(signerAccessCode, signatureBytes, "signature");
client.signerSelf.uploadSignature(signerAccessCode, signatureBytes, "signature", true);
byte[] saved = client.signerSelf.downloadSignature(signerAccessCode, "signature");
```

A signer with a digital certificate must confirm their data *and* accept the terms before `getSign` will
return the document; otherwise it answers HTTP 400. A signer using a virtual assignment must confirm their data
before signing, or the sign call answers HTTP 400.

Signing itself, and declining, are mutually exclusive:

```java
client.assignments.signEntries(doc.getId(), assignment.getId(), signerAccessCode, List.of(
    new AssignmentSignEntry("item-1", "field-1", "page-1", "John Doe")));

// Alternative to the above, not a follow-up:
// client.assignments.decline(doc.getId(), assignment.getId(), signerAccessCode, "Clause 3 is unacceptable");
```

A signer with several pending documents can work through them in bulk, and can browse and download their own
copies:

```java
DocumentDetails current = client.signerSelf.getCurrentDocument(signerId, signerAccessCode);
PaginatedResult<DocumentDetails> mine = client.signerSelf.listDocuments(
    signerId, signerAccessCode, Map.of("page", "1", "per_page", "20"));
PaginatedResult<DocumentDetails> found =
    client.signerSelf.searchDocuments(signerId, signerAccessCode, "invoice");

// The artifact route is public; an overload also sends the access code where an environment requires it.
byte[] signerCopy = client.signerSelf.downloadDocument(signerId, doc.getId(), "pades");
byte[] authorizedCopy = client.signerSelf.downloadDocument(
    signerId, doc.getId(), "original", signerAccessCode);

client.signerSelf.signMultiple(signerAccessCode, List.of(doc1.getId(), doc2.getId()));
// Alternative to the above for the same documents, not a follow-up:
// client.signerSelf.declineMultiple(signerAccessCode, List.of(doc1.getId()), "Not interested");
```

Two public endpoints support a signer landing page before any access code exists — an unauthenticated document
view, and a request to re-send the one-time access token:

```java
AssinafyClient publicClient = new AssinafyClient(new AssinafyClientOptions());
DocumentDetails publicInfo = publicClient.documents.getPublic(doc.getId());

// The document must be in pending_signature. channel is "email" or "whatsapp".
publicClient.documents.sendToken(doc.getId(), "signer@example.com", "email");
```

---

## 9. Tracking progress and collecting the result

Poll for state, or — better — subscribe to webhooks and fetch state when one arrives.

```java
DocumentDetails currentState = client.documents.details(doc.getId());
SigningProgress progress = client.documents.getSigningProgress(doc.getId());
boolean done = client.documents.isFullySigned(doc.getId());
List<DocumentActivity> activity = client.documents.activities(doc.getId());
List<DocumentStatsRow> accountStats = client.accounts.stats(Map.of("granularity", "monthly"));
List<DocumentStatsRow> allAccountStats = client.users.stats(Map.of("granularity", "monthly"));
```

The workspace has a single webhook subscription, updated with a create-or-replace `PUT`. There is no
hard-delete endpoint; `inactivate()` stops deliveries and keeps the configuration.

```java
WebhookSubscription sub = client.webhooks.register(
    new RegisterWebhookPayload("https://example.com/webhooks", "admin@example.com")
        .setEvents(List.of("document_ready", "signer_signed_document"))
        .setActive(true));

client.webhooks.getSubscription();
client.webhooks.update(new RegisterWebhookPayload(sub.getUrl(), sub.getEmail())
    .setEvents(sub.getEvents()).setActive(sub.isActive()));
client.webhooks.inactivate();

List<WebhookEventTypeInfo> eventTypes = client.webhooks.listEventTypes();
PaginatedResult<WebhookDispatch> dispatches = client.webhooks.listDispatches(
    new ListDispatchesParams().setEvent("document_ready").setDelivered(false));
client.webhooks.retryDispatch(dispatchId);
```

Once the document reaches `certificated`, its artifacts are available. `original` is the uploaded PDF,
`certificated` is the signed one, `certificate-page` is the audit page, `pades` exists only when a
digital-certificate signer took part, and `bundle` is a zip of the rest.

```java
byte[] signedPdf = client.documents.download(doc.getId());               // defaults to "certificated"
byte[] original = client.documents.download(doc.getId(), "original");
byte[] bundle = client.documents.download(doc.getId(), "bundle");
byte[] thumbnail = client.documents.thumbnail(doc.getId());
byte[] pageImage = client.documents.downloadPage(doc.getId(), pageId);

// Public, unauthenticated verification by the hash printed on a signed document.
DocumentVerification verification = client.documents.verify(signatureHash);
boolean valid = Boolean.TRUE.equals(verification.getIsValid());
```

Delete only what you own and only when the status allows it — `documents.statuses()` reports which statuses
are deletable.

```java
boolean deletable = client.documents.statuses().stream()
    .anyMatch(status -> currentState.getStatus().equals(status.getCode())
        && Boolean.TRUE.equals(status.getDeletable()));
if (deletable) {
    client.documents.delete(doc.getId());
}
```

---

## 10. Sessions, passwords, and API keys

The `auth` resource covers the credential lifecycle itself. The password-reset routes are public; the rest need
a bearer token or an API key.

```java
AuthenticationResult session = client.auth.login("user@example.com", "password");
String accessToken = session.getAccessToken();

AuthenticationResult googleSession = client.auth.socialLogin(
    new SocialLoginPayload("google", googleToken, true));

// Rotate keys through a bearer session so the client does not retain a key it just revoked.
AssinafyClient tokenClient = new AssinafyClient(new AssinafyClientOptions().setToken(accessToken));
ApiKeyResponse masked = tokenClient.auth.getApiKey();     // masked; the full key is never retrievable
ApiKeyResponse created = tokenClient.auth.createApiKey("password"); // replaces any previous key
tokenClient.auth.deleteApiKey();

tokenClient.auth.linkSocialLogin("google", googleToken);
tokenClient.auth.changePassword("user@example.com", "old-password", "new-password");

AssinafyClient publicClient = new AssinafyClient(new AssinafyClientOptions());
publicClient.auth.requestPasswordReset("user@example.com");
publicClient.auth.resetPassword("user@example.com", resetToken, "new-password");
```

---

## 11. Errors

Everything the SDK throws descends from `AssinafyException`, so one catch block can be the backstop while the
three subtypes let you separate "my input was wrong" from "the API said no" from "the network failed".

```java
import com.assinafy.sdk.exceptions.*;

try {
    client.documents.upload(new File("contract.pdf"));
} catch (ValidationException e) {
    // Caught before any request was sent: missing IDs, bad email, oversized file, malformed payload.
    System.err.println("Validation: " + e.getMessage() + " " + e.getErrors());
} catch (ApiException e) {
    // The API rejected it. getResponseBody() keeps the complete error JSON.
    System.err.println("API error " + e.getStatusCode() + ": " + e.getMessage());
    Integer backoff = e.getRetryAfterSeconds(); // populated only for retryable 429/503
} catch (NetworkException e) {
    // Transport failure, or a response body that could not be parsed.
    System.err.println("Network: " + e.getMessage());
} catch (AssinafyException e) {
    System.err.println("SDK error: " + e.getMessage());
}
```

The standard error body is `{ "status": integer, "message": string, "data": object|null }`. Treat both 400 and
422 as validation failures. A blocked account deletion adds a `restrictions` array naming each blocker.

---

## 12. Development

```bash
# Run tests in Docker (recommended)
docker compose run --rm test

# Or run the complete local verification with the Maven Wrapper (requires JDK 25+)
./mvnw verify

# Live smoke tests against the sandbox (skipped unless credentials are set; defaults to the sandbox base URL)
ASSINAFY_API_KEY=... ASSINAFY_ACCOUNT_ID=... ./mvnw test -Dtest=LiveSmokeTest

# Explicit opt-in for the tests that dispatch real signing-request email
ASSINAFY_API_KEY=... ASSINAFY_ACCOUNT_ID=... ASSINAFY_LIVE_EMAILS=true \
  ASSINAFY_TEST_EMAIL=... ASSINAFY_SECOND_TEST_EMAIL=... ./mvnw test -Dtest=LiveSmokeTest
```

`LiveSmokeTest` refuses non-sandbox base URLs. Keep live credentials in environment/CI secrets, never Maven
properties or source files.

The automated sandbox suite covers API-key workflows through assignment creation. Completing a signature also
requires the short-lived signer access code and one-time verification code delivered out of band, plus an
account with document/credit capacity. Use the signer self-service sequence from section 8 with a disposable
assignment when validating that final step; CI does not fabricate or persist either credential.

GitHub's manual `sandbox-live` workflow reads credentials and test recipients from the protected `sandbox`
environment. Notification-producing cases remain disabled unless the dispatch input explicitly enables them.
The mirrored GitLab manual job uses protected, masked, sandbox-scoped variables and the same opt-in rule.

CI runs `./mvnw verify` on the current JDK 25 LTS. GitLab is the source of truth and mirrors to GitHub, where
the equivalent Actions workflows run. Releases publish to GitHub Packages on a `v*` tag via the `release`
profile (`-Prelease`, which also builds `-sources` and `-javadoc` jars).

## License

MIT
