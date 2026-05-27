# Examples

## Client Setup

```java
import com.assinafy.sdk.AssinafyClient;
import com.assinafy.sdk.AssinafyClientOptions;

// Using API key (recommended)
AssinafyClient client = new AssinafyClient(new AssinafyClientOptions()
    .setApiKey(System.getenv("ASSINAFY_API_KEY"))
    .setAccountId(System.getenv("ASSINAFY_ACCOUNT_ID")));

// Using sandbox
AssinafyClient sandbox = new AssinafyClient(new AssinafyClientOptions()
    .setApiKey("k_sandbox")
    .setAccountId("acc_sandbox")
    .setBaseUrl("https://sandbox.assinafy.com.br/v1"));

// Factory methods
AssinafyClient c1 = AssinafyClient.create("api-key", "account-id");
AssinafyClient c2 = AssinafyClient.fromConfig(Map.of(
    "api_key", "k_xxx",
    "account_id", "acc_xxx"
));
```

## Upload and Request Signatures (High-level)

```java
import com.assinafy.sdk.models.*;
import java.io.File;
import java.util.List;

UploadAndRequestSignaturesResult result = client.uploadAndRequestSignatures(
    new UploadAndRequestSignaturesOptions(
        new File("contract.pdf"),
        List.of(
            new UploadAndRequestSignaturesSigner("John Doe", "john@example.com"),
            new UploadAndRequestSignaturesSigner("Jane Smith", "jane@example.com")
                .setWhatsappPhoneNumber("+5548999990000")
        )
    )
    .setMessage("Please sign this contract")
    .setExpiresAt("2026-12-31T00:00:00Z")
);

DocumentDetails document = result.getDocument();
Assignment assignment = result.getAssignment();
List<String> signerIds = result.getSignerIds();
```

## Documents

```java
// Upload
DocumentDetails doc = client.documents.upload(new File("contract.pdf"));
DocumentDetails doc2 = client.documents.upload(pdfBytes, "contract.pdf");

// List with pagination
PaginatedResult<DocumentListItem> page = client.documents.list(
    Map.of("page", "1", "per_page", "20", "sort", "-created_at"));
System.out.println("Total: " + page.getMeta().getTotal());

// Details
DocumentDetails details = client.documents.details(doc.getId());

// Supported statuses
List<DocumentStatus> statuses = client.documents.statuses();

// Poll until processing completes
DocumentDetails ready = client.documents.waitUntilReady(doc.getId(), 60_000, 2_000);

// Download
byte[] signed = client.documents.download(doc.getId()); // certificated PDF
byte[] original = client.documents.download(doc.getId(), "original");
byte[] thumbnail = client.documents.thumbnail(doc.getId());

// Verify hash
Map<String, Object> verification = client.documents.verify(hash);

// Document tags
List<Tag> tags = client.documents.listTags(doc.getId());
client.documents.appendTags(doc.getId(), List.of("Urgent"));
client.documents.replaceTags(doc.getId(), List.of("Contracts", "2026-Q1"));
client.documents.detachTag(doc.getId(), tagId);

// Signing progress
boolean isSigned = client.documents.isFullySigned(doc.getId());
SigningProgress progress = client.documents.getSigningProgress(doc.getId());
System.out.printf("Signed: %d/%d (%.1f%%)%n",
    progress.getSigned(), progress.getTotal(), progress.getPercentage());
```

## Signers

```java
// Create (idempotent by email)
Signer signer = client.signers.create(
    new CreateSignerPayload("John Doe", "john@example.com")
        .setWhatsappPhoneNumber("+5548999990000")
);

// Find by email
Signer existing = client.signers.findByEmail("john@example.com");

// List
PaginatedResult<Signer> list = client.signers.list(Map.of("search", "john", "per_page", "50"));

// Update
client.signers.update(signer.getId(),
    new UpdateSignerPayload().setFullName("Johnny Doe"));

// Delete
client.signers.delete(signer.getId());
```

## Assignments

```java
// Create virtual assignment
Assignment assignment = client.assignments.create(doc.getId(),
    new CreateAssignmentPayload()
        .setMethod("virtual")
        .setSignerStrings(signer1.getId(), signer2.getId())
        .setMessage("Please review and sign")
        .setExpiresAt("2024-12-31T23:59:00Z")
        .setCopyReceivers(List.of(observerId)));

// Estimate cost
Map<String, Object> cost = client.assignments.estimateCost(doc.getId(),
    new CreateAssignmentPayload()
        .setSigners(List.of(new SignerRef().setVerificationMethod("Whatsapp"))));

// Resend notification
client.assignments.resendNotification(doc.getId(), assignment.getId(), signer1.getId());

// Reset expiration
client.assignments.resetExpiration(doc.getId(), assignment.getId(), "2025-06-30T00:00:00Z");

// Estimate resend cost and inspect WhatsApp notifications
client.assignments.estimateResendCost(doc.getId(), assignment.getId(), signer1.getId());
client.assignments.whatsappNotifications(doc.getId(), assignment.getId());
```

## Webhooks

```java
// Register / update subscription
WebhookSubscription sub = client.webhooks.register(
    new RegisterWebhookPayload("https://example.com/webhooks/assinafy", "admin@example.com")
        .setEvents(List.of(
            "document_ready",
            "document_prepared",
            "signer_signed_document",
            "signer_rejected_document",
            "document_processing_failed"
        ))
);

// Get current subscription
WebhookSubscription current = client.webhooks.getSubscription();

// Inactivate without deleting
client.webhooks.inactivate();

// List event types
List<WebhookEventTypeInfo> types = client.webhooks.listEventTypes();

// Dispatch history
PaginatedResult<WebhookDispatch> dispatches = client.webhooks.listDispatches(
    new ListDispatchesParams().setDelivered(false).setPerPage(20));

// Retry failed dispatch
client.webhooks.retryDispatch(dispatchId);
```

