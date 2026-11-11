package com.assinafy.sdk.resources;

import com.assinafy.sdk.AssinafyClient;
import com.assinafy.sdk.AssinafyClientOptions;
import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.NetworkException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.ConfirmSignerDataPayload;
import com.assinafy.sdk.models.CreateAssignmentPayload;
import com.assinafy.sdk.models.CreateDocumentFromTemplateOptions;
import com.assinafy.sdk.models.CreateEnvelopePayload;
import com.assinafy.sdk.models.EnvelopeDocumentPayload;
import com.assinafy.sdk.models.NotificationPolicy;
import com.assinafy.sdk.models.PushPreferences;
import com.assinafy.sdk.models.SignerRef;
import com.assinafy.sdk.models.TemplateSigner;
import com.assinafy.sdk.models.UpdateEnvelopePayload;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExtendedOperationsTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private MockWebServer server;
    private AssinafyClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new AssinafyClient(new AssinafyClientOptions().setBaseUrl(server.url("/v1").toString())
                .setApiKey("test-key").setToken("test-bearer").setAccountId("acc").setTimeoutMs(1000));
    }

    @AfterEach
    void tearDown() throws IOException { server.shutdown(); }

    private void ok(String data) {
        server.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
                .setBody("{\"status\":200,\"data\":" + data + "}"));
    }

    private RecordedRequest request(String method, String path) throws InterruptedException {
        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo(method);
        assertThat(request.getPath()).isEqualTo("/v1" + path);
        return request;
    }

    private JsonNode body(RecordedRequest request) throws IOException {
        return JSON.readTree(request.getBody().readUtf8());
    }

    private EnvelopeDocumentPayload document() {
        return new EnvelopeDocumentPayload().setId("doc").setSignerIds(List.of("signer"));
    }

    private CreateEnvelopePayload envelope() {
        return new CreateEnvelopePayload().setName("Agreement").setDocuments(List.of(document()))
                .setSigners(List.of(SignerRef.of("signer").setVerificationMethod("Email")
                        .setNotificationMethods(List.of("Email"))));
    }

    @Test
    void createsEnvelopeWithExactPayloadAndIdempotencyHeader() throws Exception {
        ok("""
                {"id":"env","name":"Agreement","documents":[{"id":"doc","position":1,
                  "name":"contract.pdf","status":"pending_signature","artifacts":{"original":"https://example.com"},
                  "signers":[{"id":"signer","completed":false,"signed_at":null}]}],
                 "counts":{"pending_signature":1},"signing_urls":[{"signer_id":"signer","url":null,"embedded":true}]}
                """);
        var payload = envelope().setMaxTotalCredits(new BigDecimal("1.25"));
        payload.getSigners().getFirst().setEmbedded(true);
        var result = client.envelopes.create(payload, "creation-1", "other");
        RecordedRequest sent = request("POST", "/accounts/other/envelopes");
        assertThat(sent.getHeader("Idempotency-Key")).isEqualTo("creation-1");
        assertThat(sent.getHeader("X-Api-Key")).isEqualTo("test-key");
        assertThat(body(sent)).isEqualTo(JSON.readTree("""
                {"name":"Agreement","max_total_credits":1.25,
                 "signers":[{"id":"signer","verification_method":"Email","notification_methods":["Email"],
                             "embedded":true}],"documents":[{"id":"doc","signer_ids":["signer"]}]}
                """));
        assertThat(result.documents().getFirst().signers().getFirst().completed()).isFalse();
        assertThat(result.counts()).containsEntry("pending_signature", 1);
        assertThat(result.signingUrls().getFirst().getEmbedded()).isTrue();
    }

    @Test
    void envelopeListEstimateGetUpdateAndDeleteContracts() throws Exception {
        server.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
                .setHeader("X-Pagination-Total-Count", "2").setBody("{\"status\":200,\"data\":[{\"id\":\"env\"}]}"));
        assertThat(client.envelopes.list(Map.of("page", "2"), "other").getMeta().getTotal()).isEqualTo(2);
        request("GET", "/accounts/other/envelopes?page=2");
        ok("{\"total_credits\":1.25}");
        assertThat(client.envelopes.estimateCost(envelope()).getTotalCredits()).isEqualTo(1.25);
        assertThat(body(request("POST", "/accounts/acc/envelopes/estimate-cost")).path("documents")).hasSize(1);
        ok("{\"id\":\"env\"}");
        assertThat(client.envelopes.get("env").id()).isEqualTo("env");
        request("GET", "/accounts/acc/envelopes/env");
        ok("{\"id\":\"env\",\"message\":null}");
        client.envelopes.update("env", new UpdateEnvelopePayload().setMessage(null));
        assertThat(body(request("PATCH", "/accounts/acc/envelopes/env")))
                .isEqualTo(JSON.readTree("{\"message\":null}"));
        ok("[]");
        client.envelopes.delete("env");
        request("DELETE", "/accounts/acc/envelopes/env");
    }

    @Test
    void envelopeActivitiesDownloadsCancelResendAndDocumentMembership() throws Exception {
        ok("[{\"id\":9,\"event\":\"signer_signed_document\",\"document_id\":\"doc\",\"document_name\":\"a.pdf\"}]");
        assertThat(client.envelopes.activities("env", Map.of("page", "2")).getData().getFirst().getDocumentId())
                .isEqualTo("doc");
        request("GET", "/accounts/acc/envelopes/env/activities?page=2");
        server.enqueue(new MockResponse().setHeader("Content-Type", "application/zip").setBody("zip-bytes"));
        assertThat(client.envelopes.download("env", "bundle")).isEqualTo("zip-bytes".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        request("GET", "/accounts/acc/envelopes/env/download/bundle");
        ok("[{\"id\":\"doc\",\"status\":\"rejected_by_user\",\"cancelled\":true}]");
        assertThat(client.envelopes.cancel("env").getFirst().cancelled()).isTrue();
        request("PUT", "/accounts/acc/envelopes/env/cancel");
        ok("{\"is_sent\":true,\"envelope_id\":\"env\",\"signer_id\":\"signer\"}");
        assertThat(client.envelopes.resend("env", "signer").isSent()).isTrue();
        request("PUT", "/accounts/acc/envelopes/env/signers/signer/resend");
        ok("{\"id\":\"env\"}");
        client.envelopes.addDocument("env", document().setMaxTotalCredits(new BigDecimal("2.5")));
        assertThat(body(request("POST", "/accounts/acc/envelopes/env/documents")))
                .isEqualTo(JSON.readTree("{\"id\":\"doc\",\"signer_ids\":[\"signer\"],\"max_total_credits\":2.5}"));
        ok("{\"id\":\"env\"}");
        assertThat(client.envelopes.removeDocument("env", "doc").id()).isEqualTo("env");
        request("DELETE", "/accounts/acc/envelopes/env/documents/doc");
    }

    @Test
    void envelopeCollectionsNormalizeNullAndMutationsAreNeverReplayed() throws Exception {
        ok("null");
        assertThat(client.envelopes.list().getData()).isEmpty();
        request("GET", "/accounts/acc/envelopes");
        ok("null");
        assertThat(client.envelopes.cancel("env")).isEmpty();
        request("PUT", "/accounts/acc/envelopes/env/cancel");
        server.enqueue(new MockResponse().setResponseCode(503).setHeader("Retry-After", "0"));
        assertThatThrownBy(() -> client.envelopes.create(envelope(), "same-key")).isInstanceOf(ApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(3);
    }

    @Test
    void invalidEnvelopeInputsFailBeforeNetwork() {
        assertThatThrownBy(() -> client.envelopes.get("../escape")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> client.envelopes.create(envelope().setName("<script>")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> client.envelopes.create(envelope().setDocuments(List.of())))
                .isInstanceOf(ValidationException.class);
        var payload = envelope();
        payload.getSigners().getFirst().setStep(1);
        assertThatThrownBy(() -> client.envelopes.create(payload)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> client.envelopes.update("env", new UpdateEnvelopePayload()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> client.envelopes.create(envelope(), "bad\r\nHeader: x"))
                .isInstanceOf(ValidationException.class);
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void embedOriginCrudAndSessionMintingUseWorkspaceCredentials() throws Exception {
        ok("[{\"id\":\"origin\",\"origin\":\"https://example.com\"}]");
        assertThat(client.embed.listOrigins().getFirst().origin()).isEqualTo("https://example.com");
        request("GET", "/accounts/acc/embed-origins");
        ok("{\"id\":\"origin\",\"origin\":\"https://example.com\"}");
        client.embed.addOrigin("https://example.com");
        assertThat(body(request("POST", "/accounts/acc/embed-origins")))
                .isEqualTo(JSON.readTree("{\"origin\":\"https://example.com\"}"));
        ok("{\"id\":\"origin\"}");
        client.embed.updateOrigin("origin", "http://localhost:3000");
        assertThat(body(request("PUT", "/accounts/acc/embed-origins/origin")).path("origin").asText())
                .isEqualTo("http://localhost:3000");
        ok("[]");
        client.embed.deleteOrigin("origin");
        request("DELETE", "/accounts/acc/embed-origins/origin");
        ok("{\"url\":\"https://example.com/bearer-secret\",\"expires_at\":\"2026-11-11T12:05:00Z\"}");
        var session = client.embed.createSession("doc", "asg", "signer");
        assertThat(session.toString()).doesNotContain("bearer-secret");
        assertThat(request("POST", "/documents/doc/assignments/asg/signers/signer/embed-sessions")
                .getHeader("X-Api-Key")).isEqualTo("test-key");
    }

    @Test
    void embedExchangeAndDownloadTokensNeverCarryWorkspaceCredentials() throws Exception {
        ok("{\"signer_access_code\":\"secret-code\",\"signer_id\":\"signer\",\"embed_origins\":[\"https://example.com\"]}");
        var result = client.embed.exchange("d".repeat(64), "https://example.com");
        assertThat(result.signerAccessCode()).isEqualTo("secret-code");
        assertThat(result.toString()).doesNotContain("secret-code");
        var exchange = request("POST", "/embed/sessions/" + "d".repeat(64) + "/exchange");
        assertNoWorkspaceAuth(exchange);
        assertThat(body(exchange)).isEqualTo(JSON.readTree("{\"parent_origin\":\"https://example.com\"}"));
        server.enqueue(new MockResponse().setHeader("Content-Type", "application/pdf").setBody("pdf"));
        client.signerSelf.downloadCertifiedCopy("download-token", true);
        assertNoWorkspaceAuth(request("GET", "/signers/downloads/download-token/pades"));
        ok("{\"available\":true,\"notification_method\":\"Email\",\"sent_to\":\"s***@example.com\"}");
        assertThat(client.signerSelf.resendCertifiedCopy("download-token").sentTo()).isEqualTo("s***@example.com");
        assertNoWorkspaceAuth(request("POST", "/signers/downloads/download-token/resend"));
    }

    private static void assertNoWorkspaceAuth(RecordedRequest sent) {
        assertThat(sent.getHeader("X-Api-Key")).isNull();
        assertThat(sent.getHeader("Authorization")).isNull();
    }

    @Test
    void rejectsUnsafeEmbedOriginsAndNormalizesEmptyCollections() throws Exception {
        for (String origin : List.of("https://example.com/path", "https://example.com?q=x", "https://a@b.com",
                "http://example.com", "https://*.example.com", "https://example.com#fragment")) {
            assertThatThrownBy(() -> client.embed.addOrigin(origin)).isInstanceOf(ValidationException.class);
        }
        assertThat(server.getRequestCount()).isZero();
        ok("null");
        assertThat(client.embed.listOrigins()).isEmpty();
    }

    @Test
    void readsUsageSigningFeaturesAndUpdatesPolicyWithoutResponseOnlyFields() throws Exception {
        ok("{\"plan\":\"Pro\",\"balances\":{\"credits\":{\"balance\":1.25,\"limit\":null,\"spent\":2}}}");
        assertThat(client.accounts.usage().balances().get("credits").balance()).isEqualByComparingTo("1.25");
        request("GET", "/accounts/acc/usage");
        ok("{\"id\":\"acc\",\"signing_features\":{\"DigitalCertificate\":true,\"Embed\":false}}");
        assertThat(client.accounts.get(Map.of("expand", "signing_features"), null).getSigningFeatures())
                .containsEntry("DigitalCertificate", true);
        request("GET", "/accounts/acc?expand=signing_features");
        ok("{\"editable\":true,\"owners_receive_all\":false,\"events\":{\"SignerViewed\":false}}");
        var policy = client.accounts.getNotificationPolicy();
        assertThat(policy.getEditable()).isTrue();
        request("GET", "/accounts/acc/notification-policy");
        ok("{\"editable\":true,\"owners_receive_all\":true,\"events\":{\"SignerViewed\":false}}");
        client.accounts.updateNotificationPolicy(policy.setOwnersReceiveAll(true));
        assertThat(body(request("PUT", "/accounts/acc/notification-policy")))
                .isEqualTo(JSON.readTree("{\"owners_receive_all\":true,\"events\":{\"SignerViewed\":false}}"));
        assertThatThrownBy(() -> client.accounts.updateNotificationPolicy(new NotificationPolicy().setEditable(true)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void personalPushPreferencesArePartialAndSeparateFromEmail() throws Exception {
        ok("{\"SignerViewed\":true,\"DocumentCompleted\":true}");
        assertThat(client.users.getPushPreferences().getSignerViewed()).isTrue();
        request("GET", "/users/self/push-preferences");
        ok("{\"SignerViewed\":false,\"DocumentCompleted\":true}");
        client.users.updatePushPreferences(new PushPreferences().setSignerViewed(false));
        assertThat(body(request("PUT", "/users/self/push-preferences")))
                .isEqualTo(JSON.readTree("{\"SignerViewed\":false}"));
        assertThatThrownBy(() -> client.users.updatePushPreferences(new PushPreferences()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void webhookRotationPreservesGraceMetadataAndTestDeliveryDuration() throws Exception {
        ok("{\"secret\":\"whsec_test-secret\",\"previous_secret_expires_at\":\"2026-11-12T12:00:00Z\"}");
        var rotation = client.webhooks.rotateEndpointSecret("endpoint", true);
        assertThat(rotation.previousSecretExpiresAt()).isEqualTo("2026-11-12T12:00:00Z");
        assertThat(rotation.toString()).doesNotContain("whsec_test-secret");
        assertThat(body(request("POST", "/accounts/acc/webhooks/endpoints/endpoint/secret/rotate")))
                .isEqualTo(JSON.readTree("{\"keep_previous\":true}"));
        ok("{\"id\":\"dispatch\",\"duration_ms\":123,\"event\":\"webhook_test\"}");
        assertThat(client.webhooks.testEndpoint("endpoint").getDurationMs()).isEqualTo(123L);
        request("POST", "/accounts/acc/webhooks/endpoints/endpoint/test");
    }

    @Test
    void signingSuccessArraysAndWhatsappConfirmationAreAccepted() throws Exception {
        ok("[]");
        assertThat(client.signerSelf.verifyEmail("123456", "code")).isNull();
        assertThat(body(request("POST", "/verify?signer-access-code=code")))
                .isEqualTo(JSON.readTree("{\"verification-code\":\"123456\"}"));
        ok("[]");
        assertThat(client.signerSelf.verifyWhatsapp("654321", "code")).isNull();
        assertNoWorkspaceAuth(request("POST", "/verify?signer-access-code=code"));
        ok("[]");
        assertThat(client.signerSelf.confirmSignerData("doc", "code", new ConfirmSignerDataPayload()
                .setWhatsappPhoneNumber("+5548999990000").setHasAcceptedTerms(true))).isNull();
        assertThat(body(request("PUT", "/documents/doc/signers/confirm-data?signer-access-code=code")))
                .isEqualTo(JSON.readTree("{\"whatsapp_phone_number\":\"+5548999990000\",\"has_accepted_terms\":true}"));
        ok("[123]");
        assertThatThrownBy(() -> client.signerSelf.verifyEmail("123456", "code"))
                .isInstanceOf(NetworkException.class);
    }

    @Test
    void closedSignerViewsBrandingAndCertifiedCopyRequestsUseOnlySignerAuth() throws Exception {
        ok("{\"id\":\"doc\",\"is_signer_access_closed\":true,\"certified_copy\":{\"available\":true,"
                + "\"notification_method\":\"Whatsapp\",\"resends_left\":1},\"envelope\":{\"id\":\"env\"}}");
        var view = client.signerSelf.getSign("code", null, "envelope", "doc");
        assertThat(view.getSignerAccessClosed()).isTrue();
        assertThat(view.getCertifiedCopy().resendsLeft()).isEqualTo(1);
        assertThat(view.getEnvelope().id()).isEqualTo("env");
        RecordedRequest signing = server.takeRequest();
        assertThat(signing.getRequestUrl().queryParameter("document_id")).isEqualTo("doc");
        assertNoWorkspaceAuth(signing);
        ok("{\"account_name\":\"Workspace\"}");
        assertThat(client.signerSelf.getTheme("code").getAccountName()).isEqualTo("Workspace");
        assertNoWorkspaceAuth(request("GET", "/sign/theme?signer-access-code=code"));
        server.enqueue(new MockResponse().setHeader("Content-Type", "image/png").setBody("png"));
        client.signerSelf.downloadLogo("code");
        assertNoWorkspaceAuth(request("GET", "/sign/logo?signer-access-code=code"));
        ok("{\"available\":false,\"notification_method\":\"Whatsapp\",\"resends_left\":0}");
        assertThat(client.signerSelf.requestCertifiedCopy("code", "doc").available()).isFalse();
        RecordedRequest copy = request("POST", "/sign/certified-copy?signer-access-code=code");
        assertNoWorkspaceAuth(copy);
        assertThat(body(copy)).isEqualTo(JSON.readTree("{\"document_id\":\"doc\"}"));
    }

    @Test
    void expiredDownloadHtmlIsNotReturnedAsPdf() {
        server.enqueue(new MockResponse().setResponseCode(302).setHeader("Location", "/expired"));
        server.enqueue(new MockResponse().setHeader("Content-Type", "text/html").setBody("<html>Expired</html>"));
        assertThatThrownBy(() -> client.signerSelf.downloadCertifiedCopy("expired-token", false))
                .isInstanceOf(ApiException.class).hasMessageContaining("HTML");
    }

    @Test
    void assignmentCreditCeilingEmbeddedFlagAndCertificateEstimatesAreRepresentable() throws Exception {
        ok("{\"id\":\"asg\"}");
        client.assignments.create("doc", new CreateAssignmentPayload().setMaxTotalCredits(new BigDecimal("1.25"))
                .setSigners(List.of(SignerRef.of("signer").setEmbedded(true))));
        assertThat(body(request("POST", "/documents/doc/assignments")))
                .isEqualTo(JSON.readTree("{\"method\":\"virtual\",\"max_total_credits\":1.25,"
                        + "\"signers\":[{\"id\":\"signer\",\"embedded\":true}]}"));
        ok("{\"total_credits\":2}");
        client.assignments.estimateCost("doc", new CreateAssignmentPayload().setSigners(List.of(
                new SignerRef().setVerificationMethod("DigitalCertificate"),
                new SignerRef().setVerificationMethod("DigitalCertificate"))));
        assertThat(body(request("POST", "/documents/doc/assignments/estimate-cost")))
                .isEqualTo(JSON.readTree("{\"method\":\"virtual\",\"signers\":["
                        + "{\"verification_method\":\"DigitalCertificate\"},{\"verification_method\":\"DigitalCertificate\"}]}"));
    }

    @Test
    void templateCreationCarriesCeilingEmbeddingAndIdempotencyKey() throws Exception {
        ok("{\"id\":\"doc\"}");
        client.documents.createFromTemplate("template", List.of(new TemplateSigner("role", "signer").setEmbedded(true)),
                new CreateDocumentFromTemplateOptions().setIdempotencyKey("template-request")
                        .setMaxTotalCredits(new BigDecimal("0.50")));
        var sent = request("POST", "/accounts/acc/templates/template/documents");
        assertThat(sent.getHeader("Idempotency-Key")).isEqualTo("template-request");
        assertThat(body(sent)).isEqualTo(JSON.readTree("{\"signers\":[{\"role_id\":\"role\",\"id\":\"signer\","
                + "\"embedded\":true}],\"max_total_credits\":0.50}"));
    }

    @Test
    void expandedReadsSignedRendersAndActivityPaginationAreAvailable() throws Exception {
        ok("{\"id\":\"doc\",\"envelope_id\":\"env\",\"envelope\":{\"id\":\"env\",\"name\":\"Agreement\"}}");
        assertThat(client.documents.details("doc", Map.of("expand", "envelope_id")).getEnvelopeId()).isEqualTo("env");
        request("GET", "/documents/doc?expand=envelope_id");
        ok("[]");
        assertThat(client.documents.activitiesPage("doc", Map.of("page", "2")).getData()).isEmpty();
        request("GET", "/documents/doc/activities?page=2");
        server.enqueue(new MockResponse().setHeader("Content-Type", "image/png").setBody("png"));
        client.documents.thumbnail("doc", "certificated");
        request("GET", "/documents/doc/thumbnail?artifact=certificated");
        server.enqueue(new MockResponse().setHeader("Content-Type", "image/png").setBody("png"));
        client.documents.downloadPage("doc", "page", "certificated");
        request("GET", "/documents/doc/pages/page/download?artifact=certificated");
        ok("{\"document_id\":\"doc\"}");
        client.contracts.related("doc", Map.of("expand", "document"), null);
        request("GET", "/accounts/acc/documents/doc/contract/related?expand=document");
        ok("{\"id\":\"doc\",\"envelope\":{\"id\":\"env\",\"name\":\"Agreement\"}}");
        assertThat(client.signerSelf.getCurrentDocument("signer", "code", "envelope").getEnvelope().id())
                .isEqualTo("env");
        var current = server.takeRequest();
        assertThat(current.getRequestUrl().encodedPath()).isEqualTo("/v1/signers/signer/document");
        assertThat(current.getRequestUrl().queryParameter("expand")).isEqualTo("envelope");
        assertNoWorkspaceAuth(current);
    }
    @Test
    void bearerCredentialModeSurvivesRootBaseUrlsAndSameOriginRedirects() throws Exception {
        client = new AssinafyClient(new AssinafyClientOptions().setBaseUrl(server.url("/").toString())
                .setApiKey("test-key").setAccountId("acc"));
        for (String credential : List.of("download", "signer")) {
            server.enqueue(new MockResponse().setResponseCode(302).setHeader("Location", "/expired"));
            server.enqueue(new MockResponse().setHeader("Content-Type", "text/html").setBody("<html>Expired</html>"));
            if (credential.equals("download")) {
                assertThatThrownBy(() -> client.signerSelf.downloadCertifiedCopy("token", false))
                        .isInstanceOf(ApiException.class);
            } else {
                assertThatThrownBy(() -> client.signerSelf.downloadLogo("code")).isInstanceOf(ApiException.class);
            }
            assertNoWorkspaceAuth(server.takeRequest());
            assertNoWorkspaceAuth(server.takeRequest());
        }
        ok("{\"signer_access_code\":\"code\"}");
        client.embed.exchange("d".repeat(64), null);
        assertNoWorkspaceAuth(server.takeRequest());
    }
}
