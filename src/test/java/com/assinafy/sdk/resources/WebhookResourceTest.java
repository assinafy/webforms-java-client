package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.RegisterWebhookPayload;
import com.assinafy.sdk.models.WebhookDispatch;
import com.assinafy.sdk.models.WebhookEndpoint;
import com.assinafy.sdk.models.WebhookEndpointPayload;
import com.assinafy.sdk.models.WebhookSubscription;
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

class WebhookResourceTest {

    private MockWebServer server;
    private WebhookResource resource;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        resource = new WebhookResource(new OkHttpClient(), server.url("/").toString(), "acc");
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
    void register_sendsExplicitEvents() throws Exception {
        server.enqueue(okJson(Map.of("is_active", true)));

        resource.register(new RegisterWebhookPayload("https://example.com/webhook", "ops@example.com")
                .setEvents(List.of("document_ready", "document_prepared")));

        RecordedRequest req = server.takeRequest();
        String body = req.getBody().readUtf8();
        assertThat(body).contains("document_ready");
        assertThat(body).contains("document_prepared");
        assertThat(body).contains("\"is_active\":true");
    }

    @Test
    void register_requiresEvents() {
        assertThatThrownBy(() -> resource.register(
                new RegisterWebhookPayload("https://example.com/webhook", "ops@example.com")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("event");
    }

    @Test
    void register_rejectsNullPayloadAsValidationError() {
        assertThatThrownBy(() -> resource.register(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("payload");
    }

    @Test
    void register_validatesUriEmailAndEventValuesBeforeRequest() {
        assertThatThrownBy(() -> resource.register(new RegisterWebhookPayload("relative", "ops@example.com")
                .setEvents(List.of("document_ready"))))
                .isInstanceOf(ValidationException.class).hasMessageContaining("URL");
        assertThatThrownBy(() -> resource.register(new RegisterWebhookPayload("https://example.com", "bad")
                .setEvents(List.of("document_ready"))))
                .isInstanceOf(ValidationException.class).hasMessageContaining("email");
        assertThatThrownBy(() -> resource.register(new RegisterWebhookPayload("https://example.com", "signer@example.com")
                .setEvents(List.of(" "))))
                .isInstanceOf(ValidationException.class).hasMessageContaining("event");
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void listEventTypes_callsGlobalEventTypesEndpoint() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.listEventTypes();

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/webhooks/event-types");
    }

    @Test
    void listDispatches_passesFiltersAndPaginationHeaders() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of("status", 200, "data", List.of()));
        server.enqueue(new MockResponse()
                .setBody(body)
                .setHeader("Content-Type", "application/json")
                .setHeader("x-pagination-current-page", "1")
                .setHeader("x-pagination-per-page", "20")
                .setHeader("x-pagination-total-count", "2")
                .setHeader("x-pagination-page-count", "1"));

        PaginatedResult<WebhookDispatch> result = resource.listDispatches(
                new com.assinafy.sdk.models.ListDispatchesParams()
                        .setDelivered(false)
                        .setPerPage(20));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).contains("/accounts/acc/webhooks");
        assertThat(req.getPath()).contains("delivered=false");
        assertThat(result.getMeta()).isNotNull();
        assertThat(result.getMeta().getCurrentPage()).isEqualTo(1);
        assertThat(result.getMeta().getTotal()).isEqualTo(2);
    }

    @Test
    void listDispatches_parsesPayloadAndTimestamps() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of("status", 200, "data", List.of(
                Map.of(
                        "id", "dispatch-1",
                        "event", "document_ready",
                        "payload", Map.of("account_id", "acc"),
                        "created_at", "2024-01-15T10:30:00Z",
                        "updated_at", "2024-01-15T10:30:01Z"
                )
        )));
        server.enqueue(new MockResponse()
                .setBody(body)
                .setHeader("Content-Type", "application/json"));

        PaginatedResult<WebhookDispatch> result = resource.listDispatches();

