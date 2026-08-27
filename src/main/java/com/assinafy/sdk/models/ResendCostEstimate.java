package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Cost estimate for resending a signature request to a single signer, returned by
 * {@code POST /documents/{id}/assignments/{id}/signers/{id}/estimate-resend-cost}.
 *
 * <p>The model accepts the complete {@link CostEstimate} fields and the compact aliases {@code total} and
 * {@code has_sufficient_credits}. Compact breakdown items may omit {@code quantity} and {@code unit_cost}.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ResendCostEstimate extends CostEstimate {

    /** Total credits required to resend. */
    private Double total;

    @JsonProperty("has_sufficient_credits")
    private Boolean hasSufficientCredits;

    /** Creates an empty resend estimate model for JSON deserialization. */
    public ResendCostEstimate() {}

    /**
     * Returns total credits required, or {@code null} when omitted.
     *
     * @return total credits required, or {@code null} when omitted
     */
    public Double getTotal() { return total != null ? total : getTotalCredits(); }
    /**
     * Sets total credits required.
     *
     * @param total total credits required
     */
    public void setTotal(Double total) {
        this.total = total;
        setTotalCredits(total);
    }

    /**
     * Returns wire {@code has_sufficient_credits}, or {@code null} when omitted.
     *
     * @return wire {@code has_sufficient_credits}, or {@code null} when omitted
     */
    public Boolean getHasSufficientCredits() {
        return hasSufficientCredits != null ? hasSufficientCredits : getHasSufficientResources();
    }
    /**
     * Sets wire {@code has_sufficient_credits}.
     *
     * @param hasSufficientCredits wire {@code has_sufficient_credits}
     */
    public void setHasSufficientCredits(Boolean hasSufficientCredits) {
        this.hasSufficientCredits = hasSufficientCredits;
        setHasSufficientResources(hasSufficientCredits);
    }
}
