package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class RegisterWebhookPayload {

    private final String url;
    private final String email;
    private List<String> events;

    @JsonProperty("is_active")
    private Boolean active;

    public RegisterWebhookPayload(String url, String email) {
        this.url = url;
        this.email = email;
    }

    public String getUrl() { return url; }
    public String getEmail() { return email; }

    public List<String> getEvents() { return events; }
    public RegisterWebhookPayload setEvents(List<String> events) { this.events = events; return this; }

    public Boolean getActive() { return active; }
    public RegisterWebhookPayload setActive(Boolean active) { this.active = active; return this; }
}
