package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Request body for creating or replacing a workspace's single webhook subscription. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class RegisterWebhookPayload {

    private final String url;
    private final String email;
    private List<String> events;

    @JsonProperty("is_active")
    private Boolean active;

    /**
     * Creates an instance.
     *
     * @param url required HTTPS endpoint that receives webhook POST requests
     * @param email required contact email for delivery notices
     */
    public RegisterWebhookPayload(String url, String email) {
        this.url = url;
        this.email = email;
    }

    /**
     * Returns required wire {@code url}.
     *
     * @return required wire {@code url}
     */
    public String getUrl() { return url; }
    /**
     * Returns required wire {@code email}.
     *
     * @return required wire {@code email}
     */
    public String getEmail() { return email; }

    /**
     * Returns required wire {@code events} values, or {@code null} before configuration.
     *
     * @return required wire {@code events} values, or {@code null} before configuration
     */
    public List<String> getEvents() { return events; }
    /**
     * Sets one or more codes returned by {@code GET /webhooks/event-types}.
     *
     * @param events one or more codes returned by {@code GET /webhooks/event-types}
     * @return this payload
     */
    public RegisterWebhookPayload setEvents(List<String> events) { this.events = events; return this; }

    /**
     * Returns optional wire {@code is_active}; {@code null} defaults to {@code true} in the resource.
     *
     * @return optional wire {@code is_active}; {@code null} defaults to {@code true} in the resource
     */
    public Boolean getActive() { return active; }
    /**
     * Sets wire {@code is_active}; {@code null} uses the resource default of {@code true}.
     *
     * @param active wire {@code is_active}; {@code null} uses the resource default of {@code true}
     * @return this payload
     */
    public RegisterWebhookPayload setActive(Boolean active) { this.active = active; return this; }
}
