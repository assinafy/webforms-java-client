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
        @JsonProperty("updated_at") String updatedAt) {}
