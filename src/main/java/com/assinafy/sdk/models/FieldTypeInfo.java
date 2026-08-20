package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** A supported field/validation type code and its display name. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class FieldTypeInfo {

    /** Creates an empty response model for deserialization. */
    public FieldTypeInfo() {}

    private String type;
    private String name;

    /**
     * Returns the machine-readable field type code.
     *
     * @return the machine-readable field type code
     */
    public String getType() { return type; }

    /**
     * Sets machine-readable field type code.
     *
     * @param type machine-readable field type code
     */
    public void setType(String type) { this.type = type; }

    /**
     * Returns the human-readable type name.
     *
     * @return the human-readable type name
     */
    public String getName() { return name; }

    /**
     * Sets human-readable type name.
     *
     * @param name human-readable type name
     */
    public void setName(String name) { this.name = name; }
}
