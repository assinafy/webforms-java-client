package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Cost estimate for resending a signature request to a single signer, returned by
 * {@code POST /documents/{id}/assignments/{id}/signers/{id}/estimate-resend-cost}.
 *
 * <p>Note: the live API returns this compact shape ({@code total}, {@code breakdown}, {@code credit_balance},
 * {@code has_sufficient_credits}) rather than the full {@link CostEstimate} the OpenAPI spec references for this
 * endpoint — the SDK follows the live behavior. The breakdown items here populate only
 * {@code code}/{@code name}/{@code cost} (no {@code quantity}/{@code unit_cost}).</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ResendCostEstimate {

    /** Total credits required to resend. */
    private Double total;

    private List<CostEstimateBreakdownItem> breakdown;

    @JsonProperty("credit_balance")
    private Double creditBalance;

    @JsonProperty("has_sufficient_credits")
    private Boolean hasSufficientCredits;

    /** Creates an empty resend estimate model for JSON deserialization. */
    public ResendCostEstimate() {}

    /**
     * Returns total credits required, or {@code null} when omitted.
     *
     * @return total credits required, or {@code null} when omitted
     */
    public Double getTotal() { return total; }
    /**
     * Sets total credits required.
     *
     * @param total total credits required
     */
    public void setTotal(Double total) { this.total = total; }

    /**
     * Returns compact resend-cost breakdown entries.
     *
     * @return compact resend-cost breakdown entries
     */
    public List<CostEstimateBreakdownItem> getBreakdown() { return breakdown; }
    /**
     * Sets compact resend-cost breakdown entries.
     *
     * @param breakdown compact resend-cost breakdown entries
     */
    public void setBreakdown(List<CostEstimateBreakdownItem> breakdown) { this.breakdown = breakdown; }

    /**
     * Returns wire {@code credit_balance}, or {@code null} when omitted.
     *
     * @return wire {@code credit_balance}, or {@code null} when omitted
     */
    public Double getCreditBalance() { return creditBalance; }
    /**
     * Sets wire {@code credit_balance}.
     *
     * @param creditBalance wire {@code credit_balance}
     */
    public void setCreditBalance(Double creditBalance) { this.creditBalance = creditBalance; }

    /**
     * Returns wire {@code has_sufficient_credits}, or {@code null} when omitted.
     *
     * @return wire {@code has_sufficient_credits}, or {@code null} when omitted
     */
    public Boolean getHasSufficientCredits() { return hasSufficientCredits; }
    /**
     * Sets wire {@code has_sufficient_credits}.
     *
     * @param hasSufficientCredits wire {@code has_sufficient_credits}
     */
    public void setHasSufficientCredits(Boolean hasSufficientCredits) {
        this.hasSufficientCredits = hasSufficientCredits;
    }
}
