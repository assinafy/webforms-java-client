package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class WebhookDispatch {

    private String id;
    private String resource;
    private String event;

    @JsonProperty("activity_id")
    private Long activityId;

    private String endpoint;
    private Map<String, Object> payload;
    private boolean delivered;

    @JsonProperty("http_status")
    private Integer httpStatus;

    @JsonProperty("response_body")
    private String responseBody;

    private String error;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    public boolean isDelivered() { return delivered; }
    public void setDelivered(boolean delivered) { this.delivered = delivered; }

    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
