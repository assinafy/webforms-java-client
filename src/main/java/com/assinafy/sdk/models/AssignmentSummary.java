package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class AssignmentSummary {

    @JsonProperty("signer_count")
    private int signerCount;

    @JsonProperty("completed_count")
    private int completedCount;

    private List<Signer> signers;

    public int getSignerCount() { return signerCount; }
    public void setSignerCount(int signerCount) { this.signerCount = signerCount; }

    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }

    public List<Signer> getSigners() { return signers; }
    public void setSigners(List<Signer> signers) { this.signers = signers; }
}
