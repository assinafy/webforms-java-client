package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Supported outbound webhook event code and its human-readable description. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class WebhookEventTypeInfo {

    private String id;
    private String description;

    /** Creates an empty event-type model for JSON deserialization. */
    public WebhookEventTypeInfo() {}

    /**
     * Returns event code.
     *
     * @return event code
     */
    public String getId() { return id; }
    /**
     * Sets event code.
     *
     * @param id event code
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns event description.
     *
     * @return event description
     */
    public String getDescription() { return description; }
    /**
     * Sets event description.
     *
     * @param description event description
     */
    public void setDescription(String description) { this.description = description; }
}
