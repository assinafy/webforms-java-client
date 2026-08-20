package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** JWT authentication session with the current user and accessible accounts. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class AuthenticationResult {

    /** Creates an empty response model for deserialization. */
    public AuthenticationResult() {}

    @JsonProperty("access_token")
    private String accessToken;

    private User user;
    private List<WorkspaceAccount> accounts;

    /**
     * Returns bearer token from {@code access_token}.
     *
     * @return bearer token from {@code access_token}
     */
    public String getAccessToken() { return accessToken; }

    /**
     * Sets value of {@code access_token}.
     *
     * @param accessToken value of {@code access_token}
     */
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    /**
     * Returns the authenticated user.
     *
     * @return the authenticated user
     */
    public User getUser() { return user; }

    /**
     * Sets authenticated user.
     *
     * @param user authenticated user
     */
    public void setUser(User user) { this.user = user; }

    /**
     * Returns accounts available to the authenticated user.
     *
     * @return accounts available to the authenticated user
     */
    public List<WorkspaceAccount> getAccounts() { return accounts; }

    /**
     * Sets accessible accounts.
     *
     * @param accounts accessible accounts
     */
    public void setAccounts(List<WorkspaceAccount> accounts) { this.accounts = accounts; }
}
