package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Per-field result returned by multi-field validation. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class FieldValidationResult {

    /** Creates an empty response model for deserialization. */
    public FieldValidationResult() {}

    @JsonProperty("field_id")
    private String fieldId;

    private String type;
    private Boolean success;

    @JsonProperty("error_message")
    private String errorMessage;

    /**
     * Returns the validated {@code field_id}.
     *
     * @return the validated {@code field_id}
     */
    public String getFieldId() { return fieldId; }

    /**
     * Sets value of {@code field_id}.
     *
     * @param fieldId value of {@code field_id}
     */
    public void setFieldId(String fieldId) { this.fieldId = fieldId; }

    /**
     * Returns the field's validation type.
     *
     * @return the field's validation type
     */
    public String getType() { return type; }

    /**
     * Sets field validation type.
     *
     * @param type field validation type
     */
    public void setType(String type) { this.type = type; }

    /**
     * Returns whether the value passed validation.
     *
     * @return whether the value passed validation
     */
    public Boolean getSuccess() { return success; }

    /**
     * Sets validation outcome.
     *
     * @param success validation outcome
     */
    public void setSuccess(Boolean success) { this.success = success; }

    /**
     * Returns validation error text, normally empty when successful.
     *
     * @return validation error text, normally empty when successful
     */
    public String getErrorMessage() { return errorMessage; }

    /**
     * Sets value of {@code error_message}.
     *
     * @param errorMessage value of {@code error_message}
     */
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
