package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Documents sent together; each retains its own status and certificate.
 *
 * @param resource resource discriminator
 * @param id envelope identifier
 * @param name invitation name
 * @param message signer message or null
 * @param documents documents in order
 * @param counts counts by status, null when empty
 * @param signingUrls one link per pending signer
 * @param createdAt creation timestamp
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DocumentEnvelope(
        @JsonProperty("resource") String resource,
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("message") String message,
        @JsonProperty("documents") List<EnvelopeDocument> documents,
        @JsonProperty("counts") Map<String, Integer> counts,
        @JsonProperty("signing_urls") List<SigningUrl> signingUrls,
        @JsonProperty("created_at") String createdAt) {}
