package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.AssinafyException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.OAuthAuthorizationRequest;
import com.assinafy.sdk.models.OAuthProtectedResource;
import com.assinafy.sdk.models.OAuthTokens;
import com.assinafy.sdk.models.OAuthUserInfo;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okio.BufferedSink;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Client for the OAuth 2.1 authorization-code flow, used by applications acting in <em>other people's</em>
 * workspaces with those users' permission. Automating your own workspace needs an API key instead, and nothing
 * here applies.
 *
 * <p>Two hosts take part. The browser-facing authorization page lives on the authorization server
 * ({@code https://auth.assinafy.com.br}); the token, revoke, and userinfo endpoints live on this API. This
 * resource builds the first URL and calls the rest.</p>
 *
 * <p>The token and revoke requests are sent as {@code application/x-www-form-urlencoded} fields. The three
 * endpoints below answer with flat JSON rather than the API's {@code {status,message,data}}
 * envelope, because no standard OAuth client would look for {@code access_token} or {@code error} inside a
 * {@code data} key. A failure still surfaces as {@link com.assinafy.sdk.exceptions.ApiException}, whose
 * {@code getOAuthError()} carries the RFC 6749 error code.</p>
 *
 * <p>The typical server-side sequence is:</p>
 *
 * <pre>{@code
 * String verifier = OAuthResource.generateCodeVerifier();
 * String state = OAuthResource.generateState();
 * // Store verifier and state in the user's session, then redirect the browser to:
 * String url = client.oauth.authorizationUrl(
 *         new OAuthAuthorizationRequest("client-id", "https://myapp.com/oauth/callback")
 *                 .setScopes(List.of("documents:read", "documents:write", "offline_access"))
 *                 .setState(state)
 *                 .setCodeVerifier(verifier));
 *
 * // On the callback, after checking state and iss:
 * OAuthTokens tokens = client.oauth.exchangeAuthorizationCode(
 *         "client-id", "client-secret", code, "https://myapp.com/oauth/callback", verifier);
 *
 * AssinafyClient asUser = new AssinafyClient(
 *         new AssinafyClientOptions().setToken(tokens.getAccessToken()));
 * String workspaceId = asUser.accounts.list().get(0).getId();
 * }</pre>
 */
public final class OAuthResource extends BaseResource {

    /** Browser-facing authorization endpoint of the Assinafy authorization server. */
    public static final String DEFAULT_AUTHORIZATION_ENDPOINT = "https://auth.assinafy.com.br/oauth/authorize";

    /** Path of the RFC 9728 protected-resource metadata, served at the API host root rather than under /v1. */
    private static final String PROTECTED_RESOURCE_PATH = "/.well-known/oauth-protected-resource";

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64URL = Base64.getUrlEncoder().withoutPadding();
    private static final Pattern CODE_VERIFIER = Pattern.compile("[A-Za-z0-9._~-]{43,128}");

    /**
     * Creates an instance. Requests go through a copy of {@code httpClient} that neither retries after a
     * connection failure nor follows redirects, and the token and revoke bodies are one-shot, so OkHttp never
     * sends them twice — not even for a {@code 408} or a {@code 503} with {@code Retry-After: 0}, which it would
     * otherwise repeat. Replaying a token request the server already processed would reuse a retired refresh
     * token and end the user's connection.
     *
     * @param httpClient shared HTTP client
     * @param baseUrl API base URL
     */
    public OAuthResource(OkHttpClient httpClient, String baseUrl) {
        super(httpClient != null
                ? httpClient.newBuilder().retryOnConnectionFailure(false).followRedirects(false).build()
                : null, baseUrl, null);
    }

    /**
     * Generates an RFC 7636 code verifier: 43 characters drawn from the unreserved set. Create a new one for
     * every connection attempt, keep it in the user's session, and send it to
     * {@link #exchangeAuthorizationCode(String, String, String, String, String)}.
     *
     * @return a fresh PKCE code verifier
     */
    public static String generateCodeVerifier() {
        return randomToken(32);
    }

    /**
     * Generates the per-attempt {@code state} value that protects the redirect against CSRF. Keep it in the
     * user's session and compare it with the {@code state} returned to the redirect URI.
     *
     * @return a fresh opaque state value
     */
    public static String generateState() {
        return randomToken(16);
    }

    /**
     * Builds the authorization URL the user's browser is sent to. Use a full page navigation, not an AJAX
     * call. {@code response_type=code} and {@code code_challenge_method=S256} are fixed, and the challenge is
     * the SHA-256 of the supplied verifier, so the two can never disagree.
     *
     * <p>If the {@code client_id} or {@code redirect_uri} is wrong, the authorization server shows an error on
     * its own page instead of redirecting back, because returning to an unverified address would be unsafe.</p>
     *
     * @param request required client, redirect URI, scopes, state, and code verifier
     * @return the absolute authorization URL
     * @throws ValidationException when a required field is absent or malformed
     */
    public String authorizationUrl(OAuthAuthorizationRequest request) {
        if (request == null) {
            throw new ValidationException("Authorization request is required");
        }
        requireValue(request.getClientId(), "Client ID");
        requireRedirectUri(request.getRedirectUri());
        requireValue(request.getState(), "State");
        requireCodeVerifier(request.getCodeVerifier());
        List<String> scopes = request.getScopes();
        if (scopes == null || scopes.isEmpty()) {
            throw new ValidationException("At least one scope is required");
        }
        String resource = apiOrigin();
        if (request.getResource() != null && !request.getResource().isBlank()
                && !resource.equals(request.getResource())) {
            throw new ValidationException("Resource must be " + resource
                    + ", the value the code exchange sends; any other value fails with invalid_target");
        }

        String endpoint = request.getAuthorizationEndpoint() != null
                && !request.getAuthorizationEndpoint().isBlank()
                ? request.getAuthorizationEndpoint()
                : DEFAULT_AUTHORIZATION_ENDPOINT;
        HttpUrl parsed;
        try {
            parsed = HttpUrl.get(endpoint);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Authorization endpoint must be a valid URL", e);
        }

        HttpUrl.Builder builder = parsed.newBuilder()
                .addQueryParameter("response_type", "code")
                .addQueryParameter("client_id", request.getClientId())
                .addQueryParameter("redirect_uri", request.getRedirectUri())
                .addQueryParameter("scope", String.join(" ", scopes))
                .addQueryParameter("state", request.getState())
                .addQueryParameter("code_challenge", codeChallenge(request.getCodeVerifier()))
                .addQueryParameter("code_challenge_method", "S256")
                .addQueryParameter("resource", resource);
        if (request.getNonce() != null && !request.getNonce().isBlank()) {
            builder.addQueryParameter("nonce", request.getNonce());
        }
        return builder.build().toString();
    }

    /**
     * {@code POST /oauth/token} — exchanges the one-time authorization code for tokens. Call it from your
     * server: the code is single-use and expires 60 seconds after the user approves.
     *
     * @param clientId required application identifier
     * @param clientSecret secret of a confidential application, or {@code null} for a public one
     * @param code required authorization code returned to the redirect URI
     * @param redirectUri required redirect URI, identical to the one sent to the authorization server
     * @param codeVerifier required PKCE verifier matching the challenge sent to the authorization server
     * @return the issued token set
     * @throws ValidationException when a required field is absent or malformed
     */
    public OAuthTokens exchangeAuthorizationCode(String clientId, String clientSecret, String code,
            String redirectUri, String codeVerifier) {
        requireValue(clientId, "Client ID");
        requireValue(code, "Authorization code");
        requireRedirectUri(redirectUri);
        requireCodeVerifier(codeVerifier);

        FormBody.Builder body = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .add("code_verifier", codeVerifier)
                .add("client_id", clientId);
        addIfPresent(body, "client_secret", clientSecret);
        body.add("resource", apiOrigin());
        return httpPost("/oauth/token", sendOnce(body), OAuthTokens.class);
    }

    /**
     * {@code POST /oauth/token} — trades the current refresh token for a new token set, without the user.
     *
     * <p>Every refresh returns a <em>new</em> refresh token and retires the old one. A replayed refresh token
     * cannot be told apart from a stolen one, so it ends the whole connection and the user has to reconnect.
     * Store the new value before doing anything else with the response, and refresh one at a time per
     * connection.</p>
     *
     * <p>The SDK sends this request once and never re-sends it on its own. Never re-send the token yourself
     * after a failure that may have reached the server — a timeout, a dropped connection, a {@code 5xx} — since
     * the first attempt may already have retired it. Re-read your storage instead: continue only if it holds a
     * different, newer refresh token; if it still holds the one you sent, the outcome is unknown, so ask the
     * user to reconnect. Only a failure that provably happened before sending is safe to retry: a
     * {@link com.assinafy.sdk.exceptions.NetworkException} caused by an {@code UnknownHostException} (DNS), a
     * {@code ConnectException} (connection refused), or an {@code SSLHandshakeException}.</p>
     *
     * @param clientId required application identifier
     * @param clientSecret secret of a confidential application, or {@code null} for a public one
     * @param refreshToken required current refresh token
     * @return the issued token set, whose refresh token differs from the one sent
     * @throws ValidationException when a required field is absent, or a successful response carries no new
     *         refresh token (missing, blank, or the one sent); the token sent may already be retired, so ask the
     *         user to reconnect
     */
    public OAuthTokens refreshToken(String clientId, String clientSecret, String refreshToken) {
        requireValue(clientId, "Client ID");
        requireValue(refreshToken, "Refresh token");

        FormBody.Builder body = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .add("client_id", clientId);
        addIfPresent(body, "client_secret", clientSecret);
        OAuthTokens tokens = httpPost("/oauth/token", sendOnce(body), OAuthTokens.class);
        // The server retired the token just sent, so a response without a new one leaves nothing safe to store:
        // returning it would let the caller save null or keep the retired token.
        String renewed = tokens != null ? tokens.getRefreshToken() : null;
        if (renewed == null || renewed.isBlank() || renewed.equals(refreshToken)) {
            throw new ValidationException("The token endpoint returned no new refresh token, and the one sent may "
                    + "already be retired; ask the user to reconnect");
        }
        return tokens;
    }

    /**
     * {@code POST /oauth/revoke} — revokes an access or refresh token when a user disconnects, rather than
     * only forgetting it locally. Revoking a refresh token ends the whole connection; revoke the one saved most
     * recently, never an older copy, because every refresh retires the token it was sent.
     *
     * <p>Every token outcome answers HTTP 200, including a token that is unknown, malformed, or already
     * revoked, so the endpoint cannot be used to probe whether a token exists — nor tell you that the token you
     * revoked was a retired one while the connection stays active. Only failed client authentication answers
     * 401.</p>
     *
     * @param clientId required application identifier
     * @param clientSecret secret of a confidential application, or {@code null} for a public one
     * @param token required access or refresh token to revoke
     * @param tokenTypeHint optional {@code access_token} or {@code refresh_token} hint
     * @throws ValidationException when a required field is absent or the hint is not a documented value
     */
    public void revoke(String clientId, String clientSecret, String token, String tokenTypeHint) {
        requireValue(clientId, "Client ID");
        requireValue(token, "Token");
        if (tokenTypeHint != null && !"access_token".equals(tokenTypeHint)
                && !"refresh_token".equals(tokenTypeHint)) {
            throw new ValidationException("Token type hint must be 'access_token' or 'refresh_token'");
        }

        FormBody.Builder body = new FormBody.Builder().add("token", token);
        addIfPresent(body, "token_type_hint", tokenTypeHint);
        body.add("client_id", clientId);
        addIfPresent(body, "client_secret", clientSecret);
        httpPostVoid("/oauth/revoke", sendOnce(body));
    }

    /**
     * Revokes a token without a type hint.
     *
     * @param clientId required application identifier
     * @param clientSecret secret of a confidential application, or {@code null} for a public one
     * @param token required access or refresh token to revoke
     */
    public void revoke(String clientId, String clientSecret, String token) {
        revoke(clientId, clientSecret, token, null);
    }

    /**
     * {@code GET /oauth/userinfo} — returns the claims of the user who authorized this client's access token.
     * Requires the {@code openid} scope; {@code name} additionally requires {@code profile} and {@code email}
     * additionally requires {@code email}.
     *
     * @return the authorizing user's claims
     */
    public OAuthUserInfo userInfo() {
        return httpGet("/oauth/userinfo", OAuthUserInfo.class);
    }

    /**
     * {@code GET /.well-known/oauth-protected-resource} — returns this API's RFC 9728 metadata: its canonical
     * resource identifier, the authorization servers that may issue tokens for it, and the scopes it accepts.
     * It is public and is served at the API host root, outside the {@code /v1} prefix.
     *
     * @return the protected-resource metadata
     */
    public OAuthProtectedResource protectedResourceMetadata() {
        return httpGetAbsolute(apiOrigin() + PROTECTED_RESOURCE_PATH, OAuthProtectedResource.class);
    }

    /** Returns the origin of the configured base URL, with no path and no default port. */
    private String apiOrigin() {
        String origin = HttpUrl.get(baseUrl).newBuilder().encodedPath("/").build().toString();
        return origin.endsWith("/") ? origin.substring(0, origin.length() - 1) : origin;
    }

    private static String randomToken(int byteCount) {
        byte[] bytes = new byte[byteCount];
        RANDOM.nextBytes(bytes);
        return BASE64URL.encodeToString(bytes);
    }

    private static String codeChallenge(String codeVerifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return BASE64URL.encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new AssinafyException("SHA-256 is required to derive a PKCE code challenge", e);
        }
    }

    /**
     * Builds the form as a one-shot body, which OkHttp never sends twice. {@code retryOnConnectionFailure(false)}
     * alone does not stop OkHttp repeating a request answered {@code 503} with {@code Retry-After: 0}.
     */
    private static RequestBody sendOnce(FormBody.Builder form) {
        RequestBody body = form.build();
        return new RequestBody() {
            @Override public MediaType contentType() { return body.contentType(); }
            @Override public long contentLength() throws IOException { return body.contentLength(); }
            @Override public void writeTo(BufferedSink sink) throws IOException { body.writeTo(sink); }
            @Override public boolean isOneShot() { return true; }
        };
    }

    private static void addIfPresent(FormBody.Builder body, String key, String value) {
        if (value != null && !value.isBlank()) {
            body.add(key, value);
        }
    }

    private static void requireValue(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(name + " is required");
        }
    }

    private static void requireRedirectUri(String redirectUri) {
        requireValue(redirectUri, "Redirect URI");
        if (!redirectUri.startsWith("https://") || redirectUri.contains("#")) {
            throw new ValidationException("Redirect URI must be an HTTPS URL without a fragment");
        }
    }

    private static void requireCodeVerifier(String codeVerifier) {
        requireValue(codeVerifier, "Code verifier");
        if (!CODE_VERIFIER.matcher(codeVerifier).matches()) {
            throw new ValidationException(
                    "Code verifier must be 43 to 128 characters from A-Z a-z 0-9 - . _ ~");
        }
    }
}
