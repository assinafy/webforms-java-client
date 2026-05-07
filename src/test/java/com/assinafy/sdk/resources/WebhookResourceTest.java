package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.RegisterWebhookPayload;
import com.assinafy.sdk.models.WebhookDispatch;
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
                        "created_at", 1705312200,
                        "updated_at", 1705312201
                )
        )));
        server.enqueue(new MockResponse()
                .setBody(body)
                .setHeader("Content-Type", "application/json"));

        PaginatedResult<WebhookDispatch> result = resource.listDispatches();

        WebhookDispatch dispatch = result.getData().get(0);
        assertThat(dispatch.getPayload()).containsEntry("account_id", "acc");
        assertThat(dispatch.getCreatedAt()).isEqualTo("1705312200");
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
}
