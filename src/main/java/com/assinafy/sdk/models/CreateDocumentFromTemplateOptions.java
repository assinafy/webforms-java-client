package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreateDocumentFromTemplateOptions {

    private String name;
    private String message;

    @JsonProperty("expires_at")
    private String expiresAt;

    @JsonProperty("editor_fields")
    private List<Object> editorFields;

    public String getName() { return name; }
    public CreateDocumentFromTemplateOptions setName(String name) { this.name = name; return this; }

    public String getMessage() { return message; }
    public CreateDocumentFromTemplateOptions setMessage(String message) { this.message = message; return this; }

    public String getExpiresAt() { return expiresAt; }
    public CreateDocumentFromTemplateOptions setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; return this; }

    public List<Object> getEditorFields() { return editorFields; }
    public CreateDocumentFromTemplateOptions setEditorFields(List<Object> editorFields) {
        this.editorFields = editorFields;
        return this;
    }
}
