package com.assinafy.sdk;

import com.assinafy.sdk.models.DocumentStatus;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssinafyClientTest {

    @Test
    void constructor_allowsNoCredentialsForUnauthenticatedEndpoints() {
        AssinafyClient client = new AssinafyClient(new AssinafyClientOptions().setAccountId("acc"));
        assertThat(client.auth).isNotNull();
        assertThat(client.documents).isNotNull();
    }

    @Test
    void constructor_acceptsApiKey() {
        AssinafyClient client = new AssinafyClient(new AssinafyClientOptions()
                .setApiKey("k")
                .setAccountId("acc"));
        assertThat(client.documents).isNotNull();
        assertThat(client.signers).isNotNull();
        assertThat(client.assignments).isNotNull();
        assertThat(client.webhooks).isNotNull();
        assertThat(client.templates).isNotNull();
        assertThat(client.tags).isNotNull();
        assertThat(client.fields).isNotNull();
        assertThat(client.signerSelf).isNotNull();
        assertThat(client.auth).isNotNull();
    }

    @Test
    void constructor_acceptsLegacyToken() {
        AssinafyClient client = new AssinafyClient(new AssinafyClientOptions()
                .setToken("t")
                .setAccountId("acc"));
        assertThat(client.documents).isNotNull();
    }

    @Test
    void create_buildsConfiguredClient() {
        AssinafyClient client = AssinafyClient.create("k", "acc", opts -> opts.setTimeoutMs(60_000));
        assertThat(client.documents).isNotNull();
    }

    @Test
    void fromConfig_acceptsSnakeCaseKeys() {
        AssinafyClient client = AssinafyClient.fromConfig(Map.of(
                "api_key", "k",
                "account_id", "acc"
        ));
        assertThat(client.documents).isNotNull();
    }

    @Test
    void fromConfig_acceptsCamelCaseKeys() {
        AssinafyClient client = AssinafyClient.fromConfig(Map.of(
                "apiKey", "k",
                "accountId", "acc"
        ));
        assertThat(client.documents).isNotNull();
    }

    @Test
    void apiKey_setsXApiKeyHeader() {
        AssinafyClient client = new AssinafyClient(new AssinafyClientOptions()
                .setApiKey("my-key")
                .setAccountId("acc"));
        assertThat(client.getHttpClient()).isNotNull();
        assertThat(client.getBaseUrl()).isEqualTo("https://api.assinafy.com.br/v1");
    }

    @Test
    void baseUrl_trailingSlashIsStripped() {
        AssinafyClient client = new AssinafyClient(new AssinafyClientOptions()
                .setApiKey("k")
                .setAccountId("acc")
                .setBaseUrl("https://sandbox.assinafy.com.br/v1/"));
        assertThat(client.getBaseUrl()).isEqualTo("https://sandbox.assinafy.com.br/v1");
    }

    @Test
    void defaultBaseUrl_isProductionApi() {
        AssinafyClient client = new AssinafyClient(new AssinafyClientOptions().setApiKey("k"));
        assertThat(client.getBaseUrl()).isEqualTo("https://api.assinafy.com.br/v1");
    }

    @Test
    void maxRetries_retriesOn429ThenSucceeds() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();
        try {
            server.enqueue(new MockResponse().setResponseCode(429)
                    .setHeader("Retry-After", "0")
                    .setBody("{\"status\":429,\"message\":\"Too Many Requests\"}")
                    .setHeader("Content-Type", "application/json"));
            server.enqueue(new MockResponse().setResponseCode(200)
                    .setBody("{\"status\":200,\"data\":[{\"code\":\"uploaded\",\"deletable\":false}]}")
                    .setHeader("Content-Type", "application/json"));

            AssinafyClient client = AssinafyClient.create("k", "acc",
                    opts -> opts.setBaseUrl(server.url("/v1").toString()).setMaxRetries(2));

            List<DocumentStatus> statuses = client.documents.statuses();

            assertThat(statuses).hasSize(1);
            assertThat(server.getRequestCount()).isEqualTo(2);
        } finally {
            server.shutdown();
        }
    }

    @Test
    void noRetryByDefault_surfaces429() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();
        try {
            server.enqueue(new MockResponse().setResponseCode(429)
                    .setBody("{\"status\":429,\"message\":\"Too Many Requests\"}")
                    .setHeader("Content-Type", "application/json"));

            AssinafyClient client = AssinafyClient.create("k", "acc",
                    opts -> opts.setBaseUrl(server.url("/v1").toString()));

            assertThatThrownBy(() -> client.documents.statuses())
                    .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class);
            assertThat(server.getRequestCount()).isEqualTo(1);
        } finally {
            server.shutdown();
        }
    }
}
