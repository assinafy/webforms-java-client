# Assinafy Webforms Java Client SDK

Java client SDK for the [Assinafy Webforms API](https://api.assinafy.com.br/v1/docs).

Covers the documented document, signer, assignment, field definition, webhook, template, and high-level `uploadAndRequestSignatures` flows.

## Requirements

- Java 25+
- Maven 3.8+ (or Gradle 7+)

## Installation

### Maven

```xml
<dependency>
    <groupId>com.assinafy</groupId>
    <artifactId>webforms-java-client-sdk</artifactId>
    <version>1.2.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.assinafy:webforms-java-client-sdk:1.2.0'
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
```

## Configuration

| Option           | Type    | Default                            | Description                               |
|------------------|---------|------------------------------------|-------------------------------------------|
| `apiKey`         | String  | —                                  | Preferred credential (`X-Api-Key` header) |
| `token`          | String  | —                                  | Legacy access token (`Bearer` header)     |
| `accountId`      | String  | —                                  | Default account/workspace ID              |
| `baseUrl`        | String  | `https://api.assinafy.com.br/v1`   | API base URL (sandbox or production)      |
| `timeoutMs`      | int     | `30000`                            | Request timeout in milliseconds           |

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

// Download signed PDF
byte[] pdf = client.documents.download(doc.getId());

// Check signing progress
boolean done = client.documents.isFullySigned(doc.getId());
SigningProgress progress = client.documents.getSigningProgress(doc.getId());

// Delete
client.documents.delete(doc.getId());
```

### Signers

```java
Signer signer = client.signers.create(
    new CreateSignerPayload("John Doe", "john@example.com")
        .setWhatsappPhoneNumber("+5548999990000")
);

// Idempotent by email — reuses if already exists
Signer existing = client.signers.findByEmail("john@example.com");

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
client.assignments.resetExpiration(doc.getId(), assignment.getId(), "2025-06-30T00:00:00Z");
client.assignments.estimateResendCost(doc.getId(), assignment.getId(), signer1.getId());
client.assignments.whatsappNotifications(doc.getId(), assignment.getId());
```

### Webhooks

```java
WebhookSubscription sub = client.webhooks.register(
    new RegisterWebhookPayload("https://example.com/webhooks", "admin@example.com")
        .setEvents(List.of("document_ready", "signer_signed_document"))
);

client.webhooks.getSubscription();
client.webhooks.inactivate();
client.webhooks.listEventTypes();
client.webhooks.listDispatches();
client.webhooks.retryDispatch(dispatchId);
```

### Field Definitions

```java
FieldDefinition field = client.fields.create(new CreateFieldPayload("text", "Reference")
    .setRequired(true));

PaginatedResult<FieldDefinition> fields = client.fields.list(
    Map.of("include_standard", "true"));

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
        .setNotificationMethods(List.of("Email")))
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

# Or run locally with Maven (requires Java 17+)
mvn test
```

## License

MIT
