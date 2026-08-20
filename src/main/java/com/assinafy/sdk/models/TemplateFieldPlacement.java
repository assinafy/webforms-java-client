package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Field placement and display metadata embedded in a template page. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TemplateFieldPlacement {

    private String id;

    @JsonProperty("field_id")
    private String fieldId;

    @JsonProperty("role_id")
    private String roleId;

    private String label;

    @JsonProperty("display_settings")
    private Object displaySettings;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    /** Creates an empty field-placement model for JSON deserialization. */
    public TemplateFieldPlacement() {}

    /**
     * Returns placement identifier.
     *
     * @return placement identifier
     */
    public String getId() { return id; }
    /**
     * Sets placement identifier.
     *
     * @param id placement identifier
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns wire {@code field_id}.
     *
     * @return wire {@code field_id}
     */
    public String getFieldId() { return fieldId; }
    /**
     * Sets wire {@code field_id}.
     *
     * @param fieldId wire {@code field_id}
     */
    public void setFieldId(String fieldId) { this.fieldId = fieldId; }

    /**
     * Returns wire {@code role_id}.
     *
     * @return wire {@code role_id}
     */
    public String getRoleId() { return roleId; }
    /**
     * Sets wire {@code role_id}.
     *
     * @param roleId wire {@code role_id}
     */
    public void setRoleId(String roleId) { this.roleId = roleId; }

    /**
     * Returns optional field label.
     *
     * @return optional field label
     */
    public String getLabel() { return label; }
    /**
     * Sets optional field label.
     *
     * @param label optional field label
     */
    public void setLabel(String label) { this.label = label; }

    /**
     * Returns wire {@code display_settings} object.
     *
     * @return wire {@code display_settings} object
     */
    public Object getDisplaySettings() { return displaySettings; }
    /**
     * Sets wire {@code display_settings} object.
     *
     * @param displaySettings wire {@code display_settings} object
     */
    public void setDisplaySettings(Object displaySettings) { this.displaySettings = displaySettings; }

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
