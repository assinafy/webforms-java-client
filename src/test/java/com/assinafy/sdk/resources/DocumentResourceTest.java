package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.DocumentDetails;
import com.assinafy.sdk.models.TemplateSigner;
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
                "artifacts", Map.of("original", "https://example/x.pdf")
        )));

        DocumentDetails d = resource.details("doc-1");

        assertThat(server.takeRequest().getPath()).isEqualTo("/documents/doc-1");
        assertThat(d.getId()).isEqualTo("doc-1");
        assertThat(d.getStatus()).isEqualTo("metadata_ready");
        assertThat(d.getArtifacts().getOriginal()).isEqualTo("https://example/x.pdf");
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
    void documentTagMethodsUseDocumentedEndpoints() throws Exception {
        server.enqueue(okJson(List.of(Map.of("id", "tag-1", "name", "Contracts"))));
        server.enqueue(okJson(List.of(Map.of("id", "tag-2", "name", "Urgent"))));
        server.enqueue(okJson(List.of()));
        server.enqueue(okJson(Map.of("detached", true)));

        var listed = resource.listTags("doc-1");
        var appended = resource.appendTags("doc-1", List.of("Urgent"));
        var replaced = resource.replaceTags("doc-1", List.of());
        Map<String, Object> detached = resource.detachTag("doc-1", "tag-1");

        assertThat(listed.get(0).getName()).isEqualTo("Contracts");
        assertThat(appended.get(0).getName()).isEqualTo("Urgent");
        assertThat(replaced).isEmpty();
        assertThat(detached).containsEntry("detached", true);

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
    void verify_callsHashPath() throws Exception {
        server.enqueue(okJson(Map.of("is_valid", true)));

        Map<String, Object> result = resource.verify("hash-abc");

        assertThat(server.takeRequest().getPath()).isEqualTo("/documents/hash-abc/verify");
        assertThat(result).containsEntry("is_valid", true);
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
    void sendToken_putsBody() throws Exception {
        server.enqueue(okJson(Map.of("channel", "email", "recipient", "a@b.com")));

        resource.sendToken("doc-1", "a@b.com", "email");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("PUT");
        assertThat(req.getPath()).isEqualTo("/public/documents/doc-1/send-token");
        String body = req.getBody().readUtf8();
        assertThat(body).contains("\"recipient\":\"a@b.com\"");
        assertThat(body).contains("\"channel\":\"email\"");
    }

    @Test
    void sendToken_validatesArguments() {
        assertThatThrownBy(() -> resource.sendToken("doc-1", null, "email"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.sendToken("doc-1", "a@b.com", ""))
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
