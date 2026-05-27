# Changelog

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
