package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class UpdateSignerPayload {

    @JsonProperty("full_name")
    private String fullName;

    private String email;

    @JsonProperty("whatsapp_phone_number")
    private String whatsappPhoneNumber;

    public String getFullName() {
        return fullName;
    }

    public UpdateSignerPayload setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UpdateSignerPayload setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getWhatsappPhoneNumber() {
        return whatsappPhoneNumber;
    }

    public UpdateSignerPayload setWhatsappPhoneNumber(String whatsappPhoneNumber) {
        this.whatsappPhoneNumber = whatsappPhoneNumber;
        return this;
    }
}
