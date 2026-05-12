package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.TemplateDetails;
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

class TemplateResourceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private MockWebServer server;
    private TemplateResource resource;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        resource = new TemplateResource(new OkHttpClient(), server.url("/").toString(), "acc");
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private MockResponse okJson(Object data) throws Exception {
        return new MockResponse()
                .setBody(MAPPER.writeValueAsString(Map.of("status", 200, "data", data)))
                .setHeader("Content-Type", "application/json");
    }

    @Test
    void list_hitsAccountScopedPath() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.list();

        assertThat(server.takeRequest().getPath()).isEqualTo("/accounts/acc/templates");
    }

    @Test
    void list_passesSearchAndPaginationQuery() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.list(Map.of("search", "NDA", "per_page", "10"));

        String path = server.takeRequest().getPath();
        assertThat(path).contains("search=NDA");
        assertThat(path).contains("per-page=10");
    }

    @Test
    void get_returnsParsedTemplate() throws Exception {
        server.enqueue(okJson(Map.of("id", "tmpl-1", "name", "Sample")));

        TemplateDetails details = resource.get("tmpl-1");

        assertThat(server.takeRequest().getPath()).isEqualTo("/accounts/acc/templates/tmpl-1");
        assertThat(details.getId()).isEqualTo("tmpl-1");
    }

    @Test
    void get_requiresTemplateId() {
        assertThatThrownBy(() -> resource.get(""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void get_propagatesApiErrorsAsApiException() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(MAPPER.writeValueAsString(Map.of("status", 404, "message", "Template não encontrado.")))
                .setHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> resource.get("unknown"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("não encontrado");
    }
}
