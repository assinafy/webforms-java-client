package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Sender-declared signing capacity printed on the certificate.
 * @param signerId account signer identifier
 * @param signAs capacity, defaulting to Sign
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SignerCapacity(
        @JsonProperty("signer_id") String signerId,
        @JsonProperty("sign_as") String signAs) {}
