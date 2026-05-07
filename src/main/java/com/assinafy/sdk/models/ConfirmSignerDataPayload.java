package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ConfirmSignerDataPayload {

    private String email;

    @JsonProperty("whatsapp_phone_number")
    private String whatsappPhoneNumber;

    @JsonProperty("has_accepted_terms")
    private Boolean hasAcceptedTerms;

    public String getEmail() {
        return email;
    }

    public ConfirmSignerDataPayload setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getWhatsappPhoneNumber() {
        return whatsappPhoneNumber;
    }

    public ConfirmSignerDataPayload setWhatsappPhoneNumber(String whatsappPhoneNumber) {
        this.whatsappPhoneNumber = whatsappPhoneNumber;
        return this;
    }

    public Boolean getHasAcceptedTerms() {
        return hasAcceptedTerms;
    }

    public ConfirmSignerDataPayload setHasAcceptedTerms(Boolean hasAcceptedTerms) {
        this.hasAcceptedTerms = hasAcceptedTerms;
        return this;
    }
}