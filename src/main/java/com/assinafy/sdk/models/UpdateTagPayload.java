package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class UpdateTagPayload {

    private String name;
    private String color;
    private boolean colorSet;

    public String getName() { return name; }
    public UpdateTagPayload setName(String name) {
        this.name = name;
        return this;
    }

    public String getColor() { return color; }
    public UpdateTagPayload setColor(String color) {
        this.color = color;
        this.colorSet = true;
        return this;
    }

    public UpdateTagPayload clearColor() {
        return setColor(null);
    }

    @JsonIgnore
    public boolean isColorSet() { return colorSet; }
}
