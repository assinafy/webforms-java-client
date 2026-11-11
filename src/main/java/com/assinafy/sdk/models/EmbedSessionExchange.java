package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Hosted embed-page credentials; integrations normally load the session URL instead.
 *
 * @param signerAccessCode signer bearer credential
 * @param signerId signer identifier
 * @param documentId document identifier
 * @param verificationMethod identity verification method
 * @param embedOrigins allowed parent origins
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EmbedSessionExchange(
        @JsonProperty("signer_access_code") String signerAccessCode,
        @JsonProperty("signer_id") String signerId,
        @JsonProperty("document_id") String documentId,
        @JsonProperty("verification_method") String verificationMethod,
        @JsonProperty("embed_origins") List<String> embedOrigins) {
    /**
     * Returns a description without bearer credentials.
     * @return redacted description
     */
    @Override
    public String toString() { return "EmbedSessionExchange[redacted]"; }
}
