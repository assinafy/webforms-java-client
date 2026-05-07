package com.assinafy.sdk;

import com.assinafy.sdk.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssinafyClientTest {

    @Test
    void constructor_throwsWhenNoCredentials() {
        assertThatThrownBy(() -> new AssinafyClient(new AssinafyClientOptions().setAccountId("acc")))
                .isInstanceOf(ValidationException.class);
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
        assertThat(client.fields).isNotNull();
        assertThat(client.signerSelf).isNotNull();
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
}
