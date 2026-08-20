package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Workspace account visible to the authenticated user. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class WorkspaceAccount {

    private String resource;
    private String id;
    private String name;
    private List<String> roles;

    @JsonProperty("primary_color")
    private String primaryColor;

    @JsonProperty("secondary_color")
    private String secondaryColor;

    @JsonProperty("notification_sender_type")
    private String notificationSenderType;

    @JsonProperty("is_delete_allowed")
    private Boolean deleteAllowed;

    @JsonProperty("created_at")
    private String createdAt;

    /** Creates an empty workspace-account model for JSON deserialization. */
    public WorkspaceAccount() {}

    /**
     * Returns resource discriminator, normally {@code account}.
     *
     * @return resource discriminator, normally {@code account}
     */
    public String getResource() { return resource; }
    /**
     * Sets resource discriminator.
     *
     * @param resource resource discriminator
     */
    public void setResource(String resource) { this.resource = resource; }

    /**
     * Returns account identifier.
     *
     * @return account identifier
     */
    public String getId() { return id; }
    /**
     * Sets account identifier.
     *
     * @param id account identifier
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns account name.
     *
     * @return account name
     */
    public String getName() { return name; }
    /**
     * Sets account name.
     *
     * @param name account name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns authenticated user's roles in the account.
     *
     * @return authenticated user's roles in the account
     */
    public List<String> getRoles() { return roles; }
    /**
     * Sets authenticated user's roles in the account.
     *
     * @param roles authenticated user's roles in the account
     */
    public void setRoles(List<String> roles) { this.roles = roles; }

    /**
     * Returns wire {@code primary_color}.
     *
     * @return wire {@code primary_color}
     */
    public String getPrimaryColor() { return primaryColor; }
    /**
     * Sets wire {@code primary_color}.
     *
     * @param primaryColor wire {@code primary_color}
     */
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }

    /**
     * Returns wire {@code secondary_color}.
     *
     * @return wire {@code secondary_color}
     */
    public String getSecondaryColor() { return secondaryColor; }
    /**
     * Sets wire {@code secondary_color}.
     *
     * @param secondaryColor wire {@code secondary_color}
     */
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }

    /**
     * Returns wire {@code notification_sender_type}.
     *
     * @return wire {@code notification_sender_type}
     */
    public String getNotificationSenderType() { return notificationSenderType; }

    /**
     * Sets wire {@code notification_sender_type}.
     *
     * @param notificationSenderType wire {@code notification_sender_type}
     */
    public void setNotificationSenderType(String notificationSenderType) {
        this.notificationSenderType = notificationSenderType;
    }

    /**
     * Returns wire {@code is_delete_allowed}.
     *
     * @return wire {@code is_delete_allowed}
     */
    public Boolean getDeleteAllowed() { return deleteAllowed; }
    /**
     * Sets wire {@code is_delete_allowed}.
     *
     * @param deleteAllowed wire {@code is_delete_allowed}
     */
    public void setDeleteAllowed(Boolean deleteAllowed) { this.deleteAllowed = deleteAllowed; }

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
}
