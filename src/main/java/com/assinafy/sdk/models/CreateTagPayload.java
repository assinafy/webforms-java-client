package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Request body for creating an account tag. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreateTagPayload {

    private final String name;
    private String color;

    /**
     * Sets required tag name; the API trims and collapses whitespace.
     *
     * @param name required tag name; the API trims and collapses whitespace
     */
    public CreateTagPayload(String name) {
        this.name = name;
    }

    /**
     * Returns the required tag name.
     *
     * @return the required tag name
     */
    public String getName() { return name; }

    /**
     * Returns the optional six-character hex color.
     *
     * @return the optional six-character hex color
     */
    public String getColor() { return color; }

    /**
     * Sets six-character hex color, with or without a leading {@code #}.
     *
     * @param color six-character hex color, with or without a leading {@code #}
     * @return this payload
     */
    public CreateTagPayload setColor(String color) {
        this.color = color;
        return this;
    }
}
