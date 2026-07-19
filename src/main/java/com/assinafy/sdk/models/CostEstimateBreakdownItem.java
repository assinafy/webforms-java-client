package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single line item in a {@link CostEstimate} or {@link ResendCostEstimate} breakdown — one billable
 * notification/action and its cost.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CostEstimateBreakdownItem {

    private String code;
    private String name;
    private Double cost;
    private Integer quantity;

    @JsonProperty("unit_cost")
    private Double unitCost;

    /** Machine-readable code for the billable item, e.g. {@code "NotificationWhatsapp"}. */
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    /** Human-readable label, e.g. {@code "Whatsapp Notification"}. */
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** Total cost (in credits) for this line item. */
    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }

    /** Number of units billed for this line item; {@code null} when the endpoint does not break it out. */
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    /** Per-unit cost (in credits); {@code null} when the endpoint does not break it out. */
    public Double getUnitCost() { return unitCost; }
    public void setUnitCost(Double unitCost) { this.unitCost = unitCost; }
}
