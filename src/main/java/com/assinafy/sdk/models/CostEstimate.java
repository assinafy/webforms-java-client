package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Cost breakdown for an assignment, resend, or document-from-template operation plus current balances.
 *
 * <p>Returned by {@code POST /documents/{id}/assignments/estimate-cost} and
 * {@code POST /accounts/{id}/templates/{id}/documents/estimate-cost}. Resend estimates are exposed through
 * {@link ResendCostEstimate}. The two decision fields to inspect
 * before creating an assignment are {@link #getHasSufficientResources()} and {@link #getBlockingReason()}:
 * when resources are insufficient, {@code blocking_reason} is one of {@code PendingPayment},
 * {@code InsufficientDocuments}, or {@code InsufficientCredits}.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CostEstimate {

    /** Creates an empty response model for deserialization. */
    public CostEstimate() {}

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

    /**
     * Returns documents consumed, normally {@code 1}.
     *
     * @return documents consumed, normally {@code 1}
     */
    public Integer getDocuments() { return documents; }

    /**
     * Sets number of documents consumed.
     *
     * @param documents number of documents consumed
     */
    public void setDocuments(Integer documents) { this.documents = documents; }

    /**
     * Returns notification credits required.
     *
     * @return notification credits required
     */
    public Double getCredits() { return credits; }

    /**
     * Sets notification credits required.
     *
     * @param credits notification credits required
     */
    public void setCredits(Double credits) { this.credits = credits; }

    /**
     * Returns whether the plan allowance is exhausted and an extra document will be charged.
     *
     * @return whether the plan allowance is exhausted and an extra document will be charged
     */
    public Boolean getNeedsExtraDocument() { return needsExtraDocument; }

    /**
     * Sets value of {@code needs_extra_document}.
     *
     * @param needsExtraDocument value of {@code needs_extra_document}
     */
    public void setNeedsExtraDocument(Boolean needsExtraDocument) { this.needsExtraDocument = needsExtraDocument; }

    /**
     * Returns credits charged when {@link #getNeedsExtraDocument()} is {@code true}.
     *
     * @return credits charged when {@link #getNeedsExtraDocument()} is {@code true}
     */
    public Double getExtraDocumentCost() { return extraDocumentCost; }

    /**
     * Sets value of {@code extra_document_cost}.
     *
     * @param extraDocumentCost value of {@code extra_document_cost}
     */
    public void setExtraDocumentCost(Double extraDocumentCost) { this.extraDocumentCost = extraDocumentCost; }

    /**
     * Returns total credits required, including any extra-document charge.
     *
     * @return total credits required, including any extra-document charge
     */
    public Double getTotalCredits() { return totalCredits; }

    /**
     * Sets value of {@code total_credits}.
     *
     * @param totalCredits value of {@code total_credits}
     */
    public void setTotalCredits(Double totalCredits) { this.totalCredits = totalCredits; }

    /**
     * Returns per-item cost breakdown, possibly empty when nothing is billable.
     *
     * @return per-item cost breakdown, possibly empty when nothing is billable
     */
    public List<CostEstimateBreakdownItem> getBreakdown() { return breakdown; }

    /**
     * Sets per-item cost breakdown.
     *
     * @param breakdown per-item cost breakdown
     */
    public void setBreakdown(List<CostEstimateBreakdownItem> breakdown) { this.breakdown = breakdown; }

    /**
     * Returns current document allowance balance.
     *
     * @return current document allowance balance
     */
    public Double getDocumentBalance() { return documentBalance; }

    /**
     * Sets value of {@code document_balance}.
     *
     * @param documentBalance value of {@code document_balance}
     */
    public void setDocumentBalance(Double documentBalance) { this.documentBalance = documentBalance; }

    /**
     * Returns current notification-credit balance.
     *
     * @return current notification-credit balance
     */
    public Double getCreditBalance() { return creditBalance; }

    /**
     * Sets value of {@code credit_balance}.
     *
     * @param creditBalance value of {@code credit_balance}
     */
    public void setCreditBalance(Double creditBalance) { this.creditBalance = creditBalance; }

    /**
     * Returns whether the account can afford the requested operation.
     *
     * @return whether the account can afford the requested operation
     */
    public Boolean getHasSufficientResources() { return hasSufficientResources; }

    /**
     * Sets value of {@code has_sufficient_resources}.
     *
     * @param hasSufficientResources value of {@code has_sufficient_resources}
     */
    public void setHasSufficientResources(Boolean hasSufficientResources) {
        this.hasSufficientResources = hasSufficientResources;
    }

    /**
     * Why the assignment would be blocked when resources are insufficient — one of {@code PendingPayment},
     * {@code InsufficientDocuments}, {@code InsufficientCredits}; {@code null} when resources are sufficient.
     *
     * @return the blocking reason, or {@code null}
     */
    public String getBlockingReason() { return blockingReason; }

    /**
     * Sets value of {@code blocking_reason}.
     *
     * @param blockingReason value of {@code blocking_reason}
     */
    public void setBlockingReason(String blockingReason) { this.blockingReason = blockingReason; }

    /**
     * Returns human-readable explanation, or {@code null} when unblocked.
     *
     * @return human-readable explanation, or {@code null} when unblocked
     */
    public String getMessage() { return message; }

    /**
     * Sets human-readable explanation.
     *
     * @param message human-readable explanation
     */
    public void setMessage(String message) { this.message = message; }
}
