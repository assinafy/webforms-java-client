package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Rotated signing secret and optional grace period.
 *
 * @param secret new Standard Webhooks secret
 * @param previousSecretExpiresAt old-secret expiration, or null without overlap
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookSecretRotation(
        @JsonProperty("secret") String secret,
        @JsonProperty("previous_secret_expires_at") String previousSecretExpiresAt) {
    /**
     * Returns a description without bearer credentials.
     * @return redacted description
     */
    @Override
    public String toString() { return "WebhookSecretRotation[redacted]"; }
}
