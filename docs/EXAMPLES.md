# Examples

Worked examples for every public method in the SDK, with the **request** body the SDK sends and the
**response** payload it parses.

## Response envelope

Every Assinafy API response is wrapped in a JSON envelope:

```json
{ "status": 200, "message": "", "data": { /* ... */ } }
```

- `status` — the logical status (mirrors the HTTP status). A value `>= 400` is raised as an `ApiException`,
  even when the HTTP status is 200.
- `message` — a human-readable message (populated on errors).
- `data` — the payload. The SDK unwraps `data` and returns it as the typed model; the examples below show the
  contents of `data` only. List endpoints additionally read `X-Pagination-*` response headers into
  `PaginatedResult.getMeta()`.

Error envelope example (raised as `ApiException`, `getStatusCode() == 401`):

```json
{ "status": 401, "data": null, "message": "Credenciais inválidas." }
```

## Client Setup

```java
import com.assinafy.sdk.AssinafyClient;
import com.assinafy.sdk.AssinafyClientOptions;

// Using API key (recommended) — sent as the X-Api-Key header
AssinafyClient client = new AssinafyClient(new AssinafyClientOptions()
    .setApiKey(System.getenv("ASSINAFY_API_KEY"))
    .setAccountId(System.getenv("ASSINAFY_ACCOUNT_ID")));

// Using the sandbox (recommended during development)
AssinafyClient sandbox = new AssinafyClient(new AssinafyClientOptions()
    .setApiKey("k_sandbox")
    .setAccountId("acc_sandbox")
    .setBaseUrl("https://sandbox.assinafy.com.br/v1")
    .setMaxRetries(2));     // optional: retry HTTP 429/503 honoring Retry-After

// Factory methods
AssinafyClient c1 = AssinafyClient.create("api-key", "account-id");
AssinafyClient c2 = AssinafyClient.create("api-key", "account-id", opts -> opts.setTimeoutMs(60_000));
AssinafyClient c3 = AssinafyClient.fromConfig(Map.of(
    "api_key", "k_xxx",
    "account_id", "acc_xxx"
));
```

## Authentication

```java
AuthenticationResult session = client.auth.login("user@example.com", "password");
```

Request `POST /login`:

```json
{ "email": "user@example.com", "password": "password" }
```

Response `data`:

```json
{
  "access_token": "eyJhbGciOiJIUzI1...",
  "user": { "id": "62d6...", "email": "user@example.com", "name": "User" },
  "accounts": [ { "id": "acc_xxx", "name": "Workspace", "roles": ["owner"] } ]
}
```

```java
// Social login — POST /authentication/social-login
client.auth.socialLogin(new SocialLoginPayload("google", googleToken, true));
// Request: { "provider": "google", "token": "<google-token>", "has_accepted_terms": true }

// API keys (these require a token-authenticated client, NOT an API-key-only client)
ApiKeyResponse masked  = client.auth.getApiKey();      // GET /users/api-keys     -> { "api_key": "k_****" }
ApiKeyResponse created = client.auth.createApiKey("password"); // POST /users/api-keys -> { "api_key": "k_new" }
client.auth.deleteApiKey();                            // DELETE /users/api-keys

// Password flows (token is OPTIONAL on reset — omit it when supplied out-of-band)
client.auth.changePassword("user@example.com", "old", "new"); // PUT /authentication/change-password
client.auth.requestPasswordReset("user@example.com");         // PUT /authentication/request-password-reset
client.auth.resetPassword("user@example.com", resetToken, "new"); // PUT /authentication/reset-password
client.auth.resetPassword("user@example.com", null, "new");       // token omitted from the body
```

## Documents

### Upload — `POST /accounts/{account_id}/documents`

```java
DocumentDetails doc = client.documents.upload(new File("contract.pdf"));
DocumentDetails doc2 = client.documents.upload(pdfBytes, "contract.pdf");
```

Request: `multipart/form-data` with a single `file` part (`application/pdf`, max 25 MB). Response `data`:

```json
{
  "resource": "document",
  "id": "1031ff796b7215922eac00acdcca",
  "account_id": "102d25a489f34a275d31a16045fd",
  "template_id": null,
  "name": "contract.pdf",
  "status": "uploaded",
  "artifacts": { "original": "https://sandbox.assinafy.com.br/v1/documents/1031.../download/original" },
  "is_closed": false,
  "signing_url": "https://app-sandbox.assinafy.com.br/sign/1031ff796b7215922eac00acdcca",
  "decline_reason": null,
  "declined_by": null,
  "tags": [],
  "created_at": "2026-06-05T20:49:18Z",
  "updated_at": "2026-06-05T20:49:19Z",
  "pages": []
}
```

