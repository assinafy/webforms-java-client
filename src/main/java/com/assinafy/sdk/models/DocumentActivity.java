package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentActivity {

    private long id;
    private String event;
    private String message;
    private Object payload;
    private Object origin;

    @JsonProperty("created_at")
    private String createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }

    public Object getOrigin() { return origin; }
    public void setOrigin(Object origin) { this.origin = origin; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
