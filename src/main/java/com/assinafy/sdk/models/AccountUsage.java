package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Workspace plan and remaining allowances; unavailable to third-party OAuth apps.
 *
 * @param plan plan name or null
 * @param documentsRenewalDate next renewal date or null
 * @param balances document and credit balances
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountUsage(
        @JsonProperty("plan") String plan,
        @JsonProperty("documents_renewal_date") String documentsRenewalDate,
        @JsonProperty("balances") Map<String, Balance> balances) {}
