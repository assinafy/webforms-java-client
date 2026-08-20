package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Document tag returned by tag and document endpoints. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Tag {

    private String resource;
    private String id;
    private String name;
    private String color;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    /** Creates an empty tag model for JSON deserialization. */
    public Tag() {}

    /**
     * Returns resource discriminator, normally {@code tag}.
     *
     * @return resource discriminator, normally {@code tag}
     */
    public String getResource() { return resource; }

    /**
     * Sets resource discriminator.
     *
     * @param resource resource discriminator
     */
    public void setResource(String resource) { this.resource = resource; }

    /**
     * Returns tag identifier.
     *
     * @return tag identifier
     */
    public String getId() { return id; }

    /**
     * Sets tag identifier.
     *
     * @param id tag identifier
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns tag name.
     *
     * @return tag name
     */
    public String getName() { return name; }

    /**
     * Sets tag name.
     *
     * @param name tag name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns optional tag color.
     *
     * @return optional tag color
     */
    public String getColor() { return color; }

    /**
     * Sets optional tag color.
     *
     * @param color optional tag color
     */
    public void setColor(String color) { this.color = color; }

    /**
     * Returns wire {@code created_at} timestamp.
     *
     * @return wire {@code created_at} timestamp
     */
    public String getCreatedAt() { return createdAt; }

    /**
     * Sets wire {@code created_at} timestamp.
     *
     * @param createdAt wire {@code created_at} timestamp
     */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    /**
     * Returns wire {@code updated_at} timestamp.
     *
     * @return wire {@code updated_at} timestamp
     */
    public String getUpdatedAt() { return updatedAt; }

    /**
     * Sets wire {@code updated_at} timestamp.
     *
     * @param updatedAt wire {@code updated_at} timestamp
     */
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
