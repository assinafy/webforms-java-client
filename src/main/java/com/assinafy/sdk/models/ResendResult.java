package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of resending a signature request to a signer, returned by
 * {@code PUT /documents/{id}/assignments/{id}/signers/{id}/resend}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ResendResult {

    @JsonProperty("is_sent")
    private Boolean sent;

    @JsonProperty("document_id")
    private String documentId;

    @JsonProperty("signer_id")
    private String signerId;

    /** Creates an empty resend result for JSON deserialization. */
    public ResendResult() {}

    /**
     * Returns wire {@code is_sent}; {@code true} when the notification was dispatched.
     *
     * @return wire {@code is_sent}; {@code true} when the notification was dispatched
     */
    public Boolean getSent() { return sent; }
    /**
     * Sets wire {@code is_sent}.
     *
     * @param sent wire {@code is_sent}
     */
    public void setSent(Boolean sent) { this.sent = sent; }

    /**
     * Returns wire {@code document_id}.
     *
     * @return wire {@code document_id}
     */
    public String getDocumentId() { return documentId; }
    /**
     * Sets wire {@code document_id}.
     *
     * @param documentId wire {@code document_id}
     */
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    /**
     * Returns wire {@code signer_id}.
     *
     * @return wire {@code signer_id}
     */
    public String getSignerId() { return signerId; }
    /**
     * Sets wire {@code signer_id}.
     *
     * @param signerId wire {@code signer_id}
     */
    public void setSignerId(String signerId) { this.signerId = signerId; }
}
