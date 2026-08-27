# Examples

Worked end-to-end examples for the SDK. The [API reference](API_REFERENCE.md) contains the complete
89-operation matrix and centralized request/response payload definitions.

## Response envelope

Every JSON Assinafy API response is wrapped in a JSON envelope:

```json
{ "status": 200, "message": "", "data": { /* ... */ } }
```

- `status` — the logical status (normally mirrors the HTTP status). A value `>= 400` is raised as an `ApiException`,
  even when the HTTP status is 200.
- `message` — a human-readable message (populated on errors).
- `data` — the payload. The SDK unwraps `data` and returns it as the typed model; the examples below show the
  contents of `data` only. List endpoints additionally read `X-Pagination-*` response headers into
  `PaginatedResult.getMeta()`.
- Logo, document artifact, thumbnail, page-image, and signature-image downloads are raw binary responses;
  those methods return `byte[]` and do not parse an envelope.

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
    .setMaxRetries(2));     // optional: retry safe reads on HTTP 429/503; mutations are never replayed

// Factory methods
AssinafyClient c1 = AssinafyClient.create("api-key", "account-id");
AssinafyClient c2 = AssinafyClient.create("api-key", "account-id", opts -> opts.setTimeoutMs(60_000));
AssinafyClient c3 = AssinafyClient.fromConfig(Map.of(
    "api_key", "k_xxx",
    "account_id", "acc_xxx"
));
```

## Accounts and authenticated user

```java
List<WorkspaceAccount> accounts = client.accounts.list();                       // GET /accounts
WorkspaceAccount account = client.accounts.get();                              // GET /accounts/{defaultId}
WorkspaceAccount created = client.accounts.create(                             // POST /accounts
    new AccountPayload("Legal Operations").setNotificationSenderType("Account"));
WorkspaceAccount renamed = client.accounts.update(                             // PUT /accounts/{defaultId}
    new AccountPayload().setName("Legal"));

AccountTheme theme = client.accounts.getTheme();                               // GET .../theme
client.accounts.uploadLogo(pngBytes, "logo.png");                             // POST .../logo
byte[] logo = client.accounts.downloadLogo();                                  // GET .../logo
client.accounts.deleteLogo();                                                  // DELETE .../logo

List<DocumentStatsRow> monthly = client.accounts.stats(Map.of("granularity", "monthly"));
List<DocumentStatsRow> daily = client.accounts.stats(
    Map.of("granularity", "daily", "month", "2026-08"));

User me = client.users.getSelf();                                              // GET /users/self
NotificationPreferences current = client.users.getNotificationPreferences();
NotificationPreferences updated = client.users.updateNotificationPreferences(
    new NotificationPreferences().setDocumentCompleted(true).setSignerDeclined(true));
List<DocumentStatsRow> crossAccount = client.users.stats(Map.of("granularity", "monthly"));

// Permanent. The explicit ID avoids deleting the client's default workspace by mistake.
client.accounts.delete(false, created.getId());
```

Account create/update sends `{"name": string?, "notification_sender_type": "User"|"Account"?}`. Account and
user stats return `DocumentStatsRow[]`; each row contains `period`, `documents_uploaded`, `documents_sent`,
`signature_requests`, `signature_requests_notification_email`,
`signature_requests_notification_whatsapp`, `signature_requests_notification_bypass`,
`signature_requests_verification_email`, `signature_requests_verification_whatsapp`,
`signature_requests_verification_bypass`, `signature_requests_verification_digital_certificate`,
`signature_requests_viewed`, `signature_requests_completed`, and `documents_certified`. Daily stats require a
`month` in `YYYY-MM` form.

Notification preferences use these nine case-sensitive boolean JSON keys: `DocumentCompleted`,
`SignerDeclined`, `DocumentCancelled`, `DocumentAboutToExpire`, `DocumentExpired`,
`DocumentExpirationReset`, `DocumentProcessingFailed`, `TemplateProcessingFailed`, and
`SignerWhatsappFailed`. A PUT merges non-null values and returns the complete map.

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
  "accounts": [ { "id": "acc_xxx", "name": "Workspace", "roles": ["owner"],
                    "is_delete_allowed": false, "created_at": "2026-08-20T12:00:00Z" } ]
}
```

