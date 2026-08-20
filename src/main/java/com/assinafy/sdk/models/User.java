package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Authenticated user profile returned by user and authentication endpoints. */
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

    /** Creates an empty user model for JSON deserialization. */
    public User() {}

    /**
     * Returns user identifier.
     *
     * @return user identifier
     */
    public String getId() { return id; }
    /**
     * Sets user identifier.
     *
     * @param id user identifier
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns user name.
     *
     * @return user name
     */
    public String getName() { return name; }
    /**
     * Sets user name.
     *
     * @param name user name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns user email.
     *
     * @return user email
     */
    public String getEmail() { return email; }
    /**
     * Sets user email.
     *
     * @param email user email
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns telephone number, or {@code null}.
     *
     * @return telephone number, or {@code null}
     */
    public String getTelephone() { return telephone; }
    /**
     * Sets telephone number.
     *
     * @param telephone telephone number
     */
    public void setTelephone(String telephone) { this.telephone = telephone; }

    /**
     * Returns wire {@code government_id}, or {@code null}.
     *
     * @return wire {@code government_id}, or {@code null}
     */
    public String getGovernmentId() { return governmentId; }
    /**
     * Sets wire {@code government_id}.
     *
     * @param governmentId wire {@code government_id}
     */
    public void setGovernmentId(String governmentId) { this.governmentId = governmentId; }

    /**
     * Returns wire {@code is_email_verified}.
     *
     * @return wire {@code is_email_verified}
     */
    public Boolean getEmailVerified() { return emailVerified; }
    /**
     * Sets wire {@code is_email_verified}.
     *
     * @param emailVerified wire {@code is_email_verified}
     */
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    /**
     * Returns wire {@code has_accepted_terms}.
     *
     * @return wire {@code has_accepted_terms}
     */
    public Boolean getHasAcceptedTerms() { return hasAcceptedTerms; }
    /**
     * Sets wire {@code has_accepted_terms}.
     *
     * @param hasAcceptedTerms wire {@code has_accepted_terms}
     */
    public void setHasAcceptedTerms(Boolean hasAcceptedTerms) { this.hasAcceptedTerms = hasAcceptedTerms; }

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
     * Returns wire {@code to_be_deleted_at}, or {@code null}.
     *
     * @return wire {@code to_be_deleted_at}, or {@code null}
     */
    public String getToBeDeletedAt() { return toBeDeletedAt; }
    /**
     * Sets wire {@code to_be_deleted_at} timestamp.
     *
     * @param toBeDeletedAt wire {@code to_be_deleted_at} timestamp
     */
    public void setToBeDeletedAt(String toBeDeletedAt) { this.toBeDeletedAt = toBeDeletedAt; }
}
