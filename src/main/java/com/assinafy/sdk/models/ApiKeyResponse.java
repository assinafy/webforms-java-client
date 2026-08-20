package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** API-key value returned by the current-user API-key endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ApiKeyResponse {

    /** Creates an empty response model for deserialization. */
    public ApiKeyResponse() {}

    @JsonProperty("api_key")
    private String apiKey;

    /**
     * Returns {@code api_key}, or {@code null} when the user has no key.
     *
     * @return {@code api_key}, or {@code null} when the user has no key
     */
    public String getApiKey() { return apiKey; }

    /**
     * Sets value of {@code api_key}.
     *
     * @param apiKey value of {@code api_key}
     */
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
}
