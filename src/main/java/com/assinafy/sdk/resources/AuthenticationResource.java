package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.ApiKeyResponse;
import com.assinafy.sdk.models.AuthenticationResult;
import com.assinafy.sdk.models.EmailResponse;
import com.assinafy.sdk.models.MfaMethods;
import com.assinafy.sdk.models.SocialLoginPayload;
import com.assinafy.sdk.models.TotpEnrollment;
import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.OkHttpClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Client for login, two-factor authentication, password recovery, social-login linking, and user API keys. */
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
     * {@code POST /login} — exchanges user credentials for an access token. For a user with two-factor
     * authentication enabled the result carries {@link AuthenticationResult#getMfaToken()} instead; complete the
     * login with {@link #verifyMfa(String, String)}.
     *
     * @param email required user email
     * @param password required password
     * @return authenticated user and access token, or the two-factor challenge
     */
    public AuthenticationResult login(String email, String password) {
        requireEmail(email, "Email");
        requireValue(password, "Password");
        return httpPost("/login", Map.of("email", email, "password", password), AuthenticationResult.class);
    }

    /**
     * {@code POST /authentication/mfa/verify} — completes a two-factor login. The challenge is single-use and
     * expires five minutes after login; an expired, reused, or over-tried challenge fails with HTTP 401.
     *
     * @param mfaToken required {@code mfa_token} returned by {@link #login(String, String)}
     * @param code required 6-digit authenticator code, or one of the recovery codes issued at enrollment
     * @return authenticated user and access token
     */
    public AuthenticationResult verifyMfa(String mfaToken, String code) {
        requireValue(mfaToken, "MFA token");
        requireValue(code, "MFA code");
        return httpPost("/authentication/mfa/verify", Map.of("mfa_token", mfaToken, "code", code),
                AuthenticationResult.class);
    }

    /**
     * {@code GET /users/self/mfa} — the authenticated user's enrolled two-factor methods and how many recovery
     * codes remain unused.
     *
     * @return enrolled methods and remaining recovery codes
     */
    public MfaMethods listMfaMethods() {
        return httpGet("/users/self/mfa", MfaMethods.class);
    }

    /**
     * {@code POST /users/self/mfa/totp} — starts an authenticator-app enrollment. The returned secret is shown
     * only once; two-factor authentication is not active until
     * {@link #confirmTotpEnrollment(String, String, String, String)} succeeds.
     *
     * @param label optional label for the device, or {@code null}
     * @return unconfirmed method with its shared secret and {@code otpauth://} URI
     */
    public TotpEnrollment startTotpEnrollment(String label) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (label != null && !label.isBlank()) body.put("label", label);
        return httpPost("/users/self/mfa/totp", body, TotpEnrollment.class);
    }

    /**
     * {@code PUT /users/self/mfa/totp/confirm} — activates an enrollment by proving a live code from the new
     * device. From then on every login requires a second factor. If the user already has a confirmed method
     * of the same type, confirming replaces it and requires re-authentication through {@code password} or
     * {@code reauthCode}; first-time enrollment needs neither.
     *
     * @param methodId required method identifier from {@link #startTotpEnrollment(String)}
     * @param code required live code from the new device
     * @param password current password when replacing a method, or {@code null}
     * @param reauthCode live code from the current device, or a recovery code, when replacing a method, or
     *        {@code null}
     * @return the recovery codes, shown only once
     */
    public List<String> confirmTotpEnrollment(String methodId, String code, String password, String reauthCode) {
        requireValue(methodId, "MFA method ID");
        requireValue(code, "MFA code");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", methodId);
        body.put("code", code);
        if (password != null && !password.isBlank()) body.put("password", password);
        if (reauthCode != null && !reauthCode.isBlank()) body.put("reauth_code", reauthCode);
        return recoveryCodes(httpPut("/users/self/mfa/totp/confirm", body, JsonNode.class));
    }

    /**
     * {@code POST /users/self/mfa/recovery-codes} — issues ten fresh recovery codes and invalidates the
     * previous set. Proves identity with the current password or a live code; a recovery code used as proof
     * is consumed.
     *
     * @param password current password, or {@code null} when {@code code} is given
     * @param code live authenticator code or existing recovery code, or {@code null} when {@code password} is
     *        given
     * @return the new recovery codes, shown only once
     */
    public List<String> regenerateRecoveryCodes(String password, String code) {
        return recoveryCodes(httpPost("/users/self/mfa/recovery-codes", reauthentication(password, code),
                JsonNode.class));
    }

    /**
     * {@code DELETE /users/self/mfa/{method_id}} — removes an enrolled method. Proves identity with the current
     * password or a live code, so a stolen session cannot silently disable two-factor authentication. Removing
     * the last method also discards the recovery codes.
     *
     * @param methodId required method identifier from {@link #listMfaMethods()}
     * @param password current password, or {@code null} when {@code code} is given
     * @param code live authenticator code or existing recovery code, or {@code null} when {@code password} is
     *        given
     * @return whether two-factor authentication is still enabled
     */
    public boolean removeMfaMethod(String methodId, String password, String code) {
        String id = requireId(methodId, "MFA method ID");
        JsonNode data = httpDelete("/users/self/mfa/" + id, reauthentication(password, code), JsonNode.class);
        return data != null && data.path("is_mfa_enabled").asBoolean(false);
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

    private static Map<String, Object> reauthentication(String password, String code) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (password != null && !password.isBlank()) body.put("password", password);
        if (code != null && !code.isBlank()) body.put("code", code);
        if (body.isEmpty()) {
            throw new ValidationException("A password or MFA code is required");
        }
        return body;
    }

    private static List<String> recoveryCodes(JsonNode data) {
        List<String> codes = new ArrayList<>();
        if (data != null) data.path("recovery_codes").forEach(code -> codes.add(code.asText()));
        return codes;
    }

    private void requireValue(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(name + " is required");
        }
    }
}
