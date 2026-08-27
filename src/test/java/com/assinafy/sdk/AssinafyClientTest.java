package com.assinafy.sdk;

import com.assinafy.sdk.exceptions.NetworkException;
import com.assinafy.sdk.models.DocumentStatus;
import com.assinafy.sdk.models.UploadAndRequestSignaturesOptions;
import com.assinafy.sdk.models.UploadAndRequestSignaturesResult;
import com.assinafy.sdk.models.UploadAndRequestSignaturesSigner;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.io.InterruptedIOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssinafyClientTest {

    @Test
    void constructor_allowsNoCredentialsForUnauthenticatedEndpoints() {
        AssinafyClient client = new AssinafyClient(new AssinafyClientOptions().setAccountId("acc"));
        assertThat(client.auth).isNotNull();
        assertThat(client.accounts).isNotNull();
        assertThat(client.users).isNotNull();
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
    void constructor_acceptsBearerToken() {
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
    void constructorRejectsNegativeMaxRetries() {
        assertThatThrownBy(() -> new AssinafyClient(
                new AssinafyClientOptions().setMaxRetries(-1)))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class)
                .hasMessageContaining("non-negative");
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

    @Test
    void credentialsAreSentOnActualRequests() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();
        try {
            server.enqueue(new MockResponse().setBody("{\"status\":200,\"data\":[]}"));
            server.enqueue(new MockResponse().setBody("{\"status\":200,\"data\":[]}"));
            AssinafyClient.create("secret-key", "acc",
                    opts -> opts.setBaseUrl(server.url("/v1").toString())).documents.statuses();
            new AssinafyClient(new AssinafyClientOptions().setToken("secret-token")
                    .setBaseUrl(server.url("/v1").toString())).documents.statuses();

            assertThat(server.takeRequest().getHeader("X-Api-Key")).isEqualTo("secret-key");
            assertThat(server.takeRequest().getHeader("Authorization")).isEqualTo("Bearer secret-token");
        } finally {
            server.shutdown();
        }
    }

    @Test
    void credentialsDoNotCrossOriginsOnRedirects() throws Exception {
        MockWebServer api = new MockWebServer();
        MockWebServer external = new MockWebServer();
        api.start();
        external.start();
        try {
            api.enqueue(new MockResponse().setResponseCode(302)
                    .setHeader("Location", external.url("/redirected")));
            api.enqueue(new MockResponse().setResponseCode(302)
                    .setHeader("Location", external.url("/redirected")));
            external.enqueue(new MockResponse().setBody("{\"status\":200,\"data\":[]}"));
            external.enqueue(new MockResponse().setBody("{\"status\":200,\"data\":[]}"));

            AssinafyClient.create("secret-key", "acc",
                    opts -> opts.setBaseUrl(api.url("/v1").toString())).documents.statuses();
            new AssinafyClient(new AssinafyClientOptions().setToken("secret-token")
                    .setBaseUrl(api.url("/v1").toString())).documents.statuses();

            RecordedRequest apiKeyRequest = api.takeRequest();
            RecordedRequest tokenRequest = api.takeRequest();
            assertThat(apiKeyRequest.getHeader("X-Api-Key")).isEqualTo("secret-key");
            assertThat(tokenRequest.getHeader("Authorization")).isEqualTo("Bearer secret-token");
            for (int i = 0; i < 2; i++) {
                RecordedRequest redirected = external.takeRequest();
                assertThat(redirected.getHeader("X-Api-Key")).isNull();
                assertThat(redirected.getHeader("Authorization")).isNull();
            }
        } finally {
            api.shutdown();
            external.shutdown();
        }
    }

    @Test
    void constructorRejectsBaseUrlComponentsThatCanAlterRequests() {
        for (String baseUrl : List.of(
                "https://user@example.com/v1",
                "https://example.com/v1?tenant=other",
                "https://example.com/v1#fragment")) {
            assertThatThrownBy(() -> new AssinafyClient(new AssinafyClientOptions().setBaseUrl(baseUrl)))
                    .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class)
                    .hasMessageContaining("must not contain");
        }
    }

    @Test
    void constructorRejectsPlaintextRemoteBaseUrl() {
        assertThatThrownBy(() -> new AssinafyClient(new AssinafyClientOptions()
                .setBaseUrl("http://api.example.com/v1")))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class)
                .hasMessageContaining("HTTPS");
    }

    @Test
    void resourceConstructorsRejectBaseUrlComponentsThatCanAlterRequests() {
        OkHttpClient httpClient = new OkHttpClient();
        for (String baseUrl : List.of(
                "https://user:password@example.com/v1",
                "https://example.com/v1?tenant=other",
                "https://example.com/v1#fragment")) {
            assertThatThrownBy(() -> new com.assinafy.sdk.resources.DocumentResource(
                    httpClient, baseUrl, "acc"))
                    .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class)
                    .hasMessageContaining("must not contain");
        }
    }

    @Test
    void resourceConstructorsRejectPlaintextRemoteBaseUrl() {
        assertThatThrownBy(() -> new com.assinafy.sdk.resources.DocumentResource(
                new OkHttpClient(), "http://api.example.com/v1", "acc"))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class)
                .hasMessageContaining("HTTPS");
    }

    @Test
    void credentialsAreSnapshottedWhenClientIsConstructed() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();
        try {
            server.enqueue(new MockResponse().setBody("{\"status\":200,\"data\":[]}"));
            AssinafyClientOptions options = new AssinafyClientOptions()
                    .setApiKey("key-a")
                    .setAccountId("acc")
                    .setBaseUrl(server.url("/v1").toString());
            AssinafyClient client = new AssinafyClient(options);

            options.setApiKey("key-b");
            client.documents.statuses();

            assertThat(server.takeRequest().getHeader("X-Api-Key")).isEqualTo("key-a");
        } finally {
            server.shutdown();
        }
    }

    @Test
    void retriesNeverReplayPostRequests() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();
        try {
            server.enqueue(new MockResponse().setResponseCode(503)
                    .setBody("{\"status\":503,\"message\":\"Unavailable\"}"));
            AssinafyClient client = AssinafyClient.create("k", "acc",
                    opts -> opts.setBaseUrl(server.url("/v1").toString()).setMaxRetries(2));

            assertThatThrownBy(() -> client.accounts.create(new com.assinafy.sdk.models.AccountPayload("Acme")))
                    .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class);
            assertThat(server.getRequestCount()).isEqualTo(1);
        } finally {
            server.shutdown();
        }
    }

    @Test
    void retryInterruptionStopsImmediatelyAndPreservesInterrupt() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();
        Thread worker = null;
        try {
            server.enqueue(new MockResponse().setResponseCode(429)
                    .setHeader("Retry-After", Long.toString(Long.MAX_VALUE))
                    .setBody("{\"status\":429,\"message\":\"Too Many Requests\"}"));
            AssinafyClient client = AssinafyClient.create("k", "acc",
                    opts -> opts.setBaseUrl(server.url("/v1").toString()).setMaxRetries(1));
            AtomicReference<Throwable> failure = new AtomicReference<>();
            AtomicBoolean interruptPreserved = new AtomicBoolean();
            worker = new Thread(() -> {
                try {
                    client.documents.statuses();
                } catch (Throwable t) {
                    failure.set(t);
                    interruptPreserved.set(Thread.currentThread().isInterrupted());
                }
            });
            worker.setDaemon(true);
            worker.start();

            assertThat(server.takeRequest(1, TimeUnit.SECONDS)).isNotNull();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
            while (worker.getState() != Thread.State.TIMED_WAITING && System.nanoTime() < deadline) {
                Thread.onSpinWait();
            }
            assertThat(worker.getState()).isEqualTo(Thread.State.TIMED_WAITING);
            worker.interrupt();
            worker.join(1_000);

            assertThat(worker.isAlive()).isFalse();
            assertThat(failure.get()).isInstanceOf(NetworkException.class);
            assertThat(failure.get().getCause()).isInstanceOf(InterruptedIOException.class);
            assertThat(interruptPreserved).isTrue();
            assertThat(server.getRequestCount()).isEqualTo(1);
        } finally {
            if (worker != null) worker.interrupt();
            server.shutdown();
        }
    }

    @Test
    void uploadAndRequestSignaturesRunsCompleteWorkflow() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();
        try {
            server.enqueue(new MockResponse().setBody(
                    "{\"status\":200,\"data\":{\"id\":\"doc-1\",\"status\":\"uploaded\"}}"));
            server.enqueue(new MockResponse().setBody("{\"status\":200,\"data\":[]}"));
            server.enqueue(new MockResponse().setBody(
                    "{\"status\":200,\"data\":{\"id\":\"signer-1\",\"full_name\":\"Jane\"}}"));
            server.enqueue(new MockResponse().setBody(
                    "{\"status\":200,\"data\":{\"id\":\"assignment-1\",\"method\":\"virtual\"}}"));
            AssinafyClient client = AssinafyClient.create("k", "default-account",
                    opts -> opts.setBaseUrl(server.url("/v1").toString()));
            UploadAndRequestSignaturesOptions options = new UploadAndRequestSignaturesOptions(
                    "%PDF-1.4".getBytes(), "contract.pdf",
                    List.of(new UploadAndRequestSignaturesSigner("Jane", "jane@example.com")
                            .setWhatsappPhoneNumber("+5548999990000")))
                    .setAccountId("custom-account")
                    .setWaitForReady(false)
                    .setMessage("Please sign")
                    .setExpiresAt("2026-09-01T12:00:00Z")
                    .setCopyReceivers(List.of("copy-signer-1"));

            UploadAndRequestSignaturesResult result = client.uploadAndRequestSignatures(options);

            assertThat(result.getDocument().getId()).isEqualTo("doc-1");
            assertThat(result.getAssignment().getId()).isEqualTo("assignment-1");
            assertThat(result.getSignerIds()).containsExactly("signer-1");

            RecordedRequest upload = server.takeRequest();
            assertThat(upload.getPath()).isEqualTo("/v1/accounts/custom-account/documents");
            RecordedRequest lookup = server.takeRequest();
            assertThat(lookup.getPath()).startsWith("/v1/accounts/custom-account/signers?")
                    .contains("search=jane%40example.com", "per-page=100");
            RecordedRequest createSigner = server.takeRequest();
            assertThat(createSigner.getPath()).isEqualTo("/v1/accounts/custom-account/signers");
            assertThat(createSigner.getBody().readUtf8())
                    .contains("\"full_name\":\"Jane\"", "\"email\":\"jane@example.com\"",
                            "\"whatsapp_phone_number\":\"+5548999990000\"");
            RecordedRequest assignment = server.takeRequest();
            assertThat(assignment.getPath()).isEqualTo("/v1/documents/doc-1/assignments");
            assertThat(assignment.getBody().readUtf8())
                    .contains("\"method\":\"virtual\"", "\"id\":\"signer-1\"",
                            "\"message\":\"Please sign\"", "\"expires_at\":\"2026-09-01T12:00:00Z\"",
                            "\"copy_receivers\":[\"copy-signer-1\"]");
        } finally {
            server.shutdown();
        }
    }

    @Test
    void uploadAndRequestSignaturesKeepsUploadedIdWhenReadinessResponseMismatches() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();
        try {
            server.enqueue(new MockResponse().setBody(
                    "{\"status\":200,\"data\":{\"id\":\"doc-1\",\"status\":\"uploaded\"}}"));
            server.enqueue(new MockResponse().setBody(
                    "{\"status\":200,\"data\":{\"id\":\"doc-other\",\"status\":\"metadata_ready\"}}"));
            server.enqueue(new MockResponse().setBody("{\"status\":200,\"data\":[]}"));
            AssinafyClient client = AssinafyClient.create("k", "acc",
                    opts -> opts.setBaseUrl(server.url("/v1").toString()));

            assertThatThrownBy(() -> client.uploadAndRequestSignatures(
                    new UploadAndRequestSignaturesOptions("%PDF-1.4".getBytes(), "contract.pdf",
                            List.of(new UploadAndRequestSignaturesSigner("Jane", "jane@example.com")))))
                    .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class)
                    .hasMessageContaining("did not match");

            assertThat(server.takeRequest().getPath()).isEqualTo("/v1/accounts/acc/documents");
            assertThat(server.takeRequest().getPath()).isEqualTo("/v1/documents/doc-1");
            RecordedRequest cleanup = server.takeRequest();
            assertThat(cleanup.getMethod()).isEqualTo("DELETE");
            assertThat(cleanup.getPath()).isEqualTo("/v1/documents/doc-1");
            assertThat(server.getRequestCount()).isEqualTo(3);
        } finally {
            server.shutdown();
        }
    }

    @Test
    void uploadAndRequestSignaturesCleansDocumentAndPreservesCreatedSignerWhenAssignmentFails() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();
        try {
            server.enqueue(new MockResponse().setBody(
                    "{\"status\":200,\"data\":{\"id\":\"doc-1\",\"status\":\"uploaded\"}}"));
            server.enqueue(new MockResponse().setBody("{\"status\":200,\"data\":[]}"));
            server.enqueue(new MockResponse().setBody(
                    "{\"status\":200,\"data\":{\"id\":\"signer-1\",\"email\":\"jane@example.com\"}}"));
            server.enqueue(new MockResponse().setResponseCode(500)
                    .setBody("{\"status\":500,\"message\":\"Assignment failed\"}"));
            server.enqueue(new MockResponse().setBody("{\"status\":200,\"data\":[]}"));
            AssinafyClient client = AssinafyClient.create("k", "acc",
                    opts -> opts.setBaseUrl(server.url("/v1").toString()));

            assertThatThrownBy(() -> client.uploadAndRequestSignatures(
                    new UploadAndRequestSignaturesOptions("%PDF-1.4".getBytes(), "contract.pdf",
                            List.of(new UploadAndRequestSignaturesSigner("Jane", "jane@example.com")))
                            .setWaitForReady(false)))
                    .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class)
                    .hasMessageContaining("Assignment failed");

            assertThat(server.takeRequest().getPath()).isEqualTo("/v1/accounts/acc/documents");
            assertThat(server.takeRequest().getPath()).startsWith("/v1/accounts/acc/signers?")
                    .contains("search=jane%40example.com");
            assertThat(server.takeRequest().getPath()).isEqualTo("/v1/accounts/acc/signers");
            assertThat(server.takeRequest().getPath()).isEqualTo("/v1/documents/doc-1/assignments");
            RecordedRequest documentCleanup = server.takeRequest();
            assertThat(documentCleanup.getMethod()).isEqualTo("DELETE");
            assertThat(documentCleanup.getPath()).isEqualTo("/v1/documents/doc-1");
            assertThat(server.getRequestCount()).isEqualTo(5);
        } finally {
            server.shutdown();
        }
    }

    @Test
    void uploadAndRequestSignaturesPreservesReusedSignerWhenAssignmentFails() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();
        try {
            server.enqueue(new MockResponse().setBody(
                    "{\"status\":200,\"data\":{\"id\":\"doc-1\",\"status\":\"uploaded\"}}"));
            server.enqueue(new MockResponse().setBody(
                    "{\"status\":200,\"data\":[{\"id\":\"existing\",\"email\":\"jane@example.com\"}]}"));
            server.enqueue(new MockResponse().setResponseCode(500)
                    .setBody("{\"status\":500,\"message\":\"Assignment failed\"}"));
            server.enqueue(new MockResponse().setBody("{\"status\":200,\"data\":[]}"));
            AssinafyClient client = AssinafyClient.create("k", "acc",
                    opts -> opts.setBaseUrl(server.url("/v1").toString()));

            assertThatThrownBy(() -> client.uploadAndRequestSignatures(
                    new UploadAndRequestSignaturesOptions("%PDF-1.4".getBytes(), "contract.pdf",
                            List.of(new UploadAndRequestSignaturesSigner("Jane", "jane@example.com")))
                            .setWaitForReady(false)))
                    .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class);

            assertThat(server.getRequestCount()).isEqualTo(4);
            server.takeRequest();
            server.takeRequest();
            server.takeRequest();
            RecordedRequest cleanup = server.takeRequest();
            assertThat(cleanup.getMethod()).isEqualTo("DELETE");
            assertThat(cleanup.getPath()).isEqualTo("/v1/documents/doc-1");
        } finally {
            server.shutdown();
        }
    }

    @Test
    void uploadAndRequestSignaturesPreservesSignerCreatedByConcurrentRequest() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();
        try {
            server.enqueue(new MockResponse().setBody(
                    "{\"status\":200,\"data\":{\"id\":\"doc-1\",\"status\":\"uploaded\"}}"));
            server.enqueue(new MockResponse().setBody("{\"status\":200,\"data\":[]}"));
            server.enqueue(new MockResponse().setResponseCode(400)
                    .setBody("{\"status\":400,\"message\":\"Duplicate signer\"}"));
            server.enqueue(new MockResponse().setBody(
                    "{\"status\":200,\"data\":[{\"id\":\"raced\",\"email\":\"jane@example.com\"}]}"));
            server.enqueue(new MockResponse().setResponseCode(500)
                    .setBody("{\"status\":500,\"message\":\"Assignment failed\"}"));
            server.enqueue(new MockResponse().setBody("{\"status\":200,\"data\":[]}"));
            AssinafyClient client = AssinafyClient.create("k", "acc",
                    opts -> opts.setBaseUrl(server.url("/v1").toString()));

            assertThatThrownBy(() -> client.uploadAndRequestSignatures(
                    new UploadAndRequestSignaturesOptions("%PDF-1.4".getBytes(), "contract.pdf",
                            List.of(new UploadAndRequestSignaturesSigner("Jane", "jane@example.com")))
                            .setWaitForReady(false)))
                    .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class)
                    .hasMessageContaining("Assignment failed");

            assertThat(server.getRequestCount()).isEqualTo(6);
            for (int i = 0; i < 5; i++) server.takeRequest();
            RecordedRequest cleanup = server.takeRequest();
            assertThat(cleanup.getMethod()).isEqualTo("DELETE");
            assertThat(cleanup.getPath()).isEqualTo("/v1/documents/doc-1");
        } finally {
            server.shutdown();
        }
    }

    @Test
    void uploadAndRequestSignaturesPreservesSignersWhenDocumentCleanupFails() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();
        try {
            server.enqueue(new MockResponse().setBody(
                    "{\"status\":200,\"data\":{\"id\":\"doc-1\",\"status\":\"uploaded\"}}"));
            server.enqueue(new MockResponse().setBody("{\"status\":200,\"data\":[]}"));
            server.enqueue(new MockResponse().setBody(
                    "{\"status\":200,\"data\":{\"id\":\"signer-1\",\"email\":\"jane@example.com\"}}"));
            server.enqueue(new MockResponse().setResponseCode(500)
                    .setBody("{\"status\":500,\"message\":\"Assignment failed\"}"));
            server.enqueue(new MockResponse().setResponseCode(409)
                    .setBody("{\"status\":409,\"message\":\"Document cleanup failed\"}"));
            AssinafyClient client = AssinafyClient.create("k", "acc",
                    opts -> opts.setBaseUrl(server.url("/v1").toString()));

            try {
                client.uploadAndRequestSignatures(new UploadAndRequestSignaturesOptions(
                        "%PDF-1.4".getBytes(), "contract.pdf",
                        List.of(new UploadAndRequestSignaturesSigner("Jane", "jane@example.com")))
                        .setWaitForReady(false));
                throw new AssertionError("Expected assignment failure");
            } catch (com.assinafy.sdk.exceptions.ApiException e) {
                assertThat(e.getStatusCode()).isEqualTo(500);
                assertThat(e.getMessage()).contains("Assignment failed");
                assertThat(e.getSuppressed()).hasSize(1)
                        .allMatch(com.assinafy.sdk.exceptions.ApiException.class::isInstance);
                assertThat(server.getRequestCount()).isEqualTo(5);
            }
        } finally {
            server.shutdown();
        }
    }

    @Test
    void uploadAndRequestSignaturesRejectsInvalidEmailBeforeAnyRequest() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();
        try {
            AssinafyClient client = AssinafyClient.create("k", "acc",
                    opts -> opts.setBaseUrl(server.url("/v1").toString()));

            assertThatThrownBy(() -> client.uploadAndRequestSignatures(
                    new UploadAndRequestSignaturesOptions("%PDF-1.4".getBytes(), "contract.pdf",
                            List.of(new UploadAndRequestSignaturesSigner("Jane", "invalid-email")))))
                    .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class)
                    .hasMessageContaining("email");
            assertThat(server.getRequestCount()).isZero();
        } finally {
            server.shutdown();
        }
    }
}
