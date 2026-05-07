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
        assertThatThrownBy(() -> noAccount.create(new CreateSignerPayload("Test", "test@test.com")))
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
        server.enqueue(okList(List.of()));
        server.enqueue(okJson(Map.of("id", "123", "full_name", "Test", "email", "test@test.com")));

        resource.create(new CreateSignerPayload("Test", "test@test.com"), "custom-account");

        RecordedRequest findRequest = server.takeRequest();
        assertThat(findRequest.getPath()).contains("/accounts/custom-account/signers");
    }

    @Test
    void create_usesDefaultAccountId() throws Exception {
        server.enqueue(okList(List.of()));
        server.enqueue(okJson(Map.of("id", "123", "full_name", "Test", "email", "test@test.com")));

        resource.create(new CreateSignerPayload("Test", "test@test.com"));

        RecordedRequest findRequest = server.takeRequest();
        assertThat(findRequest.getPath()).contains("/accounts/test-account/signers");
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
    void create_reusesExistingSignerByEmail() throws Exception {
        server.enqueue(okList(List.of(
                Map.of("id", "existing", "full_name", "John", "email", "john@example.com")
        )));

        Signer result = resource.create(new CreateSignerPayload("John", "john@example.com"));

        assertThat(result.getId()).isEqualTo("existing");
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void create_serialisesWhatsappPhoneNumber() throws Exception {
        server.enqueue(okList(List.of()));
        server.enqueue(okJson(Map.of("id", "123", "full_name", "John", "email", "john@example.com")));

        resource.create(new CreateSignerPayload("John", "john@example.com")
                .setWhatsappPhoneNumber("+5548999990000"));

        server.takeRequest();
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
