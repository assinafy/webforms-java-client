package com.assinafy.sdk.resources;

import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.ScheduledSend;
import okhttp3.OkHttpClient;
import java.util.Map;

/** Workspace scheduled sends. Every operation requires documents:write for OAuth. */
public final class ScheduledSendResource extends BaseResource {
    /**
     * Creates the resource.
     * @param httpClient shared transport
     * @param baseUrl API base URL
     * @param defaultAccountId default workspace
     */
    public ScheduledSendResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    /**
     * {@code GET /accounts/{accountId}/scheduled-sends}. Lists all statuses, newest first.
     * @param params optional document_id, page and per-page
     * @param accountId workspace override, or null
     * @return scheduled sends with response-header pagination
     */
    public PaginatedResult<ScheduledSend> list(Map<String, String> params, String accountId) {
        return httpGetList(path(accountId), params, ScheduledSend.class);
    }

    /**
     * {@code GET /accounts/{accountId}/scheduled-sends} in the default workspace.
     * @param params optional document_id, page and per-page
     * @return scheduled sends with pagination
     */
    public PaginatedResult<ScheduledSend> list(Map<String, String> params) { return list(params, null); }

    /**
     * {@code GET /accounts/{accountId}/scheduled-sends} in the default workspace.
     * @return first scheduled-send page
     */
    public PaginatedResult<ScheduledSend> list() { return list(null, null); }

    /**
     * {@code PUT /accounts/{accountId}/scheduled-sends/{scheduledSendId}/cancel}.
     * Only pending sends can be cancelled; any other status returns HTTP 409.
     * @param scheduledSendId scheduled send identifier
     * @param accountId workspace override, or null
     * @return cancelled send
     */
    public ScheduledSend cancel(String scheduledSendId, String accountId) {
        return httpPut(path(accountId) + "/" + requireId(scheduledSendId, "Scheduled send ID") + "/cancel",
                null, ScheduledSend.class);
    }

    /**
     * {@code PUT /accounts/{accountId}/scheduled-sends/{scheduledSendId}/cancel} in the default workspace.
     * @param scheduledSendId scheduled send identifier
     * @return cancelled send
     */
    public ScheduledSend cancel(String scheduledSendId) { return cancel(scheduledSendId, null); }

    /**
     * {@code PUT /accounts/{accountId}/scheduled-sends/{scheduledSendId}/send}.
     * Sends a pending request now, with normal creation checks and charges. A refusal leaves it pending.
     * @param scheduledSendId scheduled send identifier
     * @param accountId workspace override, or null
     * @return sent request with its assignment or envelope identifier
     */
    public ScheduledSend sendNow(String scheduledSendId, String accountId) {
        return httpPut(path(accountId) + "/" + requireId(scheduledSendId, "Scheduled send ID") + "/send",
                null, ScheduledSend.class);
    }

    /**
     * {@code PUT /accounts/{accountId}/scheduled-sends/{scheduledSendId}/send} in the default workspace.
     * @param scheduledSendId scheduled send identifier
     * @return sent request
     */
    public ScheduledSend sendNow(String scheduledSendId) { return sendNow(scheduledSendId, null); }

    private String path(String accountId) { return "/accounts/" + accountId(accountId) + "/scheduled-sends"; }
}
