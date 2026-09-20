package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * RFC 9728 protected-resource metadata describing the API as an OAuth resource server.
 *
 * <p>RFC 8615 requires the bare metadata object, so this response is not wrapped in the
 * {@code {status,message,data}} envelope. {@link #getAuthorizationServers()} names the host that owns the
 * browser-facing flow; its own {@code /.well-known/oauth-authorization-server} document is where an OAuth
 * library reads the authorize, token, revoke, userinfo, and JWKS URLs.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class OAuthProtectedResource {

    private String resource;

    @JsonProperty("authorization_servers")
    private List<String> authorizationServers;

    @JsonProperty("scopes_supported")
    private List<String> scopesSupported;

    @JsonProperty("bearer_methods_supported")
    private List<String> bearerMethodsSupported;

    /** Creates an empty metadata model for JSON deserialization. */
    public OAuthProtectedResource() {}

    /**
     * Returns wire {@code resource}, the canonical identifier to send as the RFC 8707 resource indicator.
     *
     * @return wire {@code resource}, the canonical identifier to send as the RFC 8707 resource indicator
     */
    public String getResource() { return resource; }

    /**
     * Sets wire {@code resource}.
     *
     * @param resource wire {@code resource}
     */
    public void setResource(String resource) { this.resource = resource; }

    /**
     * Returns wire {@code authorization_servers}, the issuers allowed to mint tokens for this API.
     *
     * @return wire {@code authorization_servers}, or {@code null}
     */
    public List<String> getAuthorizationServers() { return authorizationServers; }

    /**
     * Sets wire {@code authorization_servers}.
     *
     * @param authorizationServers wire {@code authorization_servers}
     */
    public void setAuthorizationServers(List<String> authorizationServers) {
        this.authorizationServers = authorizationServers;
    }

    /**
     * Returns wire {@code scopes_supported}. It deliberately omits {@code offline_access}, which is a client
     * concern rather than a permission this resource is protected by.
     *
     * @return wire {@code scopes_supported}, or {@code null}
     */
    public List<String> getScopesSupported() { return scopesSupported; }

    /**
     * Sets wire {@code scopes_supported}.
     *
     * @param scopesSupported wire {@code scopes_supported}
     */
    public void setScopesSupported(List<String> scopesSupported) { this.scopesSupported = scopesSupported; }

    /**
     * Returns wire {@code bearer_methods_supported}; the API accepts {@code header} only.
     *
     * @return wire {@code bearer_methods_supported}, or {@code null}
     */
    public List<String> getBearerMethodsSupported() { return bearerMethodsSupported; }

    /**
     * Sets wire {@code bearer_methods_supported}.
     *
     * @param bearerMethodsSupported wire {@code bearer_methods_supported}
     */
    public void setBearerMethodsSupported(List<String> bearerMethodsSupported) {
        this.bearerMethodsSupported = bearerMethodsSupported;
    }
}
