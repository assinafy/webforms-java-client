package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.DocumentDetails;
import com.assinafy.sdk.models.DocumentListItem;
import com.assinafy.sdk.models.DocumentVerification;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.SigningProgress;
import com.assinafy.sdk.models.TemplateSigner;
import com.assinafy.sdk.models.TemplateEditorField;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentResourceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private MockWebServer server;
    private DocumentResource resource;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        resource = new DocumentResource(new OkHttpClient(), server.url("/").toString(), "acc");
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private MockResponse okJson(Object data) throws Exception {
        String body = MAPPER.writeValueAsString(Map.of("status", 200, "data", data));
        return new MockResponse().setBody(body).setHeader("Content-Type", "application/json");
    }

    /** A {@code "data": null} envelope must not become an NPE inside the derived-status helpers. */
    @Test
    void derivedHelpers_tolerateAnEnvelopeWithNoDocumentData() throws Exception {
        MockResponse nullData = new MockResponse()
                .setBody("{\"status\":200,\"message\":\"\",\"data\":null}")
                .setHeader("Content-Type", "application/json");
        server.enqueue(nullData);
        server.enqueue(nullData);

        assertThat(resource.isFullySigned("doc-1")).isFalse();

        SigningProgress progress = resource.getSigningProgress("doc-1");
        assertThat(progress.getTotal()).isZero();
        assertThat(progress.getPercentage()).isZero();
    }

    @Test
    void list_hitsAccountScopedPath() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.list();

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/accounts/acc/documents");
    }

    @Test
    void details_returnsParsedDocument() throws Exception {
        server.enqueue(okJson(Map.of(
                "id", "doc-1",
                "name", "x.pdf",
                "status", "metadata_ready",
                "artifacts", Map.of(
                        "original", "https://example/x.pdf",
                        "pades", "https://example/x-pades.pdf")
        )));

        DocumentDetails d = resource.details("doc-1");

        assertThat(server.takeRequest().getPath()).isEqualTo("/documents/doc-1");
        assertThat(d.getId()).isEqualTo("doc-1");
        assertThat(d.getStatus()).isEqualTo("metadata_ready");
        assertThat(d.getArtifacts().getOriginal()).isEqualTo("https://example/x.pdf");
        assertThat(d.getArtifacts().getPades()).isEqualTo("https://example/x-pades.pdf");
    }

    @Test
    void signingProgressCountsCompletedSignersWhenSummaryIsAbsent() throws Exception {
        server.enqueue(okJson(Map.of("id", "doc-1", "assignment", Map.of("signers", List.of(
                Map.of("id", "signer-1", "completed", true),
                Map.of("id", "signer-2", "completed", false),
                Map.of("id", "signer-3", "completed", true))))));

        SigningProgress progress = resource.getSigningProgress("doc-1");

        assertThat(progress.getSigned()).isEqualTo(2);
        assertThat(progress.getTotal()).isEqualTo(3);
        assertThat(progress.getPending()).isEqualTo(1);
        assertThat(progress.getPercentage()).isEqualTo(66.67);
    }

    @Test
    void fullySignedUsesCompletedSignersWhenSummaryIsAbsent() throws Exception {
        server.enqueue(okJson(Map.of("id", "doc-1", "assignment", Map.of("signers", List.of(
                Map.of("id", "signer-1", "completed", true),
                Map.of("id", "signer-2", "completed", true))))));

        assertThat(resource.isFullySigned("doc-1")).isTrue();
    }

    @Test
    void waitUntilReady_validatesPollingArguments() {
        assertThatThrownBy(() -> resource.waitUntilReady("doc-1", 0, 1))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Maximum wait");
        assertThatThrownBy(() -> resource.waitUntilReady("doc-1", 1, 0))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Poll interval");
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void waitUntilReady_capsSleepAtRemainingTimeout() throws Exception {
        server.enqueue(okJson(Map.of("id", "doc-1", "status", "processing")));
        long started = System.nanoTime();

        assertThatThrownBy(() -> resource.waitUntilReady("doc-1", 20, 1_000))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Timeout");

        long elapsedMs = java.util.concurrent.TimeUnit.NANOSECONDS
                .toMillis(System.nanoTime() - started);
        assertThat(elapsedMs).isLessThan(500);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void delete_hitsCorrectPath() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.delete("doc-1");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("DELETE");
        assertThat(req.getPath()).isEqualTo("/documents/doc-1");
    }

    @Test
    void download_defaultsToCertificatedArtifact() throws Exception {
        server.enqueue(new MockResponse().setBody("PDFBYTES")
                .setHeader("Content-Type", "application/pdf"));

        byte[] bytes = resource.download("doc-1");

        assertThat(server.takeRequest().getPath())
                .isEqualTo("/documents/doc-1/download/certificated");
        assertThat(new String(bytes)).isEqualTo("PDFBYTES");
    }

    @Test
    void download_acceptsCustomArtifact() throws Exception {
        server.enqueue(new MockResponse().setBody("X")
                .setHeader("Content-Type", "application/pdf"));

        resource.download("doc-1", "original");

        assertThat(server.takeRequest().getPath())
                .isEqualTo("/documents/doc-1/download/original");
    }

    @Test
    void artifactEndpointsRejectUnsafePathSegments() {
        assertThatThrownBy(() -> resource.download("../doc-1", "original"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("URL-safe path segment");
        assertThatThrownBy(() -> resource.download("doc-1", "../original"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("URL-safe path segment");
        assertThatThrownBy(() -> resource.downloadPage("doc-1", "page/1"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("URL-safe path segment");
        for (String unsafe : List.of(".", "..", "page?x=1", "page#fragment", "page%2Fchild")) {
            assertThatThrownBy(() -> resource.downloadPage("doc-1", unsafe))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("URL-safe path segment");
        }
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void downloadPage_buildsPath() throws Exception {
        server.enqueue(new MockResponse().setBody("X")
                .setHeader("Content-Type", "image/png"));

        resource.downloadPage("doc-1", "page-1");

        assertThat(server.takeRequest().getPath())
                .isEqualTo("/documents/doc-1/pages/page-1/download");
    }

    @Test
    void thumbnail_buildsPath() throws Exception {
        server.enqueue(new MockResponse().setBody("X")
                .setHeader("Content-Type", "image/png"));

        resource.thumbnail("doc-1");

        assertThat(server.takeRequest().getPath()).isEqualTo("/documents/doc-1/thumbnail");
    }

    @Test
    void statuses_returnsList() throws Exception {
        server.enqueue(okJson(List.of(
                Map.of("code", "uploaded", "deletable", false),
                Map.of("code", "certificated", "deletable", false)
        )));

        var statuses = resource.statuses();

        assertThat(server.takeRequest().getPath()).isEqualTo("/documents/statuses");
        assertThat(statuses).hasSize(2);
        assertThat(statuses.get(0).getCode()).isEqualTo("uploaded");
    }

    @Test
    void activities_returnsList() throws Exception {
        server.enqueue(okJson(List.of(Map.of("id", 1, "event", "signature_requested"))));

        var activities = resource.activities("doc-1");

        assertThat(server.takeRequest().getPath())
                .isEqualTo("/documents/doc-1/activities");
        assertThat(activities).hasSize(1);
    }

    @Test
    void createFromTemplate_serializesPayload() throws Exception {
        server.enqueue(okJson(Map.of("id", "doc-1", "name", "x.pdf")));

        resource.createFromTemplate("template-1",
                List.of(new TemplateSigner("role-1", "signer-1")
                        .setVerificationMethod("Email")
                        .setNotificationMethods(List.of("Email"))));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath())
                .isEqualTo("/accounts/acc/templates/template-1/documents");
        String body = req.getBody().readUtf8();
        assertThat(body).contains("\"role_id\":\"role-1\"");
        assertThat(body).contains("\"id\":\"signer-1\"");
        assertThat(body).contains("\"verification_method\":\"Email\"");
    }

    @Test
    void createFromTemplate_includesOptionalDocumentTags() throws Exception {
        server.enqueue(okJson(Map.of("id", "doc-1", "name", "x.pdf")));

        resource.createFromTemplate("template-1", List.of(new TemplateSigner("role-1", "signer-1")),
                new com.assinafy.sdk.models.CreateDocumentFromTemplateOptions()
                        .setTags(List.of("Contracts", "2026-Q1")), null);

        String body = server.takeRequest().getBody().readUtf8();
        assertThat(body).contains("\"tags\"", "Contracts", "2026-Q1");
    }

    @Test
    void createFromTemplate_serializesTypedEditorFields() throws Exception {
        server.enqueue(okJson(Map.of("id", "doc-1", "name", "x.pdf")));

        resource.createFromTemplate("template-1", List.of(new TemplateSigner("role-1", "signer-1")),
                new com.assinafy.sdk.models.CreateDocumentFromTemplateOptions()
                        .setEditorFields(List.of(new TemplateEditorField("field-1", "Approved"))));

        assertThat(server.takeRequest().getBody().readUtf8())
                .contains("\"editor_fields\":[{\"field_id\":\"field-1\",\"value\":\"Approved\"}]");
    }

    @Test
    void templateEditorFieldValidatesRequiredValues() {
        assertThatThrownBy(() -> new TemplateEditorField("", "value"))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThatThrownBy(() -> new TemplateEditorField("field-1", null))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
    }

    @Test
    void createFromTemplate_requiresSignerId() {
        assertThatThrownBy(() -> resource.createFromTemplate("template-1",
                List.of(new TemplateSigner("role-1"))))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("signer ID");
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void estimateTemplateCost_acceptsRoleWithoutSignerId() throws Exception {
        server.enqueue(okJson(Map.of("has_sufficient_resources", true, "total", 1)));

        resource.estimateCostFromTemplate("template-1", List.of(new TemplateSigner("role-1")));

        RecordedRequest request = server.takeRequest();
        assertThat(request.getPath())
                .isEqualTo("/accounts/acc/templates/template-1/documents/estimate-cost");
        assertThat(request.getBody().readUtf8())
                .contains("\"role_id\":\"role-1\"")
                .doesNotContain("\"id\"");
    }

    @Test
    void documentTagMethodsUseDocumentedEndpoints() throws Exception {
        server.enqueue(okJson(List.of(Map.of("id", "tag-1", "name", "Contracts"))));
        server.enqueue(okJson(List.of(Map.of("id", "tag-2", "name", "Urgent"))));
        server.enqueue(okJson(List.of()));
        server.enqueue(okJson(Map.of("detached", true)));

        var listed = resource.listTags("doc-1");
        var appended = resource.appendTags("doc-1", List.of("Urgent"));
        var replaced = resource.replaceTags("doc-1", List.of());
        boolean detached = resource.detachTag("doc-1", "tag-1");

        assertThat(listed.get(0).getName()).isEqualTo("Contracts");
        assertThat(appended.get(0).getName()).isEqualTo("Urgent");
        assertThat(replaced).isEmpty();
        assertThat(detached).isTrue();

        assertThat(server.takeRequest().getPath()).isEqualTo("/accounts/acc/documents/doc-1/tags");
        RecordedRequest append = server.takeRequest();
        assertThat(append.getMethod()).isEqualTo("POST");
        assertThat(append.getPath()).isEqualTo("/accounts/acc/documents/doc-1/tags");
        RecordedRequest replace = server.takeRequest();
        assertThat(replace.getMethod()).isEqualTo("PUT");
        assertThat(replace.getBody().readUtf8()).contains("\"tags\":[]");
        RecordedRequest detach = server.takeRequest();
        assertThat(detach.getMethod()).isEqualTo("DELETE");
        assertThat(detach.getPath()).isEqualTo("/accounts/acc/documents/doc-1/tags/tag-1");
    }

    @Test
    void documentTagMethodsValidateInputs() {
        assertThatThrownBy(() -> resource.appendTags("doc-1", List.of()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.replaceTags("doc-1", null))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.detachTag("doc-1", ""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void verify_callsHashPathAndParsesTypedResult() throws Exception {
        server.enqueue(okJson(Map.of(
                "hash", "hash-abc",
                "status", "certificated",
                "is_valid", true,
                "page_count", "1",
                "signer_count", "1",
                "message", "")));

        DocumentVerification result = resource.verify("hash-abc");

        assertThat(server.takeRequest().getPath()).isEqualTo("/documents/hash-abc/verify");
        assertThat(result.getIsValid()).isTrue();
        assertThat(result.getHash()).isEqualTo("hash-abc");
        assertThat(result.getPageCount()).isEqualTo("1");
    }

    @Test
    void rename_patchesDocumentName() throws Exception {
        server.enqueue(okJson(Map.of("id", "doc-1", "name", "Renamed.pdf", "resource", "document")));

        DocumentDetails d = resource.rename("doc-1", "Renamed.pdf");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("PATCH");
        assertThat(req.getPath()).isEqualTo("/documents/doc-1");
        assertThat(req.getBody().readUtf8()).isEqualTo("{\"name\":\"Renamed.pdf\"}");
        assertThat(d.getName()).isEqualTo("Renamed.pdf");
    }

    @Test
    void rename_validatesName() {
        assertThatThrownBy(() -> resource.rename("doc-1", " "))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.rename("doc-1", "x".repeat(256)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void search_hitsSearchEndpointWithParams() throws Exception {
        server.enqueue(okJson(List.of(Map.of("id", "doc-1", "name", "contract.pdf"))));

        PaginatedResult<DocumentListItem> page = resource.search(Map.of("search", "contract"));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/accounts/acc/documents/search?search=contract");
        assertThat(page.getData().get(0).getName()).isEqualTo("contract.pdf");
    }

    @Test
    void upload_postsMultipartFileToAccountDocuments() throws Exception {
        server.enqueue(okJson(Map.of("id", "doc-1", "name", "sample.pdf", "status", "uploaded")));

        resource.upload("%PDF-1.4 test".getBytes(), "sample.pdf");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath()).isEqualTo("/accounts/acc/documents");
        assertThat(req.getHeader("Content-Type")).startsWith("multipart/form-data");
        String body = req.getBody().readUtf8();
        assertThat(body).contains("name=\"file\"");
        assertThat(body).contains("filename=\"sample.pdf\"");
    }

    @Test
    void upload_validatesInput() {
        assertThatThrownBy(() -> resource.upload(new byte[0], "x.pdf"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.upload("data".getBytes(), "notes.txt"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getPublic_hitsPublicPathAndParsesPageCountAndCreatedBy() throws Exception {
        server.enqueue(okJson(Map.of(
                "resource", "document",
                "id", "doc-1",
                "name", "x.pdf",
                "page_count", "1",
                "created_by", "John Smith"
        )));

        DocumentDetails d = resource.getPublic("doc-1");

        assertThat(server.takeRequest().getPath()).isEqualTo("/public/documents/doc-1");
        assertThat(d.getId()).isEqualTo("doc-1");
        assertThat(d.getPageCount()).isEqualTo("1");
        assertThat(d.getCreatedBy()).isEqualTo("John Smith");
    }

    @Test
    void download_surfacesErrorEnvelopeMessageOnFailure() throws Exception {
        // Binary endpoints must surface the API's error message, not a generic "status 404".
        String body = "{\"status\":404,\"data\":null,\"message\":\"Artefato não está disponível.\"}";
        server.enqueue(new MockResponse().setResponseCode(404).setBody(body)
                .setHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> resource.download("doc-1", "certificated"))
                .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class)
                .hasMessageContaining("Artefato não está disponível.");
    }

    @Test
    void apiException_capturesRetryAfterOn429() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(429)
                .setBody(MAPPER.writeValueAsString(Map.of("status", 429, "message", "Too Many Requests")))
                .setHeader("Content-Type", "application/json")
                .setHeader("Retry-After", "30"));

        try {
            resource.statuses();
            throw new AssertionError("expected ApiException");
        } catch (com.assinafy.sdk.exceptions.ApiException e) {
            assertThat(e.getStatusCode()).isEqualTo(429);
            assertThat(e.getRetryAfterSeconds()).isEqualTo(30);
        }
    }

    @Test
    void apiException_derivesRetryAfterFromRateLimitResetOn429() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(429)
                .setBody("{\"status\":429,\"message\":\"Too Many Requests\"}")
                .setHeader("Content-Type", "application/json")
                .setHeader("X-Rate-Limit-Reset", "42"));

        try {
            resource.statuses();
            throw new AssertionError("expected ApiException");
        } catch (com.assinafy.sdk.exceptions.ApiException e) {
            assertThat(e.getRetryAfterSeconds()).isEqualTo(42);
        }
    }

    @Test
    void apiException_doesNotPopulateRetryAfterOnPermanent400() throws Exception {
        // TX-1 guard: X-Rate-Limit-Reset is present on non-retryable errors too; it must NOT become a retry hint.
        server.enqueue(new MockResponse().setResponseCode(400)
                .setBody("{\"status\":400,\"data\":null,\"message\":\"Bad request\"}")
                .setHeader("Content-Type", "application/json")
                .setHeader("X-Rate-Limit-Reset", "31"));

        try {
            resource.statuses();
            throw new AssertionError("expected ApiException");
        } catch (com.assinafy.sdk.exceptions.ApiException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
            assertThat(e.getRetryAfterSeconds()).isNull();
        }
    }

    @Test
    void httpErrorWinsOverSuccessfulEnvelopeStatus() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500)
                .setBody("{\"status\":200,\"data\":[],\"message\":\"Backend failed\"}")
                .setHeader("Content-Type", "application/json"));

        try {
            resource.statuses();
            throw new AssertionError("expected ApiException");
        } catch (com.assinafy.sdk.exceptions.ApiException e) {
            assertThat(e.getStatusCode()).isEqualTo(500);
            assertThat(e.getMessage()).isEqualTo("Backend failed");
        }
    }

    @Test
    void plainTextHttpErrorRemainsApiException() {
        server.enqueue(new MockResponse().setResponseCode(503).setBody("Service unavailable"));

        try {
            resource.statuses();
            throw new AssertionError("expected ApiException");
        } catch (com.assinafy.sdk.exceptions.ApiException e) {
            assertThat(e.getStatusCode()).isEqualTo(503);
        }
    }

    @Test
    void unresolvedRedirectRemainsApiException() {
        server.enqueue(new MockResponse().setResponseCode(302)
                .setBody("{\"status\":200,\"data\":[]}"));

        try {
            resource.statuses();
            throw new AssertionError("expected ApiException");
        } catch (com.assinafy.sdk.exceptions.ApiException e) {
            assertThat(e.getStatusCode()).isEqualTo(302);
        }
    }

    @Test
    void delete_surfacesErrorEnvelopeUnderHttp200() throws Exception {
        // TX-3 guard: an error envelope returned under HTTP 200 on the void path must not be swallowed.
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("{\"status\":403,\"data\":null,\"message\":\"Forbidden\"}")
                .setHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> resource.delete("doc-1"))
                .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class)
                .hasMessageContaining("Forbidden");
    }

    @Test
    void download_surfacesErrorEnvelopeUnderHttp200() throws Exception {
        // TX-3 guard: a JSON error envelope under HTTP 200 on the binary path must raise, not return bytes.
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("{\"status\":404,\"data\":null,\"message\":\"Artefato não está disponível.\"}")
                .setHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> resource.download("doc-1", "certificated"))
                .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class)
                .hasMessageContaining("Artefato não está disponível.");
    }

    @Test
    void downloadRejectsSuccessfulJsonAndStructuredJsonMediaTypes() {
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("{\"status\":200,\"data\":{}}")
                .setHeader("Content-Type", "application/problem+json"));

        assertThatThrownBy(() -> resource.download("doc-1", "certificated"))
                .isInstanceOf(com.assinafy.sdk.exceptions.NetworkException.class)
                .hasMessageContaining("Expected a binary response");
    }

    @Test
    void sendToken_putsBody() throws Exception {
        server.enqueue(okJson(Map.of("channel", "email", "recipient", "signer@example.com")));

        resource.sendToken("doc-1", "signer@example.com", "email");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("PUT");
        assertThat(req.getPath()).isEqualTo("/public/documents/doc-1/send-token");
        String body = req.getBody().readUtf8();
        assertThat(body).contains("\"recipient\":\"signer@example.com\"");
        assertThat(body).contains("\"channel\":\"email\"");
    }

    @Test
    void sendToken_validatesArguments() {
        assertThatThrownBy(() -> resource.sendToken("doc-1", null, "email"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.sendToken("doc-1", "signer@example.com", ""))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.sendToken("doc-1", "signer@example.com", "sms"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void list_passesCustomAccountIdInPath() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.list(Map.of("page", "2"), "custom-acc");

        assertThat(server.takeRequest().getPath())
                .isEqualTo("/accounts/custom-acc/documents?page=2");
    }

    @Test
    void list_normalisesPerPageQuery() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.list(Map.of("per_page", "5"));

        assertThat(server.takeRequest().getPath()).contains("per-page=5");
    }
}
