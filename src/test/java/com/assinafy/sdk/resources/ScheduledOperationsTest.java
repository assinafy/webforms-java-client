package com.assinafy.sdk.resources;

import com.assinafy.sdk.AssinafyClient;
import com.assinafy.sdk.AssinafyClientOptions;
import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.CreateAssignmentPayload;
import com.assinafy.sdk.models.CreateEnvelopePayload;
import com.assinafy.sdk.models.EnvelopeDocumentPayload;
import com.assinafy.sdk.models.OAuthAuthorizationRequest;
import com.assinafy.sdk.models.SignerRef;
import com.assinafy.sdk.models.TotpEnrollment;
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

class ScheduledOperationsTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private MockWebServer server;
    private AssinafyClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new AssinafyClient(new AssinafyClientOptions().setBaseUrl(server.url("/v1").toString())
                .setApiKey("test-key").setAccountId("acc").setTimeoutMs(1000));
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
        assertThat(request.getHeader("X-Api-Key")).isEqualTo("test-key");
        return request;
    }

    private JsonNode body(RecordedRequest request) throws IOException {
        return JSON.readTree(request.getBody().readUtf8());
    }

    @Test
    void scheduledAssignmentPreservesCreationValuesAndParsesFullResponse() throws Exception {
        ok("""
                {"resource":"scheduled_send","id":"job","type":"assignment","document_ids":["doc"],
                 "status":"pending","send_at":"2027-01-01T12:00:00Z","assignment_id":null,"envelope_id":null,
                 "failure_reason":null,"created_at":"2026-12-31T12:00:00Z","processed_at":null}
                """);
        var result = client.assignments.schedule("doc", new CreateAssignmentPayload().setMethod("virtual")
                .setSigners(List.of(SignerRef.of("signer").setSignAs("Witness").setStep(2).setEmbedded(true)))
                .setMaxTotalCredits(new BigDecimal("0.45")).setMessage("Please sign")
                .setExpiresAt("2027-01-02T12:00:00Z").setCopyReceivers(List.of("copy")),
                "2027-01-01T09:00:00-03:00");
        assertThat(body(request("POST", "/documents/doc/scheduled-sends"))).isEqualTo(JSON.readTree("""
                {"method":"virtual","signers":[{"id":"signer","sign_as":"Witness","step":2,"embedded":true}],
                 "max_total_credits":0.45,"message":"Please sign","expires_at":"2027-01-02T12:00:00Z",
                 "copy_receivers":["copy"],"send_at":"2027-01-01T09:00:00-03:00"}
                """));
        assertThat(result.id()).isEqualTo("job");
        assertThat(result.documentIds()).containsExactly("doc");
        assertThat(result.sendAt()).isEqualTo("2027-01-01T12:00:00Z");
        assertThat(result.assignmentId()).isNull();
        assertThat(result.processedAt()).isNull();
    }

    @Test
    void schedulesEnvelopeWithoutIdempotencyAndDoesNotMutatePayload() throws Exception {
        var payload = new CreateEnvelopePayload().setName("Agreement").setMessage("Please sign")
                .setSigners(List.of(SignerRef.of("signer")))
                .setDocuments(List.of(new EnvelopeDocumentPayload().setId("doc")
                        .setSignerIds(List.of("signer")))).setMaxTotalCredits(BigDecimal.ONE);
        ok("{\"id\":\"job\",\"type\":\"envelope\",\"status\":\"pending\"}");
        assertThat(client.envelopes.schedule(payload, "2027-01-01T12:00:00Z", "other").type())
                .isEqualTo("envelope");
        RecordedRequest sent = request("POST", "/accounts/other/scheduled-sends");
        assertThat(sent.getHeader("Idempotency-Key")).isNull();
        assertThat(body(sent)).isEqualTo(JSON.readTree("""
                {"name":"Agreement","message":"Please sign","signers":[{"id":"signer"}],
                 "documents":[{"id":"doc","signer_ids":["signer"]}],"max_total_credits":1,
                 "send_at":"2027-01-01T12:00:00Z"}
                """));
        assertThat(JSON.valueToTree(payload).has("send_at")).isFalse();
    }

    @Test
    void listsCancelsAndSendsWithWorkspaceOverrideAndPagination() throws Exception {
        server.enqueue(new MockResponse().setHeader("X-Pagination-Total-Count", "21")
                .setBody("{\"status\":200,\"data\":[{\"id\":\"job\",\"status\":\"pending\"}]}"));
        var page = client.scheduledSends.list(Map.of("document_id", "doc"), "other");
        assertThat(page.getMeta().getTotal()).isEqualTo(21);
        assertThat(page.getData().getFirst().id()).isEqualTo("job");
        request("GET", "/accounts/other/scheduled-sends?document_id=doc");
        ok("{\"status\":\"cancelled\",\"processed_at\":\"2027-01-01T12:00:00Z\"}");
        assertThat(client.scheduledSends.cancel("job", "other").status()).isEqualTo("cancelled");
        request("PUT", "/accounts/other/scheduled-sends/job/cancel");
        ok("{\"status\":\"sent\",\"assignment_id\":\"assignment\",\"envelope_id\":null}");
        assertThat(client.scheduledSends.sendNow("job").assignmentId()).isEqualTo("assignment");
        request("PUT", "/accounts/acc/scheduled-sends/job/send");
        ok("null");
        assertThat(client.scheduledSends.list().getData()).isEmpty();
        request("GET", "/accounts/acc/scheduled-sends");
    }

    @Test
    void exposesCapacitiesAndSendsCapacityInCreationAndEstimate() throws Exception {
        var payload = new CreateAssignmentPayload().setSigners(List.of(SignerRef.of("signer")
                .setStep(2).setSignAs("Witness")));
        ok("{}");
        client.assignments.create("doc", payload);
        assertThat(body(request("POST", "/documents/doc/assignments")).path("signers").get(0))
                .isEqualTo(JSON.readTree("{\"id\":\"signer\",\"step\":2,\"sign_as\":\"Witness\"}"));
        ok("{}");
        client.assignments.estimateCost("doc", payload);
        assertThat(body(request("POST", "/documents/doc/assignments/estimate-cost")).path("signers").get(0))
                .isEqualTo(JSON.readTree("{\"sign_as\":\"Witness\"}"));
        ok("[{\"signer_id\":\"signer\",\"sign_as\":\"Witness\"}]");
        assertThat(client.assignments.signerCapacities("doc").getFirst().signAs()).isEqualTo("Witness");
        request("GET", "/documents/doc/signer-capacities");
        ok("null");
        assertThat(client.assignments.signerCapacities("doc")).isEmpty();
    }

    @Test
    void watcherReplacementSupportsClearingAndReturnsContacts() throws Exception {
        ok("[{\"id\":\"watcher\",\"full_name\":\"Test Contact\",\"email\":\"watcher@example.com\"}]");
        assertThat(client.documents.setWatchers("doc", List.of("watcher")).getFirst().getId())
                .isEqualTo("watcher");
        assertThat(body(request("PUT", "/documents/doc/watchers")))
                .isEqualTo(JSON.readTree("{\"watcher_ids\":[\"watcher\"]}"));
        ok("null");
        assertThat(client.documents.listWatchers("doc")).isEmpty();
        request("GET", "/documents/doc/watchers");
        ok("[]");
        assertThat(client.documents.setWatchers("doc", List.of())).isEmpty();
        assertThat(body(request("PUT", "/documents/doc/watchers")).path("watcher_ids")).isEmpty();
    }

    @Test
    void parsesDeliveryTimestampsAndPricesEnvelopeReminder() throws Exception {
        ok("""
                [{"signer_id":"signer","notification_history":[{"event":"signature_request","status":"sent",
                  "sent_at":"2027-01-01T12:00:00Z","failed_at":null,"error_code":null,"error_message":null,
                  "delivered_at":"2027-01-01T12:00:05Z","opened_at":"2027-01-01T12:30:00Z"}]}]
                """);
        var history = client.documents.notificationHistory("doc").getFirst();
        assertThat(history.signerId()).isEqualTo("signer");
        assertThat(history.notificationHistory().getFirst().getDeliveredAt()).isEqualTo("2027-01-01T12:00:05Z");
        assertThat(history.notificationHistory().getFirst().getOpenedAt()).isEqualTo("2027-01-01T12:30:00Z");
        request("GET", "/documents/doc/notification-history");
        ok("{\"total_credits\":0.45,\"has_sufficient_resources\":true}");
        assertThat(client.envelopes.estimateResendCost("env", "signer").getTotalCredits()).isEqualTo(0.45);
        request("POST", "/accounts/acc/envelopes/env/signers/signer/estimate-resend-cost");
        ok("null");
        assertThat(client.documents.notificationHistory("doc")).isEmpty();
    }

    @Test
    void rejectsInvalidInputsBeforeNetwork() {
        assertThatThrownBy(() -> client.documents.setWatchers("doc", null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> client.documents.setWatchers("doc", List.of("../other")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> client.documents.setWatchers("doc", java.util.Collections.nCopies(11, "watcher")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> client.scheduledSends.cancel("../job")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> client.scheduledSends.sendNow("job", "../account"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> client.assignments.schedule("doc", null, "2027-01-01T12:00:00Z"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> client.envelopes.schedule(new CreateEnvelopePayload(), ""))
                .isInstanceOf(ValidationException.class);
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void mutationsAreNotReplayedAndKeepMachineReadableFailures() {
        server.enqueue(new MockResponse().setResponseCode(503).setHeader("Retry-After", "0")
                .setBody("{\"status\":503,\"message\":\"Unavailable\",\"code\":\"TemporarilyUnavailable\"}"));
        assertThatThrownBy(() -> client.scheduledSends.sendNow("job")).isInstanceOfSatisfying(ApiException.class,
                e -> assertThat(e.getCode()).isEqualTo("TemporarilyUnavailable"));
        assertThat(server.getRequestCount()).isEqualTo(1);
        server.enqueue(new MockResponse().setBody(
                "{\"status\":409,\"message\":\"Confirm first\",\"code\":\"SignerDataUnconfirmed\"}"));
        assertThatThrownBy(() -> client.signerSelf.signMultiple("code", List.of("doc")))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo("SignerDataUnconfirmed"));
        server.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody(
                "{\"status\":403,\"message\":\"Restricted\",\"code\":\"PlanRestricted\"}"));
        assertThatThrownBy(() -> client.documents.download("doc"))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo("PlanRestricted"));
    }

    @Test
    void authorizationEndpointCannotDowngradeOrOverridePkceAndTotpSecretsStayRedacted() {
        for (String endpoint : List.of("http://auth.example/authorize", "https://user:pass@auth.example/authorize",
                "https://auth.example/authorize?code_challenge_method=plain", "https://auth.example/authorize#fragment")) {
            assertThatThrownBy(() -> client.oauth.authorizationUrl(new OAuthAuthorizationRequest(
                    "client", "https://example.com/callback").setScopes(List.of("documents:read"))
                    .setState("state").setCodeVerifier(OAuthResource.generateCodeVerifier())
                    .setAuthorizationEndpoint(endpoint))).isInstanceOf(ValidationException.class);
        }
        assertThat(new TotpEnrollment("id", "test-secret", "otpauth://totp/test-secret").toString())
                .doesNotContain("test-secret", "otpauth");
    }
}
