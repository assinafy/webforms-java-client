package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class WorkspaceAccount {

    private String id;
    private String name;
    private List<String> roles;

    @JsonProperty("is_delete_allowed")
    private Boolean deleteAllowed;

    @JsonProperty("created_at")
    private String createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public Boolean getDeleteAllowed() { return deleteAllowed; }
    public void setDeleteAllowed(Boolean deleteAllowed) { this.deleteAllowed = deleteAllowed; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
