package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.CreateTagPayload;
import com.assinafy.sdk.models.Tag;
import com.assinafy.sdk.models.UpdateTagPayload;
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

class TagResourceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private MockWebServer server;
    private TagResource resource;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        resource = new TagResource(new OkHttpClient(), server.url("/").toString(), "acc");
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
    void list_hitsAccountTagsEndpoint() throws Exception {
        server.enqueue(okJson(List.of(Map.of("id", "tag-1", "name", "Contracts", "color", "ff8800"))));

        var page = resource.list(Map.of("search", "contract"));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/accounts/acc/tags?search=contract");
        assertThat(page.getData().get(0).getName()).isEqualTo("Contracts");
    }

    @Test
    void create_postsDocumentedPayload() throws Exception {
        server.enqueue(okJson(Map.of("id", "tag-1", "name", "Contracts", "color", "ff8800")));

        Tag tag = resource.create(new CreateTagPayload("Contracts").setColor("ff8800"));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath()).isEqualTo("/accounts/acc/tags");
        assertThat(req.getBody().readUtf8()).contains("\"name\":\"Contracts\"", "\"color\":\"ff8800\"");
        assertThat(tag.getId()).isEqualTo("tag-1");
    }

    @Test
    void update_omitsUnsetColorAndCanClearColor() throws Exception {
        server.enqueue(okJson(Map.of("id", "tag-1", "name", "Sales Contracts")));
        server.enqueue(okJson(Map.of("id", "tag-1", "name", "Sales Contracts")));

        resource.update("tag-1", new UpdateTagPayload().setName("Sales Contracts"));
        resource.update("tag-1", new UpdateTagPayload().clearColor());

        String renameBody = server.takeRequest().getBody().readUtf8();
        assertThat(renameBody).contains("\"name\":\"Sales Contracts\"");
        assertThat(renameBody).doesNotContain("color");

        String clearBody = server.takeRequest().getBody().readUtf8();
        assertThat(clearBody).contains("\"color\":null");
    }

    @Test
    void delete_passesForceQueryWhenRequested() throws Exception {
        server.enqueue(okJson(Map.of("deleted", true)));

        Map<String, Object> result = resource.delete("tag-1", true);

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("DELETE");
        assertThat(req.getPath()).isEqualTo("/accounts/acc/tags/tag-1?force=true");
        assertThat(result).containsEntry("deleted", true);
    }

    @Test
    void validatesPayloads() {
        assertThatThrownBy(() -> resource.create(new CreateTagPayload("")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.update("tag-1", new UpdateTagPayload()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.delete(""))
                .isInstanceOf(ValidationException.class);
    }
}
