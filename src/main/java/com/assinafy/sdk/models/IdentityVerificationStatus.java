package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Signer-facing biometric verification state.
 *
 * @param canStart true before a session exists or after it expires before submission
 * @param status provider state: Pending, InReview, Approved, Declined, Expired, Abandoned, or null
 * @param url capture bearer URL while pending; preserve its fragment and keep it out of logs and storage
 * @param expiresAt capture-link expiration in ISO-8601, or null
 * @param completedAt completion time in ISO-8601, or null
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record IdentityVerificationStatus(
        @JsonProperty("can_start") boolean canStart,
        String status,
        String url,
        @JsonProperty("expires_at") String expiresAt,
        @JsonProperty("completed_at") String completedAt) {

    /**
     * Describes the state without exposing its capture bearer URL.
     * @return state description with the URL redacted
     */
    @Override
    public String toString() {
        return "IdentityVerificationStatus[canStart=" + canStart + ", status=" + status + ", url=<redacted>"
                + ", expiresAt=" + expiresAt + ", completedAt=" + completedAt + "]";
    }
}
