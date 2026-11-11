package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Allowance for the current billing cycle.
 *
 * @param balance remaining quantity, null for unlimited
 * @param limit allowance, null when unlimited
 * @param spent quantity consumed
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Balance(
        @JsonProperty("balance") BigDecimal balance,
        @JsonProperty("limit") BigDecimal limit,
        @JsonProperty("spent") BigDecimal spent) {}
