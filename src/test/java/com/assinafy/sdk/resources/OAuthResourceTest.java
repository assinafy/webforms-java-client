package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.OAuthAuthorizationRequest;
import com.assinafy.sdk.models.OAuthProtectedResource;
import com.assinafy.sdk.models.OAuthTokens;
import com.assinafy.sdk.models.OAuthUserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OAuthResourceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String REDIRECT_URI = "https://myapp.example/oauth/callback";
    private static final String VERIFIER = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQ";

    private MockWebServer server;
    private OAuthResource resource;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        resource = new OAuthResource(new OkHttpClient(), server.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private static MockResponse json(int code, Object body) throws Exception {
        return new MockResponse()
                .setResponseCode(code)
                .setBody(MAPPER.writeValueAsString(body))
                .setHeader("Content-Type", "application/json");
    }

    private String origin() {
        HttpUrl url = server.url("/");
        return url.scheme() + "://" + url.host() + ":" + url.port();
    }

    @Test
    void generateCodeVerifier_isAnRfc7636Verifier() {
        String verifier = OAuthResource.generateCodeVerifier();

        assertThat(verifier).hasSize(43).matches("[A-Za-z0-9._~-]+");
        assertThat(OAuthResource.generateCodeVerifier()).isNotEqualTo(verifier);
    }

    @Test
    void generateState_isRandomAndUrlSafe() {
        String state = OAuthResource.generateState();

        assertThat(state).matches("[A-Za-z0-9._~-]+").isNotEqualTo(OAuthResource.generateState());
    }

    @Test
    void authorizationUrl_buildsTheDocumentedQueryWithAnS256Challenge() throws Exception {
        String url = resource.authorizationUrl(
                new OAuthAuthorizationRequest("client-1", REDIRECT_URI)
                        .setScopes(List.of("documents:read", "documents:write", "offline_access"))
                        .setState("state-1")
                        .setCodeVerifier(VERIFIER)
                        .setNonce("nonce-1"));

        HttpUrl parsed = HttpUrl.get(url);
        String expectedChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(VERIFIER.getBytes(StandardCharsets.US_ASCII)));

        assertThat(parsed.scheme()).isEqualTo("https");
        assertThat(parsed.host()).isEqualTo("auth.assinafy.com.br");
        assertThat(parsed.encodedPath()).isEqualTo("/oauth/authorize");
        assertThat(parsed.queryParameter("response_type")).isEqualTo("code");
        assertThat(parsed.queryParameter("client_id")).isEqualTo("client-1");
        assertThat(parsed.queryParameter("redirect_uri")).isEqualTo(REDIRECT_URI);
        assertThat(parsed.queryParameter("scope"))
                .isEqualTo("documents:read documents:write offline_access");
        assertThat(parsed.queryParameter("state")).isEqualTo("state-1");
        assertThat(parsed.queryParameter("code_challenge")).isEqualTo(expectedChallenge);
        assertThat(parsed.queryParameter("code_challenge_method")).isEqualTo("S256");
        assertThat(parsed.queryParameter("resource")).isEqualTo(origin());
        assertThat(parsed.queryParameter("nonce")).isEqualTo("nonce-1");
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void authorizationUrl_omitsNonceAndHonoursOverrides() {
        String url = resource.authorizationUrl(
                new OAuthAuthorizationRequest("client-1", REDIRECT_URI)
                        .setScopes(List.of("account:read"))
                        .setState("state-1")
                        .setCodeVerifier(VERIFIER)
                        .setAuthorizationEndpoint("https://auth.example/oauth/authorize")
                        .setResource("https://api.example"));

        HttpUrl parsed = HttpUrl.get(url);
        assertThat(parsed.host()).isEqualTo("auth.example");
        assertThat(parsed.queryParameter("resource")).isEqualTo("https://api.example");
        assertThat(parsed.queryParameter("nonce")).isNull();
    }

    @Test
    void authorizationUrl_rejectsMalformedInputBeforeBuildingAnything() {
        OAuthAuthorizationRequest valid = new OAuthAuthorizationRequest("client-1", REDIRECT_URI)
                .setScopes(List.of("documents:read")).setState("state-1").setCodeVerifier(VERIFIER);

        assertThatThrownBy(() -> resource.authorizationUrl(null))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.authorizationUrl(new OAuthAuthorizationRequest(null, REDIRECT_URI)
                .setScopes(List.of("documents:read")).setState("s").setCodeVerifier(VERIFIER)))
                .isInstanceOf(ValidationException.class).hasMessageContaining("Client ID");
        assertThatThrownBy(() -> resource.authorizationUrl(new OAuthAuthorizationRequest("c", "http://app/cb")
                .setScopes(List.of("documents:read")).setState("s").setCodeVerifier(VERIFIER)))
                .isInstanceOf(ValidationException.class).hasMessageContaining("HTTPS");
        assertThatThrownBy(() -> resource.authorizationUrl(valid.setCodeVerifier("too-short")))
                .isInstanceOf(ValidationException.class).hasMessageContaining("43 to 128");
        assertThatThrownBy(() -> resource.authorizationUrl(valid.setCodeVerifier(VERIFIER).setScopes(List.of())))
                .isInstanceOf(ValidationException.class).hasMessageContaining("scope");
    }

    @Test
    void exchangeAuthorizationCode_postsTheRfc6749BodyAndReadsTheFlatResponse() throws Exception {
        server.enqueue(json(200, Map.of(
                "access_token", "at", "token_type", "Bearer", "expires_in", 3600,
                "refresh_token", "rt", "scope", "documents:read documents:write", "id_token", "it")));

        OAuthTokens tokens = resource.exchangeAuthorizationCode(
                "client-1", "secret-1", "the-code", REDIRECT_URI, VERIFIER);

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/oauth/token");
        assertThat(request.getBody().readUtf8()).isEqualTo("{\"grant_type\":\"authorization_code\","
                + "\"code\":\"the-code\",\"redirect_uri\":\"" + REDIRECT_URI + "\","
                + "\"code_verifier\":\"" + VERIFIER + "\",\"client_id\":\"client-1\","
                + "\"client_secret\":\"secret-1\",\"resource\":\"" + origin() + "\"}");
        assertThat(tokens.getAccessToken()).isEqualTo("at");
        assertThat(tokens.getTokenType()).isEqualTo("Bearer");
        assertThat(tokens.getExpiresIn()).isEqualTo(3600);
        assertThat(tokens.getRefreshToken()).isEqualTo("rt");
        assertThat(tokens.getScope()).isEqualTo("documents:read documents:write");
        assertThat(tokens.getIdToken()).isEqualTo("it");
    }

    @Test
    void exchangeAuthorizationCode_omitsTheSecretForAPublicClient() throws Exception {
        server.enqueue(json(200, Map.of("access_token", "at")));

        resource.exchangeAuthorizationCode("client-1", null, "the-code", REDIRECT_URI, VERIFIER);

        assertThat(server.takeRequest().getBody().readUtf8()).doesNotContain("client_secret");
    }

    @Test
    void exchangeAuthorizationCode_validatesBeforeSending() {
        assertThatThrownBy(() -> resource.exchangeAuthorizationCode(
                " ", null, "code", REDIRECT_URI, VERIFIER))
                .isInstanceOf(ValidationException.class).hasMessageContaining("Client ID");
        assertThatThrownBy(() -> resource.exchangeAuthorizationCode(
                "client-1", null, null, REDIRECT_URI, VERIFIER))
                .isInstanceOf(ValidationException.class).hasMessageContaining("Authorization code");
        assertThatThrownBy(() -> resource.exchangeAuthorizationCode(
                "client-1", null, "code", REDIRECT_URI + "#x", VERIFIER))
                .isInstanceOf(ValidationException.class).hasMessageContaining("fragment");
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void refreshToken_postsTheRefreshGrantWithoutAResourceIndicator() throws Exception {
        server.enqueue(json(200, Map.of("access_token", "at2", "refresh_token", "rt2")));

        OAuthTokens tokens = resource.refreshToken("client-1", "secret-1", "rt1");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getPath()).isEqualTo("/oauth/token");
        assertThat(request.getBody().readUtf8()).isEqualTo("{\"grant_type\":\"refresh_token\","
                + "\"refresh_token\":\"rt1\",\"client_id\":\"client-1\",\"client_secret\":\"secret-1\"}");
        assertThat(tokens.getRefreshToken()).isEqualTo("rt2");
    }

    @Test
    void revoke_postsTheTokenAndAcceptsAnEmptySuccess() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200));

        resource.revoke("client-1", "secret-1", "rt1", "refresh_token");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/oauth/revoke");
        assertThat(request.getBody().readUtf8()).isEqualTo("{\"token\":\"rt1\","
                + "\"token_type_hint\":\"refresh_token\",\"client_id\":\"client-1\","
                + "\"client_secret\":\"secret-1\"}");
    }

    @Test
    void revoke_rejectsAnUndocumentedTypeHint() {
        assertThatThrownBy(() -> resource.revoke("client-1", null, "rt1", "session"))
                .isInstanceOf(ValidationException.class).hasMessageContaining("Token type hint");
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void revoke_surfacesTheOAuthErrorOfAFailedClientAuthentication() throws Exception {
        server.enqueue(json(401, Map.of(
                "error", "invalid_client", "error_description", "Client authentication failed.")));

        assertThatThrownBy(() -> resource.revoke("client-1", "wrong", "rt1"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Client authentication failed.")
                .satisfies(thrown -> {
                    ApiException failure = (ApiException) thrown;
                    assertThat(failure.getStatusCode()).isEqualTo(401);
                    assertThat(failure.getOAuthError()).isEqualTo("invalid_client");
                    assertThat(failure.getRetryAfterSeconds()).isNull();
                });
    }

    @Test
    void tokenEndpointError_surfacesTheRfc6749ErrorCodeAndDescription() throws Exception {
        server.enqueue(json(400, Map.of(
                "error", "invalid_grant", "error_description", "The authorization code has expired.")));

        assertThatThrownBy(() -> resource.exchangeAuthorizationCode(
                "client-1", null, "stale", REDIRECT_URI, VERIFIER))
                .isInstanceOf(ApiException.class)
                .hasMessage("The authorization code has expired.")
                .satisfies(thrown -> assertThat(((ApiException) thrown).getOAuthError())
                        .isEqualTo("invalid_grant"));
    }

    @Test
    void userInfo_readsTheFlatClaimObject() throws Exception {
        server.enqueue(json(200, Map.of(
                "sub", "user-1", "name", "Maria Silva", "email", "maria@example.com",
                "email_verified", true)));

        OAuthUserInfo claims = resource.userInfo();

        assertThat(server.takeRequest().getPath()).isEqualTo("/oauth/userinfo");
        assertThat(claims.getSub()).isEqualTo("user-1");
        assertThat(claims.getName()).isEqualTo("Maria Silva");
        assertThat(claims.getEmail()).isEqualTo("maria@example.com");
        assertThat(claims.getEmailVerified()).isTrue();
    }

    @Test
    void userInfo_reportsAMissingScopeFromTheChallengeHeader() {
        server.enqueue(new MockResponse().setResponseCode(403).setHeader("WWW-Authenticate",
                "Bearer error=\"insufficient_scope\", scope=\"documents:write\", "
                        + "resource_metadata=\"https://api.assinafy.com.br/"
                        + ".well-known/oauth-protected-resource\""));

        assertThatThrownBy(() -> resource.userInfo())
                .isInstanceOf(ApiException.class)
                .satisfies(thrown -> {
                    ApiException failure = (ApiException) thrown;
                    assertThat(failure.getStatusCode()).isEqualTo(403);
                    assertThat(failure.getOAuthError()).isEqualTo("insufficient_scope");
                    assertThat(failure.getRequiredScope()).isEqualTo("documents:write");
                });
    }

    @Test
    void protectedResourceMetadata_readsTheHostRootDocumentOutsideTheVersionPrefix() throws Exception {
        server.enqueue(json(200, Map.of(
                "resource", "https://api.assinafy.com.br",
                "authorization_servers", List.of("https://auth.assinafy.com.br"),
                "scopes_supported", List.of("documents:read", "documents:write"),
                "bearer_methods_supported", List.of("header"))));

        OAuthResource versioned = new OAuthResource(new OkHttpClient(), server.url("/v1").toString());
        OAuthProtectedResource metadata = versioned.protectedResourceMetadata();

        assertThat(server.takeRequest().getPath()).isEqualTo("/.well-known/oauth-protected-resource");
        assertThat(metadata.getResource()).isEqualTo("https://api.assinafy.com.br");
        assertThat(metadata.getAuthorizationServers()).containsExactly("https://auth.assinafy.com.br");
        assertThat(metadata.getScopesSupported()).containsExactly("documents:read", "documents:write");
        assertThat(metadata.getBearerMethodsSupported()).containsExactly("header");
    }
}
