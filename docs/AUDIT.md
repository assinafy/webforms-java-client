# Assinafy SDK Audit

## Audit 2026-07-19 (v2.0.0)

Source of truth, in priority order: (1) **live sandbox** behavior (`https://sandbox.assinafy.com.br/v1`),
(2) the machine-readable **OpenAPI 3 spec** (`https://api.assinafy.com.br/v1/docs/openapi.json` — 65 paths / 86
operations / 37 schemas), (3) the rendered docs. When live behavior contradicted the spec, live won and the
contradiction was recorded.

### Method

The full OpenAPI spec was pulled and every one of the 86 operations was catalogued and mapped to an SDK method.
The SDK was then audited file-by-file across 14 domains (each API area plus the cross-cutting transport, models,
build/CI, and docs), comparing **code ↔ spec ↔ live** for each endpoint. Each finding was verified against the
live sandbox with disposable, self-cleaning data (create-then-delete under a unique prefix), and each material
finding was independently re-checked adversarially. Coverage delta this round vs. the API: **6 documented
endpoints were missing** and are now implemented; **1 method targeted a non-existent route** and was removed;
**5 methods returned untyped `Map`** and are now typed. All changes are verified by 154 mock unit tests and 20
live smoke tests (green on JDK 21 + 25).

### Confirmed findings and resolutions

| Severity | Area | Finding | Resolution |
|---|---|---|---|
| High | Signer self | `acceptTerms`/`verifyEmail` sent the `signer-access-code` in the JSON body; the API requires it as a query parameter (would 401 in production). The unit tests enshrined the wrong placement. | Send it as `?signer-access-code=`; `verifyEmail` body is only `{"verification-code"}`. Tests corrected. |
| High | Webhooks | `deleteSubscription()` hit `DELETE .../webhooks/subscriptions`, which the API does not route (live: router-level 404, vs controller 404 on routed methods) — could never succeed. | **Removed.** Use `inactivate()`; there is no hard-delete server-side. |
| High | Signer self | `ConfirmSignerDataPayload` used `whatsapp_phone_number`/`has_accepted_terms`; the endpoint's schema is `full_name`/`email`/`government_id`, and the returned `Signer` was discarded. | Corrected the payload fields; `confirmSignerData` now returns `Signer`. |
| High | Documents | `PATCH /documents/{id}` (rename) and `GET .../documents/search` were unimplemented (both live-verified 200). | Added `documents.rename(...)` (+ a `httpPatch` transport helper) and `documents.search(...)`. |
| High | Assignments | `GET /assignments` (list) was unimplemented; live requires the account context as the camelCase `?accountId=`. | Added `assignments.list(...)` sending `accountId` literally. |
| Medium | Fields | `validate-multiple` dropped a `null` value (`@JsonInclude(NON_NULL)`) → API 400, inconsistent with single `validate(null)`. | Removed the annotation so `{"value": null}` is always serialized. |
| Medium | Transport | `executeVoid`/`executeBinary` didn't inspect the envelope on HTTP 200, so a `200 + error-envelope` (e.g. a failed download) was swallowed/returned as the artifact. | Both paths now raise `ApiException` on an error envelope even under HTTP 2xx. |
| Medium | Transport | `getRetryAfterSeconds()` was populated on every error via the always-present `X-Rate-Limit-Reset`, wrongly signalling a retry on permanent 400/401. | Only attached on retryable statuses (429/503). |
| Medium | Models | `estimateCost`, `estimateCostFromTemplate`, `estimateResendCost`, `resendNotification`, `verify` returned untyped `Map`. | Added typed `CostEstimate` (+`CostEstimateBreakdownItem`), `ResendCostEstimate`, `ResendResult`, `DocumentVerification`. |
| Medium | Auth | `POST /auth/link-social-login` was unimplemented (live: route exists). | Added `auth.linkSocialLogin(provider, token)`. |
| Medium | Signer self | `GET /signers/{id}/documents/search` unimplemented; `is_signature_reusable` (from `/signers/self`) dropped; `uploadSignature` omitted the `reuse` query param. | Added `searchDocuments(...)`, `Signer.getSignatureReusable()`, and a `reuse` overload. |
| Low | Tags/Docs | `tags.delete` / `documents.detachTag` / `documents.sendToken` returned an opaque `Map`; `WebhookSubscription` exposed never-populated `id`/`createdAt`. | Return `void`; removed the dead getters. |
| Low | Docs | README/EXAMPLES falsely claimed the API-key endpoints require a token client (`getApiKey` works with an API key, verified live); Javadoc gaps on filters/payloads. | Corrected the docs; enriched Javadoc; documented the stale-spec `send-token` body and the omitted OAuth flows. |

