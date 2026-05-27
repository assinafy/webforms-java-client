package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class AuthenticationResult {

    @JsonProperty("access_token")
    private String accessToken;

    private User user;
    private List<WorkspaceAccount> accounts;

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<WorkspaceAccount> getAccounts() { return accounts; }
    public void setAccounts(List<WorkspaceAccount> accounts) { this.accounts = accounts; }
}
