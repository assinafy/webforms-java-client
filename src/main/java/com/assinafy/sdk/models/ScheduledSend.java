package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Assignment or envelope held for later delivery by the member who scheduled it.
 * @param resource resource discriminator
 * @param id scheduled send identifier
 * @param type assignment or envelope
 * @param documentIds documents still present in the workspace
 * @param status pending, processing, sent, cancelled or failed
 * @param sendAt scheduled ISO 8601 timestamp
 * @param assignmentId created assignment, or null
 * @param envelopeId created envelope, or null
 * @param failureReason failure explanation, or null
 * @param createdAt creation timestamp
 * @param processedAt time sent, cancelled or failed, or null
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ScheduledSend(
        String resource,
        String id,
        String type,
        @JsonProperty("document_ids") List<String> documentIds,
        String status,
        @JsonProperty("send_at") String sendAt,
        @JsonProperty("assignment_id") String assignmentId,
        @JsonProperty("envelope_id") String envelopeId,
        @JsonProperty("failure_reason") String failureReason,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("processed_at") String processedAt) {}
