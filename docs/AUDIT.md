# Assinafy SDK Audit

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
