package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Envelope reminder delivery result.
 *
 * @param isSent whether every notification method sent
 * @param envelopeId envelope identifier
 * @param signerId signer identifier
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EnvelopeResend(
        @JsonProperty("is_sent") boolean isSent,
        @JsonProperty("envelope_id") String envelopeId,
        @JsonProperty("signer_id") String signerId) {}
