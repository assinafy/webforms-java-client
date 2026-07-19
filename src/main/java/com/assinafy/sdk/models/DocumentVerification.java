package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Verification result for a document looked up by its signature hash, returned by
 * {@code GET /documents/{signature_hash}/verify} (a public, unauthenticated endpoint).
 *
 * <p>When the hash does not resolve to a signed document, {@link #getIsValid()} is {@code false}, most fields
 * are {@code null}, and {@link #getMessage()} explains why. Note the API returns {@code page_count} and
 * {@code signer_count} as strings.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentVerification {

    private String hash;
    private String id;
    private String status;

    @JsonProperty("page_count")
    private String pageCount;

    @JsonProperty("signer_count")
    private String signerCount;

    @JsonProperty("completed_count")
    private Integer completedCount;

    @JsonProperty("completed_at")
    private String completedAt;

    @JsonProperty("verified_at")
    private String verifiedAt;

    @JsonProperty("is_valid")
    private boolean valid;

    private String message;

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /** Page count as a string (the API returns e.g. {@code "1"}); {@code null} when not verified. */
    public String getPageCount() { return pageCount; }
    public void setPageCount(String pageCount) { this.pageCount = pageCount; }

    /** Signer count as a string (the API returns e.g. {@code "1"}); {@code null} when not verified. */
    public String getSignerCount() { return signerCount; }
    public void setSignerCount(String signerCount) { this.signerCount = signerCount; }

    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    public String getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(String verifiedAt) { this.verifiedAt = verifiedAt; }

    /** {@code true} when the hash resolves to a valid, signed document. */
    public boolean getIsValid() { return valid; }
    public void setIsValid(boolean valid) { this.valid = valid; }

    /** Reason the document is not valid; empty/absent when it is valid. */
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
