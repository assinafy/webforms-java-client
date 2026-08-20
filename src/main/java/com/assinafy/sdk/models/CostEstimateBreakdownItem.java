package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single line item in a {@link CostEstimate} or {@link ResendCostEstimate} breakdown — one billable
 * notification/action and its cost.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CostEstimateBreakdownItem {

    /** Creates an empty response model for deserialization. */
    public CostEstimateBreakdownItem() {}

    private String code;
    private String name;
    private Double cost;
    private Integer quantity;

    @JsonProperty("unit_cost")
    private Double unitCost;

    /**
     * Returns machine-readable billable-item code, such as {@code NotificationWhatsapp}.
     *
     * @return machine-readable billable-item code, such as {@code NotificationWhatsapp}
     */
    public String getCode() { return code; }

    /**
     * Sets machine-readable billable-item code.
     *
     * @param code machine-readable billable-item code
     */
    public void setCode(String code) { this.code = code; }

    /**
     * Returns human-readable line-item name.
     *
     * @return human-readable line-item name
     */
    public String getName() { return name; }

    /**
     * Sets human-readable line-item name.
     *
     * @param name human-readable line-item name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns total line-item cost in credits.
     *
     * @return total line-item cost in credits
     */
    public Double getCost() { return cost; }

    /**
     * Sets total line-item cost in credits.
     *
     * @param cost total line-item cost in credits
     */
    public void setCost(Double cost) { this.cost = cost; }

    /**
     * Returns units billed, or {@code null} when the endpoint does not break them out.
     *
     * @return units billed, or {@code null} when the endpoint does not break them out
     */
    public Integer getQuantity() { return quantity; }

    /**
     * Sets units billed.
     *
     * @param quantity units billed
     */
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    /**
     * Returns per-unit cost in credits, or {@code null} when unavailable.
     *
     * @return per-unit cost in credits, or {@code null} when unavailable
     */
    public Double getUnitCost() { return unitCost; }

    /**
     * Sets value of {@code unit_cost}.
     *
     * @param unitCost value of {@code unit_cost}
     */
    public void setUnitCost(Double unitCost) { this.unitCost = unitCost; }
}
