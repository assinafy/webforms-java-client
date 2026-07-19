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

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public List<CostEstimateBreakdownItem> getBreakdown() { return breakdown; }
    public void setBreakdown(List<CostEstimateBreakdownItem> breakdown) { this.breakdown = breakdown; }

    public Double getCreditBalance() { return creditBalance; }
    public void setCreditBalance(Double creditBalance) { this.creditBalance = creditBalance; }

    public Boolean getHasSufficientCredits() { return hasSufficientCredits; }
    public void setHasSufficientCredits(Boolean hasSufficientCredits) {
        this.hasSufficientCredits = hasSufficientCredits;
    }
}
