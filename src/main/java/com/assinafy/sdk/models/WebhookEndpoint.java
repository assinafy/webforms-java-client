package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * One of the account's webhook endpoints. Every active endpoint subscribed to an event receives it.
 *
 * @param id endpoint identifier
 * @param name label that tells endpoints apart, or {@code null}
 * @param url URL that receives the events
 * @param email contact email for delivery-failure notices
 * @param events event types delivered to this endpoint
 * @param active wire {@code is_active}: whether events are delivered
 * @param signingEnabled wire {@code signing_enabled}: whether deliveries carry a {@code webhook-signature}
 * @param createdAt wire {@code created_at} ISO-8601 timestamp
 * @param updatedAt wire {@code updated_at} ISO-8601 timestamp
 * @param secret signing secret returned only when creating a signed endpoint; null in list/view responses
 * @param resource resource discriminator, normally {@code activity_event_subscription}
 * @param held wire {@code is_held}: whether delivery is paused after repeated failures
 * @param heldSince wire {@code held_since} Unix timestamp, or null while delivery runs
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookEndpoint(
        String id,
        String name,
        String url,
        String email,
        List<String> events,
        @JsonProperty("is_active") boolean active,
        @JsonProperty("signing_enabled") boolean signingEnabled,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("updated_at") String updatedAt,
        String secret,
        String resource,
        @JsonProperty("is_held") boolean held,
        @JsonProperty("held_since") Long heldSince) {

    /**
     * Creates an endpoint model with the optional creation-time signing secret.
     *
     * @param id endpoint identifier
     * @param name optional endpoint label
     * @param url receiver URL
     * @param email delivery-failure contact email
     * @param events subscribed event types
     * @param active whether delivery is active
     * @param signingEnabled whether delivery signatures are enabled
     * @param createdAt creation time in ISO-8601
     * @param updatedAt update time in ISO-8601
     * @param secret creation-time signing secret, or {@code null}
     */
    public WebhookEndpoint(String id, String name, String url, String email, List<String> events,
            boolean active, boolean signingEnabled, String createdAt, String updatedAt, String secret) {
        this(id, name, url, email, events, active, signingEnabled, createdAt, updatedAt, secret,
                null, false, null);
    }

    /**
     * Creates an endpoint model without a creation-time signing secret.
     *
     * @param id endpoint identifier
     * @param name optional endpoint label
     * @param url receiver URL
     * @param email delivery-failure contact email
     * @param events subscribed event types
     * @param active whether delivery is active
     * @param signingEnabled whether delivery signatures are enabled
     * @param createdAt creation time in ISO-8601
     * @param updatedAt update time in ISO-8601
     */
    public WebhookEndpoint(String id, String name, String url, String email, List<String> events,
            boolean active, boolean signingEnabled, String createdAt, String updatedAt) {
        this(id, name, url, email, events, active, signingEnabled, createdAt, updatedAt, null,
                null, false, null);
    }

    /**
     * Describes the endpoint without exposing its signing secret.
     * @return endpoint description with the secret redacted
     */
    @Override
    public String toString() {
        return "WebhookEndpoint[id=" + id + ", name=" + name + ", url=" + url + ", email=" + email
                + ", events=" + events + ", active=" + active + ", signingEnabled=" + signingEnabled
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", secret=<redacted>"
                + ", resource=" + resource + ", held=" + held + ", heldSince=" + heldSince + "]";
    }
}