### Get details — `GET /documents/{document_id}`

```java
DocumentDetails details = client.documents.details(doc.getId());      // alias: client.documents.get(id)
DocumentDetails ready   = client.documents.waitUntilReady(doc.getId(), 60_000, 2_000);
```

Response `data` (once processed — note the populated `pages` and `assignment`):

```json
{
  "resource": "document",
  "id": "1031ff796b7215922eac00acdcca",
  "name": "contract.pdf",
  "status": "metadata_ready",
  "artifacts": {
    "original": "https://.../download/original",
    "thumbnail": "https://.../thumbnail"
  },
  "is_closed": false,
  "assignment": null,
  "pages": [
    { "id": "1031ff79aca3e44a04b2575ae900", "number": 1, "height": 1651, "width": 1275,
      "download_url": "https://.../pages/1031ff79aca3e44a04b2575ae900/download" }
  ]
}
```

### List — `GET /accounts/{account_id}/documents`

```java
PaginatedResult<DocumentListItem> page = client.documents.list(
    Map.of("page", "1", "per_page", "20", "sort", "-created_at"));
System.out.println("Total: " + page.getMeta().getTotal());   // from X-Pagination-Total-Count header
```

### Statuses — `GET /documents/statuses`

```java
List<DocumentStatus> statuses = client.documents.statuses();
```

Response `data`:

```json
[
  { "code": "uploaded", "deletable": false },
  { "code": "metadata_ready", "deletable": true },
  { "code": "pending_signature", "deletable": true },
  { "code": "certificated", "deletable": false }
]
```

### Downloads, page image, thumbnail, activities, delete

```java
// GET /documents/{id}/download/{artifact_name}
byte[] signed   = client.documents.download(doc.getId());             // defaults to "certificated"
byte[] original = client.documents.download(doc.getId(), "original");
byte[] thumb    = client.documents.thumbnail(doc.getId());            // GET /documents/{id}/thumbnail (JPEG)
byte[] pageImg  = client.documents.downloadPage(doc.getId(), pageId); // GET /documents/{id}/pages/{pid}/download
List<DocumentActivity> log = client.documents.activities(doc.getId());// GET /documents/{id}/activities
client.documents.delete(doc.getId());                                 // DELETE /documents/{id}
```

> `download(id)` requests the `"certificated"` (final signed) artifact, which is only available after the
> document is certificated. For an unsigned document it raises an `ApiException` (HTTP 404,
> "Artefato não está disponível."). Use `download(id, "original")` for the uploaded PDF.

### Verify — `GET /documents/{signature_hash}/verify` (public)

```java
Map<String, Object> verification = client.documents.verify(signatureHash);
```

### Public lookup — `GET /public/documents/{document_id}` (no API key)

```java
DocumentDetails info = client.documents.getPublic(documentId);
String pageCount = info.getPageCount();   // "1" (string)
String createdBy = info.getCreatedBy();   // "John Smith"
```

Response `data`:

```json
{ "resource": "document", "id": "doc1", "name": "1.pdf", "page_count": "1", "created_by": "John Smith" }
```

### Send signing token — `PUT /public/documents/{document_id}/send-token`

```java
client.documents.sendToken(documentId, "signer@example.com", "email");
// Request: { "recipient": "signer@example.com", "channel": "email" }
```

### Document tags

```java
List<Tag> tags = client.documents.listTags(doc.getId());                       // GET .../{id}/tags
client.documents.appendTags(doc.getId(), List.of("Urgent"));                   // POST .../{id}/tags
client.documents.replaceTags(doc.getId(), List.of("Contracts", "2026-Q1"));    // PUT  .../{id}/tags
client.documents.detachTag(doc.getId(), tagId);                                // DELETE .../{id}/tags/{tagId}
// append/replace request body: { "tags": ["Urgent"] }; response data: [ { "id": "...", "name": "Urgent" } ]
```

### Signing progress (client-side helpers)

```java
boolean done = client.documents.isFullySigned(doc.getId());
SigningProgress progress = client.documents.getSigningProgress(doc.getId());
System.out.printf("Signed: %d/%d (%.1f%%)%n",
    progress.getSigned(), progress.getTotal(), progress.getPercentage());
```

## Signers

```java
// Create — POST /accounts/{account_id}/signers  (idempotent by email; see note)
Signer signer = client.signers.create(
    new CreateSignerPayload("John Doe", "john@example.com").setWhatsappPhoneNumber("+5548999990000"));
```

Request:

```json
{ "full_name": "John Doe", "email": "john@example.com", "whatsapp_phone_number": "+5548999990000" }
```

