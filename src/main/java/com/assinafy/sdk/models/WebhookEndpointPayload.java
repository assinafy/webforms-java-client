package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request body for creating or updating a webhook endpoint. Unset fields are omitted, so an update changes
 * only the fields set here. Creating an endpoint requires {@code url}, {@code email}, and {@code events}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class WebhookEndpointPayload {

    private String url;
    private String email;
    private List<String> events;
    private String name;

    @JsonProperty("is_active")
    private Boolean active;

    @JsonProperty("signing_enabled")
    private Boolean signingEnabled;

    /** Creates an empty payload. */
    public WebhookEndpointPayload() {}

    /**
     * Returns wire {@code url}, or {@code null} when unset.
     *
     * @return wire {@code url}, or {@code null} when unset
     */
    public String getUrl() { return url; }

    /**
     * Sets the absolute HTTP or HTTPS URL that receives the events. It must differ from the URL of every other
     * endpoint in the workspace.
     *
     * @param url absolute HTTP or HTTPS URL
     * @return this payload
     */
    public WebhookEndpointPayload setUrl(String url) { this.url = url; return this; }

    /**
     * Returns wire {@code email}, or {@code null} when unset.
     *
     * @return wire {@code email}, or {@code null} when unset
     */
    public String getEmail() { return email; }

    /**
     * Sets the contact email for delivery-failure notices.
     *
     * @param email contact email
     * @return this payload
     */
    public WebhookEndpointPayload setEmail(String email) { this.email = email; return this; }

    /**
     * Returns wire {@code events}, or {@code null} when unset.
     *
     * @return wire {@code events}, or {@code null} when unset
     */
    public List<String> getEvents() { return events; }

    /**
     * Sets the event types to deliver, as returned by {@code GET /webhooks/event-types}.
     *
     * @param events one or more event-type codes
     * @return this payload
     */
    public WebhookEndpointPayload setEvents(List<String> events) { this.events = events; return this; }

    /**
     * Returns wire {@code name}, or {@code null} when unset.
     *
     * @return wire {@code name}, or {@code null} when unset
     */
    public String getName() { return name; }

    /**
     * Sets a label that tells endpoints apart.
     *
     * @param name endpoint label
     * @return this payload
     */
    public WebhookEndpointPayload setName(String name) { this.name = name; return this; }

    /**
     * Returns wire {@code is_active}, or {@code null} when unset.
     *
     * @return wire {@code is_active}, or {@code null} when unset
     */
    public Boolean getActive() { return active; }

    /**
     * Sets whether events are delivered. The API defaults a new endpoint to {@code true}.
     *
     * @param active whether events are delivered
     * @return this payload
     */
    public WebhookEndpointPayload setActive(Boolean active) { this.active = active; return this; }

    /**
     * Returns wire {@code signing_enabled}, or {@code null} when unset.
     *
     * @return wire {@code signing_enabled}, or {@code null} when unset
     */
    public Boolean getSigningEnabled() { return signingEnabled; }

    /**
     * Sets whether deliveries carry a Standard Webhooks signature. The API defaults a new endpoint to
     * {@code false}. Enabling it generates a secret when the endpoint has none and keeps the current one
     * otherwise; disabling it discards the secret.
     *
     * @param signingEnabled whether deliveries are signed
     * @return this payload
     */
    public WebhookEndpointPayload setSigningEnabled(Boolean signingEnabled) {
        this.signingEnabled = signingEnabled;
        return this;
    }
}
