# Assinafy Webforms Java Client SDK

Java client SDK for the [Assinafy Webforms API](https://api.assinafy.com.br/v1/docs).

Covers the documented authentication, document, signer, assignment, field definition, webhook, template, tag, and high-level `uploadAndRequestSignatures` flows.

## Requirements

- Java 21+ (the SDK is compiled to Java 21 bytecode; CI verifies it on JDK 21 and 25)
- Maven 3.8+ (or Gradle 7+)

## Installation

### Maven

```xml
<dependency>
    <groupId>com.assinafy</groupId>
    <artifactId>webforms-java-client-sdk</artifactId>
    <version>1.5.1</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.assinafy:webforms-java-client-sdk:1.5.1'
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

> **Full request/response payloads** for every method are in [docs/EXAMPLES.md](docs/EXAMPLES.md).

## Response envelope

Every response is wrapped as `{ "status": <int>, "message": "<string>", "data": <payload> }`. The SDK unwraps
`data` into the typed model and raises an `ApiException` whenever `status >= 400` (even under an HTTP 200).
List endpoints also read the `X-Pagination-*` response headers into `PaginatedResult.getMeta()`.

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

// These API-key endpoints require a token-authenticated client.
ApiKeyResponse masked = client.auth.getApiKey();
ApiKeyResponse created = client.auth.createApiKey("password");
client.auth.deleteApiKey();

client.auth.changePassword("user@example.com", "old-password", "new-password");
client.auth.requestPasswordReset("user@example.com");
client.auth.resetPassword("user@example.com", resetToken, "new-password");
```

## Configuration

| Option           | Type    | Default                            | Description                               |
|------------------|---------|------------------------------------|-------------------------------------------|
| `apiKey`         | String  | —                                  | Preferred credential (`X-Api-Key` header) |
| `token`          | String  | —                                  | Legacy access token (`Bearer` header)     |
| `accountId`      | String  | —                                  | Default account/workspace ID              |
| `baseUrl`        | String  | `https://api.assinafy.com.br/v1`   | API base URL (sandbox or production)      |
| `timeoutMs`      | int     | `30000`                            | Request timeout in milliseconds           |
| `maxRetries`     | int     | `0`                                | Auto-retries on HTTP 429/503 (honors `Retry-After`) |

### Factory Methods

```java
// Positional factory
AssinafyClient client = AssinafyClient.create("api-key", "account-id",
    opts -> opts.setTimeoutMs(60_000));

// From a map (snake_case or camelCase keys)
AssinafyClient client = AssinafyClient.fromConfig(Map.of(
    "api_key", System.getenv("ASSINAFY_API_KEY"),
    "account_id", System.getenv("ASSINAFY_ACCOUNT_ID")
));
```

## Resources

### Documents

```java
// Upload from file
DocumentDetails doc = client.documents.upload(new File("contract.pdf"));

// Upload from bytes
DocumentDetails doc = client.documents.upload(pdfBytes, "contract.pdf");

// List documents
PaginatedResult<DocumentListItem> page = client.documents.list(Map.of("page", "1", "per_page", "20"));

// Get document details
DocumentDetails details = client.documents.details(doc.getId());

// Wait until ready for signing
DocumentDetails ready = client.documents.waitUntilReady(doc.getId());

// Download the final signed PDF (defaults to the "certificated" artifact; 404s until certificated)
byte[] pdf = client.documents.download(doc.getId());
byte[] original = client.documents.download(doc.getId(), "original");
byte[] thumbnail = client.documents.thumbnail(doc.getId());
byte[] pageImage = client.documents.downloadPage(doc.getId(), pageId);

// Audit trail
List<DocumentActivity> activities = client.documents.activities(doc.getId());

// Check signing progress
boolean done = client.documents.isFullySigned(doc.getId());
SigningProgress progress = client.documents.getSigningProgress(doc.getId());

// Delete
client.documents.delete(doc.getId());

// Public (unauthenticated) — minimal info for signer landing pages
DocumentDetails publicInfo = client.documents.getPublic(doc.getId());
client.documents.sendToken(doc.getId(), "signer@example.com", "email");

// Document tags
List<Tag> tags = client.documents.listTags(doc.getId());
client.documents.appendTags(doc.getId(), List.of("Urgent"));
client.documents.replaceTags(doc.getId(), List.of("Contracts", "2026-Q1"));
client.documents.detachTag(doc.getId(), tagId);
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
client.signers.update(signer.getId(), new UpdateSignerPayload().setFullName("Johnny Doe"));
client.signers.delete(signer.getId());
```

### Assignments

```java
Assignment assignment = client.assignments.create(doc.getId(),
    new CreateAssignmentPayload()
        .setMethod("virtual")
        .setSignerStrings(signer1.getId(), signer2.getId())
        .setMessage("Please review and sign")
        .setExpiresAt("2024-12-31T23:59:00Z"));

client.assignments.resendNotification(doc.getId(), assignment.getId(), signer1.getId());
client.assignments.resetExpiration(doc.getId(), assignment.getId(), "2027-06-30T00:00:00Z");
client.assignments.clearExpiration(doc.getId(), assignment.getId());  // remove expiration (sends expires_at: null)
client.assignments.estimateResendCost(doc.getId(), assignment.getId(), signer1.getId());
client.assignments.whatsappNotifications(doc.getId(), assignment.getId());
```

### Assignments (signer-facing)

Endpoints authorised via a short-lived `signer-access-code`. These are typically called from a
signer landing page rather than from the account-holder's server.

```java
// Fetch the assignment the signer is being asked to complete (GET /sign returns the document view,
// whose getAssignment()/getCurrentSigner() carry the signer-facing assignment data).
DocumentDetails signingView = client.signerSelf.getSign(signerAccessCode);

// Submit collect-method field values
client.assignments.sign(doc.getId(), assignmentId, signerAccessCode, List.of(
    Map.of(
        "itemId", "item-1",
        "fieldId", "field-1",
        "pageId", "page-1",
        "value", "John Doe"
    )
));

// Decline the assignment
client.assignments.decline(doc.getId(), assignmentId, signerAccessCode, "Not happy with clause 3");
```

### Signer Self-Service

```java
// Profile and terms
Signer self = client.signerSelf.getSelf(signerAccessCode);
client.signerSelf.acceptTerms(signerAccessCode);

// Email/WhatsApp verification flow
client.signerSelf.verifyEmail("123456", signerAccessCode);
client.signerSelf.confirmSignerData(doc.getId(), signerAccessCode,
    new ConfirmSignerDataPayload().setEmail("a@b.com").setHasAcceptedTerms(true));

// Signature image upload (image type auto-detected as PNG or JPEG)
client.signerSelf.uploadSignature(signerAccessCode, signatureBytes, "signature");
byte[] saved = client.signerSelf.downloadSignature(signerAccessCode, "signature");

// Multi-document signer flows
DocumentDetails signingView = client.signerSelf.getSign(signerAccessCode);
DocumentDetails current = client.signerSelf.getCurrentDocument(signerId, signerAccessCode);
PaginatedResult<DocumentDetails> mine = client.signerSelf.listDocuments(
    signerId, signerAccessCode, Map.of("status", "pending_signature"));
byte[] signerCopy = client.signerSelf.downloadDocument(signerId, doc.getId(), "original", signerAccessCode);
client.signerSelf.signMultiple(signerAccessCode, List.of(doc1.getId(), doc2.getId()));
client.signerSelf.declineMultiple(signerAccessCode, List.of(doc1.getId()), "Not interested");
```

### Webhooks

```java
WebhookSubscription sub = client.webhooks.register(
    new RegisterWebhookPayload("https://example.com/webhooks", "admin@example.com")
        .setEvents(List.of("document_ready", "signer_signed_document"))
);

client.webhooks.getSubscription();
client.webhooks.inactivate();         // deactivate but keep the subscription
client.webhooks.deleteSubscription(); // permanently remove the subscription
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
client.tags.delete(updated.getId(), true);
```

### Field Definitions

```java
FieldDefinition field = client.fields.create(new CreateFieldPayload("text", "Reference")
    .setRequired(true));

PaginatedResult<FieldDefinition> fields = client.fields.list(
    Map.of("include_standard", "true"));

FieldDefinition one = client.fields.get(field.getId());
client.fields.update(field.getId(), new UpdateFieldPayload().setName("Internal Reference"));
client.fields.delete(field.getId());

FieldValidationResult validation = client.fields.validate(field.getId(), "ABC-123");
List<FieldTypeInfo> fieldTypes = client.fields.listTypes();
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

# Or run locally with the Maven Wrapper (requires JDK 21+)
./mvnw test

# Live smoke tests against the sandbox (skipped unless credentials are set; defaults to the sandbox base URL)
ASSINAFY_API_KEY=... ASSINAFY_ACCOUNT_ID=... ./mvnw test -Dtest=LiveSmokeTest
```

CI runs `mvn verify` on a JDK 21 + 25 matrix (GitHub Actions, mirrored to `.gitlab-ci.yml`). Releases publish
to GitHub Packages on a `v*` tag via the `release` profile (`-Prelease`, which also builds `-sources` and
`-javadoc` jars).

## License

MIT
