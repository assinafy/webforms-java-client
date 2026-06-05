package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class SocialLoginPayload {

    @JsonProperty("provider")
    private final String provider;

    @JsonProperty("token")
    private final String token;

    @JsonProperty("has_accepted_terms")
    private final Boolean hasAcceptedTerms;

    public SocialLoginPayload(String provider, String token, Boolean hasAcceptedTerms) {
        this.provider = provider;
        this.token = token;
        this.hasAcceptedTerms = hasAcceptedTerms;
    }

    public String getProvider() { return provider; }
    public String getToken() { return token; }
    public Boolean getHasAcceptedTerms() { return hasAcceptedTerms; }
}
