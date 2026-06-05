package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.AuthenticationResult;
import com.assinafy.sdk.models.SocialLoginPayload;
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

class AuthenticationResourceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private MockWebServer server;
    private AuthenticationResource resource;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        resource = new AuthenticationResource(new OkHttpClient(), server.url("/").toString());
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
    void login_postsCredentialsAndParsesSession() throws Exception {
        server.enqueue(okJson(Map.of(
                "access_token", "jwt",
                "user", Map.of("id", "user-1", "email", "me@example.com"),
                "accounts", List.of(Map.of("id", "acc", "name", "Workspace", "roles", List.of("owner")))
        )));

        AuthenticationResult result = resource.login("me@example.com", "secret");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath()).isEqualTo("/login");
        assertThat(req.getBody().readUtf8()).contains("\"email\":\"me@example.com\"", "\"password\":\"secret\"");
        assertThat(result.getAccessToken()).isEqualTo("jwt");
        assertThat(result.getUser().getId()).isEqualTo("user-1");
        assertThat(result.getAccounts().get(0).getId()).isEqualTo("acc");
    }

    @Test
    void socialLogin_postsDocumentedBody() throws Exception {
        server.enqueue(okJson(Map.of("access_token", "jwt")));

        resource.socialLogin(new SocialLoginPayload("google", "provider-token", true));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/authentication/social-login");
        assertThat(req.getBody().readUtf8()).contains("\"provider\":\"google\"",
                "\"token\":\"provider-token\"", "\"has_accepted_terms\":true");
    }

    @Test
    void apiKeyMethodsUseDocumentedEndpoints() throws Exception {
        server.enqueue(okJson(Map.of("api_key", "masked")));
        server.enqueue(okJson(Map.of("api_key", "new-key")));
        server.enqueue(okJson(List.of()));

        assertThat(resource.getApiKey().getApiKey()).isEqualTo("masked");
        assertThat(resource.createApiKey("secret").getApiKey()).isEqualTo("new-key");
        resource.deleteApiKey();

        assertThat(server.takeRequest().getPath()).isEqualTo("/users/api-keys");
        RecordedRequest create = server.takeRequest();
        assertThat(create.getMethod()).isEqualTo("POST");
        assertThat(create.getBody().readUtf8()).contains("\"password\":\"secret\"");
        RecordedRequest delete = server.takeRequest();
        assertThat(delete.getMethod()).isEqualTo("DELETE");
        assertThat(delete.getPath()).isEqualTo("/users/api-keys");
    }

    @Test
    void passwordMethodsUseDocumentedEndpoints() throws Exception {
        server.enqueue(okJson(Map.of("email", "me@example.com")));
        server.enqueue(okJson(Map.of("email", "me@example.com")));
        server.enqueue(okJson(Map.of("email", "me@example.com")));

        resource.changePassword("me@example.com", "old", "new");
        resource.requestPasswordReset("me@example.com");
        resource.resetPassword("me@example.com", "token", "new");

        assertThat(server.takeRequest().getPath()).isEqualTo("/authentication/change-password");
        assertThat(server.takeRequest().getPath()).isEqualTo("/authentication/request-password-reset");
        assertThat(server.takeRequest().getPath()).isEqualTo("/authentication/reset-password");
    }

    @Test
    void resetPassword_omitsTokenWhenAbsentButKeepsEmailAndNewPassword() throws Exception {
        server.enqueue(okJson(Map.of("email", "me@example.com")));

        // The docs mark `token` optional; a null token must be accepted and simply omitted from the body.
        resource.resetPassword("me@example.com", null, "new-secret");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/authentication/reset-password");
        String body = req.getBody().readUtf8();
        assertThat(body).contains("\"email\":\"me@example.com\"", "\"new_password\":\"new-secret\"");
        assertThat(body).doesNotContain("\"token\"");
    }

    @Test
    void resetPassword_includesTokenWhenProvided() throws Exception {
        server.enqueue(okJson(Map.of("email", "me@example.com")));

        resource.resetPassword("me@example.com", "reset-tok", "new-secret");

        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"token\":\"reset-tok\"");
    }

    @Test
    void resetPassword_stillRequiresEmailAndNewPassword() {
        assertThatThrownBy(() -> resource.resetPassword("", null, "new"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.resetPassword("me@example.com", "tok", ""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validatesRequiredFields() {
        assertThatThrownBy(() -> resource.login("", "secret")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.socialLogin(new SocialLoginPayload("google", "token", null)))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.createApiKey("")).isInstanceOf(ValidationException.class);
    }
}
