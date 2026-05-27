package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class EmailResponse {

    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
