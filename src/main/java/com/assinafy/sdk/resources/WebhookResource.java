package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.ListDispatchesParams;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.RegisterWebhookPayload;
import com.assinafy.sdk.models.WebhookDispatch;
import com.assinafy.sdk.models.WebhookEndpoint;
import com.assinafy.sdk.models.WebhookEndpointPayload;
import com.assinafy.sdk.models.WebhookEventTypeInfo;
import com.assinafy.sdk.models.WebhookSubscription;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.OkHttpClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Client for webhook endpoints and their signing secrets, the legacy single subscription, event types, dispatch
 * history, and retries.
 *
 * <p>An account has one webhook endpoint, or up to three on paid plans. The {@code /webhooks/subscriptions}
 * operations ({@link #register}, {@link #getSubscription}, {@link #inactivate}) act on the account's oldest
 * endpoint; the {@code /webhooks/endpoints} operations manage each endpoint individually.</p>
 */
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
     * {@code GET /accounts/{account_id}/webhooks/endpoints} — list the account's webhook endpoints, oldest
     * first.
     *
     * @param accountId account override, or {@code null} for the client default
     * @return webhook endpoints, never {@code null}
     */
    public List<WebhookEndpoint> listEndpoints(String accountId) {
        String id = accountId(accountId);
        return orEmpty(httpGet("/accounts/" + id + "/webhooks/endpoints",
                new TypeReference<List<WebhookEndpoint>>() {}));
    }

    /**
     * {@code GET /accounts/{account_id}/webhooks/endpoints}.
     *
     * @return default account's webhook endpoints, never {@code null}
     */
    public List<WebhookEndpoint> listEndpoints() {
        return listEndpoints(null);
    }

    /**
     * {@code POST /accounts/{account_id}/webhooks/endpoints} — register a URL to receive the account's events.
     * Requires {@code url}, {@code email}, and {@code events}. Creating an endpoint past the plan's limit
     * (1, or 3 on paid plans) fails with HTTP 403; reusing another endpoint's URL fails with HTTP 400. When
     * {@code signing_enabled} is {@code true} a signing secret is generated; read it with
     * {@link #getEndpointSecret(String, String)}.
     *
     * @param payload endpoint values
     * @param accountId account override, or {@code null} for the client default
     * @return created endpoint
     */
    public WebhookEndpoint createEndpoint(WebhookEndpointPayload payload, String accountId) {
        if (payload == null) {
            throw new ValidationException("Webhook endpoint payload is required");
        }
        requireWebhookUrl(payload.getUrl());
        requireEmail(payload.getEmail(), "Webhook email");
        requireEvents(payload.getEvents());
        String id = accountId(accountId);
        return httpPost("/accounts/" + id + "/webhooks/endpoints", payload, WebhookEndpoint.class);
    }

    /**
     * {@code POST /accounts/{account_id}/webhooks/endpoints}.
     *
     * @param payload endpoint values
     * @return created endpoint in the default account
     */
    public WebhookEndpoint createEndpoint(WebhookEndpointPayload payload) {
        return createEndpoint(payload, null);
    }

    /**
     * {@code GET /accounts/{account_id}/webhooks/endpoints/{endpoint_id}}.
     *
     * @param endpointId required endpoint identifier
     * @param accountId account override, or {@code null} for the client default
     * @return webhook endpoint
     */
    public WebhookEndpoint getEndpoint(String endpointId, String accountId) {
        return httpGet(endpointPath(endpointId, accountId), WebhookEndpoint.class);
    }

    /**
     * {@code GET /accounts/{account_id}/webhooks/endpoints/{endpoint_id}}.
     *
     * @param endpointId required endpoint identifier
     * @return webhook endpoint in the default account
     */
    public WebhookEndpoint getEndpoint(String endpointId) {
        return getEndpoint(endpointId, null);
    }

    /**
     * {@code PUT /accounts/{account_id}/webhooks/endpoints/{endpoint_id}} — change only the fields set on the
     * payload. Enabling signing generates a secret when the endpoint has none; disabling it discards the
     * secret.
     *
     * @param endpointId required endpoint identifier
     * @param payload fields to change; at least one must be set
     * @param accountId account override, or {@code null} for the client default
     * @return updated endpoint
     */
    public WebhookEndpoint updateEndpoint(String endpointId, WebhookEndpointPayload payload, String accountId) {
        if (payload == null || (payload.getUrl() == null && payload.getEmail() == null
                && payload.getEvents() == null && payload.getName() == null && payload.getActive() == null
                && payload.getSigningEnabled() == null)) {
            throw new ValidationException("At least one webhook endpoint attribute is required");
        }
        if (payload.getUrl() != null) requireWebhookUrl(payload.getUrl());
        if (payload.getEmail() != null) requireEmail(payload.getEmail(), "Webhook email");
        if (payload.getEvents() != null) requireEvents(payload.getEvents());
        return httpPut(endpointPath(endpointId, accountId), payload, WebhookEndpoint.class);
    }

    /**
     * {@code PUT /accounts/{account_id}/webhooks/endpoints/{endpoint_id}}.
     *
     * @param endpointId required endpoint identifier
     * @param payload fields to change; at least one must be set
     * @return updated endpoint in the default account
     */
    public WebhookEndpoint updateEndpoint(String endpointId, WebhookEndpointPayload payload) {
        return updateEndpoint(endpointId, payload, null);
    }

    /**
     * {@code DELETE /accounts/{account_id}/webhooks/endpoints/{endpoint_id}} — stop delivering to an endpoint
     * and free its slot.
     *
     * @param endpointId required endpoint identifier
     * @param accountId account override, or {@code null} for the client default
     */
    public void deleteEndpoint(String endpointId, String accountId) {
        httpDelete(endpointPath(endpointId, accountId));
    }

    /**
     * {@code DELETE /accounts/{account_id}/webhooks/endpoints/{endpoint_id}}.
     *
     * @param endpointId required endpoint identifier
     */
    public void deleteEndpoint(String endpointId) {
        deleteEndpoint(endpointId, null);
    }

    /**
     * {@code GET /accounts/{account_id}/webhooks/endpoints/{endpoint_id}/secret} — the Standard Webhooks
     * secret ({@code whsec_} followed by the base64 key) that signs deliveries to this endpoint. Pass it to
     * {@link com.assinafy.sdk.WebhookVerifier}. Fails with HTTP 400 when signing is disabled. Requires an API
     * key or user session; OAuth applications cannot read it.
     *
     * @param endpointId required endpoint identifier
     * @param accountId account override, or {@code null} for the client default
     * @return signing secret
     */
    public String getEndpointSecret(String endpointId, String accountId) {
        return secret(httpGet(endpointPath(endpointId, accountId) + "/secret", JsonNode.class));
    }

    /**
     * {@code GET /accounts/{account_id}/webhooks/endpoints/{endpoint_id}/secret}.
     *
     * @param endpointId required endpoint identifier
     * @return signing secret of an endpoint in the default account
     */
    public String getEndpointSecret(String endpointId) {
        return getEndpointSecret(endpointId, null);
    }

    /**
     * {@code POST /accounts/{account_id}/webhooks/endpoints/{endpoint_id}/secret/rotate} — replace the
     * signing secret. The old secret stops working immediately: deliveries sent afterwards are signed only
     * with the returned one. Fails with HTTP 400 when signing is disabled. Requires an API key or user
     * session; OAuth applications cannot rotate it.
     *
     * @param endpointId required endpoint identifier
     * @param accountId account override, or {@code null} for the client default
     * @return new signing secret
     */
    public String rotateEndpointSecret(String endpointId, String accountId) {
        return secret(httpPost(endpointPath(endpointId, accountId) + "/secret/rotate", null, JsonNode.class));
    }

    /**
     * {@code POST /accounts/{account_id}/webhooks/endpoints/{endpoint_id}/secret/rotate}.
     *
     * @param endpointId required endpoint identifier
     * @return new signing secret of an endpoint in the default account
     */
    public String rotateEndpointSecret(String endpointId) {
        return rotateEndpointSecret(endpointId, null);
    }

    /**
     * {@code PUT /accounts/{account_id}/webhooks/subscriptions} — create or replace the account's oldest
     * webhook endpoint. The body carries {@code url}, {@code email}, {@code events}, and {@code is_active}
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
        requireWebhookUrl(payload.getUrl());
        requireEmail(payload.getEmail(), "Webhook email");
        requireEvents(payload.getEvents());

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
     * {@code PUT /accounts/{account_id}/webhooks/subscriptions}.
     * Returns created or replaced subscription in the default account.
     *
     * @param payload required subscription values
     * @return created or replaced subscription in the default account
     */
    public WebhookSubscription register(RegisterWebhookPayload payload) {
        return register(payload, null);
    }

    /**
     * {@code PUT /accounts/{account_id}/webhooks/subscriptions}.
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
     * {@code PUT /accounts/{accountId}/webhooks/subscriptions}.
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
     * {@code GET /accounts/{account_id}/webhooks/subscriptions}.
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
     * {@code PUT /accounts/{account_id}/webhooks/inactivate}.
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
     * ({@code page}, {@code per-page}, {@code endpoint_id}, {@code event}, {@code delivered}, {@code from},
     * {@code to}).
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
     * {@code GET /accounts/{account_id}/webhooks}.
     * Returns default account's webhook-dispatch page.
     *
     * @param params optional typed filters and pagination values
     * @return default account's webhook-dispatch page
     */
    public PaginatedResult<WebhookDispatch> listDispatches(ListDispatchesParams params) {
        return listDispatches(params, null);
    }

    /**
     * {@code GET /accounts/{account_id}/webhooks}.
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
     * {@code POST /accounts/{account_id}/webhooks/{dispatch_id}/retry}.
     * Returns updated dispatch record in the default account.
     *
     * @param dispatchId required dispatch identifier
     * @return updated dispatch record in the default account
     */
    public WebhookDispatch retryDispatch(String dispatchId) {
        return retryDispatch(dispatchId, null);
    }

    private String endpointPath(String endpointId, String accountId) {
        String id = accountId(accountId);
        return "/accounts/" + id + "/webhooks/endpoints/" + requireId(endpointId, "Webhook endpoint ID");
    }

    private static String secret(JsonNode data) {
        String secret = data == null ? null : data.path("secret").asText(null);
        if (secret == null || secret.isBlank()) {
            throw new ValidationException("The API returned no webhook signing secret");
        }
        return secret;
    }

    private static void requireWebhookUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new ValidationException("Webhook URL is required");
        }
        try {
            URI uri = new URI(url);
            if (!uri.isAbsolute() || uri.getHost() == null || (!"http".equalsIgnoreCase(uri.getScheme())
                    && !"https".equalsIgnoreCase(uri.getScheme()))) {
                throw new ValidationException("Webhook URL must be an absolute HTTP or HTTPS URL");
            }
        } catch (URISyntaxException e) {
            throw new ValidationException("Webhook URL must be a valid URI");
        }
    }

    private static void requireEvents(List<String> events) {
        if (events == null || events.isEmpty()
                || events.stream().anyMatch(event -> event == null || event.isBlank())) {
            throw new ValidationException("At least one webhook event is required");
        }
    }

    private Map<String, String> buildDispatchQueryParams(ListDispatchesParams params) {
        Map<String, String> result = new HashMap<>();
        if (params == null) return result;
        if (params.getPage() != null) result.put("page", String.valueOf(params.getPage()));
        if (params.getPerPage() != null) result.put("per-page", String.valueOf(params.getPerPage()));
        if (params.getEndpointId() != null && !params.getEndpointId().isBlank()) {
            result.put("endpoint_id", params.getEndpointId());
        }
        if (params.getEvent() != null && !params.getEvent().isBlank()) result.put("event", params.getEvent());
        if (params.getDelivered() != null) result.put("delivered", String.valueOf(params.getDelivered()));
        if (params.getFrom() != null) result.put("from", String.valueOf(params.getFrom()));
        if (params.getTo() != null) result.put("to", String.valueOf(params.getTo()));
        return result;
    }
}
