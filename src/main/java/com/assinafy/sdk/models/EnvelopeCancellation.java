package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Cancellation outcome for one document.
 *
 * @param id document identifier
 * @param status resulting status
 * @param cancelled whether this call cancelled it
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EnvelopeCancellation(
        @JsonProperty("id") String id,
        @JsonProperty("status") String status,
        @JsonProperty("cancelled") boolean cancelled) {}
