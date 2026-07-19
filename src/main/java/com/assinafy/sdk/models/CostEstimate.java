package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Cost breakdown for an assignment (or a document-from-template) plus the account's current balances.
 *
 * <p>Returned by {@code POST /documents/{id}/assignments/estimate-cost} and
 * {@code POST /accounts/{id}/templates/{id}/documents/estimate-cost}. The two decision fields to inspect
 * before creating an assignment are {@link #getHasSufficientResources()} and {@link #getBlockingReason()}:
 * when resources are insufficient, {@code blocking_reason} is one of {@code PendingPayment},
 * {@code InsufficientDocuments}, or {@code InsufficientCredits}.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CostEstimate {

    /** Number of documents consumed (always {@code 1}). */
    private Integer documents;

    /** Total notification credits required for the assignment. */
    private Double credits;

    @JsonProperty("needs_extra_document")
    private Boolean needsExtraDocument;

    @JsonProperty("extra_document_cost")
    private Double extraDocumentCost;

    @JsonProperty("total_credits")
    private Double totalCredits;

    private List<CostEstimateBreakdownItem> breakdown;

    @JsonProperty("document_balance")
    private Double documentBalance;

    @JsonProperty("credit_balance")
    private Double creditBalance;

    @JsonProperty("has_sufficient_resources")
    private Boolean hasSufficientResources;

    @JsonProperty("blocking_reason")
    private String blockingReason;

    private String message;

    public Integer getDocuments() { return documents; }
    public void setDocuments(Integer documents) { this.documents = documents; }

    public Double getCredits() { return credits; }
    public void setCredits(Double credits) { this.credits = credits; }

    /** {@code true} when the plan's document allowance is exhausted and an extra document will be charged. */
    public Boolean getNeedsExtraDocument() { return needsExtraDocument; }
    public void setNeedsExtraDocument(Boolean needsExtraDocument) { this.needsExtraDocument = needsExtraDocument; }

    /** Credits charged for the extra document when {@link #getNeedsExtraDocument()} is {@code true}. */
    public Double getExtraDocumentCost() { return extraDocumentCost; }
    public void setExtraDocumentCost(Double extraDocumentCost) { this.extraDocumentCost = extraDocumentCost; }

    public Double getTotalCredits() { return totalCredits; }
    public void setTotalCredits(Double totalCredits) { this.totalCredits = totalCredits; }

    /** Per-item cost breakdown; may be empty when nothing is billable. */
    public List<CostEstimateBreakdownItem> getBreakdown() { return breakdown; }
    public void setBreakdown(List<CostEstimateBreakdownItem> breakdown) { this.breakdown = breakdown; }

    public Double getDocumentBalance() { return documentBalance; }
    public void setDocumentBalance(Double documentBalance) { this.documentBalance = documentBalance; }

    public Double getCreditBalance() { return creditBalance; }
    public void setCreditBalance(Double creditBalance) { this.creditBalance = creditBalance; }

    public Boolean getHasSufficientResources() { return hasSufficientResources; }
    public void setHasSufficientResources(Boolean hasSufficientResources) {
        this.hasSufficientResources = hasSufficientResources;
    }

    /**
     * Why the assignment would be blocked when resources are insufficient — one of {@code PendingPayment},
     * {@code InsufficientDocuments}, {@code InsufficientCredits}; {@code null} when resources are sufficient.
     */
    public String getBlockingReason() { return blockingReason; }
    public void setBlockingReason(String blockingReason) { this.blockingReason = blockingReason; }

    /** Human-readable explanation accompanying a blocking reason; {@code null} when unblocked. */
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
