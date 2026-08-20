package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Signer-specific URL returned for an active document assignment. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SigningUrl {

    @JsonProperty("signer_id")
    private String signerId;

    private String url;

    /** Creates an empty signing URL model for JSON deserialization. */
    public SigningUrl() {}

    /**
     * Returns wire {@code signer_id}.
     *
     * @return wire {@code signer_id}
     */
    public String getSignerId() { return signerId; }

    /**
     * Sets wire {@code signer_id}.
     *
     * @param signerId wire {@code signer_id}
     */
    public void setSignerId(String signerId) { this.signerId = signerId; }

    /**
     * Returns signer-facing URL.
     *
     * @return signer-facing URL
     */
    public String getUrl() { return url; }

    /**
     * Sets signer-facing URL.
     *
     * @param url signer-facing URL
     */
    public void setUrl(String url) { this.url = url; }
}