### Verified not-a-bug (kept after live testing)

- **`templates.get(templateId)`** (`GET /accounts/{id}/templates/{id}`) is **absent from the spec** but is a
  real controller live (resource-level `"Template não encontrado."` 404, not the router-level 404). Unlike the
  phantom `assignments.get()` removed in 1.5.0, this route exists — **kept**.
- **`documents.sendToken`** sends `{recipient, channel}` and **document-tag** append/replace accept tag *names*:
  both contradict the spec but match live behavior — the SDK is correct, the spec is stale.
- **`create/update` field payloads** accept more fields than the documented body (e.g. `is_active` on create,
  `type`/`is_required` on update) — live honors them, so the broader payloads are correct.

### Intentionally out of scope

Account/user/logo/theme management (`/accounts` CRUD, `/accounts/{id}/logo`, `/accounts/{id}/theme` — 9 live
endpoints) is deliberately not wrapped: this SDK stays focused on the webforms document-signing flows. The
browser-redirect OAuth endpoints (`GET /auth/authenticate`, `GET /login-callback`) are front-end redirects, not
JSON APIs, and are likewise omitted.

### Verification

```bash
mvn clean test          # 154 unit tests, 0 failures (JDK 21 and 25)
ASSINAFY_API_KEY=... ASSINAFY_ACCOUNT_ID=... ASSINAFY_BASE_URL=https://sandbox.assinafy.com.br/v1 \
  mvn test -Dtest=LiveSmokeTest   # 20 live tests, 0 failures
```

Live coverage this round added: document rename (PATCH), lightweight search, assignments list (camelCase
`accountId`), and typed public verify — on top of the existing document/field/tag/signer lifecycles and the
assignment create + reset/clear-expiration round-trip.

---

## Audit 2026-06-05 (v1.5.0)

Source of truth: https://api.assinafy.com.br/v1/docs (rendered to markdown and parsed endpoint-by-endpoint).
Live verification: https://sandbox.assinafy.com.br/v1.

### Method

Every documented endpoint (~80) was catalogued from the API reference. The SDK was then audited file-by-file
across 14 areas — each API domain (authentication, signer, signer self-service, document, template, tag,
assignment, signer documents, field, webhooks) plus four cross-cutting areas (transport/envelope/error
handling, all models, build/CI tooling, and documentation). Each finding was independently re-verified against
the docs and, where a read-only call could decide it, against the live sandbox. Findings requiring a write
(create/expiration/404 route checks) were confirmed live with disposable sandbox data.

### Confirmed findings and resolutions

| Severity | Area | Finding | Resolution |
|---|---|---|---|
| Critical | Assignment | `assignments.get()` hit an undocumented route returning 404 for every call (verified live; sibling sub-route on same IDs returns 200, auth failures return 401). | Removed; callers use `signerSelf.getSign()` (`GET /sign`). |
| High | Assignment | `resetExpiration` rejected `null`, but the API accepts `null` to clear the expiration (verified live). | Accept `null`; send `{"expires_at": null}`; added `clearExpiration(...)`. |
| High | Tooling | CI built only on JDK 25 while the pom targeted Java 17 — the bytecode floor was never exercised. | Target **Java 21**; CI matrix on JDK 21 + 25. |
| High | Docs | README/EXAMPLES had zero request/response JSON payloads; Javadoc verb+path missing on 5 resources. | EXAMPLES rewritten with real payloads; verb+path Javadoc added to every public method. |
| Medium | Auth | `resetPassword` over-required `token` (docs mark it optional). | Made `token` optional. |
| Medium | Signer | Idempotent create silently dropped updated fields; recovery only caught 409 (live returns 400). | Documented semantics; recovery now also handles 400. |
| Medium | Signer self | `uploadSignature` returned raw JSON-envelope bytes and ignored envelope errors. | Returns `void`; parses envelope; raises `ApiException` on error. |
| Medium | Document / models | Public lookup dropped `page_count`/`created_by`; signer-facing doc dropped `current_signer`. | Added the fields to `DocumentDetails`. |
| Medium | Assignment / models | `Signer` (used for `assignment.signers`) dropped `step`, `notified`, `notification_history`. | Added the fields + `AssignmentSignerNotification` model. |
| Medium | Transport | Binary download errors discarded the API error envelope; no 429/`Retry-After` handling. | Binary errors now surface message/body; `ApiException.getRetryAfterSeconds()` + opt-in `maxRetries`. |
| Medium | Tooling | Actions on mutable tags; no source/javadoc jars; no publish/release workflow; smoke test defaulted to production. | SHA-pinned actions + Dependabot; `release` profile + `release.yml` + `distributionManagement`; smoke test defaults to sandbox. |
| Low | Field / models | `validate(null)` threw NPE; page dimensions typed `double` though the API returns integers. | `validate` forwards `{"value": null}`; dimensions are `int`. |

