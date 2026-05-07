package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class UpdateFieldPayload {

    private String type;
    private String name;
    private String regex;

    @JsonProperty("is_required")
    private Boolean required;

    @JsonProperty("is_active")
    private Boolean active;

    public String getType() { return type; }
    public UpdateFieldPayload setType(String type) { this.type = type; return this; }

    public String getName() { return name; }
    public UpdateFieldPayload setName(String name) { this.name = name; return this; }

    public String getRegex() { return regex; }
    public UpdateFieldPayload setRegex(String regex) { this.regex = regex; return this; }

    public Boolean getRequired() { return required; }
    public UpdateFieldPayload setRequired(Boolean required) { this.required = required; return this; }

    public Boolean getActive() { return active; }
    public UpdateFieldPayload setActive(Boolean active) { this.active = active; return this; }
}
