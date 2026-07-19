# Changelog

## [2.0.0] - 2026-07-19

Second full file-by-file audit against https://api.assinafy.com.br/v1/docs, verified live against the sandbox
API endpoint-by-endpoint. See [docs/AUDIT.md](docs/AUDIT.md) for the detailed report. This is a **breaking**
release: several methods that returned untyped `Map<String, Object>` now return typed models, one method that
targeted a non-existent route was removed, and a signer-facing payload's field names were corrected.

### Breaking
- **Typed returns replace raw `Map<String, Object>`** (the SDK is now uniformly typed):
  - `assignments.estimateCost(...)` → `CostEstimate` (was `Map`).
  - `documents.estimateCostFromTemplate(...)` → `CostEstimate` (was `Map`).
  - `assignments.estimateResendCost(...)` → `ResendCostEstimate` (was `Map`) — modelled on the **live** compact
    shape (`total`, `breakdown`, `credit_balance`, `has_sufficient_credits`), which differs from the full
    `CostEstimate` the OpenAPI spec references for that route.
  - `assignments.resendNotification(...)` → `ResendResult` (was `Map`).
  - `documents.verify(...)` → `DocumentVerification` (was `Map`).
- **`tags.delete(...)`, `documents.detachTag(...)`, and `documents.sendToken(...)` now return `void`** (were
  `Map<String, Object>`). The server response carried only a boolean/echo already asserted by the envelope path.
- **Removed `webhooks.deleteSubscription()`.** It targeted `DELETE /accounts/{id}/webhooks/subscriptions`, a
  route the API does not define (verified live: it returns the router-level 404 "Página não encontrada", while
  routed methods on the same path reach the controller). Use `webhooks.inactivate()` (`PUT .../inactivate`) to
  stop deliveries — there is no hard-delete server-side.
- **`signerSelf.confirmSignerData(...)` now returns the updated `Signer`** (was `void`), and
  **`ConfirmSignerDataPayload` fields are corrected** to the endpoint's real schema: `full_name`, `email`,
  `government_id` (removed `whatsapp_phone_number` and `has_accepted_terms`, which the endpoint ignores).
- **`WebhookSubscription` no longer exposes `getId()`/`getCreatedAt()`** — the API models the subscription as a
  singleton per account and returns neither field (both getters always returned `null`).

### Fixed
- **`signerSelf.acceptTerms(...)` and `signerSelf.verifyEmail(...)` now send the `signer-access-code` as the
  required query parameter** instead of in the JSON body. Per the API's security scheme the code is a query
  parameter, so the previous body placement failed authentication against the real API. `verifyEmail` sends
  only `{"verification-code": ...}` in the body; `acceptTerms` sends no body.
- **`fields.validateMultiple(...)` now serializes a `null` value** as `{"field_id": ..., "value": null}`
  instead of dropping the `value` key (`@JsonInclude(NON_NULL)` was removed from `FieldValidationPayload`). The
  API requires the key to be present, so a `null` value previously produced an HTTP 400 — inconsistent with the
  single `validate(...)` path, which already worked.
- **The void and binary transport paths now surface an error envelope returned under HTTP 200.**
  `executeVoid`/`executeBinary` inspect the envelope status (as the typed path already did), so a
  `{"status": 4xx, ...}` body under HTTP 200 on a delete/download raises `ApiException` instead of being
  swallowed (or returned as if it were the artifact).
- **`ApiException.getRetryAfterSeconds()` is only populated on retryable statuses (429/503).** It previously
  fell back to the always-present `X-Rate-Limit-Reset` header on every error, so a permanent 400/401 wrongly
  reported a retry hint. A caller keying retries on its presence no longer backs off on non-retryable failures.

### Added
- **New endpoint coverage** (all verified live):
  - `documents.rename(documentId, name)` → `PATCH /documents/{id}` (and a new `httpPatch` transport helper).
  - `documents.search(params[, accountId])` → `GET /accounts/{id}/documents/search` (lightweight search).
  - `assignments.list([params][, accountId])` → `GET /assignments` (sends the account context as the camelCase
    `accountId` query parameter, as the API requires).
  - `signerSelf.searchDocuments(signerId, signerAccessCode, term)` → `GET /signers/{id}/documents/search`.
  - `auth.linkSocialLogin(provider, token)` → `POST /auth/link-social-login`.
  - `webhooks.update(...)` — discoverability alias for `register(...)` (the spec names the PUT "Update").
