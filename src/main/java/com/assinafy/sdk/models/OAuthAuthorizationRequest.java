package com.assinafy.sdk.models;

import java.util.List;

/**
 * Inputs for the browser-facing authorization URL of the OAuth 2.1 authorization-code flow.
 *
 * <p>This payload is never serialized to JSON; it is turned into a URL by
 * {@code AssinafyClient.oauth.authorizationUrl(...)}, which adds the fixed {@code response_type=code} and
 * {@code code_challenge_method=S256} parameters and derives the challenge from the verifier.</p>
 *
 * <p>The {@code state} and {@code codeVerifier} must be freshly generated per connection attempt and kept in
 * the user's session: {@code state} is checked when the browser returns, and the verifier is sent to the token
 * endpoint.</p>
 */
public final class OAuthAuthorizationRequest {

    private String clientId;
    private String redirectUri;
    private List<String> scopes;
    private String state;
    private String codeVerifier;
    private String nonce;
    private String authorizationEndpoint;
    private String resource;

    /** Creates an empty authorization request. */
    public OAuthAuthorizationRequest() {}

    /**
     * Creates a request for the required client and redirect URI.
     *
     * @param clientId application identifier issued when the OAuth application was registered
     * @param redirectUri one of the application's registered redirect URIs, matched character for character
     */
    public OAuthAuthorizationRequest(String clientId, String redirectUri) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    /**
     * Returns required {@code client_id}.
     *
     * @return required {@code client_id}
     */
    public String getClientId() { return clientId; }

    /**
     * Sets required {@code client_id}.
     *
     * @param clientId required {@code client_id}
     * @return this request
     */
    public OAuthAuthorizationRequest setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Returns required {@code redirect_uri}.
     *
     * @return required {@code redirect_uri}
     */
    public String getRedirectUri() { return redirectUri; }

    /**
     * Sets required {@code redirect_uri}. It must be HTTPS, carry no fragment, and match a registered URI
     * exactly — a trailing slash makes it a different URI.
     *
     * @param redirectUri required {@code redirect_uri}
     * @return this request
     */
    public OAuthAuthorizationRequest setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    /**
     * Returns the scopes to request, joined with spaces into {@code scope}.
     *
     * @return the scopes to request, or {@code null}
     */
    public List<String> getScopes() { return scopes; }

    /**
     * Sets the scopes to request. They must be within what the application is registered for, and the user
     * approves all of them or none.
     *
     * @param scopes required non-empty scope list
     * @return this request
     */
    public OAuthAuthorizationRequest setScopes(List<String> scopes) {
        this.scopes = scopes;
        return this;
    }

    /**
     * Returns required {@code state}, the per-attempt CSRF token.
     *
     * @return required {@code state}, the per-attempt CSRF token
     */
    public String getState() { return state; }

    /**
     * Sets required {@code state}. Store it in the user's session and compare it with the value returned to
     * the redirect URI before accepting the response.
     *
     * @param state required per-attempt CSRF token
     * @return this request
     */
    public OAuthAuthorizationRequest setState(String state) {
        this.state = state;
        return this;
    }

    /**
     * Returns the required RFC 7636 PKCE code verifier the challenge is derived from.
     *
     * @return the required RFC 7636 PKCE code verifier the challenge is derived from
     */
    public String getCodeVerifier() { return codeVerifier; }

    /**
     * Sets the required PKCE code verifier: 43 to 128 characters from {@code A-Z a-z 0-9 - . _ ~}. Store it in
     * the user's session; the token exchange needs the same value.
     *
     * @param codeVerifier required PKCE code verifier
     * @return this request
     */
    public OAuthAuthorizationRequest setCodeVerifier(String codeVerifier) {
        this.codeVerifier = codeVerifier;
        return this;
    }

    /**
     * Returns optional {@code nonce}, echoed in the {@code id_token}.
     *
     * @return optional {@code nonce}, or {@code null}
     */
    public String getNonce() { return nonce; }

    /**
     * Sets optional {@code nonce}, echoed in the {@code id_token} when the {@code openid} scope is requested.
     *
     * @param nonce optional OpenID Connect nonce
     * @return this request
     */
    public OAuthAuthorizationRequest setNonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    /**
     * Returns the authorization-server endpoint override, or {@code null} for the default.
     *
     * @return the authorization-server endpoint override, or {@code null} for the default
     */
    public String getAuthorizationEndpoint() { return authorizationEndpoint; }

    /**
     * Sets an authorization endpoint other than the default. Use the {@code authorization_endpoint} published
     * by the authorization server named in the protected-resource metadata.
     *
     * @param authorizationEndpoint absolute authorization endpoint URL, or {@code null} for the default
     * @return this request
     */
    public OAuthAuthorizationRequest setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
        return this;
    }

    /**
     * Returns the RFC 8707 resource indicator override, or {@code null} for the client's API origin.
     *
     * @return the RFC 8707 resource indicator override, or {@code null} for the client's API origin
     */
    public String getResource() { return resource; }

    /**
     * Sets the RFC 8707 resource indicator naming the API the token is for. It defaults to the origin of the
     * client's base URL ({@code https://api.assinafy.com.br} in production), which is also the value the code
     * exchange sends. Any other value is rejected, because the token endpoint answers a {@code resource} that
     * differs from the authorized one with {@code invalid_target}.
     *
     * @param resource resource indicator, or {@code null} for the client's API origin
     * @return this request
     */
    public OAuthAuthorizationRequest setResource(String resource) {
        this.resource = resource;
        return this;
    }
}
