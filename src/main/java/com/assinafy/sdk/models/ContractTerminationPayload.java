package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Optional reason and optimistic-concurrency precondition for terminating a tracked contract. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ContractTerminationPayload {

    private String reason;

    @JsonProperty("updated_at")
    private String updatedAt;

    /** Creates an empty termination payload. */
    public ContractTerminationPayload() {}

    /**
     * Returns the private termination reason recorded on the document activity.
     *
     * @return termination reason, or {@code null}
     */
    public String getReason() { return reason; }

    /**
     * Sets the optional termination reason.
     *
     * @param reason reason of at most 500 characters
     * @return this body
     */
    public ContractTerminationPayload setReason(String reason) { this.reason = reason; return this; }

    /**
     * Returns the last-read contract timestamp used as a write precondition.
     *
     * @return ISO 8601 timestamp, or {@code null}
     */
    public String getUpdatedAt() { return updatedAt; }

    /**
     * Sets the last-read contract timestamp used as a write precondition.
     *
     * @param updatedAt ISO 8601 timestamp
     * @return this body
     */
    public ContractTerminationPayload setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
}
