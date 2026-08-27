package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.CreateSignerPayload;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.models.UpdateSignerPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignerResourceTest {

    private MockWebServer server;
    private SignerResource resource;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        resource = new SignerResource(new OkHttpClient(), server.url("/").toString(), "test-account");
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private MockResponse okJson(Object data) throws Exception {
        String body = MAPPER.writeValueAsString(Map.of("status", 200, "data", data));
        return new MockResponse().setBody(body).setHeader("Content-Type", "application/json");
    }

    private MockResponse okList(List<?> data) throws Exception {
        String body = MAPPER.writeValueAsString(Map.of("status", 200, "data", data));
        return new MockResponse().setBody(body).setHeader("Content-Type", "application/json");
    }

    @Test
    void update_throwsWhenNoSignerId() {
        assertThatThrownBy(() -> resource.update("", new UpdateSignerPayload().setFullName("Test")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void delete_throwsWhenNoSignerId() {
        assertThatThrownBy(() -> resource.delete(""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_throwsWhenNoAccountId() {
        SignerResource noAccount = new SignerResource(new OkHttpClient(), server.url("/").toString(), null);
        assertThatThrownBy(() -> noAccount.create(new CreateSignerPayload("Test", "test@example.com")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_throwsOnInvalidEmail() {
        assertThatThrownBy(() -> resource.create(new CreateSignerPayload("Test", "not-an-email")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("email");
    }

    @Test
    void create_usesCustomAccountId() throws Exception {
        server.enqueue(okJson(Map.of("id", "123", "full_name", "Test", "email", "test@example.com")));

        resource.create(new CreateSignerPayload("Test", "test@example.com"), "custom-account");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/accounts/custom-account/signers");
    }

    @Test
    void create_usesDefaultAccountId() throws Exception {
        server.enqueue(okJson(Map.of("id", "123", "full_name", "Test", "email", "test@example.com")));

        resource.create(new CreateSignerPayload("Test", "test@example.com"));

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/accounts/test-account/signers");
    }

    @Test
    void list_passesSearchViaQueryParams() throws Exception {
        server.enqueue(okList(List.of()));

        resource.list(Map.of("search", "john@example.com", "per_page", "20"));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).contains("search=john%40example.com");
        assertThat(req.getPath()).contains("per-page=20");
    }

    @Test
    void list_returnsPaginationMetaFromHeaders() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of("status", 200, "data", List.of()));
        server.enqueue(new MockResponse()
                .setBody(body)
                .setHeader("Content-Type", "application/json")
                .setHeader("x-pagination-current-page", "2")
                .setHeader("x-pagination-per-page", "20")
                .setHeader("x-pagination-total-count", "45")
                .setHeader("x-pagination-page-count", "3"));

        PaginatedResult<Signer> result = resource.list(Map.of("page", "2"));

        assertThat(result.getMeta()).isNotNull();
        assertThat(result.getMeta().getCurrentPage()).isEqualTo(2);
        assertThat(result.getMeta().getPerPage()).isEqualTo(20);
        assertThat(result.getMeta().getTotal()).isEqualTo(45);
        assertThat(result.getMeta().getLastPage()).isEqualTo(3);
    }

    @Test
    void findByEmail_returnsNullWhenNoMatch() throws Exception {
        server.enqueue(okList(List.of()));

        Signer result = resource.findByEmail("nobody@example.com");
        assertThat(result).isNull();
    }

    @Test
    void findByEmail_returnsMatchingSigner() throws Exception {
        server.enqueue(okList(List.of(
                Map.of("id", "1", "full_name", "John", "email", "JOHN@EXAMPLE.COM")
        )));

        Signer result = resource.findByEmail("john@example.com");
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
    }

    @Test
    void findByEmailSearchesEveryReportedPage() throws Exception {
        server.enqueue(okList(List.of(
                        Map.of("id", "1", "full_name", "Other", "email", "other@example.com")))
                .setHeader("x-pagination-current-page", "1")
                .setHeader("x-pagination-page-count", "2"));
        server.enqueue(okList(List.of(
                        Map.of("id", "2", "full_name", "John", "email", "JOHN@EXAMPLE.COM")))
                .setHeader("x-pagination-current-page", "2")
                .setHeader("x-pagination-page-count", "2"));

        Signer result = resource.findByEmail("john@example.com");

        assertThat(result.getId()).isEqualTo("2");
        assertThat(server.takeRequest().getPath()).contains("page=1", "per-page=100");
        assertThat(server.takeRequest().getPath()).contains("page=2", "per-page=100");
    }

    @Test
    void findOrCreate_reusesExistingSignerByEmail() throws Exception {
        server.enqueue(okList(List.of(
                Map.of("id", "existing", "full_name", "John", "email", "john@example.com")
        )));

        Signer result = resource.findOrCreate(new CreateSignerPayload("John", "john@example.com"));

        assertThat(result.getId()).isEqualTo("existing");
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void findOrCreate_recoversFromDuplicate400ByReturningExisting() throws Exception {
        // A duplicate response can race with the pre-check; the recovery lookup returns the exact match.
        server.enqueue(okList(List.of()));
        server.enqueue(new MockResponse().setResponseCode(400)
                .setBody("{\"status\":400,\"data\":null,\"message\":\"Um signatário com este e-mail já existe.\"}")
                .setHeader("Content-Type", "application/json"));
        server.enqueue(okList(List.of(
                Map.of("id", "existing", "full_name", "John", "email", "john@example.com"))));

        Signer result = resource.findOrCreate(new CreateSignerPayload("John", "john@example.com"));

        assertThat(result.getId()).isEqualTo("existing");
        assertThat(server.getRequestCount()).isEqualTo(3);
    }

    @Test
    void findOrCreate_rethrowsNonDuplicate400() throws Exception {
        // When the recovery lookup finds no exact match, the original error must surface.
        server.enqueue(okList(List.of()));
        server.enqueue(new MockResponse().setResponseCode(400)
                .setBody("{\"status\":400,\"data\":null,\"message\":\"Email inválido.\"}")
                .setHeader("Content-Type", "application/json"));
        server.enqueue(okList(List.of()));

        assertThatThrownBy(() -> resource.findOrCreate(new CreateSignerPayload("John", "john@example.com")))
                .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class)
                .hasMessageContaining("Email inválido.");
    }

    @Test
    void findOrCreatePreservesDuplicateWhenRecoveryLookupFails() throws Exception {
        server.enqueue(okList(List.of()));
        server.enqueue(new MockResponse().setResponseCode(409)
                .setBody("{\"status\":409,\"message\":\"Duplicate signer\"}")
                .setHeader("Content-Type", "application/json"));
        server.enqueue(new MockResponse().setResponseCode(503)
                .setBody("{\"status\":503,\"message\":\"Lookup unavailable\"}")
                .setHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> resource.findOrCreate(new CreateSignerPayload("John", "john@example.com")))
                .isInstanceOfSatisfying(com.assinafy.sdk.exceptions.ApiException.class, error -> {
                    assertThat(error.getStatusCode()).isEqualTo(409);
                    assertThat(error.getSuppressed()).singleElement()
                            .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class)
                            .extracting(suppressed -> ((com.assinafy.sdk.exceptions.ApiException) suppressed)
                                    .getStatusCode())
                            .isEqualTo(503);
                });
    }

    @Test
    void get_hitsSignerEndpoint() throws Exception {
        server.enqueue(okJson(Map.of("id", "s1", "full_name", "John", "email", "john@example.com")));

        Signer signer = resource.get("s1");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("GET");
        assertThat(req.getPath()).isEqualTo("/accounts/test-account/signers/s1");
        assertThat(signer.getFullName()).isEqualTo("John");
    }

    @Test
    void updateAndDelete_hitSignerEndpoints() throws Exception {
        server.enqueue(okJson(Map.of("id", "s1", "full_name", "Johnny")));
        server.enqueue(okJson(List.of()));

        Signer updated = resource.update("s1", new UpdateSignerPayload().setFullName("Johnny"));
        resource.delete("s1");

        RecordedRequest put = server.takeRequest();
        assertThat(put.getMethod()).isEqualTo("PUT");
        assertThat(put.getPath()).isEqualTo("/accounts/test-account/signers/s1");
        assertThat(updated.getFullName()).isEqualTo("Johnny");
        RecordedRequest del = server.takeRequest();
        assertThat(del.getMethod()).isEqualTo("DELETE");
        assertThat(del.getPath()).isEqualTo("/accounts/test-account/signers/s1");
    }

    @Test
    void update_serializesAndParsesGovernmentId() throws Exception {
        server.enqueue(okJson(Map.of("id", "s1", "government_id", "15774136604")));

        Signer updated = resource.update("s1",
                new UpdateSignerPayload().setGovernmentId("15774136604"));

        RecordedRequest request = server.takeRequest();
        assertThat(request.getBody().readUtf8())
                .isEqualTo("{\"government_id\":\"15774136604\"}");
        assertThat(updated.getGovernmentId()).isEqualTo("15774136604");
    }

    @Test
    void update_rejectsEmptyPayloadWithoutSendingRequest() {
        assertThatThrownBy(() -> resource.update("s1", new UpdateSignerPayload()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("At least one signer attribute");
        assertThatThrownBy(() -> resource.update("s1",
                new UpdateSignerPayload().setFullName("John").setEmail(" ")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("email");
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void create_serialisesWhatsappPhoneNumber() throws Exception {
        server.enqueue(okJson(Map.of("id", "123", "full_name", "John", "email", "john@example.com")));

        resource.create(new CreateSignerPayload("John", "john@example.com")
                .setWhatsappPhoneNumber("+5548999990000"));

        RecordedRequest postRequest = server.takeRequest();
        String body = postRequest.getBody().readUtf8();
        assertThat(body).contains("whatsapp_phone_number");
        assertThat(body).contains("+5548999990000");
        assertThat(body).doesNotContain("\"phone\"");
    }

    @Test
    void create_allowsWhatsappOnlySigner() throws Exception {
        server.enqueue(okJson(Map.of("id", "123", "full_name", "John",
                "whatsapp_phone_number", "+5548999990000")));

        resource.create(new CreateSignerPayload("John", null).setWhatsappPhoneNumber("+5548999990000"));

        RecordedRequest postRequest = server.takeRequest();
        String body = postRequest.getBody().readUtf8();
        assertThat(body).contains("whatsapp_phone_number");
        assertThat(body).doesNotContain("\"email\"");
    }
}
