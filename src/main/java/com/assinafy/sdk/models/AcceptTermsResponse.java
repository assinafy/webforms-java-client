package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Result of confirming a signer's terms acceptance and identity data. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class AcceptTermsResponse {

    /** Creates an empty response model for deserialization. */
    public AcceptTermsResponse() {}

    @JsonProperty("full_name")
    private String fullName;

    private String email;

    @JsonProperty("has_accepted_terms")
    private Boolean hasAcceptedTerms;

    /**
     * Returns the confirmed {@code full_name}.
     *
     * @return the confirmed {@code full_name}
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets confirmed value of {@code full_name}.
     *
     * @param fullName confirmed value of {@code full_name}
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Returns the confirmed email address.
     *
     * @return the confirmed email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets confirmed email address.
     *
     * @param email confirmed email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns whether {@code has_accepted_terms} is set for the user.
     *
     * @return whether {@code has_accepted_terms} is set for the user
     */
    public Boolean getHasAcceptedTerms() {
        return hasAcceptedTerms;
    }

    /**
     * Sets value of {@code has_accepted_terms}.
     *
     * @param hasAcceptedTerms value of {@code has_accepted_terms}
     */
    public void setHasAcceptedTerms(Boolean hasAcceptedTerms) {
        this.hasAcceptedTerms = hasAcceptedTerms;
    }
}