        WebhookDispatch dispatch = result.getData().get(0);
        assertThat(dispatch.getPayload()).containsEntry("account_id", "acc");
        assertThat(dispatch.getCreatedAt()).isEqualTo("2024-01-15T10:30:00Z");
    }

    @Test
    void register_putsToSubscriptionsPath() throws Exception {
        server.enqueue(okJson(Map.of("is_active", true, "url", "https://example.com/webhook",
                "email", "ops@example.com", "events", List.of("document_ready"))));

        WebhookSubscription sub = resource.register(
                new RegisterWebhookPayload("https://example.com/webhook", "ops@example.com")
                        .setEvents(List.of("document_ready")));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("PUT");
        assertThat(req.getPath()).isEqualTo("/accounts/acc/webhooks/subscriptions");
        assertThat(sub.isActive()).isTrue();
        assertThat(sub.getUrl()).isEqualTo("https://example.com/webhook");
    }

    @Test
    void getSubscription_getsSubscriptionsPath() throws Exception {
        server.enqueue(okJson(Map.of("is_active", false, "url", "https://hooks/x",
                "email", "ops@example.com", "events", List.of("document_ready"),
                "updated_at", "2026-07-18T02:36:02Z")));

        WebhookSubscription sub = resource.getSubscription();

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("GET");
        assertThat(req.getPath()).isEqualTo("/accounts/acc/webhooks/subscriptions");
        assertThat(sub.getEvents()).containsExactly("document_ready");
        assertThat(sub.getUpdatedAt()).isEqualTo("2026-07-18T02:36:02Z");
    }

    @Test
    void getSubscription_surfacesInvalidAccount404() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(404)
                .setBody(MAPPER.writeValueAsString(Map.of("status", 404, "message", "Conta não encontrada.")))
                .setHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> resource.getSubscription())
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Conta não encontrada");
    }

    @Test
    void retryDispatch_postsToRetryPath() throws Exception {
        server.enqueue(okJson(Map.of("id", "dispatch-1", "event", "document_ready")));

        resource.retryDispatch("dispatch-1");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath()).isEqualTo("/accounts/acc/webhooks/dispatch-1/retry");
    }

    @Test
    void retryDispatch_requiresDispatchId() {
        assertThatThrownBy(() -> resource.retryDispatch(""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void inactivate_hitsDocumentedEndpoint() throws Exception {
        server.enqueue(okJson(Map.of("is_active", false)));

        resource.inactivate();

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/accounts/acc/webhooks/inactivate");
    }

    private static Map<String, Object> endpointJson() {
        return Map.of("id", "ep1", "name", "ERP", "url", "https://example.com/hook", "email", "ops@example.com",
                "events", List.of("document_ready"), "is_active", true, "signing_enabled", true,
                "created_at", "2026-10-01T12:00:00Z", "updated_at", "2026-10-01T12:00:00Z");
    }

    @Test
    void listEndpoints_getsTheCollectionAndParsesEveryField() throws Exception {
        server.enqueue(okJson(List.of(endpointJson())));

        List<WebhookEndpoint> endpoints = resource.listEndpoints();

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("GET");
        assertThat(req.getPath()).isEqualTo("/accounts/acc/webhooks/endpoints");
        assertThat(endpoints).containsExactly(new WebhookEndpoint("ep1", "ERP", "https://example.com/hook",
                "ops@example.com", List.of("document_ready"), true, true,
                "2026-10-01T12:00:00Z", "2026-10-01T12:00:00Z"));
    }

    @Test
    void listEndpoints_treatsNullDataAsEmpty() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"status\":200,\"data\":null}"));
        assertThat(resource.listEndpoints("other")).isEmpty();
        assertThat(server.takeRequest().getPath()).isEqualTo("/accounts/other/webhooks/endpoints");
    }

    @Test
    void createEndpoint_postsOnlyTheFieldsThatAreSet() throws Exception {
        server.enqueue(okJson(endpointJson()));

        WebhookEndpoint created = resource.createEndpoint(new WebhookEndpointPayload()
                .setUrl("https://example.com/hook").setEmail("ops@example.com")
                .setEvents(List.of("document_ready")).setName("ERP").setSigningEnabled(true));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath()).isEqualTo("/accounts/acc/webhooks/endpoints");
        assertThat(MAPPER.readTree(req.getBody().readUtf8())).isEqualTo(MAPPER.valueToTree(Map.of(
                "url", "https://example.com/hook", "email", "ops@example.com",
                "events", List.of("document_ready"), "name", "ERP", "signing_enabled", true)));
        assertThat(created.signingEnabled()).isTrue();
    }

    @Test
    void createEndpoint_requiresUrlEmailAndEventsBeforeRequest() {
        assertThatThrownBy(() -> resource.createEndpoint(null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.createEndpoint(new WebhookEndpointPayload()
                .setUrl("ftp://example.com").setEmail("ops@example.com").setEvents(List.of("document_ready"))))
                .isInstanceOf(ValidationException.class).hasMessageContaining("HTTP");
        assertThatThrownBy(() -> resource.createEndpoint(new WebhookEndpointPayload()
                .setUrl("https://example.com/hook").setEvents(List.of("document_ready"))))
                .isInstanceOf(ValidationException.class).hasMessageContaining("email");
        assertThatThrownBy(() -> resource.createEndpoint(new WebhookEndpointPayload()
                .setUrl("https://example.com/hook").setEmail("ops@example.com")))
                .isInstanceOf(ValidationException.class).hasMessageContaining("event");
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void createEndpoint_surfacesThePlanLimitAsApiException() {
        server.enqueue(new MockResponse().setResponseCode(403)
                .setBody("{\"status\":403,\"message\":\"Limit reached\",\"data\":null}"));

        assertThatThrownBy(() -> resource.createEndpoint(new WebhookEndpointPayload()
                .setUrl("https://example.com/hook").setEmail("ops@example.com")
                .setEvents(List.of("document_ready"))))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatusCode()).isEqualTo(403));
    }

    @Test
    void getEndpoint_getsOneEndpoint() throws Exception {
        server.enqueue(okJson(endpointJson()));

        assertThat(resource.getEndpoint("ep1").id()).isEqualTo("ep1");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("GET");
        assertThat(req.getPath()).isEqualTo("/accounts/acc/webhooks/endpoints/ep1");
    }

    @Test
    void updateEndpoint_putsOnlyChangedFields() throws Exception {
        server.enqueue(okJson(endpointJson()));

        resource.updateEndpoint("ep1", new WebhookEndpointPayload().setActive(false), "other");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("PUT");
        assertThat(req.getPath()).isEqualTo("/accounts/other/webhooks/endpoints/ep1");
        assertThat(req.getBody().readUtf8()).isEqualTo("{\"is_active\":false}");
    }

    @Test
    void updateEndpoint_rejectsEmptyOrInvalidChangesBeforeRequest() {
        assertThatThrownBy(() -> resource.updateEndpoint("ep1", new WebhookEndpointPayload()))
                .isInstanceOf(ValidationException.class).hasMessageContaining("attribute");
        assertThatThrownBy(() -> resource.updateEndpoint("ep1", new WebhookEndpointPayload().setEvents(List.of())))
                .isInstanceOf(ValidationException.class).hasMessageContaining("event");
        assertThatThrownBy(() -> resource.updateEndpoint("../x", new WebhookEndpointPayload().setName("n")))
                .isInstanceOf(ValidationException.class);
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void deleteEndpoint_deletesOneEndpoint() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.deleteEndpoint("ep1");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("DELETE");
        assertThat(req.getPath()).isEqualTo("/accounts/acc/webhooks/endpoints/ep1");
    }

    @Test
    void getEndpointSecret_returnsTheSecret() throws Exception {
        server.enqueue(okJson(Map.of("secret", "whsec_abc")));

        assertThat(resource.getEndpointSecret("ep1")).isEqualTo("whsec_abc");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("GET");
        assertThat(req.getPath()).isEqualTo("/accounts/acc/webhooks/endpoints/ep1/secret");
    }

    @Test
    void rotateEndpointSecret_postsAndReturnsTheNewSecret() throws Exception {
        server.enqueue(okJson(Map.of("secret", "whsec_new")));

        assertThat(resource.rotateEndpointSecret("ep1", "other")).isEqualTo("whsec_new");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath()).isEqualTo("/accounts/other/webhooks/endpoints/ep1/secret/rotate");
    }

    @Test
    void endpointSecret_rejectsAResponseWithoutASecret() throws Exception {
        server.enqueue(okJson(Map.of()));
        assertThatThrownBy(() -> resource.getEndpointSecret("ep1")).isInstanceOf(ValidationException.class);
    }

    @Test
    void listDispatches_filtersByEndpointAndParsesEndpointId() throws Exception {
        server.enqueue(okJson(List.of(Map.of("id", "d1", "endpoint_id", "ep1", "delivered", true))));

        PaginatedResult<WebhookDispatch> result = resource.listDispatches(
                new com.assinafy.sdk.models.ListDispatchesParams().setEndpointId("ep1"));

        assertThat(server.takeRequest().getPath()).isEqualTo("/accounts/acc/webhooks?endpoint_id=ep1");
        assertThat(result.getData().get(0).getEndpointId()).isEqualTo("ep1");
    }
}
