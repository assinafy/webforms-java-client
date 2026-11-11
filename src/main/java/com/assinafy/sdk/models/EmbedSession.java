package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Single-use embed URL, valid for five minutes. Never log or store it in browser storage.
 *
 * @param url bearer URL for the hosted iframe
 * @param expiresAt expiration timestamp
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EmbedSession(
        @JsonProperty("url") String url,
        @JsonProperty("expires_at") String expiresAt) {
    /**
     * Returns a description without bearer credentials.
     * @return redacted description
     */
    @Override
    public String toString() { return "EmbedSession[redacted]"; }
}
