package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Signer role defined by a template. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TemplateRole {

    private String id;
    private String name;

    @JsonProperty("assignment_type")
    private String assignmentType;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    /** Creates an empty template-role model for JSON deserialization. */
    public TemplateRole() {}

    /**
     * Returns role identifier.
     *
     * @return role identifier
     */
    public String getId() { return id; }
    /**
     * Sets role identifier.
     *
     * @param id role identifier
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns role name.
     *
     * @return role name
     */
    public String getName() { return name; }
    /**
     * Sets role name.
     *
     * @param name role name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns wire {@code assignment_type}.
     *
     * @return wire {@code assignment_type}
     */
    public String getAssignmentType() { return assignmentType; }
    /**
     * Sets wire {@code assignment_type}.
     *
     * @param assignmentType wire {@code assignment_type}
     */
    public void setAssignmentType(String assignmentType) { this.assignmentType = assignmentType; }

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
