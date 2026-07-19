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

    /** {@code true} when the notification was dispatched. */
    public Boolean getSent() { return sent; }
    public void setSent(Boolean sent) { this.sent = sent; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getSignerId() { return signerId; }
    public void setSignerId(String signerId) { this.signerId = signerId; }
}
