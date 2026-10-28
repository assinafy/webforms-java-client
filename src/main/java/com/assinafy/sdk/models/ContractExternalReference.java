package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Identifier for a tracked contract in another system.
 *
 * @param system external system code, such as {@code hubspot}
 * @param id contract identifier in that system
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ContractExternalReference(String system, String id) {}
