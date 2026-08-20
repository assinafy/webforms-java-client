package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Document representation returned in account document lists. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentListItem {

    /** Creates an empty response model for deserialization. */
    public DocumentListItem() {}

    private String id;
    private String name;
    private String status;
    private String resource;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("template_id")
    private String templateId;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("is_closed")
    private boolean closed;

    private Assignment assignment;
    private DocumentArtifacts artifacts;
    private List<DocumentPage> pages;
    private List<Tag> tags;

    @JsonProperty("signing_url")
    private String signingUrl;

    @JsonProperty("decline_reason")
    private String declineReason;

    @JsonProperty("declined_by")
    private Signer declinedBy;

    /**
     * Returns the document identifier.
     *
     * @return the document identifier
     */
    public String getId() { return id; }

    /**
     * Sets document identifier.
     *
     * @param id document identifier
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns the document name.
     *
     * @return the document name
     */
    public String getName() { return name; }

    /**
     * Sets document name.
     *
     * @param name document name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the document lifecycle status code.
     *
     * @return the document lifecycle status code
     */
    public String getStatus() { return status; }

    /**
     * Sets document lifecycle status code.
     *
     * @param status document lifecycle status code
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Returns the API resource marker, normally {@code document}.
     *
     * @return the API resource marker, normally {@code document}
     */
    public String getResource() { return resource; }

    /**
     * Sets API resource marker.
     *
     * @param resource API resource marker
     */
    public void setResource(String resource) { this.resource = resource; }

    /**
     * Returns the owning {@code account_id}.
     *
     * @return the owning {@code account_id}
     */
    public String getAccountId() { return accountId; }

    /**
     * Sets value of {@code account_id}.
     *
     * @param accountId value of {@code account_id}
     */
    public void setAccountId(String accountId) { this.accountId = accountId; }

    /**
     * Returns source {@code template_id}, or {@code null} for an uploaded document.
     *
     * @return source {@code template_id}, or {@code null} for an uploaded document
     */
    public String getTemplateId() { return templateId; }

    /**
     * Sets value of {@code template_id}.
     *
     * @param templateId value of {@code template_id}
     */
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    /**
     * Returns ISO 8601 {@code created_at} timestamp.
     *
     * @return ISO 8601 {@code created_at} timestamp
     */
    public String getCreatedAt() { return createdAt; }

    /**
     * Sets value of {@code created_at}.
     *
     * @param createdAt value of {@code created_at}
     */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    /**
     * Returns ISO 8601 {@code updated_at} timestamp.
     *
     * @return ISO 8601 {@code updated_at} timestamp
     */
    public String getUpdatedAt() { return updatedAt; }

    /**
     * Sets value of {@code updated_at}.
     *
     * @param updatedAt value of {@code updated_at}
     */
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Returns value of {@code is_closed}.
     *
     * @return value of {@code is_closed}
     */
    public boolean isClosed() { return closed; }

    /**
     * Sets value of {@code is_closed}.
     *
     * @param closed value of {@code is_closed}
     */
    public void setClosed(boolean closed) { this.closed = closed; }

    /**
     * Returns expanded assignment data when included, or {@code null}.
     *
     * @return expanded assignment data when included, or {@code null}
     */
    public Assignment getAssignment() { return assignment; }

    /**
     * Sets expanded assignment data.
     *
     * @param assignment expanded assignment data
     */
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }

    /**
     * Returns available document artifact URLs.
     *
     * @return available document artifact URLs
     */
    public DocumentArtifacts getArtifacts() { return artifacts; }

    /**
     * Sets document artifact URLs.
     *
     * @param artifacts document artifact URLs
     */
    public void setArtifacts(DocumentArtifacts artifacts) { this.artifacts = artifacts; }

    /**
     * Returns rendered pages when included by the endpoint.
     *
     * @return rendered pages when included by the endpoint
     */
    public List<DocumentPage> getPages() { return pages; }

    /**
     * Sets rendered document pages.
     *
     * @param pages rendered document pages
     */
    public void setPages(List<DocumentPage> pages) { this.pages = pages; }

    /**
     * Returns tags attached to the document.
     *
     * @return tags attached to the document
     */
    public List<Tag> getTags() { return tags; }

    /**
     * Sets document tags.
     *
     * @param tags document tags
     */
    public void setTags(List<Tag> tags) { this.tags = tags; }

    /**
     * Returns signing URL when included by the endpoint.
     *
     * @return signing URL when included by the endpoint
     */
    public String getSigningUrl() { return signingUrl; }

    /**
     * Sets value of {@code signing_url}.
     *
     * @param signingUrl value of {@code signing_url}
     */
    public void setSigningUrl(String signingUrl) { this.signingUrl = signingUrl; }

    /**
     * Returns decline reason, or {@code null} when the document was not declined.
     *
     * @return decline reason, or {@code null} when the document was not declined
     */
    public String getDeclineReason() { return declineReason; }

    /**
     * Sets value of {@code decline_reason}.
     *
     * @param declineReason value of {@code decline_reason}
     */
    public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }

    /**
     * Returns signer who declined, or {@code null}.
     *
     * @return signer who declined, or {@code null}
     */
    public Signer getDeclinedBy() { return declinedBy; }

    /**
     * Sets value of {@code declined_by}.
     *
     * @param declinedBy value of {@code declined_by}
     */
    public void setDeclinedBy(Signer declinedBy) { this.declinedBy = declinedBy; }
}
