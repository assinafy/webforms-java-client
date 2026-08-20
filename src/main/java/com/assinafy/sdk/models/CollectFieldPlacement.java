package com.assinafy.sdk.models;

import com.assinafy.sdk.exceptions.ValidationException;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A field assigned to a signer within one collect-assignment page entry.
 *
 * @param signerId signer responsible for the value
 * @param fieldId field definition identifier
 * @param displaySettings page-image geometry and presentation metadata
 */
public record CollectFieldPlacement(
        @JsonProperty("signer_id") String signerId,
        @JsonProperty("field_id") String fieldId,
        @JsonProperty("display_settings") DisplaySettings displaySettings) {
    /**
     * Validates the identifiers and geometry required for a collect field.
     *
     * @param signerId signer identifier serialized as {@code signer_id}
     * @param fieldId field identifier serialized as {@code field_id}
     * @param displaySettings geometry serialized as {@code display_settings}
     */
    public CollectFieldPlacement {
        if (signerId == null || signerId.isBlank() || fieldId == null || fieldId.isBlank()
                || displaySettings == null) {
            throw new ValidationException("signerId, fieldId, and displaySettings are required");
        }
    }
}
