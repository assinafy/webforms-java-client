package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreateSignerPayload {

    @JsonProperty("full_name")
    private final String fullName;

    private final String email;

    @JsonProperty("whatsapp_phone_number")
    private String whatsappPhoneNumber;

    public CreateSignerPayload(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getWhatsappPhoneNumber() {
        return whatsappPhoneNumber;
    }

    public CreateSignerPayload setWhatsappPhoneNumber(String whatsappPhoneNumber) {
        this.whatsappPhoneNumber = whatsappPhoneNumber;
        return this;
    }
}
