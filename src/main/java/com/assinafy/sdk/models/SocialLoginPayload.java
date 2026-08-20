package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Request body for authentication with a supported social identity provider. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class SocialLoginPayload {

    @JsonProperty("provider")
    private final String provider;

    @JsonProperty("token")
    private final String token;

    @JsonProperty("has_accepted_terms")
    private final Boolean hasAcceptedTerms;

    /**
     * Creates an instance.
     *
     * @param provider required provider code accepted by the API
     * @param token required provider-issued identity token
     * @param hasAcceptedTerms optional wire {@code has_accepted_terms}
     */
    public SocialLoginPayload(String provider, String token, Boolean hasAcceptedTerms) {
        this.provider = provider;
        this.token = token;
        this.hasAcceptedTerms = hasAcceptedTerms;
    }

    /**
     * Returns provider code.
     *
     * @return provider code
     */
    public String getProvider() { return provider; }

    /**
     * Returns provider-issued token.
     *
     * @return provider-issued token
     */
    public String getToken() { return token; }

    /**
     * Returns wire {@code has_accepted_terms}, or {@code null} when omitted.
     *
     * @return wire {@code has_accepted_terms}, or {@code null} when omitted
     */
    public Boolean getHasAcceptedTerms() { return hasAcceptedTerms; }
}
