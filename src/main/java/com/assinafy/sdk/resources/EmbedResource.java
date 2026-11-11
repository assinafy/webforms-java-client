package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.EmbedOrigin;
import com.assinafy.sdk.models.EmbedSession;
import com.assinafy.sdk.models.EmbedSessionExchange;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.OkHttpClient;

import java.net.URI;
import java.util.List;
import java.util.Map;

/** Registered parent origins and single-use hosted signing sessions. */
public final class EmbedResource extends BaseResource {
    /**
     * Creates the resource.
     * @param httpClient shared transport
     * @param baseUrl API base URL
     * @param defaultAccountId default workspace
     */
    public EmbedResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    /**
     * {@code GET /accounts/{accountId}/embed-origins}. API key or user session only.
     * @param accountId workspace override, or null
     * @return origins, never null
     */
    public List<EmbedOrigin> listOrigins(String accountId) {
        return orEmpty(httpGet(path(accountId), new TypeReference<List<EmbedOrigin>>() {}));
    }

    /**
     * {@code GET /accounts/{accountId}/embed-origins} in the default workspace.
     * @return origins, never null
     */
    public List<EmbedOrigin> listOrigins() { return listOrigins(null); }

    /**
     * {@code POST /accounts/{accountId}/embed-origins}. At most ten origins per workspace.
     * @param origin exact HTTPS origin; HTTP is allowed for localhost and 127.0.0.1
     * @param accountId workspace override, or null
     * @return registered origin
     */
    public EmbedOrigin addOrigin(String origin, String accountId) {
        validateOrigin(origin);
        return httpPost(path(accountId), Map.of("origin", origin), EmbedOrigin.class);
    }

    /**
     * {@code POST /accounts/{accountId}/embed-origins} in the default workspace.
     * @param origin exact parent origin
     * @return registered origin
     */
    public EmbedOrigin addOrigin(String origin) { return addOrigin(origin, null); }

    /**
     * {@code PUT /accounts/{accountId}/embed-origins/{originId}}.
     * @param originId registered origin identifier
     * @param origin new exact parent origin
     * @param accountId workspace override, or null
     * @return updated origin
     */
    public EmbedOrigin updateOrigin(String originId, String origin, String accountId) {
        validateOrigin(origin);
        return httpPut(path(accountId) + "/" + requireId(originId, "Origin ID"),
                Map.of("origin", origin), EmbedOrigin.class);
    }

    /**
     * {@code PUT /accounts/{accountId}/embed-origins/{originId}} in the default workspace.
     * @param originId registered origin identifier
     * @param origin new parent origin
     * @return updated origin
     */
    public EmbedOrigin updateOrigin(String originId, String origin) { return updateOrigin(originId, origin, null); }

    /**
     * {@code DELETE /accounts/{accountId}/embed-origins/{originId}}. Removing the last origin disables embedding.
     * @param originId registered origin identifier
     * @param accountId workspace override, or null
     */
    public void deleteOrigin(String originId, String accountId) {
        httpDelete(path(accountId) + "/" + requireId(originId, "Origin ID"));
    }

    /**
     * {@code DELETE /accounts/{accountId}/embed-origins/{originId}} in the default workspace.
     * @param originId registered origin identifier
     */
    public void deleteOrigin(String originId) { deleteOrigin(originId, null); }

    /**
     * {@code POST /documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/embed-sessions}.
     * The signer must have embedded=true. Load the URL once, in an iframe, within five minutes.
     * @param documentId document identifier
     * @param assignmentId assignment identifier
     * @param signerId signer identifier
     * @return single-use bearer URL; never log it
     */
    public EmbedSession createSession(String documentId, String assignmentId, String signerId) {
        return httpPost("/documents/" + requireId(documentId, "Document ID") + "/assignments/"
                + requireId(assignmentId, "Assignment ID") + "/signers/" + requireId(signerId, "Signer ID")
                + "/embed-sessions", null, EmbedSession.class);
    }

    /**
     * {@code POST /embed/sessions/{token}/exchange}. Intended for Assinafy's hosted embed page.
     * Integrations normally load the session URL instead. The token is spent even on a 409 refusal.
     * @param token single-use 64-character token
     * @param parentOrigin optional origin of the parent page
     * @return signer credentials; never log or persist in browser storage
     */
    public EmbedSessionExchange exchange(String token, String parentOrigin) {
        requireId(token, "Embed token");
        return httpPost("/embed/sessions/" + token + "/exchange",
                parentOrigin == null ? Map.of() : Map.of("parent_origin", parentOrigin), EmbedSessionExchange.class);
    }

    private String path(String accountId) { return "/accounts/" + accountId(accountId) + "/embed-origins"; }

    private static void validateOrigin(String origin) {
        requireValue(origin, "Embed origin");
        try {
            URI uri = URI.create(origin);
            boolean local = "localhost".equalsIgnoreCase(uri.getHost()) || "127.0.0.1".equals(uri.getHost());
            if (uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawQuery() != null
                    || uri.getRawFragment() != null || !(uri.getRawPath().isEmpty() || "/".equals(uri.getRawPath()))
                    || !("https".equalsIgnoreCase(uri.getScheme())
                    || (local && "http".equalsIgnoreCase(uri.getScheme()))) || uri.getPort() > 65535) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Embed origin must be an exact HTTPS origin (HTTP only on localhost)");
        }
    }
}
