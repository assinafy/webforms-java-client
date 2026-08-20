package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** One field value assigned to a signer on a document page. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class AssignmentItem {

    /** Creates an empty response model for deserialization. */
    public AssignmentItem() {}

    private String id;
    private DocumentPage page;
    private Signer signer;
    private FieldDefinition field;

    @JsonProperty("display_settings")
    private Object displaySettings;

    private Object value;
    private Boolean completed;

    /**
     * Returns the assignment-item identifier.
     *
     * @return the assignment-item identifier
     */
    public String getId() { return id; }

    /**
     * Sets assignment-item identifier.
     *
     * @param id assignment-item identifier
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns the page containing the item, or {@code null} when not applicable.
     *
     * @return the page containing the item, or {@code null} when not applicable
     */
    public DocumentPage getPage() { return page; }

    /**
     * Sets page containing the item.
     *
     * @param page page containing the item
     */
    public void setPage(DocumentPage page) { this.page = page; }

    /**
     * Returns the signer responsible for this item.
     *
     * @return the signer responsible for this item
     */
    public Signer getSigner() { return signer; }

    /**
     * Sets signer responsible for this item.
     *
     * @param signer signer responsible for this item
     */
    public void setSigner(Signer signer) { this.signer = signer; }

    /**
     * Returns the associated field definition, or {@code null} when not applicable.
     *
     * @return the associated field definition, or {@code null} when not applicable
     */
    public FieldDefinition getField() { return field; }

    /**
     * Sets associated field definition.
     *
     * @param field associated field definition
     */
    public void setField(FieldDefinition field) { this.field = field; }

    /**
     * Returns {@code display_settings}; collect items normally contain a settings object.
     *
     * @return {@code display_settings}; collect items normally contain a settings object
     */
    public Object getDisplaySettings() { return displaySettings; }

    /**
     * Sets value of {@code display_settings}.
     *
     * @param displaySettings value of {@code display_settings}
     */
    public void setDisplaySettings(Object displaySettings) { this.displaySettings = displaySettings; }

    /**
     * Returns the captured field value, or {@code null} before completion.
     *
     * @return the captured field value, or {@code null} before completion
     */
    public Object getValue() { return value; }

    /**
     * Sets captured field value.
     *
     * @param value captured field value
     */
    public void setValue(Object value) { this.value = value; }

    /**
     * Returns whether the signer completed this item.
     *
     * @return whether the signer completed this item
     */
    public Boolean getCompleted() { return completed; }

    /**
     * Sets completion state.
     *
     * @param completed completion state
     */
    public void setCompleted(Boolean completed) { this.completed = completed; }
}
