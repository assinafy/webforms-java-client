package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.CreateAssignmentPayload;
import com.assinafy.sdk.models.SignerRef;
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

class AssignmentResourceTest {

    private MockWebServer server;
    private AssignmentResource resource;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        resource = new AssignmentResource(new OkHttpClient(), server.url("/").toString(), "acc");
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
    void create_postsToCorrectUrl() throws Exception {
        server.enqueue(okJson(Map.of("id", "assignment-1")));

        CreateAssignmentPayload payload = new CreateAssignmentPayload().setSignerStrings("s1", "s2");
        Assignment result = resource.create("doc-1", payload);

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/documents/doc-1/assignments");
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(result.getId()).isEqualTo("assignment-1");
    }

    @Test
    void create_sendsNormalisedBody() throws Exception {
        server.enqueue(okJson(Map.of("id", "a1")));

        resource.create("doc-1", new CreateAssignmentPayload().setSignerStrings("s1", "s2"));

        RecordedRequest req = server.takeRequest();
        String body = req.getBody().readUtf8();
        assertThat(body).contains("\"signers\"");
        assertThat(body).contains("\"s1\"");
        assertThat(body).contains("\"s2\"");
    }

    @Test
    void create_includesOptionalFields() throws Exception {
        server.enqueue(okJson(Map.of("id", "a1")));

        CreateAssignmentPayload payload = new CreateAssignmentPayload()
                .setSignerStrings("s1")
                .setMessage("hi")
                .setExpiresAt("2024-12-31")
                .setCopyReceivers(List.of("copy@example.com"));
        resource.create("doc-1", payload);

        RecordedRequest req = server.takeRequest();
        String body = req.getBody().readUtf8();
        assertThat(body).contains("\"message\":\"hi\"");
        assertThat(body).contains("\"expires_at\":\"2024-12-31\"");
        assertThat(body).contains("\"copy_receivers\"");
    }

    @Test
    void create_throwsOnEmptySigners() {
        assertThatThrownBy(() -> resource.create("doc-1", new CreateAssignmentPayload()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_requiresDocumentId() {
        assertThatThrownBy(() -> resource.create("", new CreateAssignmentPayload().setSignerStrings("s1")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_parsesDocumentedAssignmentShape() throws Exception {
        server.enqueue(okJson(Map.of(
                "id", "a1",
                "copy_receivers", List.of(Map.of("id", "copy-1", "full_name", "Copy Receiver")),
                "items", List.of(Map.of(
                        "id", "item-1",
                        "page", Map.of("id", "page-1", "number", 1),
                        "signer", Map.of("id", "signer-1", "full_name", "Signer"),
                        "field", Map.of("id", "field-1", "name", "Signature", "type", "signature"),
                        "display_settings", Map.of("top", 10),
                        "completed", false
                )),
                "summary", Map.of(
                        "signer_count", 1,
                        "completed_count", 0,
                        "signers", List.of(Map.of("id", "signer-1", "completed", false))
                ),
                "signing_urls", List.of(Map.of("signer_id", "signer-1", "url", "https://example.com/sign"))
        )));

        Assignment result = resource.create("doc-1", new CreateAssignmentPayload().setSignerStrings("signer-1"));

        assertThat(result.getCopyReceivers()).hasSize(1);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getSummary().getSigners()).hasSize(1);
        assertThat(result.getSigningUrls().get(0).getSignerId()).isEqualTo("signer-1");
    }

    @Test
    void create_acceptsSignerRefObjects() throws Exception {
        server.enqueue(okJson(Map.of("id", "a1")));

        CreateAssignmentPayload payload = new CreateAssignmentPayload()
                .setSigners(List.of(SignerRef.of("s1"), new SignerRef().setId("s2")));
        resource.create("doc-1", payload);

        RecordedRequest req = server.takeRequest();
        String body = req.getBody().readUtf8();
        assertThat(body).contains("\"s1\"");
        assertThat(body).contains("\"s2\"");
    }

    @Test
    void resendNotification_requiresAllThreeIds() {
        assertThatThrownBy(() -> resource.resendNotification("", "a", "s"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.resendNotification("d", "", "s"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.resendNotification("d", "a", ""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void estimateCost_acceptsSignerDescriptorsWithoutIds() throws Exception {
        server.enqueue(okJson(Map.of("total_credits", 0.45)));

        CreateAssignmentPayload payload = new CreateAssignmentPayload()
                .setSigners(List.of(new SignerRef().setVerificationMethod("Whatsapp")));
        resource.estimateCost("doc-1", payload);

        RecordedRequest req = server.takeRequest();
        String body = req.getBody().readUtf8();
        assertThat(body).contains("Whatsapp");
    }

    @Test
    void estimateCost_requiresDocumentId() {
        assertThatThrownBy(() -> resource.estimateCost("",
                new CreateAssignmentPayload().setSignerStrings("s1")))
                .isInstanceOf(ValidationException.class);
    }
}