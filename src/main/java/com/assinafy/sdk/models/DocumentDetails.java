package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Full document representation returned by owner, signer, and public lookup endpoints. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentDetails {

    /** Creates an empty response model for deserialization. */
    public DocumentDetails() {}

    private String id;
    private String name;
    private String status;
    private String resource;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("template_id")
    private String templateId;

    private Assignment assignment;
    private DocumentArtifacts artifacts;

    private List<DocumentPage> pages;
    private List<Tag> tags;

    @JsonProperty("current_signer")
    private Signer currentSigner;

    @JsonProperty("page_count")
    private String pageCount;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("is_closed")
    private boolean closed;

    @JsonProperty("decline_reason")
    private String declineReason;

    @JsonProperty("declined_by")
    private Signer declinedBy;

    @JsonProperty("signing_url")
    private String signingUrl;

    @JsonProperty("download_url")
    private String downloadUrl;

    @JsonProperty("download_final_url")
    private String downloadFinalUrl;

    private List<DocumentActivity> activities;

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
     * Returns the source {@code template_id}, or {@code null} for an uploaded document.
     *
     * @return the source {@code template_id}, or {@code null} for an uploaded document
     */
    public String getTemplateId() { return templateId; }

    /**
     * Sets value of {@code template_id}.
     *
     * @param templateId value of {@code template_id}
     */
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    /**
     * Returns expanded assignment data when requested, or {@code null}.
     *
     * @return expanded assignment data when requested, or {@code null}
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
     * Returns rendered document pages.
     *
     * @return rendered document pages
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
     * Returns the decline reason, or {@code null} when the document was not declined.
     *
     * @return the decline reason, or {@code null} when the document was not declined
     */
    public String getDeclineReason() { return declineReason; }

    /**
     * Sets value of {@code decline_reason}.
     *
     * @param declineReason value of {@code decline_reason}
     */
    public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }

    /**
     * Returns the signer who declined, or {@code null}.
     *
     * @return the signer who declined, or {@code null}
     */
    public Signer getDeclinedBy() { return declinedBy; }

    /**
     * Sets value of {@code declined_by}.
     *
     * @param declinedBy value of {@code declined_by}
     */
    public void setDeclinedBy(Signer declinedBy) { this.declinedBy = declinedBy; }

    /**
     * Returns the document signing URL when included by the endpoint.
     *
     * @return the document signing URL when included by the endpoint
     */
    public String getSigningUrl() { return signingUrl; }

    /**
     * Sets value of {@code signing_url}.
     *
     * @param signingUrl value of {@code signing_url}
     */
    public void setSigningUrl(String signingUrl) { this.signingUrl = signingUrl; }

    /**
     * Returns signer-facing document download URL when available.
     *
     * @return signer-facing document download URL when available
     */
    public String getDownloadUrl() { return downloadUrl; }

    /**
     * Sets value of {@code download_url}.
     *
     * @param downloadUrl value of {@code download_url}
     */
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    /**
     * Returns signer-facing final-document URL when available.
     *
     * @return signer-facing final-document URL when available
     */
    public String getDownloadFinalUrl() { return downloadFinalUrl; }

    /**
     * Sets value of {@code download_final_url}.
     *
     * @param downloadFinalUrl value of {@code download_final_url}
     */
    public void setDownloadFinalUrl(String downloadFinalUrl) { this.downloadFinalUrl = downloadFinalUrl; }

    /**
     * Returns activity history when included by the endpoint.
     *
     * @return activity history when included by the endpoint
     */
    public List<DocumentActivity> getActivities() { return activities; }

    /**
     * Sets document activity history.
     *
     * @param activities document activity history
     */
    public void setActivities(List<DocumentActivity> activities) { this.activities = activities; }

    /**
     * The signer resolved from the {@code signer-access-code} on signer-facing endpoints
     * ({@code GET /signers/{signer_id}/document} and {@code GET /sign}); {@code null} on account-owner
     * document responses.
     *
     * @return the signer selected by the access code, or {@code null}
     */
    public Signer getCurrentSigner() { return currentSigner; }

    /**
     * Sets value of {@code current_signer}.
     *
     * @param currentSigner value of {@code current_signer}
     */
    public void setCurrentSigner(Signer currentSigner) { this.currentSigner = currentSigner; }

    /**
     * Page count as returned by the public lookup ({@code GET /public/documents/{document_id}}). The API
     * returns this as a string (e.g. {@code "1"}); {@code null} on the full account-owner document shape,
     * which exposes the page list via {@link #getPages()} instead.
     *
     * @return page count as returned by the API, or {@code null}
     */
    public String getPageCount() { return pageCount; }

    /**
     * Sets value of {@code page_count}.
     *
     * @param pageCount value of {@code page_count}
     */
    public void setPageCount(String pageCount) { this.pageCount = pageCount; }

    /**
     * Display name of the document's creator, returned by the public lookup
     * ({@code GET /public/documents/{document_id}}); {@code null} on other document responses.
     *
     * @return creator display name, or {@code null}
     */
    public String getCreatedBy() { return createdBy; }

    /**
     * Sets value of {@code created_by}.
     *
     * @param createdBy value of {@code created_by}
     */
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
