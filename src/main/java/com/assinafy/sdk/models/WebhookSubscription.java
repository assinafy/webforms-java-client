package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A workspace's single webhook subscription. The API models it as one subscription per account (there is no
 * per-subscription {@code id} or creation timestamp — see {@code GET/PUT .../webhooks/subscriptions}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class WebhookSubscription {

    private String url;
    private String email;
    private List<String> events;

    @JsonProperty("is_active")
    private boolean active;

    @JsonProperty("updated_at")
    private String updatedAt;

    /** Creates an empty webhook-subscription model for JSON deserialization. */
    public WebhookSubscription() {}

    /**
     * Returns HTTPS delivery URL, or {@code null}.
     *
     * @return HTTPS delivery URL, or {@code null}
     */
    public String getUrl() { return url; }
    /**
     * Sets HTTPS delivery URL.
     *
     * @param url HTTPS delivery URL
     */
    public void setUrl(String url) { this.url = url; }

    /**
     * Returns fallback notification email, or {@code null}.
     *
     * @return fallback notification email, or {@code null}
     */
    public String getEmail() { return email; }
    /**
     * Sets fallback notification email.
     *
     * @param email fallback notification email
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns subscribed event codes.
     *
     * @return subscribed event codes
     */
    public List<String> getEvents() { return events; }
    /**
     * Sets subscribed event codes.
     *
     * @param events subscribed event codes
     */
    public void setEvents(List<String> events) { this.events = events; }

    /**
     * Returns wire {@code is_active}.
     *
     * @return wire {@code is_active}
     */
    public boolean isActive() { return active; }
    /**
     * Sets wire {@code is_active}.
     *
     * @param active wire {@code is_active}
     */
    public void setActive(boolean active) { this.active = active; }

    /**
     * Returns wire {@code updated_at} timestamp.
     *
     * @return wire {@code updated_at} timestamp
     */
    public String getUpdatedAt() { return updatedAt; }
    /**
     * Sets wire {@code updated_at} timestamp.
     *
     * @param updatedAt wire {@code updated_at} timestamp
     */
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
