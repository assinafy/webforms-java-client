package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Full template representation returned by the template detail endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TemplateDetails {

    private String id;
    private String name;
    private String resource;

    @JsonProperty("document_name")
    private String documentName;

    private String message;
    private String status;

    @JsonProperty("account_id")
    private String accountId;

    private List<TemplateRole> roles;
    private List<TemplatePage> pages;
    private List<Tag> tags;

    @JsonProperty("default_document_tags")
    private List<Tag> defaultDocumentTags;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    /** Creates an empty template model for JSON deserialization. */
    public TemplateDetails() {}

    /**
     * Returns template identifier.
     *
     * @return template identifier
     */
    public String getId() { return id; }
    /**
     * Sets template identifier.
     *
     * @param id template identifier
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns template name.
     *
     * @return template name
     */
    public String getName() { return name; }
    /**
     * Sets template name.
     *
     * @param name template name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns resource discriminator.
     *
     * @return resource discriminator
     */
    public String getResource() { return resource; }
    /**
     * Sets resource discriminator.
     *
     * @param resource resource discriminator
     */
    public void setResource(String resource) { this.resource = resource; }

    /**
     * Returns wire {@code document_name}.
     *
     * @return wire {@code document_name}
     */
    public String getDocumentName() { return documentName; }
    /**
     * Sets wire {@code document_name}.
     *
     * @param documentName wire {@code document_name}
     */
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    /**
     * Returns default invitation message, or {@code null}.
     *
     * @return default invitation message, or {@code null}
     */
    public String getMessage() { return message; }
    /**
     * Sets default invitation message.
     *
     * @param message default invitation message
     */
    public void setMessage(String message) { this.message = message; }

    /**
     * Returns template status.
     *
     * @return template status
     */
    public String getStatus() { return status; }
    /**
     * Sets template status.
     *
     * @param status template status
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Returns owning wire {@code account_id}.
     *
     * @return owning wire {@code account_id}
     */
    public String getAccountId() { return accountId; }
    /**
     * Sets owning wire {@code account_id}.
     *
     * @param accountId owning wire {@code account_id}
     */
    public void setAccountId(String accountId) { this.accountId = accountId; }

    /**
     * Returns signer roles.
     *
     * @return signer roles
     */
    public List<TemplateRole> getRoles() { return roles; }
    /**
     * Sets signer roles.
     *
     * @param roles signer roles
     */
    public void setRoles(List<TemplateRole> roles) { this.roles = roles; }

    /**
     * Returns template pages.
     *
     * @return template pages
     */
    public List<TemplatePage> getPages() { return pages; }
    /**
     * Sets template pages.
     *
     * @param pages template pages
     */
    public void setPages(List<TemplatePage> pages) { this.pages = pages; }

    /**
     * Returns tags assigned to the template.
     *
     * @return tags assigned to the template
     */
    public List<Tag> getTags() { return tags; }
    /**
     * Sets tags assigned to the template.
     *
     * @param tags tags assigned to the template
     */
    public void setTags(List<Tag> tags) { this.tags = tags; }

    /**
     * Returns wire {@code default_document_tags} copied to generated documents.
     *
     * @return wire {@code default_document_tags} copied to generated documents
     */
    public List<Tag> getDefaultDocumentTags() { return defaultDocumentTags; }

    /**
     * Sets wire {@code default_document_tags}.
     *
     * @param defaultDocumentTags wire {@code default_document_tags}
     */
    public void setDefaultDocumentTags(List<Tag> defaultDocumentTags) {
        this.defaultDocumentTags = defaultDocumentTags;
    }

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