- `Signer.getSignatureReusable()` (`is_signature_reusable` from `GET /signers/self`), and an
  `uploadSignature(..., Boolean reuse)` overload that sets the `reuse` query parameter.
- New typed models: `CostEstimate`, `CostEstimateBreakdownItem`, `DocumentVerification`, `ResendCostEstimate`,
  `ResendResult`.

### Documentation
- Corrected the README/EXAMPLES claim that the API-key endpoints require a token-authenticated client:
  `getApiKey()` works with an API-key-only client (verified live). Documented that the browser-redirect OAuth
  flows (`GET /auth/authenticate`, `GET /login-callback`) are intentionally out of scope for a server SDK, and
  that `documents.sendToken` uses `{recipient, channel}` (the live contract) rather than the spec's stale
  `{email}` body. Enriched method Javadoc across tags, documents, fields, and signer-self.

### Notes
- **`templates.get(templateId)` was re-verified and kept.** Although `GET /accounts/{id}/templates/{id}` is
  absent from the OpenAPI spec, live probing proves it is a real controller (resource-level "Template não
  encontrado." 404, not the router-level 404), unlike the phantom route removed in 1.5.0.
- Account/user/logo/theme management endpoints remain intentionally out of scope; this SDK stays focused on the
  webforms document-signing flows.

### Tooling
- Dependency bumps (patch): Jackson Databind 2.22.0 → 2.22.1, JUnit Jupiter 6.1.0 → 6.1.2. AssertJ (3.27.7),
  and the compiler/surefire plugins are kept on their latest **stable** releases (the newer 4.0.0-M1 /
  4.0.0-beta / 3.6.0-M1 artifacts are pre-releases). OkHttp remains on 4.12.0 — 5.x publishes as a
  Kotlin-Multiplatform artifact and requires switching to `okhttp-jvm` plus MockWebServer API changes; tracked
  separately.
- GitHub Actions bumped and re-pinned to commit SHAs: `actions/checkout` v6.0.3 → **v7.0.0**,
  `actions/setup-java` v5.2.0 → **v5.6.0** (in both `ci.yml` and `release.yml`). Least-privilege permissions,
  the JDK 21 + 25 matrix, concurrency, and the GitLab CI mirror are unchanged.
- Reproducible-build timestamp updated to the 2.0.0 release date.

### Test Suite
- 154 mock-backed unit tests + 20 live smoke tests (skipped without env vars). All green on JDK 21 and 25.

## [1.5.1] - 2026-06-05

### Changed
- Dependency maintenance (Dependabot), all verified green on JDK 21 + 25:
  - JUnit Jupiter 5.11.4 → 6.1.0 (test scope; JUnit 6 requires Java 17+, satisfied by the Java 21 baseline)
  - Jackson Databind 2.18.2 → 2.22.0
  - AssertJ 3.27.3 → 3.27.7
  - maven-compiler-plugin 3.13.0 → 3.15.0, maven-surefire-plugin 3.5.2 → 3.5.6,
    maven-source-plugin 3.3.1 → 3.4.0, maven-javadoc-plugin 3.11.2 → 3.12.0,
    maven-enforcer-plugin 3.5.0 → 3.6.3
  - GitHub Actions: actions/checkout v4.2.2 → v6.0.3, actions/setup-java v4.7.1 → v5.2.0 (SHA-pinned)

### Held
- OkHttp 4.12.0 → 5.x is **not** taken yet: OkHttp 5 publishes as a Kotlin-Multiplatform artifact, so a plain
  Maven build resolves `com.squareup.okhttp3:okhttp:5.x` to a classless KMP root (`package okhttp3 does not
  exist`). Migrating requires switching to the `okhttp-jvm` artifact and adapting to MockWebServer API changes.
  Tracked separately.

## [1.5.0] - 2026-06-05

Full file-by-file audit against https://api.assinafy.com.br/v1/docs, verified live against the sandbox API.
See [docs/AUDIT.md](docs/AUDIT.md) for the detailed audit report.

### Breaking
- **Removed `AssignmentResource.get(documentId, assignmentId, signerAccessCode)`.** It targeted an undocumented
  route that returns HTTP 404 for every call (verified live: a sibling sub-route on the same IDs returns 200,
  and auth failures return 401 — so the 404 is route-nonexistence). Use
  `client.signerSelf.getSign(signerAccessCode)` (`GET /sign`), whose `DocumentDetails` carries the signer-facing
  assignment view.
- **`SignerSelfResource.uploadSignature(...)` now returns `void`** (was `byte[]`). `POST /signature` returns a
  JSON envelope, not an artifact; the method now parses that envelope and raises `ApiException` on an error
  (including an error envelope returned under HTTP 200) instead of returning the raw JSON bytes.

### Fixed
- `AssignmentResource.resetExpiration` now accepts a `null` `expires_at` to clear an assignment's expiration
  (the documented behavior, verified live), and sends `{"expires_at": null}` instead of failing. Added
  `clearExpiration(documentId, assignmentId)` for that intent.
- `AuthenticationResource.resetPassword` no longer requires `token`; the docs mark it optional (it may be
  delivered out-of-band). `email` and `new_password` remain required.
- `FieldResource.validate` no longer throws `NullPointerException` on a `null` value; it forwards
  `{"value": null}` to the API.
- Binary endpoints (document/page/thumbnail/signature download) now surface the server's error message and
  body via `ApiException`, instead of a generic "API request failed with status N".
- `SignerResource.create` duplicate-email recovery now also handles the HTTP 400 the live API returns for a
  duplicate (previously only 409). Removed a redundant `toLowerCase` in `findByEmail`.

### Added
- `current_signer`, `page_count`, and `created_by` fields on `DocumentDetails` (signer-facing and public
  document responses were silently dropping these documented fields).
- `step`, `notified`, and `notification_history` on `Signer` (the assignment-signer fields), plus a new
  `AssignmentSignerNotification` model — exposes sequential-signing order, per-signer notified state, and
  delivery history.
- Opt-in retry: `AssinafyClientOptions.setMaxRetries(int)` retries HTTP 429/503 honoring `Retry-After`.
  `ApiException.getRetryAfterSeconds()` surfaces the server's hint regardless.
- Per-method Javadoc (HTTP verb + path) on every public resource method (previously missing on the Signer,
  Field, Template, Webhook, and most Document methods).
- `docs/EXAMPLES.md` now documents every method with full request and response JSON payloads.

### Changed
- **Java toolchain:** compile target raised from Java 17 to **Java 21**; CI now runs `mvn verify` on a JDK
  **21 + 25** matrix (was a single JDK 25 job that never exercised the bytecode floor). `DocumentPage`/
  `TemplatePage` dimensions are now `int` (the API returns integers).
- **GitHub Actions:** actions pinned to commit SHAs, build matrix added, least-privilege permissions kept.
  Added Dependabot, a tag-triggered `release.yml` (GitHub Packages, `packages: write`), and a `.gitlab-ci.yml`
  for GitLab→GitHub mirror parity.
- **pom.xml:** `distributionManagement` (GitHub Packages), a `release` profile that attaches `-sources`/
  `-javadoc` jars, `maven-enforcer-plugin` (JDK 21+/Maven 3.8+), reproducible-build timestamp, SCM/developer
  metadata, and refreshed dependencies (Jackson 2.18.2, JUnit 5.11.4, AssertJ 3.27.3, Surefire 3.5.2). Added a
  Maven Wrapper (`./mvnw`).
- `LiveSmokeTest` now defaults to the **sandbox** base URL (was production) and honors `ASSINAFY_BASE_URL`;
  expanded to cover the document and field lifecycles and the assignment expiration round-trip.

### Verified Live (sandbox)
- All catalogue/read endpoints, full document lifecycle (upload → process → page/thumbnail/original download →
  delete), field CRUD + validate/validate-multiple, tag CRUD, signer CRUD, assignment create + estimate-cost +
  reset/clear expiration, public-document 404, and the removed-route 404.

### Test Suite
- 120 mock-backed unit tests + 16 live smoke tests (skipped without env vars). All green on JDK 21 and 25.

## [1.4.0] - 2026-05-27

### Added
- Authentication resource for the documented login, social-login, API-key, and password flows:
  `client.auth.login`, `socialLogin`, `getApiKey`, `createApiKey`, `deleteApiKey`,
  `changePassword`, `requestPasswordReset`, and `resetPassword`.
- Workspace tag resource for `GET/POST/PUT/DELETE /accounts/{account_id}/tags`.
- Document tag helpers for listing, replacing, appending, and detaching tags.
- Signer-facing helpers for `GET /sign`, filtered signer document listing, and signer-scoped
  document artifact download.
- Tag parsing on document and template models, `default_document_tags` parsing on template details,
  document-tag support when creating documents from templates, and sequential-signing `step` support.

### Changed
- Maven now emits Java 17 bytecode with `--release 17` for broader SDK compatibility.
- The client can now be constructed without credentials for public/authentication endpoints; authenticated
  endpoints still require API credentials at the API layer.

### Verified Live
- `GET /accounts/{id}/tags`
- Full tag CRUD round-trip (create -> update -> force delete)
- Existing catalogue, document, webhook, template, field, signer CRUD, and public-document smoke checks.

### Test Suite
- 108 mock-backed unit tests + 12 live smoke tests (skipped without env vars). All green.

## [1.3.0] - 2026-05-12

### Added
- **Public document endpoints** (no auth required, used by signer landing pages):
  - `client.documents.getPublic(documentId)` → `GET /public/documents/{id}`
  - `client.documents.sendToken(documentId, recipient, channel)` → `PUT /public/documents/{id}/send-token`
- **Signer-facing assignment operations** (authorised via `signer-access-code`):
  - `client.assignments.get(documentId, assignmentId, signerAccessCode)`
  - `client.assignments.sign(documentId, assignmentId, signerAccessCode, entries)`
  - `client.assignments.decline(documentId, assignmentId, signerAccessCode, reason)`
- **Signer Self-Service multi-document flows**:
  - `client.signerSelf.getCurrentDocument(signerId, signerAccessCode)`
  - `client.signerSelf.listDocuments(signerId, signerAccessCode)`
  - `client.signerSelf.signMultiple(signerAccessCode, documentIds)`
  - `client.signerSelf.declineMultiple(signerAccessCode, documentIds, reason)`
- **Image content-type detection** for `signerSelf.uploadSignature` — auto-selects `image/png` vs
  `image/jpeg` from the file header (was hard-coded to PNG).
- `LiveSmokeTest` JUnit class — runs against the real API when `ASSINAFY_API_KEY` and
  `ASSINAFY_ACCOUNT_ID` env vars are set; skipped otherwise.
- New unit-test coverage for `DocumentResource`, `TemplateResource`, and `SignerSelfResource`.

### Changed
- `BaseResource` query-parameter handling unified — `httpGetBinary`, `httpPostBinary`, `httpPut`, and
  `httpPost` now all support a typed `Map<String, String>` for query strings. SignerSelf and
  Assignment resources no longer hand-concatenate access codes into URLs.
- Resource methods that the API documents as returning an empty payload (`data: []`) — `assignments.sign`,
  `assignments.decline`, `signerSelf.signMultiple`, `signerSelf.declineMultiple` — now have a `void`
  signature and use new `httpPutVoid` / `httpPostVoid` helpers, fixing a latent JSON-deserialisation
  bug that surfaced when those endpoints were called.
- Tightened input validation: `assignments.resetExpiration` rejects blank `expires_at`,
  `signerSelf.uploadSignature` requires non-empty image bytes and validates the `type` argument.
- Resource methods now carry per-method Javadoc citing the exact HTTP verb and path.

### Verified Live
- Authentication via `X-Api-Key`
- `GET /documents/statuses`, `/field-types`, `/webhooks/event-types`
- `GET /accounts/{id}/templates`, `/documents`, `/signers`, `/fields`, `/webhooks`, `/webhooks/subscriptions`
- Full signer CRUD round-trip (create → find by email → update → get → delete)
- 404 envelope parsing on `GET /public/documents/{id}`

### Test Suite
- 91 unit tests + 10 live smoke tests (skipped without env vars). All green.

## [1.2.0] - 2026-05-06

### Added
- Initial Java SDK release
- `AssinafyClient` with documented `DocumentResource`, `SignerResource`, `AssignmentResource`, `FieldResource`, `WebhookResource`, and `TemplateResource`
- `uploadAndRequestSignatures` high-level helper
- Full test suite with `MockWebServer`
- Docker Compose support for running tests in a container
- Support for `X-Pagination-*` response headers for paginated endpoints
- Idempotent signer creation (reuse by email)
