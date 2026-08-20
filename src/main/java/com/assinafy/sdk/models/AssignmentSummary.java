package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Signer totals returned with an assignment. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class AssignmentSummary {

    /** Creates an empty response model for deserialization. */
    public AssignmentSummary() {}

    @JsonProperty("signer_count")
    private int signerCount;

    @JsonProperty("completed_count")
    private int completedCount;

    private List<Signer> signers;

    /**
     * Returns total {@code signer_count}.
     *
     * @return total {@code signer_count}
     */
    public int getSignerCount() { return signerCount; }

    /**
     * Sets total {@code signer_count}.
     *
     * @param signerCount total {@code signer_count}
     */
    public void setSignerCount(int signerCount) { this.signerCount = signerCount; }

    /**
     * Returns number of signers who completed signing.
     *
     * @return number of signers who completed signing
     */
    public int getCompletedCount() { return completedCount; }

    /**
     * Sets value of {@code completed_count}.
     *
     * @param completedCount value of {@code completed_count}
     */
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }

    /**
     * Returns signer summaries included by the endpoint.
     *
     * @return signer summaries included by the endpoint
     */
    public List<Signer> getSigners() { return signers; }

    /**
     * Sets signer summaries.
     *
     * @param signers signer summaries
     */
    public void setSigners(List<Signer> signers) { this.signers = signers; }
}
