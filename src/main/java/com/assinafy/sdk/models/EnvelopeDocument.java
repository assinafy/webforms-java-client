package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * One document in an envelope.
 *
 * @param id document identifier
 * @param position one-based order
 * @param name document name
 * @param status document status
 * @param artifacts available artifact URLs
 * @param signers signer progress
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EnvelopeDocument(
        @JsonProperty("id") String id,
        @JsonProperty("position") Integer position,
        @JsonProperty("name") String name,
        @JsonProperty("status") String status,
        @JsonProperty("artifacts") Map<String, String> artifacts,
        @JsonProperty("signers") List<EnvelopeSigner> signers) {}
