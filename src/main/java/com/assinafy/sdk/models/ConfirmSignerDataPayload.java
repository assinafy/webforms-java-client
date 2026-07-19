package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Body for {@code PUT /documents/{document_id}/signers/confirm-data} — the signer confirms/updates their
 * identifying data before signing. All fields are optional; only the ones set are sent.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ConfirmSignerDataPayload {

    @JsonProperty("full_name")
    private String fullName;

    private String email;

    @JsonProperty("government_id")
    private String governmentId;

    public String getFullName() {
        return fullName;
    }

    public ConfirmSignerDataPayload setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public ConfirmSignerDataPayload setEmail(String email) {
        this.email = email;
        return this;
    }

    /** Government identifier (e.g. CPF in Brazil). */
    public String getGovernmentId() {
        return governmentId;
    }

    public ConfirmSignerDataPayload setGovernmentId(String governmentId) {
        this.governmentId = governmentId;
        return this;
    }
}
