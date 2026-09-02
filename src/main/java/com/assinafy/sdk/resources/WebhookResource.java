package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.ListDispatchesParams;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.RegisterWebhookPayload;
import com.assinafy.sdk.models.WebhookDispatch;
import com.assinafy.sdk.models.WebhookEventTypeInfo;
import com.assinafy.sdk.models.WebhookSubscription;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.OkHttpClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/** Client for webhook subscriptions, event types, dispatch history, and retries. */
public final class WebhookResource extends BaseResource {

    /**
     * Creates an instance.
     *
     * @param httpClient shared HTTP client
     * @param baseUrl API base URL
     * @param defaultAccountId default account identifier, or {@code null}
     */
    public WebhookResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    /**
     * {@code PUT /accounts/{account_id}/webhooks/subscriptions} — create or replace the workspace's single
     * webhook subscription. The body carries {@code url}, {@code email}, {@code events}, and {@code is_active}
     * (defaults to {@code true} when {@code payload.active} is {@code null}).
     *
     * @param payload required subscription values
     * @param accountId account override, or {@code null} for the client default
     * @return created or replaced subscription
     */
    public WebhookSubscription register(RegisterWebhookPayload payload, String accountId) {
        if (payload == null) {
            throw new ValidationException("Webhook payload is required");
        }
        if (payload.getUrl() == null || payload.getUrl().isBlank()) {
            throw new ValidationException("Webhook URL is required");
        }
        try {
            URI uri = new URI(payload.getUrl());
            if (!uri.isAbsolute() || uri.getHost() == null || (!"http".equalsIgnoreCase(uri.getScheme())
                    && !"https".equalsIgnoreCase(uri.getScheme()))) {
                throw new ValidationException("Webhook URL must be an absolute HTTP or HTTPS URL");
            }
        } catch (URISyntaxException e) {
            throw new ValidationException("Webhook URL must be a valid URI");
        }
        requireEmail(payload.getEmail(), "Webhook email");
        if (payload.getEvents() == null || payload.getEvents().isEmpty()
                || payload.getEvents().stream().anyMatch(event -> event == null || event.isBlank())) {
            throw new ValidationException("At least one webhook event is required");
        }

        String id = accountId(accountId);
        boolean active = payload.getActive() == null || payload.getActive();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("url", payload.getUrl());
        body.put("email", payload.getEmail());
        body.put("events", payload.getEvents());
        body.put("is_active", active);

        return httpPut("/accounts/" + id + "/webhooks/subscriptions", body, WebhookSubscription.class);
    }

    /**
     * Returns created or replaced subscription in the default account.
     *
     * @param payload required subscription values
     * @return created or replaced subscription in the default account
     */
    public WebhookSubscription register(RegisterWebhookPayload payload) {
        return register(payload, null);
    }

    /**
     * Alias for {@link #register(RegisterWebhookPayload, String)}. The subscription uses one create-or-replace
     * {@code PUT}, so registering and updating invoke the same operation.
     *
     * @param payload required subscription values
     * @param accountId account override, or {@code null} for the client default
     * @return created or replaced subscription
     */
    public WebhookSubscription update(RegisterWebhookPayload payload, String accountId) {
        return register(payload, accountId);
    }

    /**
     * Returns created or replaced subscription in the default account.
     *
     * @param payload required subscription values
     * @return created or replaced subscription in the default account
     */
    public WebhookSubscription update(RegisterWebhookPayload payload) {
        return register(payload, null);
    }

    /**
     * {@code GET /accounts/{account_id}/webhooks/subscriptions} — fetch the workspace's webhook subscription.
     * A valid account returns an inactive subscription object when none has been configured; invalid accounts
     * surface the API's 404 as an {@link com.assinafy.sdk.exceptions.ApiException}.
     *
     * @param accountId account override, or {@code null} for the client default
     * @return workspace subscription, including the inactive/default representation
     */
    public WebhookSubscription getSubscription(String accountId) {
        String id = accountId(accountId);
        return httpGet("/accounts/" + id + "/webhooks/subscriptions", WebhookSubscription.class);
    }

