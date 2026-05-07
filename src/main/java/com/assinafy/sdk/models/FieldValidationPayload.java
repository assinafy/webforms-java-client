package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
