package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.CreateFieldPayload;
import com.assinafy.sdk.models.FieldDefinition;
import com.assinafy.sdk.models.FieldTypeInfo;
import com.assinafy.sdk.models.FieldValidationPayload;
import com.assinafy.sdk.models.FieldValidationResult;
import com.assinafy.sdk.models.UpdateFieldPayload;
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

class FieldResourceTest {

    private MockWebServer server;
    private FieldResource resource;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        resource = new FieldResource(new OkHttpClient(), server.url("/").toString(), "acc");
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
    void create_postsDocumentedBody() throws Exception {
        server.enqueue(okJson(Map.of("id", "field-1", "name", "CPF", "type", "cpf")));

        FieldDefinition result = resource.create(new CreateFieldPayload("cpf", "CPF").setRequired(true));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/accounts/acc/fields");
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getBody().readUtf8()).contains("\"type\":\"cpf\"", "\"name\":\"CPF\"",
                "\"is_required\":true");
        assertThat(result.getId()).isEqualTo("field-1");
    }

    @Test
    void list_passesDocumentedFilters() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.list(Map.of("include_inactive", "true", "include_standard", "true"));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).contains("/accounts/acc/fields");
        assertThat(req.getPath()).contains("include_inactive=true");
        assertThat(req.getPath()).contains("include_standard=true");
    }

    @Test
    void update_putsToDocumentedEndpoint() throws Exception {
        server.enqueue(okJson(Map.of("id", "field-1", "name", "New Field Name", "type", "text")));

        FieldDefinition result = resource.update("field-1", new UpdateFieldPayload().setName("New Field Name"));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/accounts/acc/fields/field-1");
        assertThat(req.getMethod()).isEqualTo("PUT");
        assertThat(result.getName()).isEqualTo("New Field Name");
    }

    @Test
    void validate_addsSignerAccessCodeWhenProvided() throws Exception {
        server.enqueue(okJson(Map.of("type", "cpf", "success", true, "error_message", "")));

        FieldValidationResult result = resource.validate("field-1", "400.676.228-36", "access-code");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).contains("/accounts/acc/fields/field-1/validate");
        assertThat(req.getPath()).contains("signer-access-code=access-code");
        assertThat(result.getSuccess()).isTrue();
    }

    @Test
    void validate_allowsNullValueWithoutNpe() throws Exception {
        // A null value must be forwarded as {"value":null}, not throw NullPointerException client-side.
        server.enqueue(okJson(Map.of("type", "text", "success", false, "error_message", "Required")));

        FieldValidationResult result = resource.validate("field-1", null);

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getBody().readUtf8()).isEqualTo("{\"value\":null}");
        assertThat(result.getSuccess()).isFalse();
    }

    @Test
    void validateMultiple_postsValues() throws Exception {
        server.enqueue(okJson(List.of(Map.of("field_id", "field-1", "type", "email", "success", true,
                "error_message", ""))));

        List<FieldValidationResult> result = resource.validateMultiple(List.of(
                new FieldValidationPayload("field-1", "test@example.com")));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/accounts/acc/fields/validate-multiple");
        assertThat(req.getBody().readUtf8()).contains("\"field_id\":\"field-1\"");
        assertThat(result).hasSize(1);
    }

    @Test
    void listTypes_callsGlobalFieldTypesEndpoint() throws Exception {
        server.enqueue(okJson(List.of(Map.of("type", "text", "name", "Text"))));

        List<FieldTypeInfo> result = resource.listTypes();

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/field-types");
        assertThat(result.get(0).getType()).isEqualTo("text");
    }

    @Test
    void create_requiresTypeAndName() {
        assertThatThrownBy(() -> resource.create(new CreateFieldPayload(null, "Name")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.create(new CreateFieldPayload("text", "")))
                .isInstanceOf(ValidationException.class);
    }
}
