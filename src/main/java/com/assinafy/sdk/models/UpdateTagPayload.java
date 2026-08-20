package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/** Partial tag update; omitted properties remain unchanged. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class UpdateTagPayload {

    private String name;
    private String color;
    private boolean colorSet;

    /** Creates an empty partial tag update. */
    public UpdateTagPayload() {}

    /**
     * Returns tag name, or {@code null} when omitted.
     *
     * @return tag name, or {@code null} when omitted
     */
    public String getName() { return name; }

    /**
     * Sets optional tag name.
     *
     * @param name optional tag name
     * @return this payload
     */
    public UpdateTagPayload setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns tag color, or {@code null}.
     *
     * @return tag color, or {@code null}
     */
    public String getColor() { return color; }

    /**
     * Sets or clears the tag color; {@code null} is serialized when explicitly supplied.
     *
     * @param color optional tag color, or {@code null} to clear it
     * @return this payload
     */
    public UpdateTagPayload setColor(String color) {
        this.color = color;
        this.colorSet = true;
        return this;
    }

    /**
     * Explicitly clears the tag color with {@code "color": null}.
     *
     * @return this payload
     */
    public UpdateTagPayload clearColor() {
        return setColor(null);
    }

    /**
     * Returns whether {@code color} will be included, including an explicit {@code null}.
     *
     * @return whether {@code color} will be included, including an explicit {@code null}
     */
    @JsonIgnore
    public boolean isColorSet() { return colorSet; }
}