```java
// Social login — POST /authentication/social-login
client.auth.socialLogin(new SocialLoginPayload("google", googleToken, true));
// Request: { "provider": "google", "token": "<google-token>", "has_accepted_terms": true }

// Link a social provider to the authenticated user — POST /auth/link-social-login
client.auth.linkSocialLogin("google", googleToken);
// Request: { "provider": "google", "token": "<google-token>" }   (no has_accepted_terms, unlike social-login)

// Use the bearer session for key rotation so the client does not retain a key it just revoked.
AssinafyClient tokenClient = new AssinafyClient(
    new AssinafyClientOptions().setToken(session.getAccessToken()));
ApiKeyResponse masked  = tokenClient.auth.getApiKey();      // GET /users/api-keys
ApiKeyResponse created = tokenClient.auth.createApiKey("password"); // POST /users/api-keys
tokenClient.auth.deleteApiKey();                            // DELETE /users/api-keys

// Authenticated change-password remains on the bearer client.
tokenClient.auth.changePassword("user@example.com", "old", "new"); // PUT /authentication/change-password

// Reset routes are public; token is OPTIONAL in resetPassword and omitted when supplied out-of-band.
AssinafyClient publicClient = new AssinafyClient(new AssinafyClientOptions());
publicClient.auth.requestPasswordReset("user@example.com");         // PUT /authentication/request-password-reset
publicClient.auth.resetPassword("user@example.com", resetToken, "new"); // PUT /authentication/reset-password
publicClient.auth.resetPassword("user@example.com", null, "new");       // token omitted from the body
```

## Documents

### Upload — `POST /accounts/{account_id}/documents`

```java
DocumentDetails doc = client.documents.upload(new File("contract.pdf"));
DocumentDetails doc2 = client.documents.upload(pdfBytes, "contract.pdf");
```

Request: `multipart/form-data` with a single `file` part (`application/pdf`, max 25 MB and 2,000 pages).
Response `data`:

```json
{
  "resource": "document",
  "id": "1031ff796b7215922eac00acdcca",
  "account_id": "account-id",
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
    Map.of("page", "1", "per_page", "20", "sort", "updated_at"));
System.out.println("Total: " + page.getMeta().getTotal());   // from X-Pagination-Total-Count header
```

Supported filters: `status`, `method`, `search`, comma-separated tag IDs in `tags` (all IDs must match),
`sort=name|updated_at`, plus `page`/`per-page`.

### Search — `GET /accounts/{account_id}/documents/search`

```java
// Lightweight search — same DocumentListItem shape but without the expanded assignment/pages sub-objects.
PaginatedResult<DocumentListItem> hits = client.documents.search(Map.of("search", "invoice", "status", "pending_signature"));
```

### Rename — `PATCH /documents/{document_id}`

```java
DocumentDetails renamed = client.documents.rename(doc.getId(), "Signed contract.pdf");
// Request: { "name": "Signed contract.pdf" }   (required, max 255 chars; allowed before an assignment exists)
// Response data: the full DocumentDetails with the updated name.
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
// After assignment completion/status=certificated: byte[] signed = client.documents.download(doc.getId());
byte[] original = client.documents.download(doc.getId(), "original");
byte[] thumb    = client.documents.thumbnail(doc.getId());            // GET /documents/{id}/thumbnail (JPEG)
byte[] pageImg  = client.documents.downloadPage(doc.getId(), pageId); // GET /documents/{id}/pages/{pid}/download
List<DocumentActivity> log = client.documents.activities(doc.getId());// GET /documents/{id}/activities
```

> `download(id)` requests the `"certificated"` (final signed) artifact, which is only available after the
> document is certificated. For an unsigned document it raises an `ApiException` (HTTP 404,
> "Artefato não está disponível."). Use `download(id, "original")` for the uploaded PDF.

### Verify — `GET /documents/{signature_hash}/verify` (public)

```java
DocumentVerification verification = client.documents.verify(signatureHash);
if (verification.getIsValid()) { /* signed & valid */ }
```

Response `data` (an invalid/unknown hash returns `is_valid: false` with a `message`; `page_count`/`signer_count`
are strings):

```json
{
  "hash": "FE32EDDA...", "id": "63ddb172402799bfc991d10d", "status": "certificated",
  "page_count": "1", "signer_count": "1", "completed_count": 1,
  "completed_at": "2026-01-27T19:27:44Z", "verified_at": "2026-01-27T19:27:46Z",
  "is_valid": true, "message": ""
}
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

`channel` is `email` or `whatsapp`, and `recipient` is the matching address or phone number. The document must
be in `pending_signature`.

### Document tags

```java
List<Tag> tags = client.documents.listTags(doc.getId());                       // GET .../{id}/tags
client.documents.appendTags(doc.getId(), List.of(urgentTagId));                // POST .../{id}/tags
client.documents.replaceTags(doc.getId(), List.of(contractTagId, quarterTagId));// PUT  .../{id}/tags
boolean detached = client.documents.detachTag(doc.getId(), tagId);             // {"detached":true}
// append/replace request body: { "tags": ["<tag-id>"] }; response data: [ { "id": "...", "name": "Urgent" } ]
```

### Signing progress (client-side helpers)

```java
boolean done = client.documents.isFullySigned(doc.getId());
SigningProgress progress = client.documents.getSigningProgress(doc.getId());
System.out.printf("Signed: %d/%d (%.1f%%)%n",
    progress.getSigned(), progress.getTotal(), progress.getPercentage());