Response `data`:

```json
{
  "resource": "signer",
  "id": "62d6ee35c7741ca4006b9e11",
  "full_name": "John Doe",
  "email": "john@example.com",
  "whatsapp_phone_number": "+5548999990000",
  "has_accepted_terms": false
}
```

> **Idempotent create.** When an email is supplied, `create` looks the signer up first and returns the
> existing record without re-creating it; it does **not** update changed fields on an existing signer — use
> `update` for that.

```java
Signer existing = client.signers.findByEmail("john@example.com");            // GET .../signers?search=...
Signer fetched  = client.signers.get(signer.getId());                        // GET .../signers/{id}
PaginatedResult<Signer> list = client.signers.list(Map.of("search", "john"));// GET .../signers
client.signers.update(signer.getId(), new UpdateSignerPayload().setFullName("Johnny Doe")); // PUT .../signers/{id}
client.signers.delete(signer.getId());                                       // DELETE .../signers/{id}
```

## Assignments

### Create — `POST /documents/{documentId}/assignments`

```java
Assignment assignment = client.assignments.create(doc.getId(),
    new CreateAssignmentPayload()
        .setMethod("virtual")
        .setSignerStrings(signer1.getId(), signer2.getId())
        .setMessage("Please review and sign")
        .setExpiresAt("2026-12-31T23:59:00Z")
        .setCopyReceivers(List.of(observerId)));
```

Request:

```json
{
  "method": "virtual",
  "signers": [ { "id": "<signer1>" }, { "id": "<signer2>" } ],
  "message": "Please review and sign",
  "expires_at": "2026-12-31T23:59:00Z",
  "copy_receivers": ["<observerId>"]
}
```

Response `data` (abridged — note per-signer `step`, `notified`, `notification_history`):

```json
{
  "resource": "assignment",
  "id": "1031ffc6bdf1a03ed08eb86ffbad",
  "sender_email": "owner@example.com",
  "method": "virtual",
  "expires_at": "2026-12-31T23:59:00Z",
  "signers": [
    { "id": "1030...", "full_name": "Bill M", "email": "billm@billm.org",
      "completed": false, "verification_method": "Email", "notification_methods": ["Email"],
      "step": 1, "notified": true, "notification_history": [] }
  ],
  "items": [ { "id": "1031...", "page": null, "signer": { "id": "1030..." },
              "field": { "id": "102d...", "name": "Virtual", "type": "virtual" }, "completed": false } ],
  "summary": { "signer_count": 1, "completed_count": 0, "signers": [ { "id": "1030...", "completed": false } ] },
  "signing_urls": [ { "signer_id": "1030...", "url": "https://app.../sign/...?email=billm%40billm.org" } ]
}
```

### Estimate cost — `POST /documents/{documentId}/assignments/estimate-cost`

```java
Map<String, Object> cost = client.assignments.estimateCost(doc.getId(),
    new CreateAssignmentPayload()
        .setSigners(List.of(new SignerRef().setId(signerId).setVerificationMethod("Email"))));
```

Response `data`:

```json
{
  "documents": 1, "credits": 0, "total_credits": 0,
  "document_balance": 67, "credit_balance": 0,
  "has_sufficient_resources": true, "blocking_reason": null
}
```

### Expiration, resend, notifications

```java
// PUT /documents/{doc}/assignments/{asg}/reset-expiration  — { "expires_at": "..." }
client.assignments.resetExpiration(doc.getId(), assignment.getId(), "2027-06-30T00:00:00Z");
// Pass null (or use clearExpiration) to remove the expiration: { "expires_at": null }
client.assignments.clearExpiration(doc.getId(), assignment.getId());

// PUT .../assignments/{asg}/signers/{signerId}/resend
client.assignments.resendNotification(doc.getId(), assignment.getId(), signer1.getId());
// POST .../assignments/{asg}/signers/{signerId}/estimate-resend-cost
client.assignments.estimateResendCost(doc.getId(), assignment.getId(), signer1.getId());
// GET .../assignments/{asg}/whatsapp-notifications
List<WhatsappNotification> notifications = client.assignments.whatsappNotifications(doc.getId(), assignment.getId());
```

### Signer-facing (authorised via `signer-access-code` query param)

```java
// POST .../assignments/{asg}  — submit collect-method field values (entries is a JSON array)
client.assignments.sign(documentId, assignmentId, signerAccessCode, List.of(
    Map.of("itemId", "item-1", "fieldId", "field-1", "pageId", "page-1", "value", "John Doe")));

// PUT .../assignments/{asg}/reject  — { "decline_reason": "..." }
client.assignments.decline(documentId, assignmentId, signerAccessCode, "Not happy with clause 3");
```

