package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.AuthenticationResult;
import com.assinafy.sdk.models.MfaMethods;
import com.assinafy.sdk.models.SocialLoginPayload;
import com.assinafy.sdk.models.TotpEnrollment;
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
    void loginRejectsInvalidEmailWithoutSendingRequest() {
        assertThatThrownBy(() -> resource.login("not-an-email", "password"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("email");
        assertThat(server.getRequestCount()).isZero();
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
    void linkSocialLogin_postsProviderAndTokenOnly() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.linkSocialLogin("google", "provider-token");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath()).isEqualTo("/auth/link-social-login");
        String body = req.getBody().readUtf8();
        assertThat(body).contains("\"provider\":\"google\"", "\"token\":\"provider-token\"");
        assertThat(body).doesNotContain("has_accepted_terms");
    }

    @Test
    void linkSocialLogin_validatesRequiredFields() {
        assertThatThrownBy(() -> resource.linkSocialLogin("", "token")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.linkSocialLogin("google", "")).isInstanceOf(ValidationException.class);
    }

    @Test
    void getApiKey_returnsNullWhenEnvelopeDataIsNull() throws Exception {
        // The API returns data:null when no key has been generated yet; getApiKey() must surface that as null.
        server.enqueue(new MockResponse()
                .setBody("{\"status\":200,\"message\":\"\",\"data\":null}")
                .setHeader("Content-Type", "application/json"));

        assertThat(resource.getApiKey()).isNull();
    }

    @Test
    void login_surfacesEnvelopeErrorAsApiException() throws Exception {
        // A status>=400 envelope (even under HTTP 200/401) must raise ApiException with the code + message.
        server.enqueue(new MockResponse().setResponseCode(401)
                .setBody("{\"status\":401,\"data\":null,\"message\":\"Credenciais inválidas.\"}")
                .setHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> resource.login("me@example.com", "wrong"))
                .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class)
                .hasMessageContaining("Credenciais inválidas.");
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

    @Test
    void login_exposesTheTwoFactorChallenge() throws Exception {
        server.enqueue(okJson(Map.of("mfa_token", "challenge")));

        AuthenticationResult result = resource.login("me@example.com", "secret");

        assertThat(result.getAccessToken()).isNull();
        assertThat(result.getMfaToken()).isEqualTo("challenge");
    }

    @Test
    void verifyMfa_postsTheChallengeAndCode() throws Exception {
        server.enqueue(okJson(Map.of("access_token", "jwt")));

        assertThat(resource.verifyMfa("challenge", "123456").getAccessToken()).isEqualTo("jwt");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath()).isEqualTo("/authentication/mfa/verify");
        assertThat(MAPPER.readTree(req.getBody().readUtf8()))
                .isEqualTo(MAPPER.valueToTree(Map.of("mfa_token", "challenge", "code", "123456")));
    }

    @Test
    void verifyMfa_requiresBothValuesBeforeRequest() {
        assertThatThrownBy(() -> resource.verifyMfa(" ", "123456")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.verifyMfa("challenge", null)).isInstanceOf(ValidationException.class);
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void listMfaMethods_parsesMethodsAndRemainingCodes() throws Exception {
        server.enqueue(okJson(Map.of("methods", List.of(Map.of("id", "m1", "type", "Totp", "label", "Phone",
                "confirmed_at", "2026-09-09T14:21:03Z", "last_used_at", "2026-09-09T18:02:44Z")),
                "recovery_codes_remaining", 8)));

        MfaMethods methods = resource.listMfaMethods();

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("GET");
        assertThat(req.getPath()).isEqualTo("/users/self/mfa");
        assertThat(methods.recoveryCodesRemaining()).isEqualTo(8);
        assertThat(methods.methods()).containsExactly(new MfaMethods.Method("m1", "Totp", "Phone",
                "2026-09-09T14:21:03Z", "2026-09-09T18:02:44Z"));
    }

    @Test
    void listMfaMethods_normalisesMissingMethods() throws Exception {
        server.enqueue(okJson(Map.of("recovery_codes_remaining", 0)));
        assertThat(resource.listMfaMethods().methods()).isEmpty();
    }

    @Test
    void startTotpEnrollment_postsTheLabelAndParsesTheSecret() throws Exception {
        server.enqueue(okJson(Map.of("id", "m1", "secret", "GEZDGNBV",
                "provisioning_uri", "otpauth://totp/x?secret=GEZDGNBV")));

        TotpEnrollment enrollment = resource.startTotpEnrollment("Phone");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath()).isEqualTo("/users/self/mfa/totp");
        assertThat(req.getBody().readUtf8()).isEqualTo("{\"label\":\"Phone\"}");
        assertThat(enrollment).isEqualTo(new TotpEnrollment("m1", "GEZDGNBV", "otpauth://totp/x?secret=GEZDGNBV"));
    }

    @Test
    void startTotpEnrollment_sendsAnEmptyObjectWithoutLabel() throws Exception {
        server.enqueue(okJson(Map.of("id", "m1")));
        resource.startTotpEnrollment(null);
        assertThat(server.takeRequest().getBody().readUtf8()).isEqualTo("{}");
    }

    @Test
    void confirmTotpEnrollment_putsTheCodeAndReturnsRecoveryCodes() throws Exception {
        server.enqueue(okJson(Map.of("recovery_codes", List.of("ABCD-EFGH-JKMN", "PQRS-TUVW-XYZA"))));

        List<String> codes = resource.confirmTotpEnrollment("m1", "123456", null, "654321");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("PUT");
        assertThat(req.getPath()).isEqualTo("/users/self/mfa/totp/confirm");
        assertThat(MAPPER.readTree(req.getBody().readUtf8()))
                .isEqualTo(MAPPER.valueToTree(Map.of("id", "m1", "code", "123456", "reauth_code", "654321")));
        assertThat(codes).containsExactly("ABCD-EFGH-JKMN", "PQRS-TUVW-XYZA");
    }

    @Test
    void regenerateRecoveryCodes_postsTheProof() throws Exception {
        server.enqueue(okJson(Map.of("recovery_codes", List.of("ABCD-EFGH-JKMN"))));

        assertThat(resource.regenerateRecoveryCodes("pw", null)).containsExactly("ABCD-EFGH-JKMN");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath()).isEqualTo("/users/self/mfa/recovery-codes");
        assertThat(req.getBody().readUtf8()).isEqualTo("{\"password\":\"pw\"}");
    }

    @Test
    void removeMfaMethod_deletesWithProofAndReturnsRemainingState() throws Exception {
        server.enqueue(okJson(Map.of("is_mfa_enabled", false)));

        assertThat(resource.removeMfaMethod("m1", null, "123456")).isFalse();

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("DELETE");
        assertThat(req.getPath()).isEqualTo("/users/self/mfa/m1");
        assertThat(req.getBody().readUtf8()).isEqualTo("{\"code\":\"123456\"}");
    }

    @Test
    void reauthenticatedMfaCallsRequireAPasswordOrCode() {
        assertThatThrownBy(() -> resource.regenerateRecoveryCodes(null, " "))
                .isInstanceOf(ValidationException.class).hasMessageContaining("password");
        assertThatThrownBy(() -> resource.removeMfaMethod("m1", null, null))
                .isInstanceOf(ValidationException.class);
        assertThat(server.getRequestCount()).isZero();
    }
}
