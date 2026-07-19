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

    public FieldValidationPayload(String fieldId, Object value) {
        this.fieldId = fieldId;
        this.value = value;
    }

    public String getFieldId() { return fieldId; }
    public Object getValue() { return value; }
}
