package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Token set returned by the OAuth token endpoint.
 *
 * <p>RFC 6749 §5.1 requires {@code access_token} at the top level, so this response is a flat JSON object
 * rather than the {@code {status,message,data}} envelope the rest of the API uses.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class OAuthTokens {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("issued_token_type")
    private String issuedTokenType;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    private String scope;

    @JsonProperty("id_token")
    private String idToken;

    /** Creates an empty token model for JSON deserialization. */
    public OAuthTokens() {}

    /**
     * Returns wire {@code access_token}, sent as {@code Authorization: Bearer} on every API call.
     *
     * @return wire {@code access_token}, sent as {@code Authorization: Bearer} on every API call
     */
    public String getAccessToken() { return accessToken; }

    /**
     * Sets wire {@code access_token}.
     *
     * @param accessToken wire {@code access_token}
     */
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    /**
     * Returns wire {@code token_type}, always {@code Bearer}.
     *
     * @return wire {@code token_type}, always {@code Bearer}
     */
    public String getTokenType() { return tokenType; }

    /**
     * Sets wire {@code token_type}.
     *
     * @param tokenType wire {@code token_type}
     */
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    /**
     * Returns the optional RFC 8693 token-type identifier in a token-exchange response.
     *
     * @return wire {@code issued_token_type}, or {@code null} for authorization-code and refresh grants
     */
    public String getIssuedTokenType() { return issuedTokenType; }

    /**
     * Sets wire {@code issued_token_type}.
     *
     * @param issuedTokenType token-type identifier
     */
    public void setIssuedTokenType(String issuedTokenType) { this.issuedTokenType = issuedTokenType; }

    /**
     * Returns wire {@code expires_in}, the access token's lifetime in seconds.
     *
     * @return wire {@code expires_in}, the access token's lifetime in seconds
     */
    public Integer getExpiresIn() { return expiresIn; }

    /**
     * Sets wire {@code expires_in}.
     *
     * @param expiresIn wire {@code expires_in}
     */
    public void setExpiresIn(Integer expiresIn) { this.expiresIn = expiresIn; }

    /**
     * Returns wire {@code refresh_token}, present only when {@code offline_access} was requested and granted.
     * Every refresh issues a new one and retires the old one, so the new value must be stored before use.
     *
     * @return wire {@code refresh_token}, or {@code null}
     */
    public String getRefreshToken() { return refreshToken; }

    /**
     * Sets wire {@code refresh_token}.
     *
     * @param refreshToken wire {@code refresh_token}
     */
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    /**
     * Returns wire {@code scope}, the space-separated scopes actually granted to the access token. It never
     * contains {@code offline_access}, which is a request-time signal rather than a permission.
     *
     * @return wire {@code scope}, or {@code null}
     */
    public String getScope() { return scope; }

    /**
     * Sets wire {@code scope}.
     *
     * @param scope wire {@code scope}
     */
    public void setScope(String scope) { this.scope = scope; }

    /**
     * Returns wire {@code id_token}, a signed RS256 OpenID Connect assertion. Present only when the
     * {@code openid} scope was granted. The SDK does not validate it: before trusting its claims, check it with
     * an OpenID Connect library (RS256 key from {@code https://auth.assinafy.com.br/.well-known/jwks.json}
     * matched by {@code kid}, {@code iss} {@code https://auth.assinafy.com.br}, {@code aud} your client ID,
     * {@code exp} in the future, and {@code nonce} if you sent one).
     *
     * @return wire {@code id_token}, or {@code null}
     */
    public String getIdToken() { return idToken; }

    /**
     * Sets wire {@code id_token}.
     *
     * @param idToken wire {@code id_token}
     */
    public void setIdToken(String idToken) { this.idToken = idToken; }
}
