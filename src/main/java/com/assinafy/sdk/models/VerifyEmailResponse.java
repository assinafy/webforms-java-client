package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Email-verification response, optionally including a newly issued access token. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class VerifyEmailResponse {

    private String message;

    @JsonProperty("access_token")
    private String accessToken;

    /** Creates an empty response model for JSON deserialization. */
    public VerifyEmailResponse() {}

    /**
     * Returns server confirmation message.
     *
     * @return server confirmation message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets server confirmation message.
     *
     * @param message server confirmation message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns wire {@code access_token}, or {@code null} when none was issued.
     *
     * @return wire {@code access_token}, or {@code null} when none was issued
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets wire {@code access_token}.
     *
     * @param accessToken wire {@code access_token}
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
