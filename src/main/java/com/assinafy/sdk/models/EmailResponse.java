package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Email address returned by an authentication identity lookup. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class EmailResponse {

    /** Creates an empty response model for deserialization. */
    public EmailResponse() {}

    private String email;

    /**
     * Returns the resolved email address.
     *
     * @return the resolved email address
     */
    public String getEmail() { return email; }

    /**
     * Sets resolved email address.
     *
     * @param email resolved email address
     */
    public void setEmail(String email) { this.email = email; }
}
