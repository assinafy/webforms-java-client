package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreateTagPayload {

    private final String name;
    private String color;

    public CreateTagPayload(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public String getColor() { return color; }
    public CreateTagPayload setColor(String color) {
        this.color = color;
        return this;
    }
}
