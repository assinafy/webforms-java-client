package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Request body for creating a reusable account field definition. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreateFieldPayload {

    private final String type;
    private final String name;
    private String regex;

    @JsonProperty("is_required")
    private Boolean required;

    @JsonProperty("is_active")
    private Boolean active;

    /**
     * Sets field type code, such as {@code text}, {@code signature}, or {@code cpf}.
     *
     * @param type field type code, such as {@code text}, {@code signature}, or {@code cpf}
     * @param name field display name
     */
    public CreateFieldPayload(String type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Returns the required field type code.
     *
     * @return the required field type code
     */
    public String getType() { return type; }

    /**
     * Returns the required field display name.
     *
     * @return the required field display name
     */
    public String getName() { return name; }

    /**
     * Returns the optional validation regular expression.
     *
     * @return the optional validation regular expression
     */
    public String getRegex() { return regex; }

    /**
     * Sets validation regular expression, or {@code null}.
     *
     * @param regex validation regular expression, or {@code null}
     * @return this payload
     */
    public CreateFieldPayload setRegex(String regex) { this.regex = regex; return this; }

    /**
     * Returns the optional {@code is_required} value.
     *
     * @return the optional {@code is_required} value
     */
    public Boolean getRequired() { return required; }

    /**
     * Sets whether a value is required.
     *
     * @param required whether a value is required
     * @return this payload
     */
    public CreateFieldPayload setRequired(Boolean required) { this.required = required; return this; }

    /**
     * Returns the optional {@code is_active} value.
     *
     * @return the optional {@code is_active} value
     */
    public Boolean getActive() { return active; }

    /**
     * Sets whether the field is active.
     *
     * @param active whether the field is active
     * @return this payload
     */
    public CreateFieldPayload setActive(Boolean active) { this.active = active; return this; }
}