// Delete only after documents.statuses() marks the current status deletable:
// client.documents.delete(doc.getId());
```

## Signers

```java
// Strict create — POST /accounts/{account_id}/signers
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

`create` always sends the POST and reports duplicate-email validation. Use `findOrCreate` when the application
deliberately wants exact-email reuse; it returns the existing record without updating changed fields.

```java
Signer reusable = client.signers.findOrCreate(
    new CreateSignerPayload("John Doe", "john@example.com"));
Signer existing = client.signers.findByEmail("john@example.com");            // GET .../signers?search=...
Signer fetched  = client.signers.get(signer.getId());                        // GET .../signers/{id}
PaginatedResult<Signer> list = client.signers.list(Map.of("search", "john"));// GET .../signers
client.signers.update(signer.getId(), new UpdateSignerPayload()               // PUT .../signers/{id}
    .setFullName("Johnny Doe")
    .setGovernmentId("39053344705")); // CPF/CNPJ; normalized to digits by the API
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
    { "id": "1030...", "full_name": "Example Signer", "email": "signer@example.com",
      "completed": false, "verification_method": "Email", "notification_methods": ["Email"],
      "step": 1, "notified": true, "notification_history": [] }
  ],
  "items": [ { "id": "1031...", "page": null, "signer": { "id": "1030..." },
              "field": { "id": "102d...", "name": "Virtual", "type": "virtual" }, "completed": false } ],
  "summary": { "signer_count": 1, "completed_count": 0, "signers": [ { "id": "1030...", "completed": false } ] },
  "signing_urls": [ { "signer_id": "1030...", "url": "https://app.../sign/...?email=signer%40example.com" } ]
}
```

### List — `GET /assignments`

```java
// Not document-scoped: the account context travels as the camelCase `accountId` query parameter,
// which the SDK supplies from the client default or the explicit override.
PaginatedResult<Assignment> all = client.assignments.list(Map.of("per-page", "20"));
PaginatedResult<Assignment> other = client.assignments.list(Map.of("per-page", "20"), "other-account-id");
```

### Estimate cost — `POST /documents/{documentId}/assignments/estimate-cost`

```java
CostEstimate cost = client.assignments.estimateCost(doc.getId(),
    new CreateAssignmentPayload()
        .setSigners(List.of(new SignerRef().setId(signerId).setVerificationMethod("Email"))));
if (!cost.getHasSufficientResources()) {
    System.out.println("Blocked: " + cost.getBlockingReason());   // PendingPayment | InsufficientDocuments | InsufficientCredits
}
```

For a qualified ICP-Brasil signature, use `DigitalCertificate` after storing the signer's CPF/CNPJ in
`government_id`. That signer must be alone in its signing step and the account must have the feature enabled:

```java
Signer certificateSigner = client.signers.update(signerId,
    new UpdateSignerPayload().setGovernmentId("39053344705"));
CostEstimate certificateCost = client.assignments.estimateCost(doc.getId(),
    new CreateAssignmentPayload().setMethod("virtual").setSigners(List.of(
        new SignerRef().setId(certificateSigner.getId())
            .setVerificationMethod("DigitalCertificate")
            .setNotificationMethods(List.of("Email"))
            .setStep(1))));
```

The digital-certificate signature costs two credits per signer in addition to any notification cost. Valid
artifact names are `original`, `certificated`, `certificate-page`, `pades`, and `bundle`.
After creating the assignment and completing certificate signing, download the qualified artifact with
`client.documents.download(doc.getId(), "pades")`; an estimate alone does not create that artifact.

Response `data` (typed as `CostEstimate`):

```json
{
  "documents": 1, "credits": 0, "needs_extra_document": false, "extra_document_cost": 0,
  "total_credits": 0, "breakdown": [],
  "document_balance": 67, "credit_balance": 0,
  "has_sufficient_resources": true, "blocking_reason": null, "message": null
}
```

### Expiration, resend, notifications

