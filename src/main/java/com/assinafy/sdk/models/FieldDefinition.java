package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class FieldDefinition {

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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRegex() { return regex; }
    public void setRegex(String regex) { this.regex = regex; }

    public Boolean getPreDefined() { return preDefined; }
    public void setPreDefined(Boolean preDefined) { this.preDefined = preDefined; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }

    public Boolean getStandard() { return standard; }
    public void setStandard(Boolean standard) { this.standard = standard; }

    public Boolean getReadOnly() { return readOnly; }
    public void setReadOnly(Boolean readOnly) { this.readOnly = readOnly; }

    public Boolean getVisible() { return visible; }
    public void setVisible(Boolean visible) { this.visible = visible; }
}
