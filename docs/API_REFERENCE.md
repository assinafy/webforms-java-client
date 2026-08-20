# Assinafy Java SDK API Reference

This reference maps the Java SDK to the 89 operations published at the
[Assinafy API documentation](https://api.assinafy.com.br/v1/docs). Paths below omit the `/v1` prefix.

## Client surface

Construct with `new AssinafyClient(options)`, `AssinafyClient.create(apiKey, accountId[, configurator])`, or
`AssinafyClient.fromConfig(map)`. The client exposes `accounts`, `users`, `auth`, `documents`, `signers`,
`assignments`, `fields`, `templates`, `tags`, `webhooks`, and `signerSelf`. `getBaseUrl()` returns the normalized
base URL; `getHttpClient()` exposes the configured OkHttp client for advanced integration and diagnostics.

| `AssinafyClientOptions` property | Default | Meaning |
|---|---|---|
| `apiKey` | null | Preferred `X-Api-Key` credential |
| `token` | null | Bearer credential, used only when no API key is set |
| `accountId` | null | Default workspace for account-scoped methods |
| `baseUrl` | Production URL | HTTPS API root; loopback HTTP is accepted only for local tests |
| `timeoutMs` | 30,000 | Connect, read, and write timeout; must be positive |
| `maxRetries` | 0 | Number of additional attempts for safe reads on 429/503; negative values behave as zero |

Every option has a `getX()` accessor and fluent `setX(value)`. Request payload setters are also fluent. Response
model getters map the snake_case wire properties listed in the response catalog to ordinary Java camelCase
names; response setters exist for Jackson deserialization.

## Environments and authentication

| Environment | Base URL |
|---|---|
| Production | `https://api.assinafy.com.br/v1` |
| Sandbox | `https://sandbox.assinafy.com.br/v1` |

Set an API key with `AssinafyClientOptions.setApiKey`; it is sent as `X-Api-Key`. If no API key is set,
`setToken` is sent as `Authorization: Bearer <token>`. Account-scoped overloads use the explicit `accountId`
when supplied and otherwise use `AssinafyClientOptions.accountId`.

Signer-facing methods take a `signerAccessCode` and send it as the `signer-access-code` query parameter. The
document verification, public-document lookup/token, and signer artifact download routes are public in the
published contract. The four password/social-login entry points are also usable before authentication.

Path identifiers must be non-blank URL-unreserved values (`A-Z`, `a-z`, `0-9`, `.`, `_`, `~`, `-`). The SDK
rejects unsafe path input before a request is sent; query values are URL-encoded by OkHttp.

Never put an API key or access token in browser code, source control, logs, examples, or exception messages.

## JSON envelopes, binary responses, pagination, and retries

Successful JSON endpoints return:

```json
{
  "status": 200,
  "message": "",
  "data": {}
}
```

The SDK returns only `data`. It raises `ApiException` when the HTTP response is not successful or when the
logical envelope `status` is `400` or greater, including an error envelope carried by HTTP 200. `ApiException`
exposes the status, response body, and rate-limit retry hint. Resource, client, payload, and typed record
validation raises `ValidationException`. Transport failures raise `NetworkException`.

Use `ApiException.getStatusCode()`, `getResponseBody()`, and `getRetryAfterSeconds()` for diagnostics and
caller-managed backoff. `ValidationException.getErrors()` returns structured validation details when present.

These downloads are raw binary responses rather than JSON envelopes and return `byte[]`:

- account logo;
- document `original`, `certificated`, `certificate-page`, `pades`, or `bundle` artifact;
- document thumbnail and page image;
- saved signer `signature` or `initial` image;
- signer document artifact.

Paginated methods return `PaginatedResult<T>`. `getData()` is the response `data` array and `getMeta()` contains
`currentPage`, `perPage`, `total`, and `lastPage`, read from `X-Pagination-Current-Page`,
`X-Pagination-Per-Page`, `X-Pagination-Total-Count`, and `X-Pagination-Page-Count`. Query maps may use
`per-page`, `per_page`, or `perPage`; the SDK sends `per-page`.

`maxRetries` defaults to zero. When enabled, the client retries only safe `GET`, `HEAD`, and `OPTIONS` requests
that receive 429 or 503. It honors numeric `Retry-After` or `X-Rate-Limit-Reset`, caps the wait at 30 seconds,
and preserves thread interruption. It never automatically replays uploads, creates, updates, deletes,
notifications, or signatures.

## Request payload catalog

An asterisk marks a required field. Jackson omits null optional properties unless a method explicitly uses
null to clear a value.

| Name / Java input | JSON or multipart request |
|---|---|
| `AccountPayload` | `name*` on create (`string`), `name` on update; `notification_sender_type` (`User` or `Account`) |
| Account delete | `{ "force": boolean }`; `true` also cancels an active paid subscription |
| Account logo upload | `multipart/form-data` with binary image `file*` |
| Document upload | `multipart/form-data` with PDF `file*`, at most 25 MB and 2,000 pages |
| Login | `{ "email": string*, "password": string* }` |
| `SocialLoginPayload` | `{ "provider": "google"*, "token": string*, "has_accepted_terms": boolean* }` |
| Link social login | `{ "provider": "google"*, "token": string* }` |
| Password-reset request | `{ "email": string* }` |
| Password reset | `{ "email": string*, "token": string?, "new_password": string* }` |
| Password change | `{ "email": string*, "password": string*, "new_password": string* }` |
| API-key creation | `{ "password": string* }` |
| Document rename | `{ "name": string* }`, maximum 255 characters |
| Document tag replace/append | SDK/live request: `{ "tags": [tagName, ...] }`; names not yet present are created |
| Public token | SDK/live request: `{ "recipient": string*, "channel": "email"\|"whatsapp"* }`; see the compatibility note below |
| `CreateSignerPayload` | `{ "full_name": string*, "email": string?, "whatsapp_phone_number": E.164? }` |
| `UpdateSignerPayload` | Any non-empty subset of `full_name`, `email`, `whatsapp_phone_number`, `government_id` (CPF/CNPJ) |
| Assignment expiration | `{ "expires_at": ISO-8601\|null }`; null clears expiration |
| Assignment decline | `{ "decline_reason": string* }` |
| `AssignmentSignEntry[]` | `[{ "itemId": string*, "fieldId": string*, "pageId": string*, "value": string* }, ...]` |
| Multi-sign | `{ "document_ids": [string, ...]* }` |
| Multi-decline | `{ "document_ids": [string, ...]*, "decline_reason": string* }` |
| Signer verification | `{ "verification-code": string* }` |
| `ConfirmSignerDataPayload` | Any of `full_name`, `email`, `government_id` |
| `CreateFieldPayload` | Current contract: `{ "type": string*, "name": string*, "regex": string\|null?, "is_required": boolean? }` |
| `UpdateFieldPayload` | Current contract: any of `name`, `regex`, `is_active` |
| Single field validation | `{ "value": any* }`; `signer-access-code` is optional |
| `FieldValidationPayload[]` | `[{ "field_id": string*, "value": any* }, ...]` |
| `CreateTagPayload` | `{ "name": string*, "color": six-digit-hex\|null? }`; name is at most 64 characters |
| `UpdateTagPayload` | Any of `name`, `color`; `clearColor()` deliberately sends `"color": null` |
| Signature image upload | Published portable contract: raw PNG bytes; query `type=signature\|initial`, optional `reuse=true\|false`. JPEG detection is retained for older/deployed compatibility but is not advertised by the current OpenAPI. |

The public OpenAPI schema currently shows `{ "email": ... }` for send-token, while the deployed API requires
`recipient` plus `channel`. It also describes document tag values as IDs, while the deployed API and SDK use
tag names and create missing names. `DocumentResource` follows the deployed behavior in both cases.

`CreateFieldPayload.setActive` and `UpdateFieldPayload.setType` / `setRequired` are retained for compatibility
with older deployments but are not properties of the current published schemas. For portable integrations,
create the field first and change activity with `UpdateFieldPayload.setActive`.

### Assignment create and estimate

`CreateAssignmentPayload` produces this body. The SDK defaults an omitted `method` to `virtual`; at least one
signer is required for create, and `entries` is required for a `collect` assignment.

Use `setSigners(List<SignerRef>)` when setting verification, notification, or step. `setSignerStrings(...)` and
the legacy `setSignerIds(...)` convert IDs to plain signer references; the most recently used signer setter
wins.

```json
{
  "method": "virtual | collect",
  "signers": [
    {
      "id": "signer-id",
      "verification_method": "Email | Whatsapp | DigitalCertificate",
      "notification_methods": ["Email | Whatsapp"],
      "step": 1
    }
  ],
  "entries": [
    {
      "page_id": "page-id",
      "fields": [
        {
          "signer_id": "signer-id",
          "field_id": "field-id",
          "display_settings": {
            "left": 100,
            "top": 100,
            "width": 240,
            "height": 40,
            "fontFamily": "Arial",
            "fontSize": 12,
            "backgroundColor": "#ffffff"
          }
        }
      ]
    }
  ],
  "message": "Please sign",
  "expires_at": "2026-12-31T23:59:59Z",
  "copy_receivers": ["signer-id"]
}
```

The typed collect request is assembled with `CollectAssignmentEntry`, `CollectFieldPlacement`, and
`DisplaySettings`:

```java
CreateAssignmentPayload collect = new CreateAssignmentPayload()
    .setMethod("collect")
    .setSignerStrings(signerId)
    .setCollectEntries(List.of(new CollectAssignmentEntry(pageId, List.of(
        new CollectFieldPlacement(signerId, fieldId,
            new DisplaySettings(100, 100, 240, 40, 12))))));
```

The estimate endpoint accepts `method`, `signers`, and `entries` pricing inputs; signer IDs are not required
for an estimate. Invitation-only `message`, `expires_at`, and `copy_receivers` are not part of that contract.

Verification and notification are coupled. If neither is supplied both default to `Email`; supplying only one
lets the API infer the other. `Email` verification pairs with `Email`, `Whatsapp` with `Whatsapp`, and
`DigitalCertificate` with either notification channel. Only one notification channel is accepted per signer.
Sequential `step` values must be positive, contiguous from 1, and supplied for every signer when used.

`DigitalCertificate` requires the account feature, a CPF/CNPJ in the signer's `government_id`, and a step that
contains only that signer. It adds two credits per signer (`SignatureDigitalCertificate`) plus notification
cost. The signer completes the ICP-Brasil Web PKI flow outside this server-side SDK; download the qualified PDF
with artifact name `pades`.

### Assignment collect submission

`AssignmentResource.signEntries` sends an array, not an envelope object:

```json
[
  {
    "itemId": "assignment-item-id",
    "fieldId": "field-id",
    "pageId": "page-id",
    "value": "Captured value"
  }
]
```

Use `signEntries(..., List.of(new AssignmentSignEntry(itemId, fieldId, pageId, value)))`; the legacy
`sign(..., List<?>)` method remains available for raw maps.

### Document from template

`CreateDocumentFromTemplateOptions` and `TemplateSigner` produce:

```json
{
  "signers": [
    {
      "role_id": "template-role-id",
      "id": "existing-signer-id",
      "verification_method": "Email | Whatsapp | DigitalCertificate",
      "notification_methods": ["Email | Whatsapp"],
      "step": 1
    }
  ],
  "editor_fields": [
    { "field_id": "template-field-id", "value": "Value" }
  ],
  "name": "Generated document name",
  "message": "Please sign",
  "expires_at": "2026-12-31T23:59:59Z",
  "tags": ["Contract", "Generated"]
}
```

Creation requires `role_id` and `id` for each signer role. Estimation requires only `role_id`; it also accepts
verification and notification methods for pricing. Template default tags are always applied and `tags` are
merged on top. Use `new TemplateEditorField(fieldId, value)` with `setTemplateEditorFields`; the legacy
`setEditorFields(List<?>)` method remains available for raw maps.

### Webhook subscription

`RegisterWebhookPayload` is normalized to all four required properties:

```json
{
  "events": ["document_ready", "signer_signed_document"],
  "is_active": true,
  "url": "https://example.com/webhooks/assinafy",
  "email": "owner@example.com"
}
```

`is_active` defaults to true when it is not set in the Java payload. At least one event, a URL, and an email
are required.

### Notification preferences

`NotificationPreferences` is a partial update request and a complete response. Its nine case-sensitive boolean
keys are:

```json
{
  "DocumentCompleted": true,
  "SignerDeclined": true,
  "DocumentCancelled": true,
  "DocumentAboutToExpire": true,
  "DocumentExpired": true,
  "DocumentExpirationReset": true,
  "DocumentProcessingFailed": true,
  "TemplateProcessingFailed": true,
  "SignerWhatsappFailed": true
}
```

## Query parameters

| Operation group | Supported query parameters |
|---|---|
| Paginated lists | `page`, `per-page` (maximum 100 where enforced) |
| Documents list | `status`, `method` (`virtual\|collect`), `search`, comma-separated `tags`, `sort` (`name`, `updated_at`, prefix `-` for descending), pagination |
| Document search | `search`, `status`, pagination |
| Assignments list | SDK supplies `accountId`; pagination |
| Fields list | `include_inactive`, `include_standard` |
| Signers list | `search`, pagination |
| Templates list | `search`, pagination |
| Tags list | `search` |
| Signer documents | pagination; search route takes `search` |
| Account/user stats | `granularity=monthly\|daily`; `month=YYYY-MM` is required for daily |
| Tag delete | `force=true\|false` |
| Signing view | optional `has_accepted_terms=true\|false` |
| Signature upload | `type`, optional `reuse` |
| Webhook history | `event`, `delivered`, Unix timestamps `from`/`to`, pagination |

## Response payload catalog

Properties marked nullable or contextual may be null or absent. Date/time strings are ISO 8601 unless noted.

| Java model / wire schema | Complete `data` properties |
|---|---|
| `WorkspaceAccount` / Account | `resource`, `id`, `name`, `primary_color?`, `secondary_color?`, `notification_sender_type` (`User\|Account`), `roles[]`, `is_delete_allowed`, `created_at` |
| `AccountTheme` | `account_name`, `primary_color`, `secondary_color?`, `logo` |
| `User` | `id`, `name`, `email`, `telephone?`, `government_id?`, `is_email_verified`, `has_accepted_terms`, `created_at`, `to_be_deleted_at?` |
| `AuthenticationResult` | `access_token`, `user` (`User`), `accounts[]` (stored as `WorkspaceAccount`; authentication populates `id`, `name`, `roles`, `is_delete_allowed`, `created_at`) |
| `ApiKeyResponse` | `api_key` (full only on creation, masked on read, null when none exists) |
| `EmailResponse` | `email` |
| `NotificationPreferences` | The nine boolean keys listed above |
| `DocumentStatsRow` | `period` (`YYYY-MM` or `YYYY-MM-DD`), `documents_uploaded`, `documents_sent`, `signature_requests`, `signature_requests_email`, `signature_requests_whatsapp`, `signature_requests_viewed`, `signature_requests_completed`, `documents_certified` |
| `Signer` | `resource`, `id`, `full_name`, `email?`, `whatsapp_phone_number?`, `government_id?`, `has_accepted_terms`; contextual fields: `has_signature`, `has_initial`, `is_signature_reusable`, `verification_method`, `notification_methods[]`, `step`, `notified`, `completed`, `notification_history[]` |
| `AssignmentSignerNotification` | `event`, `status` (`sent\|failed`), `error_code?`, `error_message?`, `sent_at?`, `failed_at?` |
| `Assignment` | `resource`, `id`, `sender_email`, `method` (`virtual\|collect`), `expires_at?`, `message?`, `signers[]`, `copy_receivers[]` (`Signer` objects), `items[]`, `summary`, `signing_urls[]` |
| `AssignmentItem` | `id`, `page?`, `signer`, `field?`, `display_settings`, `value?`, `completed` |
| `AssignmentSummary` | `signer_count`, `completed_count`, `signers[]` |
| `SigningUrl` | `signer_id`, `url` |
| `CostEstimate` | `documents`, `credits`, `needs_extra_document`, `extra_document_cost`, `total_credits`, `breakdown[]`, `document_balance`, `credit_balance`, `has_sufficient_resources`, `blocking_reason?`, `message?` |
| Cost breakdown | `code`, `name`, `cost`, `quantity`, `unit_cost` |
| `ResendResult` | `is_sent`, `document_id`, `signer_id` |
| `ResendCostEstimate` | Deployed response: `total`, `breakdown[]` (`code`, `name`, `cost`), `credit_balance`, `has_sufficient_credits` |
| `WhatsappNotification` | `sent_at` (Unix seconds), `header`, `body`, `buttons[]` (`text`, compatibility `url?`), `phone_number`, `signer_id` |
| `DocumentDetails` / Document | `resource`, `id`, `account_id`, `template_id?`, `name`, `status`, `artifacts`, `is_closed`, `signing_url`, `decline_reason?`, `declined_by?`, `tags[]`, `assignment?`, `pages[]`, `created_at`, `updated_at`; public/signer contexts can add `page_count`, `created_by`, `current_signer`, `download_url`, `download_final_url`, `activities` |
| `DocumentListItem` | `resource`, `id`, `account_id`, `template_id?`, `name`, `status`, `artifacts`, `is_closed`, `signing_url`, `decline_reason?`, `declined_by?`, `tags[]`, `assignment?`, `pages[]`, `created_at`, `updated_at`; lightweight search may omit expanded relationships |
| Document artifacts | URL map keyed by `original`, `certificated`, `certificate-page`, `pades`, `bundle`, and contextual `thumbnail` |
| `DocumentPage` | `id`, `number`, `height`, `width`, `download_url` |
| `DocumentStatus` | `code`, `deletable` |
| `DocumentVerification` | `hash`, `id?`, `status?`, `page_count?` (string), `signer_count?` (string), `completed_count?`, `completed_at?`, `verified_at`, `is_valid`, `message` |
| `DocumentActivity` | `id`, `event`, `message`, `payload?`, `origin?`, `created_at` |
| `FieldDefinition` | `resource`, `id`, `name`, `type`, `regex?`, `is_pre_defined`, `is_active`, `is_required`, `is_standard`, `is_read_only`, `is_visible` |
| `FieldTypeInfo` | `type`, `name` |
| `FieldValidationResult` | Single: `type`, `success`, `error_message`; multiple also includes `field_id` |
| `Tag` | `resource`, `id`, `name`, `color?`, `created_at`, `updated_at` |
| `TemplateDetails` / Template | `resource`, `id`, `name`, `document_name?`, `message?`, `status`, `pages[]`, `roles[]`, `tags[]`, `default_document_tags[]`, `created_at`, `updated_at`; SDK also reads contextual `account_id` |
| `TemplateListItem` | `resource`, `id`, `account_id?`, `name`, `document_name?`, `message?`, `status`, `pages[]`, `roles[]`, `tags[]`, `created_at`, `updated_at` |
| `TemplatePage` | `id`, `number`, `height`, `width`, `download_url`, `fields[]` |
| `TemplateFieldPlacement` | `id`, `field_id`, `role_id`, `label`, `display_settings`, `created_at`, `updated_at` |
| `TemplateRole` | `id`, `name`, `assignment_type`, `created_at`, `updated_at` |
| `WebhookSubscription` | `events[]`, `is_active`, `url?`, `email?`, `updated_at?` |
| `WebhookDispatch` | `resource`, `id`, `event`, `activity_id`, `endpoint?`, `payload?`, `delivered`, `http_status?`, `response_body?`, `error?`, `created_at`, `updated_at` |
| `WebhookEventTypeInfo` | `id`, `description` |
| `AcceptTermsResponse` | Deployed response fields: `full_name`, `email`, `has_accepted_terms` |
| `VerifyEmailResponse` | Deployed response fields: `message`, `access_token` |
| `PaginationMeta` | `current_page`, `per_page`, `total`, `last_page` (built from response headers, not envelope `data`) |

The published resend-estimate response references `CostEstimate`; the deployed endpoint returns the compact
`ResendCostEstimate` shape above, which the SDK models directly.

## Published operation matrix

Every JSON response named below is the unwrapped `data` value. `void` means the SDK validates the success
envelope and intentionally ignores its data. Status lists contain the documented success code followed by
documented error codes; the platform may additionally return global transport statuses such as 403, 415, or
429.

### Accounts — 10 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **GET** `/accounts` | `accounts.list()` | — | `List<WorkspaceAccount>` | 200, 401, 500 |
| **POST** `/accounts` | `accounts.create(payload)` | `AccountPayload` | `WorkspaceAccount` | 200, 400, 401, 500 |
| **GET** `/accounts/{accountId}` | `accounts.get([accountId])` | — | `WorkspaceAccount` | 200, 401, 404, 500 |
| **PUT** `/accounts/{accountId}` | `accounts.update(payload[, accountId])` | `AccountPayload` | `WorkspaceAccount` | 200, 400, 401, 500 |
| **DELETE** `/accounts/{accountId}` | `accounts.delete(force[, accountId])` | Account delete | `void` | 200, 400, 401, 404, 500 |
| **GET** `/accounts/{accountId}/theme` | `accounts.getTheme([accountId])` | — | `AccountTheme` | 200, 401, 500 |
| **GET** `/accounts/{accountId}/logo` | `accounts.downloadLogo([accountId])` | — | Raw `byte[]` | 200, 401, 404, 500 |
| **POST** `/accounts/{accountId}/logo` | `accounts.uploadLogo(bytes, fileName[, accountId])` | Multipart `file` | `void` | 200, 400, 401, 500 |
| **DELETE** `/accounts/{accountId}/logo` | `accounts.deleteLogo([accountId])` | — | `void` | 200, 401, 500 |
| **GET** `/accounts/{accountId}/stats` | `accounts.stats([params][, accountId])` | Stats query | `List<DocumentStatsRow>` | 200, 400, 401, 500 |

### Assignments — 7 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **GET** `/assignments` | `assignments.list([params][, accountId])` | Pagination; SDK adds `accountId` | `PaginatedResult<Assignment>` | 200, 401, 500 |
| **POST** `/documents/{documentId}/assignments` | `assignments.create(documentId, payload)` | Assignment create | `Assignment` | 200, 400, 401, 500 |
| **POST** `/documents/{documentId}/assignments/estimate-cost` | `assignments.estimateCost(documentId, payload)` | Assignment estimate | `CostEstimate` | 200, 400, 401, 500 |
| **PUT** `/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/resend` | `assignments.resendNotification(documentId, assignmentId, signerId)` | — | `ResendResult` | 200, 401, 500 |
| **POST** `/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/estimate-resend-cost` | `assignments.estimateResendCost(documentId, assignmentId, signerId)` | — | `ResendCostEstimate` | 200, 401, 500 |
| **PUT** `/documents/{documentId}/assignments/{assignmentId}/reset-expiration` | `assignments.resetExpiration(...)` / `clearExpiration(...)` | Assignment expiration | `Assignment` | 200, 400, 401, 404, 500 |
| **GET** `/documents/{documentId}/assignments/{assignmentId}/whatsapp-notifications` | `assignments.whatsappNotifications(documentId, assignmentId)` | — | `List<WhatsappNotification>` | 200, 401, 500 |

### Authentication — 9 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **POST** `/login` | `auth.login(email, password)` | Login | `AuthenticationResult` | 200, 400, 500 |
| **POST** `/authentication/social-login` | `auth.socialLogin(payload)` | `SocialLoginPayload` | `AuthenticationResult` | 200, 400, 500 |
| **POST** `/auth/link-social-login` | `auth.linkSocialLogin(provider, token)` | Link social login | `void` | 200, 400, 401, 500 |
| **PUT** `/authentication/request-password-reset` | `auth.requestPasswordReset(email)` | Password-reset request | `EmailResponse` | 200, 500 |
| **PUT** `/authentication/reset-password` | `auth.resetPassword(email, token, newPassword)` | Password reset | `EmailResponse` | 200, 400, 500 |
| **PUT** `/authentication/change-password` | `auth.changePassword(email, password, newPassword)` | Password change | `EmailResponse` | 200, 400, 401, 500 |
| **GET** `/users/api-keys` | `auth.getApiKey()` | — | `ApiKeyResponse` (`api_key` may be null) | 200, 401, 500 |
| **POST** `/users/api-keys` | `auth.createApiKey(password)` | API-key creation | `ApiKeyResponse` | 200, 401, 500 |
| **DELETE** `/users/api-keys` | `auth.deleteApiKey()` | — | `void` | 200, 401, 500 |

### Documents — 18 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **GET** `/accounts/{accountId}/documents` | `documents.list([params][, accountId])` | Document-list query | `PaginatedResult<DocumentListItem>` | 200, 401, 500 |
| **POST** `/accounts/{accountId}/documents` | `documents.upload(file or bytes, ...[, accountId])` | Multipart PDF `file` | `DocumentDetails` | 200, 400, 401, 500 |
| **GET** `/accounts/{accountId}/documents/search` | `documents.search(params[, accountId])` | Document-search query | `PaginatedResult<DocumentListItem>` | 200, 401, 500 |
| **GET** `/documents/statuses` | `documents.statuses()` | — | `List<DocumentStatus>` | 200, 401, 500 |
| **GET** `/documents/{documentId}` | `documents.details(documentId)` / `get(documentId)` | — | `DocumentDetails` | 200, 401, 404, 500 |
| **DELETE** `/documents/{documentId}` | `documents.delete(documentId)` | — | `void` | 200, 401, 404, 500 |
| **PATCH** `/documents/{documentId}` | `documents.rename(documentId, name)` | Document rename | `DocumentDetails` | 200, 400, 401, 404, 500 |
| **GET** `/documents/{documentId}/download/{artifactName}` | `documents.download(documentId[, artifactName])` | Artifact path value | Raw `byte[]` | 200, 401, 404, 500 |
| **GET** `/documents/{documentSignatureHash}/verify` | `documents.verify(hash)` | — | `DocumentVerification` | 200, 500 |
| **GET** `/documents/{documentId}/activities` | `documents.activities(documentId)` | — | `List<DocumentActivity>` | 200, 401, 500 |
| **GET** `/documents/{documentId}/thumbnail` | `documents.thumbnail(documentId)` | — | Raw `byte[]` | 200, 401, 404, 500 |
| **GET** `/documents/{documentId}/pages/{pageId}/download` | `documents.downloadPage(documentId, pageId)` | — | Raw `byte[]` | 200, 401, 404, 500 |
| **GET** `/accounts/{accountId}/documents/{documentId}/tags` | `documents.listTags(documentId[, accountId])` | — | `List<Tag>` | 200, 401, 500 |
| **PUT** `/accounts/{accountId}/documents/{documentId}/tags` | `documents.replaceTags(documentId, tags[, accountId])` | Document tags | `List<Tag>` | 200, 401, 500 |
| **POST** `/accounts/{accountId}/documents/{documentId}/tags` | `documents.appendTags(documentId, tags[, accountId])` | Document tags | `List<Tag>` | 200, 401, 500 |
| **DELETE** `/accounts/{accountId}/documents/{documentId}/tags/{tagId}` | `documents.detachTag(documentId, tagId[, accountId])` | — | `boolean` (`detached`) | 200, 401, 500 |
| **POST** `/accounts/{accountId}/templates/{templateId}/documents` | `documents.createFromTemplate(templateId, signers[, options][, accountId])` | Document from template | `DocumentDetails` | 200, 400, 401, 500 |
| **POST** `/accounts/{accountId}/templates/{templateId}/documents/estimate-cost` | `documents.estimateCostFromTemplate(templateId, signers[, accountId])` | Template estimate | `CostEstimate` | 200, 401, 500 |

### Fields — 8 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **GET** `/accounts/{accountId}/fields` | `fields.list([params][, accountId])` | Field-list query | `PaginatedResult<FieldDefinition>` | 200, 401, 500 |
| **POST** `/accounts/{accountId}/fields` | `fields.create(payload[, accountId])` | `CreateFieldPayload` | `FieldDefinition` | 200, 400, 401, 500 |
| **GET** `/accounts/{accountId}/fields/{fieldId}` | `fields.get(fieldId[, accountId])` | — | `FieldDefinition` | 200, 401, 404, 500 |
| **PUT** `/accounts/{accountId}/fields/{fieldId}` | `fields.update(fieldId, payload[, accountId])` | `UpdateFieldPayload` | `FieldDefinition` | 200, 401, 404, 500 |
| **DELETE** `/accounts/{accountId}/fields/{fieldId}` | `fields.delete(fieldId[, accountId])` | — | `void` | 200, 401, 404, 500 |
| **POST** `/accounts/{accountId}/fields/{fieldId}/validate` | `fields.validate(fieldId, value[, signerAccessCode][, accountId])` | Single validation | `FieldValidationResult` | 200, 401, 500 |
| **POST** `/accounts/{accountId}/fields/validate-multiple` | `fields.validateMultiple(values[, signerAccessCode][, accountId])` | `FieldValidationPayload[]` | `List<FieldValidationResult>` | 200, 401, 500 |
| **GET** `/field-types` | `fields.listTypes()` | — | `List<FieldTypeInfo>` | 200, 401, 500 |

### Signers — 5 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **GET** `/accounts/{accountId}/signers` | `signers.list([params][, accountId])` | `search`, pagination | `PaginatedResult<Signer>` | 200, 401, 500 |
| **POST** `/accounts/{accountId}/signers` | `signers.create(payload[, accountId])` | `CreateSignerPayload` | `Signer` | 200, 400, 401, 500 |
| **GET** `/accounts/{accountId}/signers/{signerId}` | `signers.get(signerId[, accountId])` | — | `Signer` | 200, 401, 404, 500 |
| **PUT** `/accounts/{accountId}/signers/{signerId}` | `signers.update(signerId, payload[, accountId])` | `UpdateSignerPayload` | `Signer` | 200, 400, 401, 404, 500 |
| **DELETE** `/accounts/{accountId}/signers/{signerId}` | `signers.delete(signerId[, accountId])` | — | `void` | 200, 401, 404, 500 |

`signers.create` is idempotent by exact email: it searches first and returns an existing signer without applying
new values. Use `update` when fields must change. It also recovers a duplicate-email race by re-querying.

### Signing — 17 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **GET** `/public/documents/{documentId}` | `documents.getPublic(documentId)` | — | Public `DocumentDetails` | 200, 404, 500 |
| **PUT** `/public/documents/{documentId}/send-token` | `documents.sendToken(documentId, recipient, channel)` | Public token | `void` | 200, 500 |
| **GET** `/signers/self` | `signerSelf.getSelf(signerAccessCode)` | Signer access code | `Signer` | 200, 401, 500 |
| **GET** `/signers/{signerId}/document` | `signerSelf.getCurrentDocument(signerId, signerAccessCode)` | Signer access code | `DocumentDetails` | 200, 401, 404, 500 |
| **GET** `/sign` | `signerSelf.getSign(signerAccessCode[, hasAcceptedTerms])` | Signer access code; optional flag | `DocumentDetails` | 200, 400, 401, 409, 500 |
| **POST** `/documents/{documentId}/assignments/{assignmentId}` | `assignments.signEntries(documentId, assignmentId, signerAccessCode, entries)` / `sign(...)` | Collect submission | `void` | 200, 400, 401, 409, 500 |
| **PUT** `/documents/{documentId}/assignments/{assignmentId}/reject` | `assignments.decline(documentId, assignmentId, signerAccessCode, reason)` | Assignment decline | `void` | 200, 401, 500 |
| **PUT** `/signers/documents/sign-multiple` | `signerSelf.signMultiple(signerAccessCode, documentIds)` | Multi-sign | `void` | 200, 401, 500 |
| **PUT** `/signers/documents/decline-multiple` | `signerSelf.declineMultiple(signerAccessCode, documentIds, reason)` | Multi-decline | `void` | 200, 401, 500 |
| **POST** `/verify` | `signerSelf.verifyEmail(code, signerAccessCode)` | Signer verification | `VerifyEmailResponse` | 200, 400, 401, 500 |
| **PUT** `/documents/{documentId}/signers/confirm-data` | `signerSelf.confirmSignerData(documentId, signerAccessCode, payload)` | `ConfirmSignerDataPayload` | `Signer` | 200, 401, 500 |
| **PUT** `/signers/accept-terms` | `signerSelf.acceptTerms(signerAccessCode)` | Signer access code | `AcceptTermsResponse` | 200, 401, 500 |
| **POST** `/signature` | `signerSelf.uploadSignature(signerAccessCode, bytes, type[, reuse])` | Raw PNG + query; JPEG compatibility path | `void` | 200, 401, 500 |
| **GET** `/signature/{signatureType}` | `signerSelf.downloadSignature(signerAccessCode, type)` | Signer access code | Raw `byte[]` | 200, 401, 404, 500 |
| **GET** `/signers/{signerId}/documents` | `signerSelf.listDocuments(signerId, signerAccessCode[, params])` | Signer access code, pagination | `PaginatedResult<DocumentDetails>` | 200, 401, 500 |
| **GET** `/signers/{signerId}/documents/search` | `signerSelf.searchDocuments(signerId, signerAccessCode, search)` | Signer access code, search | `PaginatedResult<DocumentDetails>` | 200, 401, 500 |
| **GET** `/signers/{signerId}/documents/{documentId}/download/{artifactName}` | `signerSelf.downloadDocument(signerId, documentId, artifactName)` | Public; compatibility overload accepts access code | Raw `byte[]` | 200, 404, 500 |

### Tags — 4 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **GET** `/accounts/{accountId}/tags` | `tags.list([params][, accountId])` | `search` | `PaginatedResult<Tag>` | 200, 401, 500 |
| **POST** `/accounts/{accountId}/tags` | `tags.create(payload[, accountId])` | `CreateTagPayload` | `Tag` | 200, 400, 401, 409, 500 |
| **PUT** `/accounts/{accountId}/tags/{tagId}` | `tags.update(tagId, payload[, accountId])` | `UpdateTagPayload` | `Tag` | 200, 400, 401, 404, 500 |
| **DELETE** `/accounts/{accountId}/tags/{tagId}` | `tags.delete(tagId[, force][, accountId])` | `force` query | `boolean` (`deleted`) | 200, 401, 404, 500 |

### Templates — 1 operation

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **GET** `/accounts/{accountId}/templates` | `templates.list([params][, accountId])` | `search`, pagination | `PaginatedResult<TemplateListItem>` | 200, 401, 500 |

Document creation and estimate operations for templates appear in the Documents table because that is how the
published contract categorizes them.

### Users — 4 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **GET** `/users/self` | `users.getSelf()` | — | `User` | 200, 401, 500 |
| **GET** `/users/self/notification-preferences` | `users.getNotificationPreferences()` | — | `NotificationPreferences` | 200, 401, 500 |
| **PUT** `/users/self/notification-preferences` | `users.updateNotificationPreferences(preferences)` | Partial `NotificationPreferences` | Complete `NotificationPreferences` | 200, 400, 401, 500 |
| **GET** `/users/self/stats` | `users.stats([params])` | Stats query | `List<DocumentStatsRow>` | 200, 400, 401, 500 |

### Webhooks — 6 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **GET** `/accounts/{accountId}/webhooks/subscriptions` | `webhooks.getSubscription([accountId])` | — | `WebhookSubscription` | 200, 401, 500 |
| **PUT** `/accounts/{accountId}/webhooks/subscriptions` | `webhooks.register(payload[, accountId])` / `update(...)` | Webhook subscription | `WebhookSubscription` | 200, 400, 401, 500 |
| **PUT** `/accounts/{accountId}/webhooks/inactivate` | `webhooks.inactivate([accountId])` | — | `WebhookSubscription` | 200, 401, 500 |
| **GET** `/webhooks/event-types` | `webhooks.listEventTypes()` | — | `List<WebhookEventTypeInfo>` | 200, 401, 500 |
| **GET** `/accounts/{accountId}/webhooks` | `webhooks.listDispatches([params][, accountId])` | Webhook-history query | `PaginatedResult<WebhookDispatch>` | 200, 401, 500 |
| **POST** `/accounts/{accountId}/webhooks/{historyId}/retry` | `webhooks.retryDispatch(historyId[, accountId])` | — | `WebhookDispatch` | 200, 400, 401, 404, 500 |

## SDK convenience and compatibility methods

These public methods compose or alias the operations above rather than adding published operations:

| Method | Behavior and result |
|---|---|
| `AssinafyClient.create(...)` | Builds an API-key client; optional configurator can set timeout, retries, or base URL |
| `AssinafyClient.fromConfig(map)` | Accepts snake/camel variants for API key, token, account ID, and base URL; API key wins over token |
| `uploadAndRequestSignatures(options)` | Uploads a PDF, optionally polls until ready, finds/creates signers by email, creates a virtual assignment; returns `UploadAndRequestSignaturesResult` |
| `documents.waitUntilReady(id[, maxWaitMs, pollIntervalMs])` | Polls document details until `metadata_ready`, `pending_signature`, or `certificated`; throws on processing failure or timeout |
| `documents.isFullySigned(id)` | Returns true for `certificated`, a positive completed summary, or a non-empty signer list whose `completed` flags are all true |
| `documents.getSigningProgress(id)` | Derives `signed`, `total`, `pending`, and `percentage` from the assignment summary, falling back to signer `completed` flags |
| `documents.download(id)` | Alias for the `certificated` artifact |
| `assignments.clearExpiration(...)` | Alias that sends `expires_at: null` |
| `assignments.signEntries(...)` | Type-safe collect submission using `AssignmentSignEntry`; delegates to `sign(...)` |
| `signers.findByEmail(email[, accountId])` | Searches up to 100 candidates and returns an exact case-insensitive email match or null |
| `webhooks.update(...)` | Alias for create-or-replace `register(...)` |
| `templates.get(templateId[, accountId])` | Compatibility route `GET /accounts/{accountId}/templates/{templateId}`; deployed and supported by the SDK but absent from the current published OpenAPI path list |

Live compatibility notes:

- `users.getSelf()` accepts both the published production shape (`AuthUser`) and the sandbox's current legacy envelope (`{ "user": ..., "accounts": [...] }`).
- The sandbox router currently returns `404` for `GET /accounts/{accountId}/stats`, `GET /users/self/stats`, and `GET /users/self/notification-preferences`, while unauthenticated probes against production return `401`, which indicates the published routes exist there.
- Sandbox account creation currently rejects the published optional `notification_sender_type` field, so live smoke coverage omits that optional input and unit coverage verifies serialization and validation for it separately.
- Sandbox signer updates currently return 200 for `government_id` but omit and do not persist that field. The
  production OpenAPI explicitly includes `government_id`; live smoke names this sandbox-version skip and unit
  coverage verifies the exact request/response mapping.
- The sandbox's collect-cost estimator still requires a top-level signer, while the production OpenAPI requires
  only `entries` for collect estimates. The SDK follows the published contract; live smoke covers both the
  sandbox-compatible signed estimate and the named signer-free sandbox-version skip.

All account-scoped resource methods expose overloads that either accept an explicit account ID or use the
client default. List methods expose no-argument overloads and parameter-map overloads where applicable.

`UploadAndRequestSignaturesOptions` is constructed with either `(File, signers)` or
`(byte[], fileName, signers)`. Optional setters are `waitForReady` (null/true waits), `message`, `expiresAt`,
`copyReceivers`, and `accountId`. Each `UploadAndRequestSignaturesSigner` contains required `name` and `email`
plus optional `whatsappPhoneNumber`. The result exposes `document`, `assignment`, and `signerIds`. This helper
always creates a virtual assignment with the API's default Email verification/notification behavior; use the
individual resource methods for collect or digital-certificate assignments.

## Webhook deliveries

Assinafy sends an HTTP `POST` with `Content-Type: application/json` to the subscription URL. Any 2xx response
is successful. Delivery is attempted at most twice (initial attempt plus one retry) with a three-second wait.
After ten consecutive failed events the circuit breaker pauses normal delivery and probes about five percent
of events until a delivery succeeds. The history stores the first 2,000 characters of the receiver response.

Every body has this shape:

```json
{
  "id": 12345,
  "event": "signer_signed_document",
  "message": "Signer completed the document",
  "payload": { "signer_full_name": "Example Signer" },
  "origin": { "ip": "203.0.113.10", "user-agent": "Example Client" },
  "created_at": 1787241600,
  "subject": { "type": "Signer", "id": "signer-id" },
  "object": { "type": "Document", "id": "document-id" },
  "account_id": "account-id"
}
```

`message`, `payload`, and `origin` may be null. `created_at` is Unix seconds. `subject` and `object` are
polymorphic and include `type` (`User`, `Signer`, `Account`, `Document`, or `Template`) followed by that
resource's fields. Document objects include expanded assignment/pages. Account objects omit integration
dispatch history.

The current event catalog contains 18 values:

| Event | Subject | Object | Event-specific `payload` keys |
|---|---|---|---|
| `document_uploaded` | User | Document | — |
| `document_metadata_ready` | User | Document | — |
| `document_prepared` | User | Document | — |
| `assignment_created` | User | Document | `user_name`, `user_email`, `user_telephone` |
| `document_ready` | Account | Document | — |
| `document_processing_failed` | Account | Document | `error_message` |
| `signature_requested` | User | Document | `signer_email`, `signer_full_name`, or `signer_whatsapp_phone_number`, depending on channel |
| `signer_created` | User | Signer | `signer_full_name` |
| `signer_email_verified` | Signer | Document | `signer_email` |
| `signer_whatsapp_verified` | Signer | Document | `signer_whatsapp_phone_number` |
| `signer_data_confirmed` | Signer | Document | `signer_email` |
| `signer_viewed_document` | Signer | Document | `signer_full_name` |
| `signer_signed_document` | Signer | Document | `signer_full_name` |
| `signer_rejected_document` | Signer | Document | `signer_full_name` |
| `user_rejected_document` | User | Document | `user_name` |
| `template_created` | User | Template | — |
| `template_processed` | User | Template | — |
| `template_processing_failed` | Account | Template | `error_message` |

`assignment_created` and `document_metadata_ready` have no guaranteed order. Consumers should deduplicate on
the event `id`, tolerate retries, and ignore unknown fields and future event values.
