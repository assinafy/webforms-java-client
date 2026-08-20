package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Optional fields used when creating a document from a template. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreateDocumentFromTemplateOptions {

    /** Creates an empty options object. */
    public CreateDocumentFromTemplateOptions() {}

    private String name;
    private String message;

    @JsonProperty("expires_at")
    private String expiresAt;

    @JsonProperty("editor_fields")
    private List<?> editorFields;

    private List<String> tags;

    /**
     * Returns the optional document name; the template name is used when omitted.
     *
     * @return the optional document name; the template name is used when omitted
     */
    public String getName() { return name; }

    /**
     * Sets title for the generated document.
     *
     * @param name title for the generated document
     * @return this options object
     */
    public CreateDocumentFromTemplateOptions setName(String name) { this.name = name; return this; }

    /**
     * Returns the optional message sent to signers.
     *
     * @return the optional message sent to signers
     */
    public String getMessage() { return message; }

    /**
     * Sets message sent to signers.
     *
     * @param message message sent to signers
     * @return this options object
     */
    public CreateDocumentFromTemplateOptions setMessage(String message) { this.message = message; return this; }

    /**
     * Returns optional ISO 8601 {@code expires_at} value.
     *
     * @return optional ISO 8601 {@code expires_at} value
     */
    public String getExpiresAt() { return expiresAt; }

    /**
     * Sets ISO 8601 assignment expiration serialized as {@code expires_at}.
     *
     * @param expiresAt ISO 8601 assignment expiration serialized as {@code expires_at}
     * @return this options object
     */
    public CreateDocumentFromTemplateOptions setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; return this; }

    /**
     * Returns values serialized as {@code editor_fields}; prefer {@link TemplateEditorField}.
     *
     * @return values serialized as {@code editor_fields}; prefer {@link TemplateEditorField}
     */
    public List<?> getEditorFields() { return editorFields; }

    /**
     * Sets the values baked into editor-role fields.
     *
     * @param editorFields editor field values
     * @return this options object
     */
    public CreateDocumentFromTemplateOptions setEditorFields(List<?> editorFields) {
        this.editorFields = editorFields;
        return this;
    }

    /**
     * Sets strongly typed values for the template's editor-role fields.
     *
     * @param editorFields editor field values
     * @return this options object
     */
    public CreateDocumentFromTemplateOptions setTemplateEditorFields(List<TemplateEditorField> editorFields) {
        return setEditorFields(editorFields);
    }

    /**
     * Returns tag names merged with the template's default document tags.
     *
     * @return tag names merged with the template's default document tags
     */
    public List<String> getTags() { return tags; }

    /**
     * Sets tag names to attach; unknown names are created by the API.
     *
     * @param tags tag names to attach; unknown names are created by the API
     * @return this options object
     */
    public CreateDocumentFromTemplateOptions setTags(List<String> tags) {
        this.tags = tags;
        return this;
    }
}
