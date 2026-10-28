# Assinafy Java SDK API Reference

This reference maps the Java SDK to the 118 operations documented at the
[Assinafy API documentation](https://api.assinafy.com.br/v1/docs). Paths below omit the `/v1` prefix, except
for the OAuth protected-resource metadata, which the API serves at its host root.

## Client surface

Construct with `new AssinafyClient(options)`, `AssinafyClient.create(apiKey, accountId[, configurator])`, or
`AssinafyClient.fromConfig(map)`. The client exposes `accounts`, `users`, `auth`, `oauth`, `documents`,
`contracts`, `signers`, `assignments`, `fields`, `templates`, `tags`, `webhooks`, and `signerSelf`.
`getBaseUrl()` returns
the normalized base URL; `getHttpClient()` exposes the configured OkHttp client for advanced integration and
diagnostics.

| `AssinafyClientOptions` property | Default | Meaning |
|---|---|---|
| `apiKey` | null | Preferred `X-Api-Key` credential |
| `token` | null | Bearer credential, used only when no API key is set |
| `accountId` | null | Default workspace for account-scoped methods |
| `baseUrl` | Production URL | HTTPS API root; loopback HTTP is accepted only for local tests |
| `timeoutMs` | 30,000 | Connect, read, and write timeout; must be positive |
| `maxRetries` | 0 | Number of additional attempts for safe reads on 429/503; must be non-negative |

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

Signer-facing methods take a `signerAccessCode` and send it as the `signer-access-code` query parameter. A
`null` or blank code is rejected before any request, and a request carrying the code never also carries the
client's API key or bearer token. Routes that accept either a signer code or workspace credentials have separate
`...AsWorkspace` methods for the workspace form: `signerSelf.listDocumentsAsWorkspace`,
`signerSelf.searchDocumentsAsWorkspace`, `fields.validateAsWorkspace`, and `fields.validateMultipleAsWorkspace`. The
document verification, public-document lookup/token, and signer artifact download routes are public. The four
password/social-login entry points are also usable before authentication, as are the three OAuth endpoints that
precede a token.

An OAuth access token is a bearer credential: set it with `setToken` and it is sent as
`Authorization: Bearer`. The API refuses an OAuth token supplied as `X-Api-Key` or in the query string. A token
is scoped to the one workspace the user chose; calling any other workspace answers 403.

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
exposes the status, response body, rate-limit headers, and retry hint. Resource, client, payload, and typed record
validation raises `ValidationException`. Transport failures raise `NetworkException`.

Use `ApiException.getStatusCode()`, `getResponseBody()`, and `getRetryAfterSeconds()` for diagnostics and
caller-managed backoff. `getRateLimitLimit()`, `getRateLimitRemaining()`, and `getRateLimitReset()` carry the
`X-Rate-Limit-Limit`, `X-Rate-Limit-Remaining`, and `X-Rate-Limit-Reset` headers of the failed response (`null`
when absent). Past the limit the API answers 429 with `Retry-After` in seconds, surfaced by
`getRetryAfterSeconds()`. `getOAuthError()` carries the RFC 6749 error code of an OAuth failure, read from a flat
`{ "error": string, "error_description": string }` body or from a `WWW-Authenticate` challenge; on an
`insufficient_scope` rejection `getRequiredScope()` names the scope to reconnect with.
`ValidationException.getErrors()` returns structured validation details when present.

The standard error body is `{ "status": integer, "message": string, "data": object|null }`. Callers should
handle both 400 and 422 as validation failures. A blocked account deletion may also include
`restrictions: [{ "code": "ActivePaidSubscription"|"PendingDocuments", "message": string,
"account_ids": [string, ...] }]`. The SDK preserves the complete error JSON in
`ApiException.getResponseBody()`. Per-operation status codes are listed below; authentication, authorization,
content-type, rate-limit, and infrastructure layers can also return 403, 415, 429, or 503.

These downloads are raw binary responses rather than JSON envelopes and return `byte[]`:

- account logo;
- document `original`, `certificated`, `certificate-page`, `pades`, or `bundle` artifact;
- document thumbnail and page image;
- saved signer `signature` or `initial` image;
- signer document artifact;
- biometric review evidence image or document PDF.

Paginated methods return `PaginatedResult<T>`. `getData()` is the response `data` array and `getMeta()` contains
`currentPage`, `perPage`, `total`, and `lastPage`, read from `X-Pagination-Current-Page`,
`X-Pagination-Per-Page`, `X-Pagination-Total-Count`, and `X-Pagination-Page-Count`. Query maps may use
`per-page`, `per_page`, or `perPage`; the SDK sends `per-page`.

`maxRetries` defaults to zero and controls additional SDK retries. OkHttp may recover connections and retry
safe reads independently. When enabled, the SDK retries only safe `GET`, `HEAD`, and `OPTIONS` requests
that receive 429 or 503. It honors numeric `Retry-After` or `X-Rate-Limit-Reset`, caps the wait at 30 seconds,
and preserves thread interruption. It never automatically replays uploads, creates, updates, deletes,
notifications, or signatures. Mutating requests follow no redirects, including HTTP 302, 307, and 308;
the original response is surfaced as `ApiException`.

## Request payload catalog

An asterisk marks a required field. Jackson omits null optional properties unless a method explicitly uses
null to clear a value.

| Name / Java input | JSON, multipart, or form request |
|---|---|
| `AccountPayload` | `name*` on create (`string`), `name` on update; `notification_sender_type` (`User` or `Account`) |
| Account delete | `{ "force": boolean }`; `true` also cancels an active paid subscription |
| Account logo upload | `multipart/form-data` with binary image `file*` |
| Document upload | `multipart/form-data` with PDF `file*`, at most 25 MB and 2,000 pages |
| Login | `{ "email": string*, "password": string* }` |
| Two-factor login | `{ "mfa_token": string*, "code": string* }`; `code` is a 6-digit authenticator code or a recovery code |
| Authenticator enrollment | `{ "label": string? }` |
| Authenticator confirmation | `{ "id": string*, "code": string*, "password": string?, "reauth_code": string? }`; `password` or `reauth_code` only when replacing a confirmed method |
| Recovery-code regeneration, two-factor method removal | `{ "password": string?, "code": string? }`; at least one is required |
| `SocialLoginPayload` | `{ "provider": "google"*, "token": string*, "has_accepted_terms": boolean* }` |
| Link social login | `{ "provider": "google"*, "token": string* }` |
| Password-reset request | `{ "email": string* }` |
| Password reset | `{ "email": string*, "token": string?, "new_password": string* }` |
| Password change | `{ "email": string*, "password": string*, "new_password": string* }` |
| API-key creation | `{ "password": string* }` |
| Document rename | `{ "name": string* }`, maximum 255 characters |
| Document tag replace/append | `{ "tags": [tagId, ...] }` |
| `DocumentContractInput` | Non-empty partial contract terms or amendment link; properties and null semantics are listed under [Tracked contracts](#tracked-contracts) |
| `ContractRenewalPayload` | Any of `end_date`, `value`, `term`, `updated_at`; `term` is required unless an explicit later `end_date` is sent |
| `ContractTerminationPayload` | Optional `reason` (maximum 500 characters) and `updated_at` write precondition |
| Public token | `{ "channel": "email"*, "recipient": signer-email* }` |
| `CreateSignerPayload` | `{ "full_name": string*, "email": string?, "whatsapp_phone_number": E.164?, "government_id": CPF/CNPJ? }` |
| `UpdateSignerPayload` | Any non-empty subset of `full_name`, `email`, `whatsapp_phone_number`, `government_id` (CPF/CNPJ) |
| Assignment expiration | `{ "expires_at": ISO-8601 date-time\|null* }`; `clearExpiration` sends `null` to remove the expiration |
| Assignment decline | `{ "decline_reason": string* }`, at most 2000 characters |
| `AssignmentSignEntry[]` | `[{ "itemId": string*, "fieldId": string*, "pageId": string*, "value": string* }, ...]` |
| Multi-sign | `{ "document_ids": [string, ...]* }` |
| Multi-decline | `{ "document_ids": [string, ...]*, "decline_reason": string* }`, reason at most 2000 characters |
| Signer verification (Email or Whatsapp) | `{ "verification-code": string* }` |
| Biometric session start | `{ "consent": true* }`; explicit signer consent is required |
| Biometric review decision | `{ "decision": "Approved"\|"Rejected"*, "note": string\|null? }`; note at most 2000 characters |
| `ConfirmSignerDataPayload` | Any of `full_name`, `email`, `government_id` |
| `CreateFieldPayload` | `{ "type": string*, "name": string*, "regex": string\|null?, "is_required": boolean? }` |
| `UpdateFieldPayload` | Any of `name`, `regex`, `is_active`; `clearRegex()` explicitly sends `"regex": null` |
| Single field validation | `{ "value": any* }` |
| `FieldValidationPayload[]` | `[{ "field_id": string*, "value": any* }, ...]` |
| `CreateTagPayload` | `{ "name": string*, "color": six-digit-hex\|null? }`; name is at most 64 characters |
| `UpdateTagPayload` | Any of `name`, `color`; `clearColor()` deliberately sends `"color": null` |
| Signature image upload | Raw PNG bytes; query `type=signature\|initial`, optional `reuse=true\|false` |
| OAuth token exchange | `application/x-www-form-urlencoded` with `grant_type=authorization_code`*, `code`*, `redirect_uri`*, `code_verifier`*, `client_id`*, `client_secret`?, `resource` |
| OAuth token refresh | `application/x-www-form-urlencoded` with `grant_type=refresh_token`*, `refresh_token`*, `client_id`*, `client_secret`? |
| OAuth token revocation | `application/x-www-form-urlencoded` with `token`*, `token_type_hint` (`access_token`\|`refresh_token`)?, `client_id`*, `client_secret`? |

Document tag requests carry tag IDs; the SDK forwards the supplied strings without rewriting them. The SDK also
exposes environment-specific request forms in [SDK extensions](#sdk-extensions).

`CreateFieldPayload.setActive` and `UpdateFieldPayload.setType` / `setRequired` are SDK extensions. They are not
properties of the documented field-create and field-update schemas.

### Assignment create and estimate

`CreateAssignmentPayload` produces this body. The SDK defaults an omitted `method` to `virtual`; at least one
signer is required for create, and `entries` is required for a `collect` assignment.

Use `setSigners(List<SignerRef>)` when setting verification, notification, or step. `setSignerStrings(...)` and
`setSignerIds(...)` convert IDs to plain signer references; the most recently used signer setter wins.

```json
{
  "method": "virtual | collect",
  "signers": [
    {
      "id": "signer-id",
      "verification_method": "Email | Whatsapp | DigitalCertificate | IdDocumentFaceMatchBasic",
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

The estimate endpoint accepts `method`, `signers`, and `entries` pricing inputs. A virtual estimate requires
non-empty `signers`; a collect estimate requires non-empty `signers` and `entries`. Each estimate signer is sent
as `{ "verification_method", "notification_methods" }`; signer `id`, `step`, `message`, `expires_at`, and
`copy_receivers` belong to creation and are not sent.

Verification and notification are coupled. If neither is supplied both default to `Email`; supplying only one
lets the API infer the other. `Email` verification pairs with `Email`, `Whatsapp` with `Whatsapp`, and
`DigitalCertificate` and `IdDocumentFaceMatchBasic` with either notification channel. Only one notification channel is accepted per signer.
Sequential `step` values must be positive, contiguous from 1, and supplied for every signer when used.

`DigitalCertificate` requires `method: "virtual"`, the account feature, a CPF/CNPJ in the signer's
`government_id`, and a step that contains only that signer. It adds 0.5 credits per signer
(`SignatureDigitalCertificate`) plus notification cost. The certificate flow completes the signer's virtual
item; the ordinary field-signing endpoint refuses certificate signers, so a certificate signer cannot complete
a `collect` assignment.

`assignments.create` and `assignments.estimateCost` reject `DigitalCertificate` with `method: "collect"`
before sending a request. The resulting `ValidationException` has
`getErrors().get("code") == AssignmentResource.DIGITAL_CERTIFICATE_REQUIRES_VIRTUAL_METHOD`, whose stable
value is `digital_certificate_requires_virtual_method`; `getErrors().get("method")` is `"collect"`.
They also reject a certificate signer that shares a step with any other signer. That error uses
`AssignmentResource.DIGITAL_CERTIFICATE_REQUIRES_EXCLUSIVE_STEP`, whose stable value is
`digital_certificate_requires_exclusive_step`, and includes the conflicting `step` in `getErrors()`.

The signer completes the ICP-Brasil Web PKI flow in a browser, through the
`POST /signers/certificate/start` and `POST /signers/certificate/complete` routes, which the API deploys but
publishes no schema for and which this server-side SDK therefore does not wrap. Download the resulting qualified
PDF with artifact name `pades`.

### Biometric identity verification

`IdDocumentFaceMatchBasic` requires the account feature and a valid CPF in `government_id`. Estimate its
per-signer fee and notification cost before creation. It uses hosted ID capture, face matching, and liveness.
The signer explicitly consents through `signerSelf.startIdentityVerification(code, true)`, which returns a
capture bearer URL. Open it unchanged, including its fragment; keep it out of logs and browser storage.
`IdentityVerificationSession` and `IdentityVerificationStatus` redact the URL in `toString()`.

`signerSelf.getIdentityVerification(code)` returns `can_start`, `status`, `url`, `expires_at`, and `completed_at`.
A session can start only before a session exists or after it expires before submission. Pending, InReview,
Approved, Declined, and Abandoned sessions cannot restart. Resends preserve the session, and restarting an
expired session does not charge again. A declined or abandoned verification needs a new document and assignment.

InReview permits viewing, data confirmation, and signing. Certification waits for every review's approval;
a document whose signers have all signed waits in `pending_identity_review`. An account reviewer reads
`assignments.getIdentityVerification(documentId, assignmentId, signerId)` and downloads the names in `images`
using `downloadIdentityVerificationImage(..., name)`. The API returns JPEG, PNG, or PDF with no-store caching;
images are available only while the review is open and return 404 after a decision. Handle evidence as private data.

`assignments.reviewIdentityVerification(..., decision, note)` accepts only `Approved` or `Rejected`.
Approval permits certification once every signer has signed and every review is approved. Rejection sets
`rejected_by_user` and sends cancellation notices to all signers and copy receivers. The note is private,
recorded in activities, and sent to the provider; the signer receives no separate decision message.
Direct provider approval has no review evidence here; its summary is on the `signer_identity_verified` activity.

### OAuth 2.1 authorization-code flow

`client.oauth` covers applications that act in **other people's** workspaces with those users' permission. An
integration automating its own workspace keeps using an API key and needs none of this.

The browser-facing authorization page lives on the authorization server, `https://auth.assinafy.com.br`; the
token, revoke, and userinfo endpoints live on this API. PKCE with `S256` is mandatory for every application,
confidential ones included.

```java
String verifier = OAuthResource.generateCodeVerifier();   // 43 chars from A-Z a-z 0-9 - . _ ~
String state = OAuthResource.generateState();
// Persist verifier and state in the user's session, then redirect the browser to:
String url = client.oauth.authorizationUrl(
        new OAuthAuthorizationRequest("client-id", "https://myapp.example/oauth/callback")
                .setScopes(List.of("documents:read", "documents:write", "offline_access"))
                .setState(state)
                .setCodeVerifier(verifier));
```

`authorizationUrl` fixes `response_type=code` and `code_challenge_method=S256`, derives `code_challenge` as the
base64url SHA-256 of the verifier, and sends the origin of the client's base URL as `resource`
(`https://api.assinafy.com.br` in production) — the same value the code exchange sends, so the two cannot
disagree (`invalid_target`). `setResource(...)` accepts only that value. Override the endpoint with
`setAuthorizationEndpoint(...)`; add `setNonce(...)` when requesting `openid`.

| Scope | Grants |
|---|---|
| `documents:read` | Read documents, their pages, tags, signers, assignments, and activity |
| `documents:write` | Create, update, and delete documents and manage their signers and assignments |
| `templates:read` | Read templates, their pages, roles, fields, and tags |
| `templates:write` | Create, update, and delete templates and their contents |
| `account:read` | Read the workspace profile, theme, and logo |
| `webhooks:write` | Create, update, and delete workspace webhook endpoints; creation returns its signing secret once, while later retrieval/rotation requires an API key or user session |
| `openid` | Identify the user and enable `GET /oauth/userinfo` |
| `profile` | Include the user's name in the `id_token` and userinfo claims |
| `email` | Include the user's email and its verification status in those claims |
| `offline_access` | Receive a refresh token; never appears in the granted `scope` of an access token |

Billing, account lifecycle, credential management, and administration are never reachable with an OAuth token,
whatever its scopes.

On the redirect URI, verify `state` against the stored value and `iss` against
`https://auth.assinafy.com.br` before using the code. The code is single-use and expires 60 seconds after
approval:

```java
OAuthTokens tokens = client.oauth.exchangeAuthorizationCode(
        "client-id", "client-secret", code, "https://myapp.example/oauth/callback", verifier);
```

Access tokens last one hour. A refresh token is valid for 30 days and every refresh returns a new one with a
fresh 30 days, so a connection only expires after 30 days without a refresh. `refreshToken(...)` returns a
**new** refresh token and retires the old one, so store the new value before using the response and refresh one
at a time per connection — a reused refresh token is indistinguishable from a stolen one and ends the whole
connection. A `2xx` response without a new `refresh_token` (missing, blank, or the one sent) raises
`ValidationException`: the token sent may already be retired, so ask the user to reconnect. A client keeps the
token it was built with, so call the API through one built with the renewed access token.

The SDK sends each token and revoke request once: `client.oauth` follows no redirects, and OkHttp never re-sends
one on its own — not after a connection failure, a `408`, or a `503` with `Retry-After: 0`. A dropped call
raises `NetworkException`, and such a response `ApiException`. Do not re-send a refresh token after a failure
that may have reached the server either: re-read storage, continue only if it holds a different, newer token,
and otherwise ask the user to reconnect. Only a `NetworkException` caused by `UnknownHostException`,
`ConnectException`, or `SSLHandshakeException` happened before sending and is safe to retry.

Redirect URIs must be HTTPS, carry no fragment, and match a registered value character for character; the SDK
rejects a malformed one before sending a request. A code verifier outside the RFC 7636 grammar of 43 to 128
unreserved characters is likewise rejected locally rather than returning `invalid_grant`.

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

Use `signEntries(..., List.of(new AssignmentSignEntry(itemId, fieldId, pageId, value)))`; the untyped
`sign(..., List<?>)` overload remains available for raw maps.

### Document from template

`CreateDocumentFromTemplateOptions` and `TemplateSigner` produce:

```json
{
  "signers": [
    {
      "role_id": "template-role-id",
      "id": "existing-signer-id",
      "email": "signer@example.com",
      "name": "Example Signer",
      "whatsapp_phone_number": "+5548999990000",
      "verification_method": "Email | Whatsapp | DigitalCertificate | IdDocumentFaceMatchBasic",
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

Creation requires `role_id` and either `id` or `email` for each signer role. When `id` is absent, the API
reuses the account signer with that email or creates one using `name` and optional `whatsapp_phone_number`.
When both are sent, `id` wins. Estimation sends each signer as
`{ "role_id"*, "verification_method", "notification_methods" }`; signer `id` and `step` are not sent. Template default tags are always applied and `tags` are
merged on top. Use `new TemplateEditorField(fieldId, value)` with `setTemplateEditorFields`; the untyped
`setEditorFields(List<?>)` overload remains available for raw maps.

The API derives a template assignment's method from its placed fields and rejects a certificate signer when
that role has placed fields. Both `documents.createFromTemplate` and `documents.estimateCostFromTemplate`
enforce the certificate-exclusive step rule before sending a request.

### Tracked contracts

`DocumentContractInput` is a partial update. An uncalled setter omits the property; a called setter includes it
even when its value is null. Null clears the property, except `auto_renew` returns to false, `currency` returns
to `BRL` while a value remains, and `status` does not accept null. The body must contain at least one property.

| Property | Type and rules |
|---|---|
| `status` | `draft`, `active`, `expired`, or `terminated`; after creation, callers set only `draft` or `active` |
| `counterparty_name` | String or null; maximum 255 characters |
| `counterparty_taxpayer_id` | Valid CPF/CNPJ, formatted or unformatted, or null |
| `value` | Decimal from 0 to 9,999,999,999,999.99 with at most two decimal places, or null |
| `currency` | Three-letter ISO 4217 code or null |
| `start_date`, `end_date` | `YYYY-MM-DD` or null; end date cannot precede start date |
| `auto_renew` | Boolean |
| `renewal_term_months` | 1–600 or null; the server default is 12 |
| `notice_period_days` | 0–3650 or null |
| `owner_id` | Active workspace member ID or null |
| `metadata` | Up to 50 string values; keys are 1–40 letters, digits, `_`, `-`, or `.`, values at most 500 characters; null clears it |
| `external_references` | Up to 20 `{ "system": string, "id": string }` items; replaces the list; empty or null clears it |
| `parent_document_id` | Links this document as an instrument for a root contract; null unlinks a pending instrument |
| `relation_type` | `amendment`, `renewal`, or `termination`; required on the first instrument link |
| `updated_at` | Last-read contract or pending-link timestamp; a stale value returns 409 `ContractChanged` without writing |

With `parent_document_id`, the other supplied properties are the instrument's delta rather than terms of its
own. The delta applies to the root after that instrument document is certificated. Applied instruments are
read-only; `contracts.related(documentId)` returns the root and every instrument in its family.

`ContractRenewalPayload` starts the next term with an explicit later `end_date`, or with the current `term` so
the server advances by `renewal_term_months`. It can also replace or clear `value`; `updated_at` is the same
optional 409 precondition. `ContractTerminationPayload` carries an optional private `reason` of at most 500
characters and the same precondition. Mutating contract calls are sent once; renewal's term/date inputs make a
caller retry safe to recognize and refuse instead of advancing twice.

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
are required. The subscription operations act on the account's oldest webhook endpoint.

### Webhook endpoint

`WebhookEndpointPayload` serves create and update. Unset properties are omitted, so an update changes only what
is set. Create requires `url`, `email`, and at least one event:

```json
{
  "url": "https://example.com/webhooks/assinafy",
  "email": "ops@example.com",
  "events": ["document_ready", "signer_signed_document"],
  "name": "ERP",
  "is_active": true,
  "signing_enabled": true
}
```

`url` must be an absolute HTTP or HTTPS URL that no other endpoint of the account uses. `is_active` defaults to
`true` and `signing_enabled` to `false` on create. Enabling signing generates a secret when the endpoint has
none and keeps the current one otherwise; disabling it discards the secret. A signed endpoint creation returns
`secret` once: store `endpoint.secret()` immediately, including with OAuth. List/view/update responses omit it.
Only API keys and user sessions may retrieve or rotate it later; OAuth apps must delete and recreate a lost endpoint.
`WebhookEndpoint` keeps its original nine-argument constructor and the ten-argument creation-secret
constructor. The canonical record also exposes `resource`, `is_held`, and `held_since`.

### Notification preferences

`NotificationPreferences` is a partial update request and a complete response. Its 13 case-sensitive boolean
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
  "SignerWhatsappFailed": true,
  "ContractRenewalNotice": true,
  "ContractExpiring": true,
  "ContractExpired": true,
  "ContractRenewed": true
}
```

## Query parameters

| Operation group | Supported query parameters |
|---|---|
| Paginated lists | `page`, `per-page` (maximum 100 where enforced) |
| Documents list | `status`, `method` (`virtual\|collect`), `search`, comma-separated tag IDs in `tags` (all IDs must match), `sort` (`name` or `updated_at`), pagination |
| Document search | `search`, `status`, pagination |
| Assignments list | `accountId` (set from the account override or client default), `sort`, `page`, `per-page` |
| Document activities | `sort=id` for oldest first or `sort=-id` for newest first (default) |
| Contracts list | `status` (comma-separated), `counterparty`, `search`, `end_date_from`, `end_date_to`, `ending_within_days`, `external_system`, `external_id`, `sort` (`end_date` or `updated_at`, prefixed with `-` for descending), `expand=document`, pagination |
| Fields list | `include_inactive`, `include_standard` |
| Signers list | `search`, pagination |
| Templates list | `search`, pagination |
| Tags list | `search` |
| Signer documents | pagination; search route takes `search` |
| Account/user stats | `granularity=monthly\|daily` (monthly by default); `month=YYYY-MM` is required for daily |
| Tag delete | `force=true\|false` |
| Signing view | optional `has_accepted_terms=true\|false` |
| Signature upload | `type`, optional `reuse` |
| Webhook deletion by URL | `url` (required), matched exactly against the registered URL |
| Webhook endpoint list | Optional `url`, matched exactly against the registered URL |
| Webhook history (`ListDispatchesParams`) | `endpoint_id`, `event`, `delivered`, Unix timestamps `from`/`to`, pagination |

## Response payload catalog

Properties marked nullable or contextual may be null or absent. Date/time strings are ISO 8601 unless noted.

| Java model / wire schema | Complete `data` properties |
|---|---|
| `WorkspaceAccount` / Account | `resource`, `id`, `name`, `primary_color?`, `secondary_color?`, `notification_sender_type` (`User\|Account`), `roles[]`, `is_delete_allowed`, `limits?` (currently `webhook_endpoints`; detail only and forward-compatible), `created_at` |
| `AccountTheme` | `account_name`, `primary_color`, `secondary_color?`, `logo` |
| `User` | `id`, `name`, `email`, `telephone?`, `government_id?`, `is_email_verified`, `has_accepted_terms`, `is_password_set` (`false` for a social-login-only account), `is_mfa_enabled`, `created_at`, `to_be_deleted_at?` |
| `AuthenticationResult` | `access_token`, `user` (`User`), `accounts[]` (stored as `WorkspaceAccount`; authentication populates `id`, `name`, `roles`, `is_delete_allowed`, `created_at`); a login for a user with two-factor authentication carries `mfa_token` instead of `access_token` |
| `MfaMethods` | `methods[]` (`MfaMethods.Method`: `id`, `type` such as `Totp`, `label?`, `confirmed_at?`, `last_used_at?`), `recovery_codes_remaining` |
| `TotpEnrollment` | `id`, `secret` (base32, returned only once), `provisioning_uri` (`otpauth://` URI for a QR code) |
| Recovery codes | `{ "recovery_codes": [string, ...] }`; the SDK returns the list. Shown only once |
| Two-factor method removal | `{ "is_mfa_enabled": boolean }`; the SDK returns the boolean |
| `ApiKeyResponse` | `api_key` (full only on creation, masked on read, null when none exists) |
| `EmailResponse` | `email` |
| `NotificationPreferences` | The 13 boolean keys listed above |
| `DocumentStatsRow` | `period` (`YYYY-MM` or `YYYY-MM-DD`), `documents_uploaded`, `documents_sent`, `signature_requests`, `signature_requests_notification_email`, `signature_requests_notification_whatsapp`, `signature_requests_notification_bypass`, `signature_requests_verification_email`, `signature_requests_verification_whatsapp`, `signature_requests_verification_bypass`, `signature_requests_verification_digital_certificate`, `signature_requests_viewed`, `signature_requests_completed`, `documents_certified` |
| `Signer` | `resource`, `id`, `full_name`, `email?`, `whatsapp_phone_number?`, `government_id?`, `has_accepted_terms`; contextual fields: `has_signature`, `has_initial`, `is_signature_reusable`, `verification_method`, `identity_verification_status?` (`Pending\|InReview\|Approved\|Declined\|Expired\|Abandoned`), `notification_methods[]`, `step`, `notified`, `completed`, `notification_history[]` |
| `IdentityVerificationStatus` | `can_start`, `status?`, `url?`, `expires_at?`, `completed_at?` |
| `IdentityVerificationSession` | `id`, `url`, `expires_at?` |
| `SignerIdentityReview` | `id`, `status`, `provider`, `verification_method`, `reason_codes[]`, `evidence_available`, `expected?`, `document?`, `matches?`, `checks[]`, `images[]`, `ruleset_version?`, `evidence_sha256?`, `reviewed_by?`, `reviewed_at?`, `review_note?`, `completed_at?` |
| `SignerIdentityReview.Reason` | `code`, `description` |
| `SignerIdentityReview.Expected` | `name?`, `cpf?` |
| `SignerIdentityReview.IdentityDocument` | `type?`, `name?`, `cpf?` |
| `SignerIdentityReview.Matches` | `cpf?` (`boolean`), `name?` (similarity 0–1), `birth_date?` (`boolean`) |
| `SignerIdentityReview.Check` | `name`, `result` (`pass`, `review`, `skipped`, or a provider failure), `score?`, `reasons[]` |
| `SignerIdentityReview.Reviewer` | `id`, `name` |
| `AssignmentSignerNotification` | `event`, `status` (`sent\|failed`), `error_code?`, `error_message?`, `sent_at?`, `failed_at?` |
| `Assignment` | `resource`, `id`, `document_id?` (null only after document deletion), `sender_email`, `method` (`virtual\|collect`), `expires_at?`, `message?`, `signers[]`, `copy_receivers[]` (`Signer` objects), `items[]`, `summary`, `signing_urls[]`, `created_at` |
| `AssignmentItem` | `id`, `page?`, `signer`, `field?`, `display_settings`, `value?`, `completed` |
| `AssignmentSummary` | `signer_count`, `completed_count`, `signers[]` |
| `SigningUrl` | `signer_id`, `url` |
| `CostEstimate` | `documents`, `credits`, `needs_extra_document`, `extra_document_cost`, `total_credits`, `breakdown[]`, `document_balance`, `credit_balance`, `has_sufficient_resources`, `blocking_reason?`, `message?` |
| `CostEstimateBreakdownItem` | `code`, `name`, `cost`, `quantity`, `unit_cost` |
| `ResendResult` | `is_sent`, `document_id`, `signer_id` |
| `ResendCostEstimate` | Documented `CostEstimate` fields; also accepts compact `total`, `breakdown[]` (`code`, `name`, `cost`), `credit_balance`, `has_sufficient_credits` |
| `WhatsappNotification` | `sent_at` (Unix seconds), `header`, `body`, `buttons[]` (`text`), `phone_number`, `signer_id` |
| `DocumentDetails` / Document | `resource`, `id`, `account_id`, `template_id?`, `name`, `status`, `artifacts`, `is_closed`, `signing_url`, `verification_code?`, `verification_url?`, `has_contract?` (workspace detail with contract tracking only), `decline_reason?`, `declined_by?`, `tags[]`, `assignment?`, `pages[]`, `created_at`, `updated_at`; public/signer contexts can add `page_count`, `created_by`, `current_signer`, `download_url`, `download_final_url`, `activities` |
| `DocumentListItem` | `resource`, `id`, `account_id`, `template_id?`, `name`, `status`, `artifacts`, `is_closed`, `signing_url`, `verification_code?`, `verification_url?`, `decline_reason?`, `declined_by?`, `tags[]`, `assignment?`, `pages[]`, `created_at`, `updated_at`; lightweight search may omit expanded relationships |
| `DocumentArtifacts` | URL map keyed by `original`, `certificated`, `certificate-page`, `pades`, `bundle`, and contextual `thumbnail` |
| `DocumentPage` | `id`, `number`, `height`, `width`, `download_url` |
| `DocumentStatus` | `code`, `deletable` |
| `DocumentVerification` | `hash`, `id?`, `agreement_code?` (printed on the document certificate), `status?`, `page_count?` (string), `signer_count?` (string), `completed_count?`, `completed_at?`, `verified_at`, `is_valid`, `message` |
| `DocumentActivity` | `id`, `event`, `message`, `payload?`, `origin?`, `created_at`. `payload` is typed `Object` because its shape varies by event — a JSON object for most events, a JSON array for others such as `document_prepared`. `origin` is a JSON object of `ip` and `user-agent`, and is null for server-generated events |
| `DocumentContract` | `resource`, `document_id`, `status`, `counterparty_name?`, `counterparty_taxpayer_id?`, `value?`, `currency?`, `start_date?`, `end_date?`, `auto_renew`, `renewal_term_months?`, `notice_period_days?`, `term`, `owner_id?`, `metadata?`, `external_references[]`, `document?` (with `expand=document`), `amendments[]` (single/related reads), `created_at`, `updated_at` |
| `DocumentContractAmendment` | `resource`, `document_id`, `document_name`, `parent_document_id`, `relation_type`, `number?`, `status`, `document_status`, `apply_error?`, `currency?`, `terms?`, `changes?` (each field as `{from,to}`), `applied_at?`, `created_at`, `updated_at` |
| `ContractExternalReference` | `system`, `id` |
| `FieldDefinition` | `resource`, `id`, `name`, `type`, `regex?`, `is_pre_defined`, `is_active`, `is_required`, `is_standard`, `is_read_only`, `is_visible` |
| `FieldTypeInfo` | `type`, `name` |
| `FieldValidationResult` | Single: `type`, `success`, `error_message`; multiple also includes `field_id` |
| `Tag` | `resource`, `id`, `name`, `color?`, `created_at`, `updated_at` |
| `TemplateDetails` / Template | `resource`, `id`, `name`, `document_name?`, `message?`, `status`, `pages[]`, `roles[]`, `tags[]`, `default_document_tags[]`, `created_at`, `updated_at`; SDK also reads contextual `account_id` |
| `TemplateListItem` | `resource`, `id`, `account_id?`, `name`, `document_name?`, `message?`, `status`, `pages[]`, `roles[]`, `tags[]`, `default_document_tags[]`, `created_at`, `updated_at` |
| `SendTokenResult` | `document` (`resource`, `id`, `name`, `page_count`, `created_by`), `channel`, `recipient` |
| `TemplatePage` | `id`, `number`, `height`, `width`, `download_url`, `fields[]` |
| `TemplateFieldPlacement` | `id`, `field_id`, `role_id`, `label`, `display_settings`, `created_at`, `updated_at` |
| `TemplateRole` | `id`, `name`, `assignment_type`, `created_at`, `updated_at` |
| `WebhookSubscription` | `events[]`, `is_active`, `is_held`, `held_since?` (Unix seconds), `url?`, `email?`, `updated_at?` |
| `WebhookEndpoint` | `resource`, `id`, `name?`, `url`, `email`, `events[]`, `is_active`, `is_held`, `held_since?` (Unix seconds), `signing_enabled`, `created_at`, `updated_at`, `secret?` (only when generated on create/update) |
| Webhook endpoint secret | `{ "secret": "whsec_..." }`; the SDK returns the string |
| `WebhookEvent` | Webhook delivery body; see [Webhook deliveries](#webhook-deliveries) |
| `WebhookDispatch` | `resource`, `id`, `event`, `activity_id`, `endpoint?`, `endpoint_id?`, `payload?`, `delivered`, `webhook_id?`, `attempt?`, `kind` (`scheduled\|probe\|manual`), `will_retry`, `http_status?`, `response_body?`, `error?`, `created_at`, `updated_at` |
| `WebhookEventTypeInfo` | `id`, `description` |
| `AcceptTermsResponse` | Optional response fields `full_name`, `email`, `has_accepted_terms`; the documented success payload has no data, so the Java return may be null |
| `VerifyEmailResponse` | Optional response fields `message`, `access_token`; the documented success payload has no data, so the Java return may be null |
| `PaginationMeta` | `current_page`, `per_page`, `total`, `last_page` (built from response headers, not envelope `data`) |
| `OAuthTokens` | `access_token`, `token_type` (`Bearer`), `expires_in` (seconds), `refresh_token?` (only with `offline_access`; a refresh always returns a new one), `scope` (space-separated, never contains `offline_access`), `id_token?` (only with `openid`), `issued_token_type?` (RFC 8693 token type URI, when supplied) |
| `OAuthUserInfo` | `sub`, `name?` (needs `profile`), `email?` and `email_verified?` (need `email`) |
| `OAuthProtectedResource` | `resource`, `authorization_servers[]`, `scopes_supported[]`, `bearer_methods_supported[]` |

`Patterns.EMAIL` is the shared basic email-validation pattern used by resource boundaries and request models.

The three OAuth response models above are **not** envelope payloads. RFC 6749, OIDC Core, and RFC 8615 each
require a flat object, so those endpoints answer with the object itself and the SDK deserializes it directly.

`ResendCostEstimate` extends `CostEstimate`. `getTotal()` falls back to `total_credits`, and
`getHasSufficientCredits()` falls back to `has_sufficient_resources`, so both accepted response forms remain
fully readable.

Statistics are zero-filled for every returned period. The four verification counters (email, WhatsApp, bypass,
and digital certificate) sum to `signature_requests`. Notification counters may sum to at least
`signature_requests` because one request can generate more than one notification attempt.

## Official operation matrix

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

### Assignments — 10 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **GET** `/assignments` | `assignments.list([params][, accountId])` | Pagination + `accountId` | `PaginatedResult<Assignment>` | 200, 401, 500 |
| **POST** `/documents/{documentId}/assignments` | `assignments.create(documentId, payload)` | Assignment create | `Assignment` | 200, 400, 401, 500 |
| **POST** `/documents/{documentId}/assignments/estimate-cost` | `assignments.estimateCost(documentId, payload)` | Assignment estimate | `CostEstimate` | 200, 400, 401, 500 |
| **PUT** `/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/resend` | `assignments.resendNotification(documentId, assignmentId, signerId)` | — | `ResendResult` | 200, 401, 500 |
| **POST** `/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/estimate-resend-cost` | `assignments.estimateResendCost(documentId, assignmentId, signerId)` | — | `ResendCostEstimate` | 200, 401, 500 |
| **PUT** `/documents/{documentId}/assignments/{assignmentId}/reset-expiration` | `assignments.resetExpiration(documentId, assignmentId, expiresAt)` | Assignment expiration | `Assignment` | 200, 400, 401, 404, 500 |
| **GET** `/documents/{documentId}/assignments/{assignmentId}/whatsapp-notifications` | `assignments.whatsappNotifications(documentId, assignmentId)` | — | `List<WhatsappNotification>` | 200, 401, 500 |
| **GET** `/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/identity-verification` | `assignments.getIdentityVerification(documentId, assignmentId, signerId)` | — | `SignerIdentityReview` | 200, 401, 404, 500 |
| **GET** `/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/identity-verification/images/{name}` | `assignments.downloadIdentityVerificationImage(documentId, assignmentId, signerId, name)` | Evidence image name | Raw `byte[]` | 200, 401, 404, 500 |
| **POST** `/documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/identity-verification/review` | `assignments.reviewIdentityVerification(documentId, assignmentId, signerId, decision, note)` | Biometric review decision | `SignerIdentityReview` | 200, 400, 401, 404, 500 |

### Authentication — 15 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **POST** `/login` | `auth.login(email, password)` | Login | `AuthenticationResult` | 200, 400, 500 |
| **POST** `/authentication/mfa/verify` | `auth.verifyMfa(mfaToken, code)` | Two-factor login | `AuthenticationResult` | 200, 400, 401, 500 |
| **GET** `/users/self/mfa` | `auth.listMfaMethods()` | — | `MfaMethods` | 200, 401, 500 |
| **POST** `/users/self/mfa/totp` | `auth.startTotpEnrollment(label)` | Authenticator enrollment | `TotpEnrollment` | 200, 401, 500 |
| **PUT** `/users/self/mfa/totp/confirm` | `auth.confirmTotpEnrollment(methodId, code, password, reauthCode)` | Authenticator confirmation | `List<String>` recovery codes | 200, 400, 401, 404, 500 |
| **POST** `/users/self/mfa/recovery-codes` | `auth.regenerateRecoveryCodes(password, code)` | Recovery-code regeneration | `List<String>` recovery codes | 200, 400, 401, 500 |
| **DELETE** `/users/self/mfa/{customId}` | `auth.removeMfaMethod(methodId, password, code)` | Two-factor method removal | `boolean` `is_mfa_enabled` | 200, 400, 401, 404, 500 |
| **POST** `/authentication/social-login` | `auth.socialLogin(payload)` | `SocialLoginPayload` | `AuthenticationResult` | 200, 400, 500 |
| **POST** `/auth/link-social-login` | `auth.linkSocialLogin(provider, token)` | Link social login | `void` | 200, 400, 401, 500 |
| **PUT** `/authentication/request-password-reset` | `auth.requestPasswordReset(email)` | Password-reset request | `EmailResponse` | 200, 500 |
| **PUT** `/authentication/reset-password` | `auth.resetPassword(email, token, newPassword)` | Password reset | `EmailResponse` | 200, 400, 500 |
| **PUT** `/authentication/change-password` | `auth.changePassword(email, password, newPassword)` | Password change | `EmailResponse` | 200, 400, 401, 500 |
| **GET** `/users/api-keys` | `auth.getApiKey()` | — | `ApiKeyResponse` (`api_key` may be null) | 200, 401, 500 |
| **POST** `/users/api-keys` | `auth.createApiKey(password)` | API-key creation | `ApiKeyResponse` | 200, 401, 500 |
| **DELETE** `/users/api-keys` | `auth.deleteApiKey()` | — | `void` | 200, 401, 500 |

### Contracts — 6 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **GET** `/accounts/{accountId}/contracts` | `contracts.list([params][, accountId])` | Contract-list query | `PaginatedResult<DocumentContract>` | 200, 400, 401, 500 |
| **GET** `/accounts/{accountId}/documents/{documentId}/contract` | `contracts.get(documentId[, accountId])` | — | `DocumentContractResult` (`DocumentContract` or `DocumentContractAmendment`) | 200, 401, 404, 500 |
| **PUT** `/accounts/{accountId}/documents/{documentId}/contract` | `contracts.set(documentId, input[, accountId])` | `DocumentContractInput` | `DocumentContractResult` | 200, 400, 401, 403, 404, 409, 500 |
| **GET** `/accounts/{accountId}/documents/{documentId}/contract/related` | `contracts.related(documentId[, accountId])` | — | Root `DocumentContract` with `amendments[]` | 200, 401, 404, 500 |
| **POST** `/accounts/{accountId}/documents/{documentId}/contract/renew` | `contracts.renew(documentId[, payload][, accountId])` | Optional `ContractRenewalPayload` | `DocumentContract` | 200, 400, 401, 403, 404, 409, 500 |
| **POST** `/accounts/{accountId}/documents/{documentId}/contract/terminate` | `contracts.terminate(documentId[, payload][, accountId])` | Optional `ContractTerminationPayload` | `DocumentContract` | 200, 400, 401, 403, 404, 409, 500 |

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
| **GET** `/documents/{documentId}/activities` | `documents.activities(documentId[, sort])` | Optional `sort=id\|-id` | `List<DocumentActivity>` | 200, 401, 500 |
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
| **POST** `/accounts/{accountId}/fields/{fieldId}/validate` | `fields.validateAsWorkspace(fieldId, value[, accountId])`; signer form `fields.validate(fieldId, value, signerAccessCode[, accountId])` | Single validation | `FieldValidationResult` | 200, 401, 500 |
| **POST** `/accounts/{accountId}/fields/validate-multiple` | `fields.validateMultipleAsWorkspace(values[, accountId])`; signer form `fields.validateMultiple(values, signerAccessCode[, accountId])` | `FieldValidationPayload[]` | `List<FieldValidationResult>` | 200, 401, 500 |
| **GET** `/field-types` | `fields.listTypes()` | — | `List<FieldTypeInfo>` | 200, 401, 500 |

### OAuth — 4 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **POST** `/oauth/token` (`authorization_code`) | `oauth.exchangeAuthorizationCode(clientId, clientSecret, code, redirectUri, codeVerifier)` | OAuth token exchange | `OAuthTokens` | 200, 400, 401, 500 |
| **POST** `/oauth/token` (`refresh_token`) | `oauth.refreshToken(clientId, clientSecret, refreshToken)` | OAuth token refresh | `OAuthTokens` | 200, 400, 401, 500 |
| **POST** `/oauth/revoke` | `oauth.revoke(clientId, clientSecret, token[, tokenTypeHint])` | OAuth token revocation | `void` | 200, 401, 500 |
| **GET** `/oauth/userinfo` | `oauth.userInfo()` | — | `OAuthUserInfo` | 200, 401, 403, 500 |
| **GET** `/.well-known/oauth-protected-resource` | `oauth.protectedResourceMetadata()` | — | `OAuthProtectedResource` | 200, 500 |

The metadata document is served at the API host root rather than under `/v1`, and the SDK derives that URL from
the configured base URL. `POST /oauth/token` is one documented operation with two grant types, which is why
five SDK methods cover four operations. Every one of them is public except `userinfo`, which reads the client's
bearer token. Revocation answers 200 for any token outcome — revoked, already revoked, unknown, or malformed —
so it cannot be used to probe whether a token exists; only failed client authentication answers 401. Revoke the
refresh token saved most recently: every refresh retires the one it was sent, and revoking a retired one also
answers 200 while the connection stays active.

### Signers — 5 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **GET** `/accounts/{accountId}/signers` | `signers.list([params][, accountId])` | `search`, pagination | `PaginatedResult<Signer>` | 200, 401, 500 |
| **POST** `/accounts/{accountId}/signers` | `signers.create(payload[, accountId])` | `CreateSignerPayload` | `Signer` | 200, 400, 401, 500 |
| **GET** `/accounts/{accountId}/signers/{signerId}` | `signers.get(signerId[, accountId])` | — | `Signer` | 200, 401, 404, 500 |
| **PUT** `/accounts/{accountId}/signers/{signerId}` | `signers.update(signerId, payload[, accountId])` | `UpdateSignerPayload` | `Signer` | 200, 400, 401, 404, 500 |
| **DELETE** `/accounts/{accountId}/signers/{signerId}` | `signers.delete(signerId[, accountId])` | — | `void` | 200, 401, 404, 500 |

`signers.create` always sends the documented POST. Use `signers.findOrCreate` only when exact-email reuse is the
intended application policy; it does not update an existing signer.

### Signing — 19 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **GET** `/public/documents/{documentId}` | `documents.getPublic(documentId)` | — | Public `DocumentDetails` | 200, 404, 500 |
| **PUT** `/public/documents/{documentId}/send-token` | `documents.sendTokenByEmail(documentId, recipient)`; `documents.sendToken(documentId, recipient, "email")` returns `void` | Public token | `SendTokenResult` | 200, 400, 404, 500 |
| **GET** `/signers/self` | `signerSelf.getSelf(signerAccessCode)` | Signer access code | `Signer` | 200, 401, 500 |
| **GET** `/signers/identity-verification` | `signerSelf.getIdentityVerification(signerAccessCode)` | Signer access code | `IdentityVerificationStatus` | 200, 401, 500 |
| **POST** `/signers/identity-verification` | `signerSelf.startIdentityVerification(signerAccessCode, consent)` | Biometric session start + signer access code | `IdentityVerificationSession` | 200, 400, 401, 500 |
| **GET** `/signers/{signerId}/document` | `signerSelf.getCurrentDocument(signerId, signerAccessCode)` | Signer access code | `DocumentDetails` | 200, 401, 404, 500 |
| **GET** `/sign` | `signerSelf.getSign(signerAccessCode[, hasAcceptedTerms])` | Signer access code; optional flag | `DocumentDetails` | 200, 400, 401, 409, 500 |
| **POST** `/documents/{documentId}/assignments/{assignmentId}` | `assignments.signEntries(documentId, assignmentId, signerAccessCode, entries)` / `sign(...)` | Collect submission | `void` | 200, 400, 401, 409, 500 |
| **PUT** `/documents/{documentId}/assignments/{assignmentId}/reject` | `assignments.decline(documentId, assignmentId, signerAccessCode, reason)` | Assignment decline | `void` | 200, 401, 500 |
| **PUT** `/signers/documents/sign-multiple` | `signerSelf.signMultiple(signerAccessCode, documentIds)` | Multi-sign | `void` | 200, 401, 500 |
| **PUT** `/signers/documents/decline-multiple` | `signerSelf.declineMultiple(signerAccessCode, documentIds, reason)` | Multi-decline | `void` | 200, 401, 500 |
| **POST** `/verify` | `signerSelf.verifyEmail(code, signerAccessCode)` | Signer verification | `VerifyEmailResponse` or null | 200, 400, 401, 500 |
| **PUT** `/documents/{documentId}/signers/confirm-data` | `signerSelf.confirmSignerData(documentId, signerAccessCode, payload)` | `ConfirmSignerDataPayload` | `Signer` | 200, 401, 500 |
| **PUT** `/signers/accept-terms` | `signerSelf.acceptTerms(signerAccessCode)` | Signer access code | `AcceptTermsResponse` or null | 200, 401, 500 |
| **POST** `/signature` | `signerSelf.uploadSignature(signerAccessCode, bytes, type[, reuse])` | Raw PNG + query | `void` | 200, 401, 500 |
| **GET** `/signature/{signatureType}` | `signerSelf.downloadSignature(signerAccessCode, type)` | Signer access code | Raw `byte[]` | 200, 401, 404, 500 |
| **GET** `/signers/{signerId}/documents` | `signerSelf.listDocuments(signerId, signerAccessCode[, params])`; workspace form `signerSelf.listDocumentsAsWorkspace(signerId, params)` | Signer access code or workspace credentials, pagination | `PaginatedResult<DocumentDetails>` | 200, 401, 500 |
| **GET** `/signers/{signerId}/documents/search` | `signerSelf.searchDocuments(signerId, signerAccessCode, search)`; workspace form `signerSelf.searchDocumentsAsWorkspace(signerId, search)` | Signer access code or workspace credentials, search | `PaginatedResult<DocumentDetails>` | 200, 401, 500 |
| **GET** `/signers/{signerId}/documents/{documentId}/download/{artifactName}` | `signerSelf.downloadDocument(signerId, documentId, artifactName)` | — | Raw `byte[]` | 200, 404, 500 |

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
official documentation categorizes them.

### Users — 4 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **GET** `/users/self` | `users.getSelf()` | — | `User` | 200, 401, 500 |
| **GET** `/users/self/notification-preferences` | `users.getNotificationPreferences()` | — | `NotificationPreferences` | 200, 401, 500 |
| **PUT** `/users/self/notification-preferences` | `users.updateNotificationPreferences(preferences)` | Partial `NotificationPreferences` | Complete `NotificationPreferences` | 200, 400, 401, 500 |
| **GET** `/users/self/stats` | `users.stats([params])` | Stats query | `List<DocumentStatsRow>` | 200, 400, 401, 500 |

### Webhooks — 14 operations

| Operation | SDK method | Request | Return | Statuses |
|---|---|---|---|---|
| **GET** `/accounts/{accountId}/webhooks/subscriptions` | `webhooks.getSubscription([accountId])` | — | `WebhookSubscription` | 200, 401, 500 |
| **PUT** `/accounts/{accountId}/webhooks/subscriptions` | `webhooks.register(payload[, accountId])` / `update(...)` | Webhook subscription | `WebhookSubscription` | 200, 400, 401, 500 |
| **PUT** `/accounts/{accountId}/webhooks/inactivate` | `webhooks.inactivate([accountId])` | — | `WebhookSubscription` | 200, 401, 500 |
| **GET** `/accounts/{accountId}/webhooks/endpoints` | `webhooks.listEndpoints([accountId])` / `listEndpointsByUrl(url[, accountId])` | Optional exact `url` | `List<WebhookEndpoint>`, oldest first | 200, 401, 500 |
| **POST** `/accounts/{accountId}/webhooks/endpoints` | `webhooks.createEndpoint(payload[, accountId])` | Webhook endpoint | `WebhookEndpoint` | 200, 400, 401, 403 (plan limit), 500 |
| **DELETE** `/accounts/{accountId}/webhooks/endpoints` | `webhooks.deleteEndpointByUrl(url[, accountId])` | Required `url` query | `void` | 200, 400, 401, 404, 500 |
| **GET** `/accounts/{accountId}/webhooks/endpoints/{endpointId}` | `webhooks.getEndpoint(endpointId[, accountId])` | — | `WebhookEndpoint` | 200, 401, 404, 500 |
| **PUT** `/accounts/{accountId}/webhooks/endpoints/{endpointId}` | `webhooks.updateEndpoint(endpointId, payload[, accountId])` | Partial webhook endpoint | `WebhookEndpoint` | 200, 400, 401, 404, 500 |
| **DELETE** `/accounts/{accountId}/webhooks/endpoints/{endpointId}` | `webhooks.deleteEndpoint(endpointId[, accountId])` | — | `void` | 200, 401, 404, 500 |
| **GET** `/accounts/{accountId}/webhooks/endpoints/{endpointId}/secret` | `webhooks.getEndpointSecret(endpointId[, accountId])` | — | `String` secret | 200, 400 (signing disabled), 401, 404, 500 |
| **POST** `/accounts/{accountId}/webhooks/endpoints/{endpointId}/secret/rotate` | `webhooks.rotateEndpointSecret(endpointId[, accountId])` | — | `String` new secret | 200, 400 (signing disabled), 401, 404, 500 |
| **GET** `/webhooks/event-types` | `webhooks.listEventTypes()` | — | `List<WebhookEventTypeInfo>` | 200, 401, 500 |
| **GET** `/accounts/{accountId}/webhooks` | `webhooks.listDispatches([params][, accountId])` | Webhook-history query | `PaginatedResult<WebhookDispatch>` | 200, 401, 500 |
| **POST** `/accounts/{accountId}/webhooks/{historyId}/retry` | `webhooks.retryDispatch(historyId[, accountId])` | — | `WebhookDispatch` | 200, 400, 401, 404, 500 |

## SDK extensions

These public methods compose official operations, provide typed aliases, or expose additional request forms:

| Method | Behavior and result |
|---|---|
| `AssinafyClient.create(...)` | Builds an API-key client; optional configurator can set timeout, retries, or base URL |
| `AssinafyClient.fromConfig(map)` | Accepts snake/camel variants for API key, token, account ID, and base URL; API key wins over token |
| `uploadAndRequestSignatures(options)` | Uploads a PDF, optionally polls until ready, finds or creates signers by email, and creates a virtual assignment; on failure, best-effort deletes the upload while preserving reusable account-scoped signers; returns `UploadAndRequestSignaturesResult` |
| `documents.waitUntilReady(id[, maxWaitMs, pollIntervalMs])` | Polls document details until `metadata_ready`, `pending_signature`, or `certificated`; throws on processing failure or timeout |
| `documents.isFullySigned(id)` | Returns true for `certificated`, a positive completed summary, or a non-empty signer list whose `completed` flags are all true |
| `documents.getSigningProgress(id)` | Derives `signed`, `total`, `pending`, and `percentage` from the assignment summary, falling back to signer `completed` flags |
| `documents.download(id)` | Alias for the `certificated` artifact |
| `assignments.clearExpiration(...)` | Sends the additional `expires_at: null` request form |
| `assignments.signEntries(...)` | Type-safe collect submission using `AssignmentSignEntry`; delegates to `sign(...)` |
| `signers.findByEmail(email[, accountId])` | Searches every reported page (100 candidates per page) and returns an exact case-insensitive email match or null |
| `signers.findOrCreate(payload[, accountId])` | Reuses an exact email match when email is supplied; otherwise creates; re-queries after duplicate-email 400/409 races |
| Field validation overloads with `signerAccessCode` | Send the required `signer-access-code` query parameter, without workspace credentials, on single or multiple validation requests |
| `signerSelf.uploadSignature(...)` with JPEG bytes | Detects JPEG input and sends `image/jpeg`; the official operation documents PNG |
| `signerSelf.downloadDocument(..., signerAccessCode)` | Adds `signer-access-code` to the signer artifact request |
| `CreateFieldPayload.setActive`, `UpdateFieldPayload.setType/setRequired` | Additional field request properties |
| `WhatsappNotification.buttons[].url` | Additional response field retained by the Java model when present |
| Concise `DocumentStatsRow` accessors | Java-only aliases for canonical notification/verification counters; historical input names deserialize without changing serialized JSON |
| `webhooks.update(...)` | Alias for create-or-replace `register(...)` |
| `new WebhookVerifier(secret).verify(id, timestamp, signature, body)` | Verifies a signed delivery's Standard Webhooks signature and timestamp, then returns the parsed `WebhookEvent`; makes no HTTP request |
| `templates.get(templateId[, accountId])` | Calls `GET /accounts/{accountId}/templates/{templateId}` and returns `TemplateDetails`; this detail route is outside the official operation list |
| `OAuthResource.generateCodeVerifier()` | Returns a fresh 43-character RFC 7636 PKCE verifier from `SecureRandom` |
| `OAuthResource.generateState()` | Returns a fresh opaque `state` value for CSRF protection on the redirect URI |
| `oauth.authorizationUrl(request)` | Builds the authorization-server URL, deriving the `S256` challenge from the verifier so the two can never disagree; makes no HTTP request |

All account-scoped resource methods expose overloads that either accept an explicit account ID or use the
client default. List methods expose no-argument overloads and parameter-map overloads where applicable.

`UploadAndRequestSignaturesOptions` is constructed with either `(File, signers)` or
`(byte[], fileName, signers)`. Optional setters are `waitForReady` (null/true waits), `message`, `expiresAt`,
`copyReceivers`, and `accountId`. Each `UploadAndRequestSignaturesSigner` contains required `name` and `email`
plus optional `whatsappPhoneNumber`. The result exposes `document`, `assignment`, and `signerIds`. This helper
always creates a virtual assignment with the API's default Email verification/notification behavior; use the
individual resource methods for collect or digital-certificate assignments.

## Webhook deliveries

An account registers one webhook endpoint, or up to three on paid plans. Assinafy sends an HTTP `POST` with
`Content-Type: application/json` to every active endpoint subscribed to the event; each endpoint is delivered
to independently and has its own failure count. Any 2xx response is successful. A connection error, timeout,
408, 429, or 5xx response is attempted up to six times, about ten minutes apart; other non-2xx responses are
final. After ten failed deliveries in a row, the endpoint is paused (`is_held`). The API probes it with its
oldest missed event after 3, 15, and 60 minutes, then every 6 hours. A successful probe lifts the pause and
delivers missed events oldest first. Editing the URL, sending `is_active: true`, or a successful manual retry
also lifts it. The history is retained for seven days and stores the first 2,000 response-body characters;
`will_retry` says whether another automatic attempt follows.

| Header | Value |
|---|---|
| `webhook-id` | Message ID, identical on every attempt of the same event to the same endpoint. Deduplicate on it |
| `webhook-timestamp` | Unix seconds of the attempt |
| `webhook-signature` | Present only when the endpoint has `signing_enabled`; one or more space-separated `v1,<base64>` entries |

### Verifying signatures

Signatures follow [Standard Webhooks](https://www.standardwebhooks.com): the HMAC-SHA256 of
`{webhook-id}.{webhook-timestamp}.{raw body}`, keyed with the base64-decoded part of the secret after
`whsec_`. `WebhookVerifier` implements it:

```java
WebhookVerifier verifier = new WebhookVerifier(storedEndpointSecret);

WebhookEvent event = verifier.verify(
        request.getHeader("webhook-id"),
        request.getHeader("webhook-timestamp"),
        request.getHeader("webhook-signature"),
        rawBodyBytes);
```

`verify` throws `ValidationException` when a header is missing, the timestamp is more than five minutes
(`WebhookVerifier.TOLERANCE`) from the local clock, no signature entry matches (compared in constant time), or
the body is not a webhook event. Pass the body exactly as received; re-serialized JSON does not verify. Rotating
the secret takes effect immediately, so replace the verifier when calling `rotateEndpointSecret`.

### Delivery body

Every body has this shape, parsed as `WebhookEvent`:

```json
{
  "id": 12345,
  "event": "signer_signed_document",
  "message": null,
  "payload": { "signer_full_name": "Example Signer" },
  "origin": { "ip": "203.0.113.10", "user-agent": "Example Client" },
  "created_at": 1787241600,
  "subject": { "type": "Signer", "id": "signer-id" },
  "object": { "type": "Document", "id": "document-id" },
  "account_id": "account-id"
}
```

`message` is reserved and currently null; `payload` and `origin` may be null. `created_at`, and the `*_at`
fields inside `subject` and `object`, are Unix seconds. `subject` and `object` are polymorphic and include
`type` (`User`, `Signer`, `Account`, `Document`, or `Template`) followed by that resource's fields. Document
objects include expanded assignment/pages. Account objects omit integration dispatch history.

The event catalog includes:

| Event | Subject | Object | Event-specific `payload` keys |
|---|---|---|---|
| `document_uploaded` | User | Document | — |
| `document_metadata_ready` | User | Document | — |
| `document_prepared` | User | Document | — |
| `assignment_created` | User | Document | `user_name`, `user_email`, `user_telephone` |
| `document_ready` | Account | Document | — |
| `document_processing_failed` | Account | Document | `error_message` |
| `signature_requested` | User | Document | `signer_full_name`, `signer_email`, `signer_whatsapp_phone_number`, `notification_method` |
| `signer_created` | User | Signer | `signer_full_name` |
| `signer_email_verified` | Signer | Document | `signer_full_name`, `signer_email` |
| `signer_whatsapp_verified` | Signer | Document | `signer_full_name`, `signer_whatsapp_phone_number` |
| `signer_data_confirmed` | Signer | Document | `signer_full_name`, `signer_email`, `signer_whatsapp_phone_number`, `verification_method` |
| `signer_viewed_document` | Signer | Document | `signer_full_name` |
| `signer_identity_verified` | Signer | Document | `signer_full_name`, `evidence` (when available) |
| `signer_identity_verification_failed` | Signer | Document | `signer_full_name`, `reason` |
| `signer_identity_review_required` | Signer | Document | `signer_full_name`, `reason_codes` |
| `signer_identity_review_approved` | Signer | Document | `signer_full_name`, `reviewer_id`, `reviewer_name`, `reason_codes` |
| `signer_identity_review_rejected` | Signer | Document | `signer_full_name`, `reviewer_id`, `reviewer_name`, `note` |
| `signer_signed_document` | Signer | Document | `signer_full_name` |
| `signer_rejected_document` | Signer | Document | `signer_full_name` |
| `user_rejected_document` | User | Document | `user_name` |
| `template_created` | User | Template | — |
| `template_processed` | User | Template | — |
| `template_processing_failed` | Account | Template | `error_message` |
| `document_contract_updated` | User or Account | Document | `user_name?`, `end_date`, `changed[]` |
| `document_contract_renewal_notice` | Account | Document | `end_date` |
| `document_contract_expiring` | Account | Document | `days_before`, `end_date` |
| `document_contract_expired` | Account | Document | `end_date`, `changed[]` |
| `document_contract_renewed` | Account or User | Document | `end_date`, `changed[]`, `user_name?` |
| `document_contract_terminated` | User | Document | `user_name`, `reason?`, `end_date`, `changed[]` |
| `document_contract_amended` | Account or User | Parent Document | `document_id`, `relation_type`, `number`, `changes?`, `end_date`, `changed[]` |
| `document_contract_amendment_updated` | User | Instrument Document | `user_name`, `parent_document_id?`, `relation_type?`, `terms?`, `changed[]` |

`assignment_created` and `document_metadata_ready` have no guaranteed order. Consumers should deduplicate on
the `webhook-id` header, which also tells deliveries of the same event to different endpoints apart, tolerate
retries, and ignore unknown fields and future event values.
