package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Body for {@code PUT /documents/{document_id}/signers/confirm-data} — the signer confirms/updates their
 * identifying data before signing. All fields are optional; only the ones set are sent.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ConfirmSignerDataPayload {

    /** Creates an empty signer-data payload. */
    public ConfirmSignerDataPayload() {}

    @JsonProperty("full_name")
    private String fullName;

    private String email;

    @JsonProperty("government_id")
    private String governmentId;

    /**
     * Returns the optional {@code full_name}.
     *
     * @return the optional {@code full_name}
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets signer name serialized as {@code full_name}.
     *
     * @param fullName signer name serialized as {@code full_name}
     * @return this payload
     */
    public ConfirmSignerDataPayload setFullName(String fullName) {
        this.fullName = fullName;
        return this;
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
     * Sets signer email address.
     *
     * @param email signer email address
     * @return this payload
     */
    public ConfirmSignerDataPayload setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Returns the optional {@code government_id}, such as a Brazilian CPF.
     *
     * @return the optional {@code government_id}, such as a Brazilian CPF
     */
    public String getGovernmentId() {
        return governmentId;
    }

    /**
     * Sets government identifier serialized as {@code government_id}.
     *
     * @param governmentId government identifier serialized as {@code government_id}
     * @return this payload
     */
    public ConfirmSignerDataPayload setGovernmentId(String governmentId) {
        this.governmentId = governmentId;
        return this;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("whatsapp_phone_number")
    private String whatsappPhoneNumber;

    /**
     * Returns signer WhatsApp contact.
     * @return signer WhatsApp contact
     */
    public String getWhatsappPhoneNumber() { return whatsappPhoneNumber; }

    /**
     * Sets signer WhatsApp contact.
     * @param value signer WhatsApp contact
     * @return this payload
     */
    public ConfirmSignerDataPayload setWhatsappPhoneNumber(String value) {
        whatsappPhoneNumber = value; return this;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("has_accepted_terms")
    private Boolean hasAcceptedTerms;

    /**
     * Returns explicit signer terms acceptance.
     * @return explicit signer terms acceptance
     */
    public Boolean getHasAcceptedTerms() { return hasAcceptedTerms; }

    /**
     * Sets explicit signer terms acceptance.
     * @param value explicit signer terms acceptance
     * @return this payload
     */
    public ConfirmSignerDataPayload setHasAcceptedTerms(Boolean value) {
        hasAcceptedTerms = value; return this;
    }
}
