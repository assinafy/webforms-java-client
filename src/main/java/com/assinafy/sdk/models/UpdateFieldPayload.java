package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Partial field update; omitted properties remain unchanged. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class UpdateFieldPayload {

    private String type;
    private String name;
    private String regex;
    private boolean regexSet;

    @JsonProperty("is_required")
    private Boolean required;

    @JsonProperty("is_active")
    private Boolean active;

    /** Creates an empty partial field update. */
    public UpdateFieldPayload() {}

    /**
     * Returns field type, or {@code null} when omitted.
     *
     * @return field type, or {@code null} when omitted
     */
    public String getType() { return type; }

    /**
     * Sets optional field type.
     *
     * @param type optional field type
     * @return this payload
     */
    public UpdateFieldPayload setType(String type) { this.type = type; return this; }

    /**
     * Returns field name, or {@code null} when omitted.
     *
     * @return field name, or {@code null} when omitted
     */
    public String getName() { return name; }

    /**
     * Sets optional field name.
     *
     * @param name optional field name
     * @return this payload
     */
    public UpdateFieldPayload setName(String name) { this.name = name; return this; }

    /**
     * Returns validation regex, or {@code null}.
     *
     * @return validation regex, or {@code null}
     */
    public String getRegex() { return regex; }

    /**
     * Sets or clears the regex; {@code null} is serialized when explicitly supplied.
     *
     * @param regex optional validation regex, or {@code null} to clear it
     * @return this payload
     */
    public UpdateFieldPayload setRegex(String regex) {
        this.regex = regex;
        this.regexSet = true;
        return this;
    }

    /**
     * Explicitly clears the field's regex with {@code "regex": null}.
     *
     * @return this payload
     */
    public UpdateFieldPayload clearRegex() { return setRegex(null); }

    /**
     * Returns whether {@code regex} will be included, including an explicit {@code null}.
     *
     * @return whether {@code regex} will be included, including an explicit {@code null}
     */
    @JsonIgnore
    public boolean isRegexSet() { return regexSet; }

    /**
     * Returns wire {@code is_required}, or {@code null} when omitted.
     *
     * @return wire {@code is_required}, or {@code null} when omitted
     */
    public Boolean getRequired() { return required; }

    /**
     * Sets optional wire {@code is_required}.
     *
     * @param required optional wire {@code is_required}
     * @return this payload
     */
    public UpdateFieldPayload setRequired(Boolean required) { this.required = required; return this; }

    /**
     * Returns wire {@code is_active}, or {@code null} when omitted.
     *
     * @return wire {@code is_active}, or {@code null} when omitted
     */
    public Boolean getActive() { return active; }

    /**
     * Sets optional wire {@code is_active}.
     *
     * @param active optional wire {@code is_active}
     * @return this payload
     */
    public UpdateFieldPayload setActive(Boolean active) { this.active = active; return this; }
}
