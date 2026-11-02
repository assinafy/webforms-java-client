package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Partial signer update. A {@code null} property is omitted; an empty email, WhatsApp number, or government
 * identifier clears that property.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class UpdateSignerPayload {

    @JsonProperty("full_name")
    private String fullName;

    private String email;

    @JsonProperty("whatsapp_phone_number")
    private String whatsappPhoneNumber;

    @JsonProperty("government_id")
    private String governmentId;

    /** Creates an empty partial signer update. */
    public UpdateSignerPayload() {}

    /**
     * Returns wire {@code full_name}, or {@code null} when omitted.
     *
     * @return wire {@code full_name}, or {@code null} when omitted
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets optional wire {@code full_name}; this property cannot be cleared.
     *
     * @param fullName optional non-empty wire {@code full_name}
     * @return this payload
     */
    public UpdateSignerPayload setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    /**
     * Returns email address, or {@code null} when omitted.
     *
     * @return email address, or {@code null} when omitted
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets an optional email address, or an empty string to clear it.
     *
     * @param email optional email address; empty clears it
     * @return this payload
     */
    public UpdateSignerPayload setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Returns wire {@code whatsapp_phone_number}, or {@code null} when omitted.
     *
     * @return wire {@code whatsapp_phone_number}, or {@code null} when omitted
     */
    public String getWhatsappPhoneNumber() {
        return whatsappPhoneNumber;
    }

    /**
     * Sets optional E.164 wire {@code whatsapp_phone_number}, or an empty string to clear it.
     *
     * @param whatsappPhoneNumber optional E.164 wire {@code whatsapp_phone_number}; empty clears it
     * @return this payload
     */
    public UpdateSignerPayload setWhatsappPhoneNumber(String whatsappPhoneNumber) {
        this.whatsappPhoneNumber = whatsappPhoneNumber;
        return this;
    }

    /**
     * Returns wire {@code government_id}, or {@code null} when omitted.
     *
     * @return wire {@code government_id}, or {@code null} when omitted
     */
    public String getGovernmentId() {
        return governmentId;
    }

    /**
     * Sets the signer's CPF/CNPJ, or an empty string to clear it; the API normalizes identifiers on save.
     *
     * @param governmentId optional wire {@code government_id}; empty clears it
     * @return this payload
     */
    public UpdateSignerPayload setGovernmentId(String governmentId) {
        this.governmentId = governmentId;
        return this;
    }
}