```java
// PUT /documents/{doc}/assignments/{asg}/reset-expiration  — { "expires_at": "..." }
client.assignments.resetExpiration(doc.getId(), assignment.getId(), "2027-06-30T00:00:00Z");
// SDK extension: pass null (or use clearExpiration) to send { "expires_at": null }.
client.assignments.clearExpiration(doc.getId(), assignment.getId());

// PUT .../assignments/{asg}/signers/{signerId}/resend  -> ResendResult { is_sent, document_id, signer_id }
ResendResult resent = client.assignments.resendNotification(doc.getId(), assignment.getId(), signer1.getId());

// POST .../assignments/{asg}/signers/{signerId}/estimate-resend-cost
ResendCostEstimate resendCost = client.assignments.estimateResendCost(doc.getId(), assignment.getId(), signer1.getId());
// Documented response: CostEstimate fields such as total_credits and has_sufficient_resources.
// Also accepted: { "total": 0, "breakdown": [ { "code": "NotificationEmailResend",
//                    "name": "Email Notification Resend", "cost": 0 } ],
//                  "credit_balance": 0, "has_sufficient_credits": true }
// getTotal() and getHasSufficientCredits() read either accepted form.

// GET .../assignments/{asg}/whatsapp-notifications
List<WhatsappNotification> notifications = client.assignments.whatsappNotifications(doc.getId(), assignment.getId());
```

### Signer-facing (authorised via `signer-access-code` query param)

```java
// POST .../assignments/{asg}  — submit collect-method field values (entries is a JSON array)
client.assignments.signEntries(documentId, assignmentId, signerAccessCode, List.of(
    new AssignmentSignEntry("item-1", "field-1", "page-1", "John Doe")));

// Mutually exclusive alternative (do not run after signEntries): PUT .../assignments/{asg}/reject
// client.assignments.decline(documentId, assignmentId, signerAccessCode, "Not happy with clause 3");
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
  "events": ["document_ready", "signer_signed_document", "document_processing_failed"],
  "is_active": true,
  "url": "https://example.com/webhooks/assinafy",
  "email": "admin@example.com",
  "updated_at": "2026-06-05T20:46:43Z"
}
```

```java
WebhookSubscription current = client.webhooks.getSubscription();    // inactive object if none is configured
client.webhooks.update(new RegisterWebhookPayload(sub.getUrl(), sub.getEmail())
    .setEvents(sub.getEvents()).setActive(sub.isActive()));          // PUT is create-or-replace
client.webhooks.inactivate();                                       // PUT .../webhooks/inactivate (stop deliveries)
List<WebhookEventTypeInfo> types = client.webhooks.listEventTypes();// GET /webhooks/event-types
PaginatedResult<WebhookDispatch> dispatches = client.webhooks.listDispatches(   // GET /accounts/{id}/webhooks
    new ListDispatchesParams().setDelivered(false).setPerPage(20));
client.webhooks.retryDispatch(dispatchId);                          // POST .../webhooks/{dispatchId}/retry
```

