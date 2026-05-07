package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class AssignmentItem {

    private String id;
    private DocumentPage page;
    private Signer signer;
    private FieldDefinition field;

    @JsonProperty("display_settings")
    private Object displaySettings;

    private Object value;
    private Boolean completed;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public DocumentPage getPage() { return page; }
    public void setPage(DocumentPage page) { this.page = page; }

    public Signer getSigner() { return signer; }
    public void setSigner(Signer signer) { this.signer = signer; }

    public FieldDefinition getField() { return field; }
    public void setField(FieldDefinition field) { this.field = field; }

    public Object getDisplaySettings() { return displaySettings; }
    public void setDisplaySettings(Object displaySettings) { this.displaySettings = displaySettings; }

    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
}