## Tags

```java
// Workspace-scoped tag catalogue
PaginatedResult<Tag> tags = client.tags.list(Map.of("search", "contract"));

// Create / update / delete
Tag created = client.tags.create(new CreateTagPayload("Contracts").setColor("ff8800"));
Tag updated = client.tags.update(created.getId(),
    new UpdateTagPayload().setName("Sales Contracts").clearColor());
client.tags.delete(updated.getId(), true);
```

## Field Definitions

```java
// Create a field definition for collect/input assignments
FieldDefinition field = client.fields.create(new CreateFieldPayload("text", "Reference")
    .setRequired(true));

// List fields
PaginatedResult<FieldDefinition> fields = client.fields.list(
    Map.of("include_standard", "true"));

// Update
client.fields.update(field.getId(), new UpdateFieldPayload().setName("Internal Reference"));

// Validate input
FieldValidationResult validation = client.fields.validate(field.getId(), "ABC-123");

// Validate multiple inputs
List<FieldValidationResult> results = client.fields.validateMultiple(List.of(
    new FieldValidationPayload(field.getId(), "ABC-123")));

// List available field types
List<FieldTypeInfo> fieldTypes = client.fields.listTypes();
```

## Templates

```java
// List
PaginatedResult<TemplateListItem> templates = client.templates.list(Map.of("search", "NDA"));

// Get details (includes roles)
TemplateDetails template = client.templates.get(templateId);
TemplateRole firstRole = template.getRoles().get(0);

// Create document from template
DocumentDetails doc = client.documents.createFromTemplate(
    templateId,
    List.of(
        new TemplateSigner(firstRole.getId(), signerId)
            .setVerificationMethod("Email")
            .setNotificationMethods(List.of("Email"))
            .setStep(1)
    ),
    new CreateDocumentFromTemplateOptions()
        .setName("NDA - John Doe")
        .setMessage("Please sign at your earliest convenience.")
        .setTags(List.of("Generated"))
);

// Estimate cost
Map<String, Object> cost = client.documents.estimateCostFromTemplate(
    templateId,
    List.of(new TemplateSigner(firstRole.getId(), signerId)));
```

## Public (Unauthenticated) Document Endpoints

```java
// Minimal info for the signer landing page (no API key required)
DocumentDetails publicInfo = client.documents.getPublic(documentId);

// Re-send the verification token to the signer
client.documents.sendToken(documentId, "signer@example.com", "email");
```

## Signer-Facing Assignment Operations

These endpoints are authorised via a short-lived `signer-access-code` query parameter rather than
the account-level API key.

```java
// Fetch the assignment as it appears to the signer
Assignment assignment = client.assignments.get(documentId, assignmentId, signerAccessCode);

// Sign (collect method) — entries is an array of {itemId, fieldId, pageId, value}
client.assignments.sign(documentId, assignmentId, signerAccessCode, List.of(
    Map.of(
        "itemId", "item-1",
        "fieldId", "field-1",
        "pageId", "page-1",
        "value", "John Doe"
    )
));

// Decline
client.assignments.decline(documentId, assignmentId, signerAccessCode, "Not happy with clause 3");
```

## Signer Self-Service

```java
// Fetch full signer-facing document details
DocumentDetails signingView = client.signerSelf.getSign(signerAccessCode);

// Profile, terms, verification
Signer self = client.signerSelf.getSelf(signerAccessCode);
client.signerSelf.acceptTerms(signerAccessCode);
client.signerSelf.verifyEmail("123456", signerAccessCode);

// Required before signing a virtual-method document
client.signerSelf.confirmSignerData(documentId, signerAccessCode,
    new ConfirmSignerDataPayload()
        .setEmail("signer@example.com")
        .setWhatsappPhoneNumber("+5548999990000")
        .setHasAcceptedTerms(true));

// Signature image upload/download (PNG vs JPEG detected from the file header)
client.signerSelf.uploadSignature(signerAccessCode, signatureBytes, "signature");
client.signerSelf.uploadSignature(signerAccessCode, initialBytes, "initial");
byte[] signature = client.signerSelf.downloadSignature(signerAccessCode, "signature");

// Multi-document flows
DocumentDetails current = client.signerSelf.getCurrentDocument(signerId, signerAccessCode);
PaginatedResult<DocumentDetails> all = client.signerSelf.listDocuments(
    signerId, signerAccessCode, Map.of("status", "pending_signature"));
byte[] signerCopy = client.signerSelf.downloadDocument(signerId, documentId, "original", signerAccessCode);
client.signerSelf.signMultiple(signerAccessCode, List.of(doc1.getId(), doc2.getId()));
client.signerSelf.declineMultiple(signerAccessCode, List.of(doc1.getId()), "Reason");
```

## Error Handling

```java
import com.assinafy.sdk.exceptions.*;

try {
    DocumentDetails doc = client.documents.upload(new File("contract.pdf"));
} catch (ValidationException e) {
    // Client-side validation failed (invalid file type, missing fields, etc.)
    System.err.println("Validation: " + e.getMessage());
    e.getErrors().forEach((k, v) -> System.err.println("  " + k + ": " + v));
} catch (ApiException e) {
    // API returned an error response
    System.err.println("API " + e.getStatusCode() + ": " + e.getMessage());
    System.err.println("Response body: " + e.getResponseBody());
} catch (NetworkException e) {
    // Connection/timeout failure
    System.err.println("Network error: " + e.getMessage());
} catch (AssinafyException e) {
    // Any other SDK error
    System.err.println("SDK error: " + e.getMessage());
}
```
