package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.ConfirmSignerDataPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignerSelfResourceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final byte[] PNG = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };
    private MockWebServer server;
    private SignerSelfResource resource;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        resource = new SignerSelfResource(new OkHttpClient(), server.url("/").toString());
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
    void getSelf_passesAccessCodeAsQueryParameter() throws Exception {
        server.enqueue(okJson(Map.of("id", "signer-1", "full_name", "Test")));

        resource.getSelf("code-123");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("GET");
        assertThat(req.getPath()).isEqualTo("/signers/self?signer-access-code=code-123");
    }

    @Test
    void getSelf_parsesSignatureReusableFlag() throws Exception {
        server.enqueue(okJson(Map.of("id", "signer-1", "full_name", "Test",
                "has_signature", true, "has_initial", false, "is_signature_reusable", false)));

        var self = resource.getSelf("code-123");

        assertThat(self.getHasSignature()).isTrue();
        assertThat(self.getSignatureReusable()).isFalse();
    }

    @Test
    void getSelf_throwsOnMissingAccessCode() {
        assertThatThrownBy(() -> resource.getSelf(""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getSign_passesAccessCodeAndTermsFlag() throws Exception {
        server.enqueue(okJson(Map.of("id", "doc-1", "name", "x.pdf")));

        resource.getSign("code-123", true);

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).contains("/sign?");
        assertThat(req.getPath()).contains("signer-access-code=code-123");
        assertThat(req.getPath()).contains("has_accepted_terms=true");
    }

    @Test
    void acceptTerms_putsAccessCodeInQueryNotBody() throws Exception {
        server.enqueue(okJson(Map.of("has_accepted_terms", true)));

        resource.acceptTerms("code-123");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("PUT");
        assertThat(req.getPath()).isEqualTo("/signers/accept-terms?signer-access-code=code-123");
        // The access code is the query parameter; it must NOT be in the body (the endpoint has no body).
        assertThat(req.getBody().readUtf8()).doesNotContain("signer-access-code");
    }

    @Test
    void verifyEmail_putsAccessCodeInQueryAndCodeInBody() throws Exception {
        server.enqueue(okJson(Map.of("verified", true)));

        resource.verifyEmail("123456", "code-123");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath()).isEqualTo("/verify?signer-access-code=code-123");
        String body = req.getBody().readUtf8();
        assertThat(body).contains("\"verification-code\":\"123456\"");
        assertThat(body).doesNotContain("signer-access-code");
    }

    @Test
    void confirmSignerData_putsSpecFieldsAndReturnsSigner() throws Exception {
        server.enqueue(okJson(Map.of("id", "signer-1", "full_name", "Jane Doe", "email", "a@b.com")));

        var signer = resource.confirmSignerData("doc-1", "code-abc",
                new ConfirmSignerDataPayload().setFullName("Jane Doe").setEmail("a@b.com")
                        .setGovernmentId("15774136604"));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("PUT");
        assertThat(req.getPath())
                .isEqualTo("/documents/doc-1/signers/confirm-data?signer-access-code=code-abc");
        String body = req.getBody().readUtf8();
        assertThat(body).contains("\"full_name\":\"Jane Doe\"");
        assertThat(body).contains("\"email\":\"a@b.com\"");
        assertThat(body).contains("\"government_id\":\"15774136604\"");
        assertThat(signer.getId()).isEqualTo("signer-1");
    }

    @Test
    void uploadSignature_sendsBinaryWithBothQueryParams() throws Exception {
        server.enqueue(okJson(List.of()));
        byte[] png = PNG.clone();

        resource.uploadSignature("code-xyz", png, "signature");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath()).contains("/signature");
        assertThat(req.getPath()).contains("signer-access-code=code-xyz");
        assertThat(req.getPath()).contains("type=signature");
        assertThat(req.getHeader("Content-Type")).contains("image/png");
        Buffer body = req.getBody();
        assertThat(body.readByteArray()).isEqualTo(png);
    }

    @Test
    void uploadSignature_includesReuseQueryWhenProvided() throws Exception {
        server.enqueue(okJson(List.of()));
        byte[] png = PNG.clone();

        resource.uploadSignature("code-xyz", png, "signature", true);

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).contains("type=signature");
        assertThat(req.getPath()).contains("reuse=true");
        assertThat(req.getPath()).contains("signer-access-code=code-xyz");
    }

    @Test
    void uploadSignature_omitsReuseWhenNull() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.uploadSignature("code-xyz", PNG, "signature");

        assertThat(server.takeRequest().getPath()).doesNotContain("reuse=");
    }

    @Test
    void searchDocuments_hitsSearchEndpointWithTerm() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.searchDocuments("signer-1", "code-1", "contract");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).contains("/signers/signer-1/documents/search?");
        assertThat(req.getPath()).contains("search=contract");
        assertThat(req.getPath()).contains("signer-access-code=code-1");
    }

    @Test
    void uploadSignature_detectsJpeg() throws Exception {
        server.enqueue(okJson(List.of()));
        byte[] jpeg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10};

        resource.uploadSignature("code-xyz", jpeg, "initial");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getHeader("Content-Type")).contains("image/jpeg");
        assertThat(req.getPath()).contains("type=initial");
    }

    @Test
    void uploadSignature_rejectsInvalidType() {
        assertThatThrownBy(() -> resource.uploadSignature("code", new byte[]{1, 2, 3}, "stamp"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void uploadSignature_surfacesEnvelopeError() throws Exception {
        // HTTP 200 carrying an error envelope must still raise an ApiException (the old binary path swallowed it).
        String body = MAPPER.writeValueAsString(Map.of("status", 422, "message", "Invalid image", "data", List.of()));
        server.enqueue(new MockResponse().setBody(body).setHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> resource.uploadSignature("code-xyz", PNG, "signature"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid image");
    }

    @Test
    void uploadSignature_rejectsTruncatedPngHeader() {
        assertThatThrownBy(() -> resource.uploadSignature("code", new byte[]{(byte) 0x89, 0x50}, "signature"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("PNG or JPEG");
    }

    @Test
    void uploadSignature_rejectsEmptyBytes() {
        assertThatThrownBy(() -> resource.uploadSignature("code", new byte[0], "signature"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void downloadSignature_appendsAccessCode() throws Exception {
        server.enqueue(new MockResponse().setBody("PNGBYTES")
                .setHeader("Content-Type", "image/png"));

        byte[] bytes = resource.downloadSignature("code-xyz", "initial");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/signature/initial?signer-access-code=code-xyz");
        assertThat(new String(bytes)).isEqualTo("PNGBYTES");
    }

    @Test
    void downloadSignature_defaultsTypeToSignature() throws Exception {
        server.enqueue(new MockResponse().setBody("X").setHeader("Content-Type", "image/png"));

        resource.downloadSignature("code", null);

        assertThat(server.takeRequest().getPath()).startsWith("/signature/signature");
    }

    @Test
    void getCurrentDocument_passesAccessCodeAndParsesCurrentSigner() throws Exception {
        server.enqueue(okJson(Map.of(
                "id", "doc-1",
                "name", "x.pdf",
                "current_signer", Map.of(
                        "id", "signer-1",
                        "full_name", "Signer Name",
                        "email", "signer@example.com",
                        "verification_method", "Email",
                        "notification_methods", List.of("Email")
                )
        )));

        var doc = resource.getCurrentDocument("signer-1", "code-1");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath())
                .isEqualTo("/signers/signer-1/document?signer-access-code=code-1");
        assertThat(doc.getCurrentSigner()).isNotNull();
        assertThat(doc.getCurrentSigner().getId()).isEqualTo("signer-1");
        assertThat(doc.getCurrentSigner().getVerificationMethod()).isEqualTo("Email");
    }

    @Test
    void listDocuments_passesAccessCode() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.listDocuments("signer-1", "code-1");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath())
                .isEqualTo("/signers/signer-1/documents?signer-access-code=code-1");
    }

    @Test
    void listDocuments_acceptsDocumentedFilters() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.listDocuments("signer-1", "code-1", Map.of("status", "pending_signature",
                "method", "virtual"));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).contains("/signers/signer-1/documents?");
        assertThat(req.getPath()).contains("status=pending_signature");
        assertThat(req.getPath()).contains("method=virtual");
        assertThat(req.getPath()).contains("signer-access-code=code-1");
    }

    @Test
    void signMultiple_putsDocumentIds() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.signMultiple("code-1", List.of("doc-1", "doc-2"));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("PUT");
        assertThat(req.getPath())
                .isEqualTo("/signers/documents/sign-multiple?signer-access-code=code-1");
        String body = req.getBody().readUtf8();
        assertThat(body).contains("doc-1").contains("doc-2").contains("document_ids");
    }

    @Test
    void signMultiple_rejectsEmptyList() {
        assertThatThrownBy(() -> resource.signMultiple("code", List.of()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void declineMultiple_putsBody() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.declineMultiple("code-1", List.of("doc-1"), "Not interested");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("PUT");
        assertThat(req.getPath())
                .isEqualTo("/signers/documents/decline-multiple?signer-access-code=code-1");
        String body = req.getBody().readUtf8();
        assertThat(body).contains("\"decline_reason\":\"Not interested\"");
        assertThat(body).contains("doc-1");
    }

    @Test
    void declineMultiple_requiresReason() {
        assertThatThrownBy(() -> resource.declineMultiple("code", List.of("doc"), ""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void downloadDocument_usesSignerScopedArtifactEndpoint() throws Exception {
        server.enqueue(new MockResponse().setBody("PDF")
                .setHeader("Content-Type", "application/pdf"));

        byte[] bytes = resource.downloadDocument("signer-1", "doc-1", "original", "code-1");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath())
                .isEqualTo("/signers/signer-1/documents/doc-1/download/original?signer-access-code=code-1");
        assertThat(new String(bytes)).isEqualTo("PDF");
    }

    @Test
    void downloadDocument_publicOverloadOmitsAccessCode() throws Exception {
        server.enqueue(new MockResponse().setBody("PDF")
                .setHeader("Content-Type", "application/pdf"));

        byte[] bytes = resource.downloadDocument("signer-1", "doc-1", "original");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getPath())
                .isEqualTo("/signers/signer-1/documents/doc-1/download/original");
        assertThat(new String(bytes)).isEqualTo("PDF");
    }

    @Test
    void downloadDocument_rejectsUnsafePathSegments() {
        assertThatThrownBy(() -> resource.downloadDocument("../signer", "doc-1", "original"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("URL-safe path segment");
        assertThatThrownBy(() -> resource.downloadDocument("signer-1", "doc/1", "original"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("URL-safe path segment");
        assertThatThrownBy(() -> resource.downloadDocument("signer-1", "doc-1", "../original"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("URL-safe path segment");
        assertThat(server.getRequestCount()).isZero();
    }
}
