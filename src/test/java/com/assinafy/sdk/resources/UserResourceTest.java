package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.NotificationPreferences;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserResourceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private MockWebServer server;
    private UserResource resource;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        resource = new UserResource(new OkHttpClient(), server.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException { server.shutdown(); }

    private MockResponse okJson(Object data) throws Exception {
        return new MockResponse().setHeader("Content-Type", "application/json")
                .setBody(MAPPER.writeValueAsString(Map.of("status", 200, "data", data)));
    }

    @Test
    void selfPreferencesUpdateAndStatsUseDocumentedContracts() throws Exception {
        server.enqueue(okJson(Map.of("id", "u1", "name", "Bill", "email", "bill@example.com",
                "is_email_verified", true, "is_password_set", true)));
        server.enqueue(okJson(Map.of("DocumentCompleted", true, "SignerDeclined", true)));
        server.enqueue(okJson(Map.of("DocumentCompleted", false, "SignerDeclined", true)));
        server.enqueue(okJson(List.of(Map.of("period", "2026-08-20", "documents_certified", 2))));

        var self = resource.getSelf();
        assertThat(self.getEmailVerified()).isTrue();
        assertThat(self.getPasswordSet()).isTrue();
        assertThat(resource.getNotificationPreferences().getDocumentCompleted()).isTrue();
        var updated = resource.updateNotificationPreferences(
                new NotificationPreferences().setDocumentCompleted(false));
        assertThat(updated.getDocumentCompleted()).isFalse();
        assertThat(resource.stats(Map.of("granularity", "daily", "month", "2026-08")))
                .singleElement().extracting(row -> row.getDocumentsCertified()).isEqualTo(2);

        assertThat(server.takeRequest().getPath()).isEqualTo("/users/self");
        assertThat(server.takeRequest().getPath()).isEqualTo("/users/self/notification-preferences");
        var update = server.takeRequest();
        assertThat(update.getMethod()).isEqualTo("PUT");
        assertThat(update.getBody().readUtf8()).isEqualTo("{\"DocumentCompleted\":false}");
        assertThat(server.takeRequest().getPath())
                .contains("/users/self/stats?").contains("granularity=daily").contains("month=2026-08");
    }

    @Test
    void getSelfAlsoAcceptsNestedUserEnvelope() throws Exception {
        server.enqueue(okJson(Map.of(
                "user", Map.of("id", "u1", "email", "bill@example.com"),
                "accounts", List.of(Map.of("id", "a1")))));

        assertThat(resource.getSelf().getEmail()).isEqualTo("bill@example.com");
    }

    @Test
    void updatePreferencesRejectsNull() {
        assertThatThrownBy(() -> resource.updateNotificationPreferences(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updatePreferencesRejectsEmptyObjectWithoutRequest() {
        assertThatThrownBy(() -> resource.updateNotificationPreferences(new NotificationPreferences()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("At least one");
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void updatePreferencesUsesAllNineCaseSensitiveKeys() throws Exception {
        NotificationPreferences all = new NotificationPreferences()
                .setDocumentCompleted(true)
                .setSignerDeclined(false)
                .setDocumentCancelled(true)
                .setDocumentAboutToExpire(false)
                .setDocumentExpired(true)
                .setDocumentExpirationReset(false)
                .setDocumentProcessingFailed(true)
                .setTemplateProcessingFailed(false)
                .setSignerWhatsappFailed(true);
        server.enqueue(okJson(MAPPER.convertValue(all, Map.class)));

        NotificationPreferences response = resource.updateNotificationPreferences(all);
        String body = server.takeRequest().getBody().readUtf8();

        assertThat(MAPPER.readTree(body).fieldNames()).toIterable().containsExactlyInAnyOrder(
                "DocumentCompleted", "SignerDeclined", "DocumentCancelled", "DocumentAboutToExpire",
                "DocumentExpired", "DocumentExpirationReset", "DocumentProcessingFailed",
                "TemplateProcessingFailed", "SignerWhatsappFailed");
        assertThat(response.getSignerWhatsappFailed()).isTrue();
        assertThat(response.getTemplateProcessingFailed()).isFalse();
    }
}