> To fetch the assignment as a signer, use `client.signerSelf.getSign(signerAccessCode)` (`GET /sign`), which
> returns a `DocumentDetails` whose `getAssignment()` / `getCurrentSigner()` carry the assignment view. (There
> is no standalone `GET /documents/{id}/assignments/{id}` endpoint.)

## Webhooks

```java
// Register / replace — PUT /accounts/{account_id}/webhooks/subscriptions
WebhookSubscription sub = client.webhooks.register(
    new RegisterWebhookPayload("https://example.com/webhooks/assinafy", "admin@example.com")
        .setEvents(List.of("document_ready", "signer_signed_document", "document_processing_failed")));
```

Request:

```json
{
  "url": "https://example.com/webhooks/assinafy",
  "email": "admin@example.com",
  "events": ["document_ready", "signer_signed_document", "document_processing_failed"],
  "is_active": true
}
```

Response `data` (also returned by `getSubscription()`):

```json
{
  "events": ["document_ready", "signer_signed_document"],
  "is_active": true,
  "url": "https://example.com/webhooks/assinafy",
  "email": "admin@example.com",
  "updated_at": "2026-06-05T20:46:43Z"
}
```

```java
WebhookSubscription current = client.webhooks.getSubscription();    // GET .../webhooks/subscriptions (null if none)
client.webhooks.inactivate();                                       // PUT .../webhooks/inactivate
client.webhooks.deleteSubscription();                               // DELETE .../webhooks/subscriptions
List<WebhookEventTypeInfo> types = client.webhooks.listEventTypes();// GET /webhooks/event-types
PaginatedResult<WebhookDispatch> dispatches = client.webhooks.listDispatches(   // GET /accounts/{id}/webhooks
    new ListDispatchesParams().setDelivered(false).setPerPage(20));
client.webhooks.retryDispatch(dispatchId);                          // POST .../webhooks/{dispatchId}/retry
```

## Tags

```java
PaginatedResult<Tag> tags = client.tags.list(Map.of("search", "contract")); // GET /accounts/{id}/tags
Tag created = client.tags.create(new CreateTagPayload("Contracts").setColor("ff8800")); // POST .../tags
// Request: { "name": "Contracts", "color": "ff8800" }   (name max 64 chars)
Tag updated = client.tags.update(created.getId(),                           // PUT .../tags/{tagId}
    new UpdateTagPayload().setName("Sales Contracts").clearColor());        // clearColor() sends "color": null
client.tags.delete(updated.getId(), true);                                  // DELETE .../tags/{tagId}?force=true
```

## Field Definitions

```java
FieldDefinition field = client.fields.create(new CreateFieldPayload("text", "Reference").setRequired(true));
```

Request `POST /accounts/{account_id}/fields`: `{ "type": "text", "name": "Reference", "is_required": true }`.
Response `data`:

```json
{
  "resource": "field_definition", "id": "1031ff7e475f33e11d21b55f6ebd",
  "name": "Reference", "type": "text", "regex": null,
  "is_pre_defined": false, "is_active": true, "is_required": true,
  "is_standard": false, "is_read_only": false, "is_visible": true
}
```

```java
PaginatedResult<FieldDefinition> fields = client.fields.list(Map.of("include_standard", "true")); // GET .../fields
FieldDefinition one = client.fields.get(field.getId());                  // GET .../fields/{id}
client.fields.update(field.getId(), new UpdateFieldPayload().setName("Internal Reference")); // PUT .../fields/{id}
client.fields.delete(field.getId());                                     // DELETE .../fields/{id}
List<FieldTypeInfo> fieldTypes = client.fields.listTypes();              // GET /field-types
```

Validate — `POST /accounts/{account_id}/fields/{field_id}/validate` (body `{ "value": ... }`):

```java
FieldValidationResult validation = client.fields.validate(field.getId(), "ABC-123");
// Response data: { "type": "text", "success": true, "error_message": "" }

List<FieldValidationResult> results = client.fields.validateMultiple(List.of(
    new FieldValidationPayload(field.getId(), "ABC-123")));
// Request: [ { "field_id": "<id>", "value": "ABC-123" } ]
// Response data: [ { "field_id": "<id>", "type": "text", "success": true, "error_message": "" } ]
```

## Templates

