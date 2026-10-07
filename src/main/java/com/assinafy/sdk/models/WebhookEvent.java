package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Body of a webhook delivery. Timestamps in the body, including the {@code *_at} fields inside
 * {@code subject} and {@code object}, are Unix seconds rather than the ISO-8601 strings REST responses use.
 *
 * @param id identifier of the activity that produced the event
 * @param event event type, as listed by {@code GET /webhooks/event-types}
 * @param message reserved; currently always {@code null}
 * @param payload event-specific parameters whose keys vary per event, or {@code null}
 * @param origin {@code ip} and {@code user-agent} of the triggering request, or {@code null}
 * @param createdAt wire {@code created_at}: when the event was recorded, in Unix seconds
 * @param subject who performed the action: a {@code User}, {@code Signer}, or {@code Account}, named by its
 *        {@code type} property
 * @param object what the action was performed on: a {@code Document}, {@code Signer}, or {@code Template}
 *        with its relations expanded, named by its {@code type} property
 * @param accountId wire {@code account_id}: the account that owns the event
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookEvent(
        long id,
        String event,
        String message,
        Map<String, Object> payload,
        Map<String, Object> origin,
        @JsonProperty("created_at") long createdAt,
        Map<String, Object> subject,
        Map<String, Object> object,
        @JsonProperty("account_id") String accountId) {}
