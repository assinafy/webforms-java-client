# Changelog

## [1.2.0] - 2026-05-06

### Added
- Initial Java SDK release
- `AssinafyClient` with documented `DocumentResource`, `SignerResource`, `AssignmentResource`, `FieldResource`, `WebhookResource`, and `TemplateResource`
- `uploadAndRequestSignatures` high-level helper
- Full test suite with `MockWebServer`
- Docker Compose support for running tests in a container
- Support for `X-Pagination-*` response headers for paginated endpoints
- Idempotent signer creation (reuse by email)