```java
PaginatedResult<TemplateListItem> templates = client.templates.list(Map.of("search", "NDA")); // GET .../templates
TemplateDetails template = client.templates.get(templateId);                  // GET .../templates/{id}
TemplateRole firstRole = template.getRoles().get(0);

// Create a document from a template — POST /accounts/{id}/templates/{tid}/documents
DocumentDetails doc = client.documents.createFromTemplate(
    templateId,
    List.of(new TemplateSigner(firstRole.getId(), signerId)
        .setVerificationMethod("Email").setNotificationMethods(List.of("Email")).setStep(1)),
    new CreateDocumentFromTemplateOptions()
        .setName("NDA - John Doe").setTags(List.of("Generated")));

// Estimate cost — POST /accounts/{id}/templates/{tid}/documents/estimate-cost
Map<String, Object> cost = client.documents.estimateCostFromTemplate(
    templateId, List.of(new TemplateSigner(firstRole.getId(), signerId)));
```

The `createFromTemplate` request body:

```json
{
  "signers": [ { "role_id": "<role>", "id": "<signer>", "verification_method": "Email",
                 "notification_methods": ["Email"], "step": 1 } ],
  "name": "NDA - John Doe",
  "tags": ["Generated"]
}
```

## Signer Self-Service (authorised via `signer-access-code`)

```java
// GET /sign — the signer-facing document + assignment view
DocumentDetails signingView = client.signerSelf.getSign(signerAccessCode);
Signer current = signingView.getCurrentSigner();   // who the access code resolved to

Signer self = client.signerSelf.getSelf(signerAccessCode);            // GET /signers/self
client.signerSelf.acceptTerms(signerAccessCode);                      // PUT /signers/accept-terms
client.signerSelf.verifyEmail("123456", signerAccessCode);            // POST /verify

// PUT /documents/{id}/signers/confirm-data — required before signing a virtual-method document
client.signerSelf.confirmSignerData(documentId, signerAccessCode,
    new ConfirmSignerDataPayload()
        .setEmail("signer@example.com").setWhatsappPhoneNumber("+5548999990000").setHasAcceptedTerms(true));

// POST /signature — upload (PNG/JPEG auto-detected); returns void, throws on error envelope
client.signerSelf.uploadSignature(signerAccessCode, signatureBytes, "signature");
byte[] saved = client.signerSelf.downloadSignature(signerAccessCode, "signature"); // GET /signature/{type}
```

### Signer documents

```java
// GET /signers/{id}/document — current document; data includes a top-level "current_signer" object
DocumentDetails currentDoc = client.signerSelf.getCurrentDocument(signerId, signerAccessCode);
Signer who = currentDoc.getCurrentSigner();

// GET /signers/{id}/documents (+ filters)
PaginatedResult<DocumentDetails> mine = client.signerSelf.listDocuments(
    signerId, signerAccessCode, Map.of("status", "pending_signature"));

// GET /signers/{id}/documents/{docId}/download/{artifact}
byte[] copy = client.signerSelf.downloadDocument(signerId, documentId, "original", signerAccessCode);

// PUT /signers/documents/sign-multiple    — { "document_ids": ["..."] }
client.signerSelf.signMultiple(signerAccessCode, List.of(doc1.getId(), doc2.getId()));
// PUT /signers/documents/decline-multiple — { "document_ids": ["..."], "decline_reason": "..." }
client.signerSelf.declineMultiple(signerAccessCode, List.of(doc1.getId()), "Reason");
```

## High-level helper

```java
UploadAndRequestSignaturesResult result = client.uploadAndRequestSignatures(
    new UploadAndRequestSignaturesOptions(
        new File("contract.pdf"),
        List.of(
            new UploadAndRequestSignaturesSigner("John Doe", "john@example.com"),
            new UploadAndRequestSignaturesSigner("Jane Smith", "jane@example.com")
                .setWhatsappPhoneNumber("+5548999990000")))
    .setMessage("Please sign this contract")
    .setExpiresAt("2026-12-31T00:00:00Z"));

DocumentDetails document = result.getDocument();   // uploaded + waited until ready
Assignment assignment = result.getAssignment();    // created virtual assignment
List<String> signerIds = result.getSignerIds();
```

This helper uploads the file, waits until it is processable, creates (idempotent-by-email) signers, and
creates a `virtual` assignment in one call.

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
    // API returned an error (HTTP non-2xx or an error envelope)
    System.err.println("API " + e.getStatusCode() + ": " + e.getMessage());
    System.err.println("Body: " + e.getResponseBody());
    if (e.getStatusCode() == 429 && e.getRetryAfterSeconds() != null) {
        Thread.sleep(e.getRetryAfterSeconds() * 1000L);   // honor Retry-After, or set options.maxRetries
    }
} catch (NetworkException e) {
    System.err.println("Network error: " + e.getMessage());
} catch (AssinafyException e) {
    System.err.println("SDK error: " + e.getMessage());
}
```
