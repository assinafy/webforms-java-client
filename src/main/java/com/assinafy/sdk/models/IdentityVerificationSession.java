package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Hosted biometric capture session opened with the signer's explicit consent.
 *
 * @param id verification identifier
 * @param url capture bearer URL; open it unchanged, including its fragment, without logging or storing it
 * @param expiresAt capture-link expiration in ISO-8601, or null
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record IdentityVerificationSession(
        String id,
        String url,
        @JsonProperty("expires_at") String expiresAt) {

    /**
     * Describes the session without exposing its capture bearer URL.
     * @return session description with the URL redacted
     */
    @Override
    public String toString() {
        return "IdentityVerificationSession[id=" + id + ", url=<redacted>, expiresAt=" + expiresAt + "]";
    }
}
