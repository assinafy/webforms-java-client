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

    /** Creates an empty response model for deserialization. */
    public DocumentVerification() {}

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

    /**
     * Returns the signature hash that was verified.
     *
     * @return the signature hash that was verified
     */
    public String getHash() { return hash; }

    /**
     * Sets verified signature hash.
     *
     * @param hash verified signature hash
     */
    public void setHash(String hash) { this.hash = hash; }

    /**
     * Returns matching document identifier, or {@code null} when not verified.
     *
     * @return matching document identifier, or {@code null} when not verified
     */
    public String getId() { return id; }

    /**
     * Sets matching document identifier.
     *
     * @param id matching document identifier
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns matching document status, or {@code null} when not verified.
     *
     * @return matching document status, or {@code null} when not verified
     */
    public String getStatus() { return status; }

    /**
     * Sets matching document status.
     *
     * @param status matching document status
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Returns {@code page_count} as a string, or {@code null} when not verified.
     *
     * @return {@code page_count} as a string, or {@code null} when not verified
     */
    public String getPageCount() { return pageCount; }

    /**
     * Sets value of {@code page_count}.
     *
     * @param pageCount value of {@code page_count}
     */
    public void setPageCount(String pageCount) { this.pageCount = pageCount; }

    /**
     * Returns {@code signer_count} as a string, or {@code null} when not verified.
     *
     * @return {@code signer_count} as a string, or {@code null} when not verified
     */
    public String getSignerCount() { return signerCount; }

    /**
     * Sets value of {@code signer_count}.
     *
     * @param signerCount value of {@code signer_count}
     */
    public void setSignerCount(String signerCount) { this.signerCount = signerCount; }

    /**
     * Returns completed signer count, or {@code null} when not verified.
     *
     * @return completed signer count, or {@code null} when not verified
     */
    public Integer getCompletedCount() { return completedCount; }

    /**
     * Sets value of {@code completed_count}.
     *
     * @param completedCount value of {@code completed_count}
     */
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }

    /**
     * Returns ISO 8601 {@code completed_at} timestamp, or {@code null}.
     *
     * @return ISO 8601 {@code completed_at} timestamp, or {@code null}
     */
    public String getCompletedAt() { return completedAt; }

    /**
     * Sets value of {@code completed_at}.
     *
     * @param completedAt value of {@code completed_at}
     */
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    /**
     * Returns ISO 8601 {@code verified_at} timestamp.
     *
     * @return ISO 8601 {@code verified_at} timestamp
     */
    public String getVerifiedAt() { return verifiedAt; }

    /**
     * Sets value of {@code verified_at}.
     *
     * @param verifiedAt value of {@code verified_at}
     */
    public void setVerifiedAt(String verifiedAt) { this.verifiedAt = verifiedAt; }

    /**
     * Returns whether the hash resolves to a valid signed document.
     *
     * @return whether the hash resolves to a valid signed document
     */
    public boolean getIsValid() { return valid; }

    /**
     * Sets value of {@code is_valid}.
     *
     * @param valid value of {@code is_valid}
     */
    public void setIsValid(boolean valid) { this.valid = valid; }

    /**
     * Returns reason the document is invalid; empty or absent when valid.
     *
     * @return reason the document is invalid; empty or absent when valid
     */
    public String getMessage() { return message; }

    /**
     * Sets verification explanation.
     *
     * @param message verification explanation
     */
    public void setMessage(String message) { this.message = message; }
}
