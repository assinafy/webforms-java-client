package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/** One recorded attempt to deliver an outbound webhook event. */
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

    /** Creates an empty webhook-dispatch model for JSON deserialization. */
    public WebhookDispatch() {}

    /**
     * Returns dispatch identifier.
     *
     * @return dispatch identifier
     */
    public String getId() { return id; }
    /**
     * Sets dispatch identifier.
     *
     * @param id dispatch identifier
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns resource discriminator.
     *
     * @return resource discriminator
     */
    public String getResource() { return resource; }
    /**
     * Sets resource discriminator.
     *
     * @param resource resource discriminator
     */
    public void setResource(String resource) { this.resource = resource; }

    /**
     * Returns event code.
     *
     * @return event code
     */
    public String getEvent() { return event; }
    /**
     * Sets event code.
     *
     * @param event event code
     */
    public void setEvent(String event) { this.event = event; }

    /**
     * Returns wire {@code activity_id}.
     *
     * @return wire {@code activity_id}
     */
    public Long getActivityId() { return activityId; }
    /**
     * Sets wire {@code activity_id}.
     *
     * @param activityId wire {@code activity_id}
     */
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    /**
     * Returns delivery endpoint.
     *
     * @return delivery endpoint
     */
    public String getEndpoint() { return endpoint; }
    /**
     * Sets delivery endpoint.
     *
     * @param endpoint delivery endpoint
     */
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    /**
     * Returns JSON webhook payload.
     *
     * @return JSON webhook payload
     */
    public Map<String, Object> getPayload() { return payload; }
    /**
     * Sets JSON webhook payload.
     *
     * @param payload JSON webhook payload
     */
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    /**
     * Returns whether delivery succeeded.
     *
     * @return whether delivery succeeded
     */
    public boolean isDelivered() { return delivered; }
    /**
     * Sets whether delivery succeeded.
     *
     * @param delivered whether delivery succeeded
     */
    public void setDelivered(boolean delivered) { this.delivered = delivered; }

    /**
     * Returns wire {@code http_status}, or {@code null} when no response was received.
     *
     * @return wire {@code http_status}, or {@code null} when no response was received
     */
    public Integer getHttpStatus() { return httpStatus; }
    /**
     * Sets wire {@code http_status}.
     *
     * @param httpStatus wire {@code http_status}
     */
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    /**
     * Returns wire {@code response_body}, or {@code null}.
     *
     * @return wire {@code response_body}, or {@code null}
     */
    public String getResponseBody() { return responseBody; }
    /**
     * Sets wire {@code response_body}.
     *
     * @param responseBody wire {@code response_body}
     */
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    /**
     * Returns delivery error, or {@code null}.
     *
     * @return delivery error, or {@code null}
     */
    public String getError() { return error; }
    /**
     * Sets delivery error.
     *
     * @param error delivery error
     */
    public void setError(String error) { this.error = error; }

    /**
     * Returns wire {@code created_at} timestamp.
     *
     * @return wire {@code created_at} timestamp
     */
    public String getCreatedAt() { return createdAt; }
    /**
     * Sets wire {@code created_at} timestamp.
     *
     * @param createdAt wire {@code created_at} timestamp
     */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

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
