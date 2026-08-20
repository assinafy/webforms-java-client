package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Reusable account field definition and its validation/display flags. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class FieldDefinition {

    /** Creates an empty response model for deserialization. */
    public FieldDefinition() {}

    private String id;
    private String resource;
    private String name;
    private String type;
    private String regex;

    @JsonProperty("is_pre_defined")
    private Boolean preDefined;

    @JsonProperty("is_active")
    private Boolean active;

    @JsonProperty("is_required")
    private Boolean required;

    @JsonProperty("is_standard")
    private Boolean standard;

    @JsonProperty("is_read_only")
    private Boolean readOnly;

    @JsonProperty("is_visible")
    private Boolean visible;

    /**
     * Returns the field identifier.
     *
     * @return the field identifier
     */
    public String getId() { return id; }

    /**
     * Sets field identifier.
     *
     * @param id field identifier
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns the API resource marker, normally {@code field}.
     *
     * @return the API resource marker, normally {@code field}
     */
    public String getResource() { return resource; }

    /**
     * Sets API resource marker.
     *
     * @param resource API resource marker
     */
    public void setResource(String resource) { this.resource = resource; }

    /**
     * Returns the field display name.
     *
     * @return the field display name
     */
    public String getName() { return name; }

    /**
     * Sets field display name.
     *
     * @param name field display name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the field or validation type code.
     *
     * @return the field or validation type code
     */
    public String getType() { return type; }

    /**
     * Sets field or validation type code.
     *
     * @param type field or validation type code
     */
    public void setType(String type) { this.type = type; }

    /**
     * Returns the optional validation regular expression.
     *
     * @return the optional validation regular expression
     */
    public String getRegex() { return regex; }

    /**
     * Sets validation regular expression.
     *
     * @param regex validation regular expression
     */
    public void setRegex(String regex) { this.regex = regex; }

    /**
     * Returns value of {@code is_pre_defined}.
     *
     * @return value of {@code is_pre_defined}
     */
    public Boolean getPreDefined() { return preDefined; }

    /**
     * Sets value of {@code is_pre_defined}.
     *
     * @param preDefined value of {@code is_pre_defined}
     */
    public void setPreDefined(Boolean preDefined) { this.preDefined = preDefined; }

    /**
     * Returns value of {@code is_active}.
     *
     * @return value of {@code is_active}
     */
    public Boolean getActive() { return active; }

    /**
     * Sets value of {@code is_active}.
     *
     * @param active value of {@code is_active}
     */
    public void setActive(Boolean active) { this.active = active; }

    /**
     * Returns value of {@code is_required}.
     *
     * @return value of {@code is_required}
     */
    public Boolean getRequired() { return required; }

    /**
     * Sets value of {@code is_required}.
     *
     * @param required value of {@code is_required}
     */
    public void setRequired(Boolean required) { this.required = required; }

    /**
     * Returns value of {@code is_standard}.
     *
     * @return value of {@code is_standard}
     */
    public Boolean getStandard() { return standard; }

    /**
     * Sets value of {@code is_standard}.
     *
     * @param standard value of {@code is_standard}
     */
    public void setStandard(Boolean standard) { this.standard = standard; }

    /**
     * Returns value of {@code is_read_only}.
     *
     * @return value of {@code is_read_only}
     */
    public Boolean getReadOnly() { return readOnly; }

    /**
     * Sets value of {@code is_read_only}.
     *
     * @param readOnly value of {@code is_read_only}
     */
    public void setReadOnly(Boolean readOnly) { this.readOnly = readOnly; }

    /**
     * Returns value of {@code is_visible}.
     *
     * @return value of {@code is_visible}
     */
    public Boolean getVisible() { return visible; }

    /**
     * Sets value of {@code is_visible}.
     *
     * @param visible value of {@code is_visible}
     */
    public void setVisible(Boolean visible) { this.visible = visible; }
}
