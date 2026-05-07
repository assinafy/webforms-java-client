package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class SigningUrl {

    @JsonProperty("signer_id")
    private String signerId;

    private String url;

    public String getSignerId() { return signerId; }
    public void setSignerId(String signerId) { this.signerId = signerId; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