    /**
     * Returns default account's webhook subscription.
     *
     * @return default account's webhook subscription
     */
    public WebhookSubscription getSubscription() {
        return getSubscription(null);
    }

    /**
     * {@code PUT /accounts/{account_id}/webhooks/inactivate} — deactivate the subscription (sets
     * {@code is_active=false}) without deleting it, so it can be re-activated later via
     * {@link #register(RegisterWebhookPayload)}.
     *
     * @param accountId account override, or {@code null} for the client default
     * @return inactive subscription
     */
    public WebhookSubscription inactivate(String accountId) {
        String id = accountId(accountId);
        return httpPut("/accounts/" + id + "/webhooks/inactivate", null, WebhookSubscription.class);
    }

    /**
     * Returns default account's inactive subscription.
     *
     * @return default account's inactive subscription
     */
    public WebhookSubscription inactivate() {
        return inactivate(null);
    }

    /**
     * {@code GET /webhooks/event-types}.
     *
     * @return subscribable event types, never {@code null}
     */
    public List<WebhookEventTypeInfo> listEventTypes() {
        return orEmpty(httpGet("/webhooks/event-types",
                new TypeReference<List<WebhookEventTypeInfo>>() {}));
    }

    /**
     * {@code GET /accounts/{account_id}/webhooks} — list webhook delivery dispatches, with optional filters
     * ({@code page}, {@code per-page}, {@code event}, {@code delivered}, {@code from}, {@code to}).
     *
     * @param params optional typed filters and pagination values
     * @param accountId account override, or {@code null} for the client default
     * @return webhook-dispatch page
     */
    public PaginatedResult<WebhookDispatch> listDispatches(ListDispatchesParams params, String accountId) {
        String id = accountId(accountId);
        Map<String, String> queryParams = buildDispatchQueryParams(params);
        return httpGetList("/accounts/" + id + "/webhooks", queryParams, WebhookDispatch.class);
    }

    /**
     * Returns default account's webhook-dispatch page.
     *
     * @param params optional typed filters and pagination values
     * @return default account's webhook-dispatch page
     */
    public PaginatedResult<WebhookDispatch> listDispatches(ListDispatchesParams params) {
        return listDispatches(params, null);
    }

    /**
     * Returns default account's first webhook-dispatch page.
     *
     * @return default account's first webhook-dispatch page
     */
    public PaginatedResult<WebhookDispatch> listDispatches() {
        return listDispatches(null, null);
    }

    /**
     * {@code POST /accounts/{account_id}/webhooks/{dispatch_id}/retry}.
     *
     * @param dispatchId required dispatch identifier
     * @param accountId account override, or {@code null} for the client default
     * @return updated dispatch record
     */
    public WebhookDispatch retryDispatch(String dispatchId, String accountId) {
        String id = accountId(accountId);
        String did = requireId(dispatchId, "Dispatch ID");
        return httpPost("/accounts/" + id + "/webhooks/" + did + "/retry", null, WebhookDispatch.class);
    }

    /**
     * Returns updated dispatch record in the default account.
     *
     * @param dispatchId required dispatch identifier
     * @return updated dispatch record in the default account
     */
    public WebhookDispatch retryDispatch(String dispatchId) {
        return retryDispatch(dispatchId, null);
    }

    private Map<String, String> buildDispatchQueryParams(ListDispatchesParams params) {
        Map<String, String> result = new HashMap<>();
        if (params == null) return result;
        if (params.getPage() != null) result.put("page", String.valueOf(params.getPage()));
        if (params.getPerPage() != null) result.put("per-page", String.valueOf(params.getPerPage()));
        if (params.getEvent() != null && !params.getEvent().isBlank()) result.put("event", params.getEvent());
        if (params.getDelivered() != null) result.put("delivered", String.valueOf(params.getDelivered()));
        if (params.getFrom() != null) result.put("from", String.valueOf(params.getFrom()));
        if (params.getTo() != null) result.put("to", String.valueOf(params.getTo()));
        return result;
    }
}
