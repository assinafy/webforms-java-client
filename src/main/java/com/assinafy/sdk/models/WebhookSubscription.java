package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class WebhookSubscription {

    private String id;
    private String url;
    private String email;
    private List<String> events;

    @JsonProperty("is_active")
    private boolean active;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getEvents() { return events; }
    public void setEvents(List<String> events) { this.events = events; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
