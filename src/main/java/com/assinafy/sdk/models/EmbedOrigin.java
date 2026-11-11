package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Registered embedding origin.
 *
 * @param id origin identifier
 * @param origin exact scheme, host and optional port
 * @param createdAt creation timestamp
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EmbedOrigin(
        @JsonProperty("id") String id,
        @JsonProperty("origin") String origin,
        @JsonProperty("created_at") String createdAt) {}
