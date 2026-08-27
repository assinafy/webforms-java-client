package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.ApiKeyResponse;
import com.assinafy.sdk.models.AuthenticationResult;
import com.assinafy.sdk.models.EmailResponse;
import com.assinafy.sdk.models.SocialLoginPayload;
import okhttp3.OkHttpClient;

import java.util.LinkedHashMap;
import java.util.Map;

/** Client for login, password recovery, social-login linking, and user API-key endpoints. */
public final class AuthenticationResource extends BaseResource {

    /**
     * Creates an instance.
     *
     * @param httpClient shared HTTP client
     * @param baseUrl API base URL
     */
    public AuthenticationResource(OkHttpClient httpClient, String baseUrl) {
        super(httpClient, baseUrl, null);
    }

    /**
     * {@code POST /login} — exchanges user credentials for an access token.
     *
     * @param email required user email
     * @param password required password
     * @return authenticated user and access token
     */
    public AuthenticationResult login(String email, String password) {
        requireEmail(email, "Email");
        requireValue(password, "Password");
        return httpPost("/login", Map.of("email", email, "password", password), AuthenticationResult.class);
    }

    /**
     * {@code POST /authentication/social-login} — exchanges a provider token for an access token.
     *
     * @param payload required provider, token, and terms-acceptance fields
     * @return authenticated user and access token
     */
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
     * {@code POST /auth/link-social-login} - link a social-login provider to the authenticated user's account.
     *
     * <p>The request body is {@code {provider, token}} — note there is no {@code has_accepted_terms} here,
     * unlike {@link #socialLogin(SocialLoginPayload)}. The success envelope carries no data, so this returns
     * {@code void}. Requires API-key or bearer authentication.</p>
     *
     * @param provider social provider identifier (e.g. {@code "google"})
     * @param token token issued by the provider
     */
    public void linkSocialLogin(String provider, String token) {
        requireValue(provider, "Provider");
        requireValue(token, "Provider token");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("provider", provider);
        body.put("token", token);
        httpPostVoid("/auth/link-social-login", body);
    }

    /**
     * {@code POST /users/api-keys} - create a new API key for the authenticated user.
     *
     * <p>The current API accepts either bearer authentication or {@code X-Api-Key}.</p>
     *
     * @param password required current user password
     * @return newly created API key
     */
    public ApiKeyResponse createApiKey(String password) {
        requireValue(password, "Password");
        return httpPost("/users/api-keys", Map.of("password", password), ApiKeyResponse.class);
    }

    /**
     * {@code GET /users/api-keys} — retrieves the masked API key for the authenticated user.
     *
     * @return API-key metadata
     */
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
     * <p>The current API accepts either bearer authentication or {@code X-Api-Key}.</p>
     *
     * @param email required user email
     * @param password required current password
     * @param newPassword required new password
     * @return server confirmation message
     */
    public EmailResponse changePassword(String email, String password, String newPassword) {
        requireEmail(email, "Email");
        requireValue(password, "Current password");
        requireValue(newPassword, "New password");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("new_password", newPassword);
        return httpPut("/authentication/change-password", body, EmailResponse.class);
    }

    /**
     * {@code PUT /authentication/request-password-reset} — requests password-reset instructions.
     *
     * @param email required user email
     * @return server confirmation message
     */
    public EmailResponse requestPasswordReset(String email) {
        requireEmail(email, "Email");
        return httpPut("/authentication/request-password-reset", Map.of("email", email), EmailResponse.class);
    }

    /**
     * {@code PUT /authentication/reset-password} - reset a password using an emailed reset token.
     *
     * <p>{@code email} and {@code newPassword} are required. {@code token} is optional and is omitted from the
     * request body when it is {@code null} or blank.</p>
     *
     * @param email required user email
     * @param token optional emailed reset token
     * @param newPassword required new password
     * @return server confirmation message
     */
    public EmailResponse resetPassword(String email, String token, String newPassword) {
        requireEmail(email, "Email");
        requireValue(newPassword, "New password");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", email);
        if (token != null && !token.isBlank()) {
            body.put("token", token);
        }
        body.put("new_password", newPassword);
        return httpPut("/authentication/reset-password", body, EmailResponse.class);
    }

    private void requireValue(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(name + " is required");
        }
    }
}
