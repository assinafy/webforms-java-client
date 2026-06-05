package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.ApiKeyResponse;
import com.assinafy.sdk.models.AuthenticationResult;
import com.assinafy.sdk.models.EmailResponse;
import com.assinafy.sdk.models.SocialLoginPayload;
import okhttp3.OkHttpClient;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AuthenticationResource extends BaseResource {

    public AuthenticationResource(OkHttpClient httpClient, String baseUrl) {
        super(httpClient, baseUrl, null);
    }

    /** {@code POST /login} - exchange user credentials for an access token. */
    public AuthenticationResult login(String email, String password) {
        requireEmail(email);
        requireValue(password, "Password");
        return httpPost("/login", Map.of("email", email, "password", password), AuthenticationResult.class);
    }

    /** {@code POST /authentication/social-login} - exchange a provider token for an access token. */
    public AuthenticationResult socialLogin(SocialLoginPayload payload) {
        if (payload == null) {
            throw new ValidationException("Social login payload is required");
        }
        requireValue(payload.getProvider(), "Provider");
        requireValue(payload.getToken(), "Provider token");
        if (payload.getHasAcceptedTerms() == null) {
            throw new ValidationException("has_accepted_terms is required");
        }
        return httpPost("/authentication/social-login", payload, AuthenticationResult.class);
    }

    /**
     * {@code POST /users/api-keys} - create a new API key for the authenticated user.
     *
     * <p>This endpoint is protected by {@code Authorization: Bearer {access_token}}, so it requires a client
     * configured with a token (via {@link com.assinafy.sdk.AssinafyClientOptions#setToken}, typically obtained
     * from {@link #login}). It will not succeed on an API-key-only client.</p>
     */
    public ApiKeyResponse createApiKey(String password) {
        requireValue(password, "Password");
        return httpPost("/users/api-keys", Map.of("password", password), ApiKeyResponse.class);
    }

    /** {@code GET /users/api-keys} - retrieve the masked API key for the authenticated user. */
    public ApiKeyResponse getApiKey() {
        return httpGet("/users/api-keys", ApiKeyResponse.class);
    }

    /** {@code DELETE /users/api-keys} - delete the authenticated user's API key. */
    public void deleteApiKey() {
        httpDelete("/users/api-keys");
    }

    /**
     * {@code PUT /authentication/change-password} - change the authenticated user's password.
     *
     * <p>Protected by {@code Authorization: Bearer {access_token}}; requires a token-configured client
     * (see {@link #login}), not an API-key-only client.</p>
     */
    public EmailResponse changePassword(String email, String password, String newPassword) {
        requireEmail(email);
        requireValue(password, "Current password");
        requireValue(newPassword, "New password");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("new_password", newPassword);
        return httpPut("/authentication/change-password", body, EmailResponse.class);
    }

    /** {@code PUT /authentication/request-password-reset} - request password reset instructions by email. */
    public EmailResponse requestPasswordReset(String email) {
        requireEmail(email);
        return httpPut("/authentication/request-password-reset", Map.of("email", email), EmailResponse.class);
    }

    /**
     * {@code PUT /authentication/reset-password} - reset a password using an emailed reset token.
     *
     * <p>Per the API, {@code email} and {@code newPassword} are required; {@code token} is optional (the docs
     * mark it {@code false}) because it may be supplied out-of-band as a URL parameter rather than in the body.
     * When {@code token} is {@code null}/blank it is simply omitted from the request body.</p>
     */
    public EmailResponse resetPassword(String email, String token, String newPassword) {
        requireEmail(email);
        requireValue(newPassword, "New password");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", email);
        if (token != null && !token.isBlank()) {
            body.put("token", token);
        }
        body.put("new_password", newPassword);
        return httpPut("/authentication/reset-password", body, EmailResponse.class);
    }

    private void requireEmail(String email) {
        requireValue(email, "Email");
    }

    private void requireValue(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(name + " is required");
        }
    }
}
