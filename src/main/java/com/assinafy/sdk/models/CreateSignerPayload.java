package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Request body for creating an account signer. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreateSignerPayload {

    @JsonProperty("full_name")
    private final String fullName;

    private final String email;

    @JsonProperty("whatsapp_phone_number")
    private String whatsappPhoneNumber;

    @JsonProperty("government_id")
    private String governmentId;

    /**
     * Creates a signer without an email; set a WhatsApp number when that is the contact channel.
     *
     * @param fullName signer name serialized as {@code full_name}
     */
    public CreateSignerPayload(String fullName) {
        this(fullName, null);
    }

    /**
     * Creates a signer with an email address.
     *
     * @param fullName signer name serialized as {@code full_name}
     * @param email signer email address, or {@code null}
     */
    public CreateSignerPayload(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }

    /**
     * Returns the required signer name serialized as {@code full_name}.
     *
     * @return the required signer name serialized as {@code full_name}
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns the optional signer email address.
     *
     * @return the optional signer email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the optional E.164 {@code whatsapp_phone_number}.
     *
     * @return the optional E.164 {@code whatsapp_phone_number}
     */
    public String getWhatsappPhoneNumber() {
        return whatsappPhoneNumber;
    }

    /**
     * Sets E.164 number serialized as {@code whatsapp_phone_number}.
     *
     * @param whatsappPhoneNumber E.164 number serialized as {@code whatsapp_phone_number}
     * @return this payload
     */
    public CreateSignerPayload setWhatsappPhoneNumber(String whatsappPhoneNumber) {
        this.whatsappPhoneNumber = whatsappPhoneNumber;
        return this;
    }

    /**
     * Returns the optional CPF serialized as {@code government_id}.
     *
     * @return the optional CPF serialized as {@code government_id}
     */
    public String getGovernmentId() {
        return governmentId;
    }

    /**
     * Sets the signer's CPF, serialized as {@code government_id}.
     *
     * @param governmentId signer CPF
     * @return this payload
     */
    public CreateSignerPayload setGovernmentId(String governmentId) {
        this.governmentId = governmentId;
        return this;
    }
}
