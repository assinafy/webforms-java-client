package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class TemplateListItem {

    private String id;
    private String name;
    private String resource;

    @JsonProperty("document_name")
    private String documentName;

    private String message;
    private String status;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    private List<TemplatePage> pages;
    private List<TemplateRole> roles;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public List<TemplatePage> getPages() { return pages; }
    public void setPages(List<TemplatePage> pages) { this.pages = pages; }

    public List<TemplateRole> getRoles() { return roles; }
    public void setRoles(List<TemplateRole> roles) { this.roles = roles; }
}