The receiver gets a JSON `POST` with `{id, event, message, payload, origin, created_at, subject, object,
account_id}`. See [Webhook deliveries](API_REFERENCE.md#webhook-deliveries) for all 18 event types and their
event-specific payload keys.

## Tags

```java
PaginatedResult<Tag> tags = client.tags.list(Map.of("search", "contract")); // GET /accounts/{id}/tags
Tag created = client.tags.create(new CreateTagPayload("Contracts").setColor("ff8800")); // POST .../tags
// Request: { "name": "Contracts", "color": "ff8800" }   (name max 64 chars)
Tag updated = client.tags.update(created.getId(),                           // PUT .../tags/{tagId}
    new UpdateTagPayload().setName("Sales Contracts").clearColor());        // clearColor() sends "color": null
boolean deleted = client.tags.delete(updated.getId(), true);                 // {"deleted":true}
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
client.fields.delete(field.getId()); // DELETE only after validation is complete
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
        .setName("NDA - John Doe")
        .setTemplateEditorFields(List.of(new TemplateEditorField(editorFieldId, "John Doe")))
        .setTags(List.of("Generated")));

// Estimate cost — POST /accounts/{id}/templates/{tid}/documents/estimate-cost  (typed CostEstimate)
CostEstimate cost = client.documents.estimateCostFromTemplate(
    templateId, List.of(new TemplateSigner(firstRole.getId(), signerId)));
```

The `createFromTemplate` request body:

```json
{
  "signers": [ { "role_id": "<role>", "id": "<signer>", "verification_method": "Email",
                 "notification_methods": ["Email"], "step": 1 } ],
  "name": "NDA - John Doe",
  "editor_fields": [ { "field_id": "<editorField>", "value": "John Doe" } ],
  "tags": ["Generated"]
}
```

## Signer Self-Service (authorised via `signer-access-code`)

```java
// GET /sign — the signer-facing document + assignment view
DocumentDetails signingView = client.signerSelf.getSign(signerAccessCode);
Signer current = signingView.getCurrentSigner();   // who the access code resolved to

Signer self = client.signerSelf.getSelf(signerAccessCode);            // GET /signers/self
boolean canReuse = Boolean.TRUE.equals(self.getSignatureReusable());  // is_signature_reusable
// The access code is sent as the ?signer-access-code query parameter for these two calls:
AcceptTermsResponse terms = client.signerSelf.acceptTerms(signerAccessCode); // may be null when data is absent
VerifyEmailResponse verified = client.signerSelf.verifyEmail(                // may be null when data is absent
    "123456", signerAccessCode);

// PUT /documents/{id}/signers/confirm-data — returns the updated Signer
Signer confirmed = client.signerSelf.confirmSignerData(documentId, signerAccessCode,
    new ConfirmSignerDataPayload()
        .setFullName("John Doe").setEmail("signer@example.com").setGovernmentId("15774136604"));
// Request: { "full_name": "John Doe", "email": "signer@example.com", "government_id": "15774136604" }

// POST /signature — documented PNG upload; the SDK also detects JPEG bytes.
client.signerSelf.uploadSignature(signerAccessCode, signatureBytes, "signature");
client.signerSelf.uploadSignature(signerAccessCode, signatureBytes, "signature", true); // ?reuse=true
byte[] saved = client.signerSelf.downloadSignature(signerAccessCode, "signature"); // GET /signature/{type}
```

### Signer documents

```java
// GET /signers/{id}/document — current document; data includes a top-level "current_signer" object
DocumentDetails currentDoc = client.signerSelf.getCurrentDocument(signerId, signerAccessCode);
Signer who = currentDoc.getCurrentSigner();

// GET /signers/{id}/documents (pagination via page/per-page)
PaginatedResult<DocumentDetails> mine = client.signerSelf.listDocuments(
    signerId, signerAccessCode, Map.of("page", "1", "per_page", "20"));

// GET /signers/{id}/documents/search — search the signer's documents by term
PaginatedResult<DocumentDetails> found = client.signerSelf.searchDocuments(signerId, signerAccessCode, "invoice");

// GET /signers/{id}/documents/{docId}/download/{artifact}
// This artifact route is public in the current API contract.
byte[] copy = client.signerSelf.downloadDocument(signerId, documentId, "pades");
// Optional overload sends signer-access-code with the artifact request.
byte[] authorizedCopy = client.signerSelf.downloadDocument(
    signerId, documentId, "original", signerAccessCode);

// PUT /signers/documents/sign-multiple    — { "document_ids": ["..."] }
client.signerSelf.signMultiple(signerAccessCode, List.of(doc1.getId(), doc2.getId()));
// PUT /signers/documents/decline-multiple — { "document_ids": ["..."], "decline_reason": "..." }
// Mutually exclusive alternative (do not run for doc1/doc2 after signMultiple):
// client.signerSelf.declineMultiple(signerAccessCode, List.of(doc1.getId()), "Reason");
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

This helper uploads the file, waits until it is processable, deliberately reuses signers by exact email when
present, and creates a `virtual` assignment. If a later step fails, it makes a best-effort deletion of the
uploaded document. Account-scoped signers remain reusable and are never deleted automatically, because another
workflow may already reference them. Cleanup errors are attached to the original exception as suppressed
exceptions.

## Error Handling

```java
import com.assinafy.sdk.exceptions.*;

try {
    DocumentDetails doc = client.documents.upload(new File("contract.pdf"));
} catch (ValidationException e) {
    // Local input/configuration or workflow-state failure (invalid file, missing ID, poll timeout, etc.).
    // Server validation failures come back as an ApiException with getStatusCode() == 400 and a message.
    System.err.println("Validation: " + e.getMessage());
} catch (ApiException e) {
    // API returned an error (HTTP non-2xx or an error envelope) — includes server-side 400 validation errors
    System.err.println("API " + e.getStatusCode() + ": " + e.getMessage());
    System.err.println("Body: " + e.getResponseBody());
    if (e.getStatusCode() == 429 && e.getRetryAfterSeconds() != null) {
        try {
            Thread.sleep(e.getRetryAfterSeconds() * 1000L); // honor Retry-After, or set options.maxRetries
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
} catch (NetworkException e) {
    System.err.println("Network error: " + e.getMessage());
} catch (AssinafyException e) {
    System.err.println("SDK error: " + e.getMessage());
}
```
