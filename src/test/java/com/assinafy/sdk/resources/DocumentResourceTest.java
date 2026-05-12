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
    void verify_callsHashPath() throws Exception {
        server.enqueue(okJson(Map.of("is_valid", true)));

        Map<String, Object> result = resource.verify("hash-abc");

        assertThat(server.takeRequest().getPath()).isEqualTo("/documents/hash-abc/verify");
        assertThat(result).containsEntry("is_valid", true);
    }

    @Test
    void getPublic_hitsPublicPath() throws Exception {
        server.enqueue(okJson(Map.of("id", "doc-1", "name", "x.pdf")));

        DocumentDetails d = resource.getPublic("doc-1");

        assertThat(server.takeRequest().getPath()).isEqualTo("/public/documents/doc-1");
        assertThat(d.getId()).isEqualTo("doc-1");
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
