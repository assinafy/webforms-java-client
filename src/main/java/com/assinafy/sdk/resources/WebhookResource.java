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

import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WebhookResource extends BaseResource {

    public WebhookResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    /**
     * {@code PUT /accounts/{account_id}/webhooks/subscriptions} — create or replace the workspace's single
     * webhook subscription. The body carries {@code url}, {@code email}, {@code events}, and {@code is_active}
     * (defaults to {@code true} when {@code payload.active} is {@code null}).
     */
    public WebhookSubscription register(RegisterWebhookPayload payload, String accountId) {
        if (payload.getUrl() == null || payload.getUrl().isBlank()) {
            throw new ValidationException("Webhook URL is required");
        }
        if (payload.getEmail() == null || payload.getEmail().isBlank()) {
            throw new ValidationException("Webhook email is required");
        }
        if (payload.getEvents() == null || payload.getEvents().isEmpty()) {
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

    public WebhookSubscription register(RegisterWebhookPayload payload) {
        return register(payload, null);
    }

    /**
     * {@code GET /accounts/{account_id}/webhooks/subscriptions} — fetch the current webhook subscription, or
     * {@code null} when none is configured (the API's 404 is mapped to {@code null}).
     */
    public WebhookSubscription getSubscription(String accountId) {
        String id = accountId(accountId);
        return httpGetOptional("/accounts/" + id + "/webhooks/subscriptions", WebhookSubscription.class);
    }

    public WebhookSubscription getSubscription() {
        return getSubscription(null);
    }

    /**
     * {@code DELETE /accounts/{account_id}/webhooks/subscriptions} — permanently delete the webhook
     * subscription. To keep it but stop deliveries, use {@link #inactivate} instead.
     */
    public void deleteSubscription(String accountId) {
        String id = accountId(accountId);
        httpDelete("/accounts/" + id + "/webhooks/subscriptions");
    }

    public void deleteSubscription() {
        deleteSubscription(null);
    }

    /**
     * {@code PUT /accounts/{account_id}/webhooks/inactivate} — deactivate the subscription (sets
     * {@code is_active=false}) without deleting it, so it can be re-activated later via {@link #register}.
     */
    public WebhookSubscription inactivate(String accountId) {
        String id = accountId(accountId);
        return httpPut("/accounts/" + id + "/webhooks/inactivate", null, WebhookSubscription.class);
    }

    public WebhookSubscription inactivate() {
        return inactivate(null);
    }

    /** {@code GET /webhooks/event-types} — list the catalogue of subscribable webhook event types. */
    public List<WebhookEventTypeInfo> listEventTypes() {
        return httpGet("/webhooks/event-types", new TypeReference<List<WebhookEventTypeInfo>>() {});
    }

    /**
     * {@code GET /accounts/{account_id}/webhooks} — list webhook delivery dispatches, with optional filters
     * ({@code page}, {@code per-page}, {@code event}, {@code delivered}, {@code from}, {@code to}).
     */
    public PaginatedResult<WebhookDispatch> listDispatches(ListDispatchesParams params, String accountId) {
        String id = accountId(accountId);
        Map<String, String> queryParams = buildDispatchQueryParams(params);
        return httpGetList("/accounts/" + id + "/webhooks", queryParams, WebhookDispatch.class);
    }

    public PaginatedResult<WebhookDispatch> listDispatches(ListDispatchesParams params) {
        return listDispatches(params, null);
    }

    public PaginatedResult<WebhookDispatch> listDispatches() {
        return listDispatches(null, null);
    }

    /** {@code POST /accounts/{account_id}/webhooks/{dispatch_id}/retry} — re-attempt a failed webhook delivery. */
    public WebhookDispatch retryDispatch(String dispatchId, String accountId) {
        String id = accountId(accountId);
        String did = requireId(dispatchId, "Dispatch ID");
        return httpPost("/accounts/" + id + "/webhooks/" + did + "/retry", null, WebhookDispatch.class);
    }

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
