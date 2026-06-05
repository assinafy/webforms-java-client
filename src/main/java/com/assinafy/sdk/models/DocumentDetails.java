package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentDetails {

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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public Assignment getAssignment() { return assignment; }
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }

    public DocumentArtifacts getArtifacts() { return artifacts; }
    public void setArtifacts(DocumentArtifacts artifacts) { this.artifacts = artifacts; }

    public List<DocumentPage> getPages() { return pages; }
    public void setPages(List<DocumentPage> pages) { this.pages = pages; }

    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public boolean isClosed() { return closed; }
    public void setClosed(boolean closed) { this.closed = closed; }

    public String getDeclineReason() { return declineReason; }
    public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }

    public Signer getDeclinedBy() { return declinedBy; }
    public void setDeclinedBy(Signer declinedBy) { this.declinedBy = declinedBy; }

    public String getSigningUrl() { return signingUrl; }
    public void setSigningUrl(String signingUrl) { this.signingUrl = signingUrl; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getDownloadFinalUrl() { return downloadFinalUrl; }
    public void setDownloadFinalUrl(String downloadFinalUrl) { this.downloadFinalUrl = downloadFinalUrl; }

    public List<DocumentActivity> getActivities() { return activities; }
    public void setActivities(List<DocumentActivity> activities) { this.activities = activities; }

    /**
     * The signer resolved from the {@code signer-access-code} on signer-facing endpoints
     * ({@code GET /signers/{signer_id}/document} and {@code GET /sign}); {@code null} on account-owner
     * document responses.
     */
    public Signer getCurrentSigner() { return currentSigner; }
    public void setCurrentSigner(Signer currentSigner) { this.currentSigner = currentSigner; }

    /**
     * Page count as returned by the public lookup ({@code GET /public/documents/{document_id}}). The API
     * returns this as a string (e.g. {@code "1"}); {@code null} on the full account-owner document shape,
     * which exposes the page list via {@link #getPages()} instead.
     */
    public String getPageCount() { return pageCount; }
    public void setPageCount(String pageCount) { this.pageCount = pageCount; }

    /**
     * Display name of the document's creator, returned by the public lookup
     * ({@code GET /public/documents/{document_id}}); {@code null} on other document responses.
     */
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
