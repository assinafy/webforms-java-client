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
}
