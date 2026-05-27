package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class User {

    private String id;
    private String name;
    private String email;
    private String telephone;

    @JsonProperty("government_id")
    private String governmentId;

    @JsonProperty("is_email_verified")
    private Boolean emailVerified;

    @JsonProperty("has_accepted_terms")
    private Boolean hasAcceptedTerms;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("to_be_deleted_at")
    private String toBeDeletedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getGovernmentId() { return governmentId; }
    public void setGovernmentId(String governmentId) { this.governmentId = governmentId; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public Boolean getHasAcceptedTerms() { return hasAcceptedTerms; }
    public void setHasAcceptedTerms(Boolean hasAcceptedTerms) { this.hasAcceptedTerms = hasAcceptedTerms; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getToBeDeletedAt() { return toBeDeletedAt; }
    public void setToBeDeletedAt(String toBeDeletedAt) { this.toBeDeletedAt = toBeDeletedAt; }
}
