package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.AccountPayload;
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

class AccountResourceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private MockWebServer server;
    private AccountResource resource;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        resource = new AccountResource(new OkHttpClient(), server.url("/").toString(), "acc");
    }

    @AfterEach
    void tearDown() throws IOException { server.shutdown(); }

    private MockResponse okJson(Object data) throws Exception {
        return new MockResponse().setHeader("Content-Type", "application/json")
                .setBody(MAPPER.writeValueAsString(Map.of("status", 200, "data", data)));
    }

    @Test
    void listAndGetParseCompleteAccountShape() throws Exception {
        Map<String, Object> account = Map.of(
                "resource", "account", "id", "acc", "name", "Acme", "roles", List.of("owner"),
                "primary_color", "aabbcc", "notification_sender_type", "Account",
                "is_delete_allowed", true);
        server.enqueue(okJson(List.of(account)));
        server.enqueue(okJson(account));

        var accounts = resource.list();
        var selected = resource.get();

        assertThat(accounts).hasSize(1);
        assertThat(selected.getPrimaryColor()).isEqualTo("aabbcc");
        assertThat(selected.getNotificationSenderType()).isEqualTo("Account");
        assertThat(server.takeRequest().getPath()).isEqualTo("/accounts");
        assertThat(server.takeRequest().getPath()).isEqualTo("/accounts/acc");
    }

    @Test
    void createAndUpdateSendDocumentedPayloads() throws Exception {
        server.enqueue(okJson(Map.of("id", "new", "name", "Acme")));
        server.enqueue(okJson(Map.of("id", "acc", "name", "Renamed")));

        resource.create(new AccountPayload("Acme").setNotificationSenderType("Account"));
        resource.update(new AccountPayload().setName("Renamed"));

        RecordedRequest create = server.takeRequest();
        assertThat(create.getMethod()).isEqualTo("POST");
        assertThat(create.getPath()).isEqualTo("/accounts");
        assertThat(create.getBody().readUtf8())
                .isEqualTo("{\"name\":\"Acme\",\"notification_sender_type\":\"Account\"}");
        RecordedRequest update = server.takeRequest();
        assertThat(update.getMethod()).isEqualTo("PUT");
        assertThat(update.getPath()).isEqualTo("/accounts/acc");
    }

    @Test
    void themeLogoStatsAndDeleteUseDocumentedContracts() throws Exception {
        server.enqueue(okJson(Map.of("account_name", "Acme", "primary_color", "aabbcc",
                "logo", "https://example.test/logo")));
        server.enqueue(new MockResponse().setBody("image")
                .setHeader("Content-Type", "image/png"));
        server.enqueue(new MockResponse().setBody("{\"status\":200,\"message\":\"\"}")
                .setHeader("Content-Type", "application/json"));
        server.enqueue(new MockResponse().setBody("{\"status\":200,\"message\":\"\"}")
                .setHeader("Content-Type", "application/json"));
        server.enqueue(okJson(List.of(Map.ofEntries(
                Map.entry("period", "2026-08"),
                Map.entry("documents_uploaded", 3),
                Map.entry("signature_requests_notification_email", 4),
                Map.entry("signature_requests_notification_whatsapp", 5),
                Map.entry("signature_requests_notification_bypass", 6),
                Map.entry("signature_requests_verification_email", 7),
                Map.entry("signature_requests_verification_whatsapp", 8),
                Map.entry("signature_requests_verification_bypass", 9),
                Map.entry("signature_requests_verification_digital_certificate", 10)))));
        server.enqueue(new MockResponse().setBody("{\"status\":200,\"data\":[]}")
                .setHeader("Content-Type", "application/json"));

        assertThat(resource.getTheme().getAccountName()).isEqualTo("Acme");
        assertThat(resource.downloadLogo()).isEqualTo("image".getBytes());
        resource.uploadLogo(new byte[] {(byte) 0x89, 0x50}, "logo.png");
        resource.deleteLogo();
        var stats = resource.stats(Map.of("granularity", "monthly"));
        assertThat(stats).hasSize(1);
        assertThat(stats.getFirst().getSignatureRequestsNotificationEmail()).isEqualTo(4);
        assertThat(stats.getFirst().getSignatureRequestsNotificationWhatsapp()).isEqualTo(5);
        assertThat(stats.getFirst().getSignatureRequestsNotificationBypass()).isEqualTo(6);
        assertThat(stats.getFirst().getSignatureRequestsVerificationEmail()).isEqualTo(7);
        assertThat(stats.getFirst().getSignatureRequestsVerificationWhatsapp()).isEqualTo(8);
        assertThat(stats.getFirst().getSignatureRequestsVerificationBypass()).isEqualTo(9);
        assertThat(stats.getFirst().getSignatureRequestsVerificationDigitalCertificate()).isEqualTo(10);
        assertThat(stats.getFirst().getSignatureRequestsEmail()).isEqualTo(4);
        assertThat(stats.getFirst().getSignatureRequestsWhatsapp()).isEqualTo(5);
        assertThat(MAPPER.writeValueAsString(stats.getFirst()))
                .contains("\"signature_requests_notification_email\":4")
                .doesNotContain("\"signatureRequestsEmail\"", "\"signatureRequestsWhatsapp\"",
                        "\"signature_requests_email\"");
        resource.delete(false);

        server.takeRequest();
        server.takeRequest();
        RecordedRequest upload = server.takeRequest();
        assertThat(upload.getPath()).isEqualTo("/accounts/acc/logo");
        assertThat(upload.getHeader("Content-Type")).startsWith("multipart/form-data");
        assertThat(upload.getBody().readUtf8()).contains("name=\"file\"; filename=\"logo.png\"");
        assertThat(server.takeRequest().getMethod()).isEqualTo("DELETE");
        assertThat(server.takeRequest().getPath()).isEqualTo("/accounts/acc/stats?granularity=monthly");
        RecordedRequest delete = server.takeRequest();
        assertThat(delete.getMethod()).isEqualTo("DELETE");
        assertThat(delete.getBody().readUtf8()).isEqualTo("{\"force\":false}");
    }

    @Test
    void statsAcceptsConciseNotificationFieldAliases() throws Exception {
        server.enqueue(okJson(List.of(Map.of(
                "signature_requests_email", 2,
                "signature_requests_whatsapp", 3))));

        var stats = resource.stats();

        assertThat(stats.getFirst().getSignatureRequestsNotificationEmail()).isEqualTo(2);
        assertThat(stats.getFirst().getSignatureRequestsNotificationWhatsapp()).isEqualTo(3);
    }

    @Test
    void validatesAccountPayloadAndLogo() {
        assertThatThrownBy(() -> resource.create(new AccountPayload()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.update(new AccountPayload()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.uploadLogo(new byte[0], "logo.png"))
                .isInstanceOf(ValidationException.class);
    }
}
