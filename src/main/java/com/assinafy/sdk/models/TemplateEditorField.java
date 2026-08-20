package com.assinafy.sdk.models;

import com.assinafy.sdk.exceptions.ValidationException;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An editor field value baked into a document created from a template.
 *
 * @param fieldId template field identifier
 * @param value value to assign
 */
public record TemplateEditorField(@JsonProperty("field_id") String fieldId, String value) {
    /**
     * Validates the two fields required by the template document endpoint.
     *
     * @param fieldId template field identifier
     * @param value value to assign
     * @throws ValidationException when the field ID is blank or the value is {@code null}
     */
    public TemplateEditorField {
        if (fieldId == null || fieldId.isBlank() || value == null) {
            throw new ValidationException("fieldId and value are required");
        }
    }
}
