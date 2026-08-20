package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One entry for {@code POST /accounts/{account_id}/fields/validate-multiple} — a field id and the value to
 * validate against that field's rules.
 *
 * <p>Both properties are always serialized (including {@code "value": null}); the API requires the
 * {@code value} key to be present even when {@code null}, so this type intentionally does <em>not</em> use
 * {@code @JsonInclude(NON_NULL)}.</p>
 */
public final class FieldValidationPayload {

    @JsonProperty("field_id")
    private final String fieldId;

    private final Object value;

    /**
     * Creates a field-validation entry.
     *
     * @param fieldId field definition identifier serialized as {@code field_id}
     * @param value value to validate; {@code null} is serialized explicitly
     */
    public FieldValidationPayload(String fieldId, Object value) {
        this.fieldId = fieldId;
        this.value = value;
    }

    /**
     * Returns the field definition identifier serialized as {@code field_id}.
     *
     * @return the field definition identifier serialized as {@code field_id}
     */
    public String getFieldId() { return fieldId; }

    /**
     * Returns the value to validate, which may be {@code null}.
     *
     * @return the value to validate, which may be {@code null}
     */
    public Object getValue() { return value; }
}
