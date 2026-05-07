package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreateFieldPayload {

    private final String type;
    private final String name;
    private String regex;

    @JsonProperty("is_required")
    private Boolean required;

    @JsonProperty("is_active")
    private Boolean active;

    public CreateFieldPayload(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() { return type; }
    public String getName() { return name; }

    public String getRegex() { return regex; }
    public CreateFieldPayload setRegex(String regex) { this.regex = regex; return this; }

    public Boolean getRequired() { return required; }
    public CreateFieldPayload setRequired(Boolean required) { this.required = required; return this; }

    public Boolean getActive() { return active; }
    public CreateFieldPayload setActive(Boolean active) { this.active = active; return this; }
}
