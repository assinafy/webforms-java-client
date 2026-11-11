package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/** Documents and signers sent together. An envelope has no signing steps. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreateEnvelopePayload {

    @JsonProperty("name")
    private String name;

    @JsonProperty("message")
    private String message;

    @JsonProperty("max_total_credits")
    private BigDecimal maxTotalCredits;

    @JsonProperty("signers")
    private List<SignerRef> signers;

    @JsonProperty("documents")
    private List<EnvelopeDocumentPayload> documents;

    /** Creates an empty model. */
    public CreateEnvelopePayload() {}

    /**
     * Returns invitation name, 1–255 characters without a less-than sign.
     * @return invitation name, 1–255 characters without a less-than sign
     */
    public String getName() { return name; }

    /**
     * Sets invitation name, 1–255 characters without a less-than sign.
     * @param value invitation name, 1–255 characters without a less-than sign
     * @return this model
     */
    public CreateEnvelopePayload setName(String value) { name = value; return this; }

    /**
     * Returns optional signer message.
     * @return optional signer message
     */
    public String getMessage() { return message; }

    /**
     * Sets optional signer message.
     * @param value optional signer message
     * @return this model
     */
    public CreateEnvelopePayload setMessage(String value) { message = value; return this; }

    /**
     * Returns non-negative spending ceiling.
     * @return non-negative spending ceiling
     */
    public BigDecimal getMaxTotalCredits() { return maxTotalCredits; }

    /**
     * Sets non-negative spending ceiling.
     * @param value non-negative spending ceiling
     * @return this model
     */
    public CreateEnvelopePayload setMaxTotalCredits(BigDecimal value) { maxTotalCredits = value; return this; }

    /**
     * Returns 1–20 distinct existing signers, without steps.
     * @return 1–20 distinct existing signers, without steps
     */
    public List<SignerRef> getSigners() { return signers; }

    /**
     * Sets 1–20 distinct existing signers, without steps.
     * @param value 1–20 distinct existing signers, without steps
     * @return this model
     */
    public CreateEnvelopePayload setSigners(List<SignerRef> value) { signers = value; return this; }

    /**
     * Returns 1–20 documents in invitation order.
     * @return 1–20 documents in invitation order
     */
    public List<EnvelopeDocumentPayload> getDocuments() { return documents; }

    /**
     * Sets 1–20 documents in invitation order.
     * @param value 1–20 documents in invitation order
     * @return this model
     */
    public CreateEnvelopePayload setDocuments(List<EnvelopeDocumentPayload> value) { documents = value; return this; }
}