No false positives survived verification (every reported discrepancy was reproduced against the docs and/or the
live API). Two intentionally-omitted endpoints were re-confirmed: `POST/PUT /accounts/{id}/templates` remain
prose-only in the docs with no request contract, so no guessed upload/update methods were added.

### Verification

```bash
mvn clean test          # 120 unit tests, 0 failures (JDK 21 and 25)
ASSINAFY_API_KEY=... ASSINAFY_ACCOUNT_ID=... ASSINAFY_BASE_URL=https://sandbox.assinafy.com.br/v1 \
  mvn test -Dtest=LiveSmokeTest   # 16 live tests, 0 failures
```

Live coverage included the full document lifecycle (upload → process → download original/thumbnail/page →
delete), field CRUD + validate, tag CRUD, signer CRUD, assignment create + estimate-cost + reset/clear
expiration, public-document 404 envelope parsing, and the now-removed 404 route.

---

## Audit 2026-05-27 (v1.4.0)

Audit date: 2026-05-27

Source of truth: https://api.assinafy.com.br/v1/docs

## Summary

The SDK was audited against the rendered Assinafy v1 API documentation and the current Java codebase.
Missing concrete endpoint coverage was added for authentication, workspace tags, document tags,
signer-facing document lookup/download, filtered signer document listing, template-created document
tags, and sequential signing steps.

The current docs mention template `POST /accounts/{account_id}/templates` and
`PUT /accounts/{account_id}/templates/{template_id}` only in object-shape prose and do not document a
request contract. No typed template upload/update methods were added for those under-specified prose-only
mentions to avoid shipping untested behavior with guessed payloads.

## Endpoint Coverage

| API area | SDK coverage |
|---|---|
| Authentication | `client.auth.login`, `socialLogin`, `getApiKey`, `createApiKey`, `deleteApiKey`, `changePassword`, `requestPasswordReset`, `resetPassword` |
| Documents | Upload, list, statuses, details, delete, artifacts, thumbnail, page download, activities, verification, public info, send token |
| Document tags | `client.documents.listTags`, `replaceTags`, `appendTags`, `detachTag` |
| Signers | Create, list, get, update, delete, find by email |
| Signer self-service | Self profile, terms, email verification, confirm data, signature upload/download, `GET /sign`, signer documents, signer document download, multi-sign, multi-decline |
| Assignments | Estimate cost, create virtual/collect, signer get/sign/decline, resend, estimate resend, reset expiration, WhatsApp notifications, sequential steps |
| Field definitions | Create, list, get, update, delete, validate, validate multiple, list field types |
| Templates | List, get, create document from template, estimate cost from template, template tags/default document tags parsed |
| Tags | List, create, update/clear color, delete with optional force |
| Webhooks | Subscription get/update/delete/inactivate, event types, dispatch list, retry dispatch |

## File Review Notes

| File area | Result |
|---|---|
| `pom.xml` | Updated to Java 17 bytecode via `--release 17`; cleaner production baseline for a public SDK. |
| `AssinafyClient` | Added `auth` and `tags`; unauthenticated construction now supports public/login flows while authenticated endpoints still fail at the API layer without credentials. |
| `BaseResource` | Added typed PUT/DELETE helpers with query support and `TypeReference` parsing to avoid raw casts. |
| `DocumentResource` | Added document tag endpoints and template-created document tags; converted map responses to typed parsing. |
| `SignerSelfResource` | Added documented `GET /sign`, filtered signer document listing, and signer-scoped document artifact download. |
| `AssignmentResource` | Added `step` serialization for sequential signing. |
| `AuthenticationResource` | New resource for documented auth, API-key, and password endpoints. |
| `TagResource` | New resource for documented workspace tag endpoints. |
| Models | Added auth, account, user, tag, and tag payload models; added tags/default-document-tags parsing and `step` support. |
| Tests | Added mock-backed coverage for auth, tags, document tags, signer-facing additions, template tag parsing, and sequential steps. |
| Docs | Updated README, examples, installation, changelog, and this audit. |

## Verification

Local clean build:

```bash
mvn clean test
```

Result: 120 tests, 0 failures, 0 errors, 12 live tests skipped by default.

Live smoke:

```bash
ASSINAFY_API_KEY=... ASSINAFY_ACCOUNT_ID=... mvn test -Dtest=LiveSmokeTest
```

Result: 12 tests, 0 failures, 0 errors, 0 skipped. The live suite covered catalogue/read endpoints,
webhook/document/template/field/tag lists, public document 404 envelope parsing, and isolated signer and
tag create-update-delete round-trips.
