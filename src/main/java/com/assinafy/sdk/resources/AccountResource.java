package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.AccountPayload;
import com.assinafy.sdk.models.AccountTheme;
import com.assinafy.sdk.models.DocumentStatsRow;
import com.assinafy.sdk.models.WorkspaceAccount;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/** Client for the account, branding, logo, and account KPI endpoints. */
public final class AccountResource extends BaseResource {

    /**
     * Creates an instance.
     *
     * @param httpClient shared HTTP client
     * @param baseUrl API base URL
     * @param defaultAccountId default account identifier, or {@code null}
     */
    public AccountResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    /**
     * {@code GET /accounts} — returns every workspace the authenticated user belongs to.
     *
     * @return workspace accounts, never {@code null}
     */
    public List<WorkspaceAccount> list() {
        return orEmpty(httpGet("/accounts", new TypeReference<List<WorkspaceAccount>>() {}));
    }

    /**
     * {@code POST /accounts} with {@code {name, notification_sender_type?}}.
     *
     * @param payload required account fields
     * @return created workspace
     */
    public WorkspaceAccount create(AccountPayload payload) {
        validatePayload(payload, true);
        return httpPost("/accounts", payload, WorkspaceAccount.class);
    }

    /**
     * {@code GET /accounts/{accountId}}.
     *
     * @param accountId account override, or {@code null} for the client default
     * @return selected workspace
     */
    public WorkspaceAccount get(String accountId) {
        String id = accountId(accountId);
        return httpGet("/accounts/" + id, WorkspaceAccount.class);
    }

    /**
     * Returns default workspace.
     *
     * @return default workspace
     */
    public WorkspaceAccount get() { return get(null); }

    /**
     * {@code PUT /accounts/{accountId}} with supplied account fields.
     *
     * @param payload required fields to update
     * @param accountId account override, or {@code null} for the client default
     * @return updated workspace
     */
    public WorkspaceAccount update(AccountPayload payload, String accountId) {
        validatePayload(payload, false);
        String id = accountId(accountId);
        return httpPut("/accounts/" + id, payload, WorkspaceAccount.class);
    }

    /**
     * Returns updated default workspace.
     *
     * @param payload required fields to update
     * @return updated default workspace
     */
    public WorkspaceAccount update(AccountPayload payload) { return update(payload, null); }

    /**
     * {@code DELETE /accounts/{accountId}} with {@code {force}} — permanently delete a workspace. With
     * {@code force=true}, the API first cancels any active paid subscription.
     *
     * @param force whether to cancel an active subscription first
     * @param accountId account override, or {@code null} for the client default
     */
    public void delete(boolean force, String accountId) {
        String id = accountId(accountId);
        httpDeleteBody("/accounts/" + id, Map.of("force", force));
    }

    /**
     * Deletes the selected resource.
     *
     * @param force whether to cancel an active subscription first
     */
    public void delete(boolean force) { delete(force, null); }

    /**
     * {@code GET /accounts/{accountId}/theme}.
     *
     * @param accountId account override, or {@code null} for the client default
     * @return account name, colors, and logo URL
     */
    public AccountTheme getTheme(String accountId) {
        String id = accountId(accountId);
        return httpGet("/accounts/" + id + "/theme", AccountTheme.class);
    }

    /**
     * Returns default account's theme.
     *
     * @return default account's theme
     */
    public AccountTheme getTheme() { return getTheme(null); }

    /**
     * {@code GET /accounts/{accountId}/logo}.
     *
     * @param accountId account override, or {@code null} for the client default
     * @return current logo image bytes
     */
    public byte[] downloadLogo(String accountId) {
        String id = accountId(accountId);
        return httpGetBinary("/accounts/" + id + "/logo");
    }

    /**
     * Returns default account's logo image bytes.
     *
     * @return default account's logo image bytes
     */
    public byte[] downloadLogo() { return downloadLogo(null); }

    /**
     * {@code POST /accounts/{accountId}/logo} — upload/replace the logo as multipart field {@code file}.
     * The content type is inferred from {@code fileName}, falling back to {@code application/octet-stream}.
     *
     * @param bytes required image bytes
     * @param fileName required source filename used for content-type detection
     * @param accountId account override, or {@code null} for the client default
     */
    public void uploadLogo(byte[] bytes, String fileName, String accountId) {
        if (bytes == null || bytes.length == 0) throw new ValidationException("Logo bytes are required");
        if (fileName == null || fileName.isBlank()) throw new ValidationException("Logo file name is required");
        String id = accountId(accountId);
        String guessedType = URLConnection.guessContentTypeFromName(fileName);
        MediaType type = MediaType.get(guessedType != null ? guessedType : "application/octet-stream");
        RequestBody multipart = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, RequestBody.create(bytes, type))
                .build();
        httpPostMultipart("/accounts/" + id + "/logo", multipart, Object.class);
    }

    /**
     * Uploads the supplied logo.
     *
     * @param bytes required image bytes
     * @param fileName required source filename
     */
    public void uploadLogo(byte[] bytes, String fileName) { uploadLogo(bytes, fileName, null); }

    /**
     * {@code DELETE /accounts/{accountId}/logo}.
     *
     * @param accountId account override, or {@code null} for the client default
     */
    public void deleteLogo(String accountId) {
        String id = accountId(accountId);
        httpDelete("/accounts/" + id + "/logo");
    }

    /** Removes the default account's current logo. */
    public void deleteLogo() { deleteLogo(null); }

    /**
     * {@code GET /accounts/{accountId}/stats} — return monthly/daily KPI rows. Supported query keys are
     * {@code granularity} ({@code monthly|daily}) and {@code month} ({@code YYYY-MM}, required for daily).
     *
     * @param params optional {@code granularity} and {@code month} query values
     * @param accountId account override, or {@code null} for the client default
     * @return document KPI rows, never {@code null}
     */
    public List<DocumentStatsRow> stats(Map<String, String> params, String accountId) {
        String id = accountId(accountId);
        return orEmpty(httpGet("/accounts/" + id + "/stats", params != null ? params : Map.of(),
                new TypeReference<List<DocumentStatsRow>>() {}));
    }

    /**
     * Returns default account's KPI rows.
     *
     * @param params optional statistics query values
     * @return default account's KPI rows
     */
    public List<DocumentStatsRow> stats(Map<String, String> params) { return stats(params, null); }

    /**
     * Returns default account's monthly KPI rows.
     *
     * @return default account's monthly KPI rows
     */
    public List<DocumentStatsRow> stats() { return stats(null, null); }

    private void validatePayload(AccountPayload payload, boolean requireName) {
        if (payload == null) throw new ValidationException("Account payload is required");
        if (requireName && (payload.getName() == null || payload.getName().isBlank())) {
            throw new ValidationException("Account name is required");
        }
        if (payload.getName() != null && payload.getName().isBlank()) {
            throw new ValidationException("Account name cannot be blank");
        }
        String sender = payload.getNotificationSenderType();
        if (sender != null && !"User".equals(sender) && !"Account".equals(sender)) {
            throw new ValidationException("Notification sender type must be 'User' or 'Account'");
        }
        if (!requireName && payload.getName() == null && sender == null) {
            throw new ValidationException("At least one account attribute is required");
        }
    }
}
