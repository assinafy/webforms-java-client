package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/** Document added to an envelope or supplied during creation. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class EnvelopeDocumentPayload {

    @JsonProperty("id")
    private String id;

    @JsonProperty("method")
    private String method;

    @JsonProperty("entries")
    private List<CollectAssignmentEntry> entries;

    @JsonProperty("signer_ids")
    private List<String> signerIds;

    @JsonProperty("expires_at")
    private String expiresAt;

    @JsonProperty("copy_receiver_ids")
    private List<String> copyReceiverIds;

    @JsonProperty("max_total_credits")
    private BigDecimal maxTotalCredits;

    /** Creates an empty model. */
    public EnvelopeDocumentPayload() {}

    /**
     * Returns metadata-ready document identifier.
     * @return metadata-ready document identifier
     */
    public String getId() { return id; }

    /**
     * Sets metadata-ready document identifier.
     * @param value metadata-ready document identifier
     * @return this model
     */
    public EnvelopeDocumentPayload setId(String value) { id = value; return this; }

    /**
     * Returns virtual (default) or collect.
     * @return virtual (default) or collect
     */
    public String getMethod() { return method; }

    /**
     * Sets virtual (default) or collect.
     * @param value virtual (default) or collect
     * @return this model
     */
    public EnvelopeDocumentPayload setMethod(String value) { method = value; return this; }

    /**
     * Returns collect field placements.
     * @return collect field placements
     */
    public List<CollectAssignmentEntry> getEntries() { return entries; }

    /**
     * Sets collect field placements.
     * @param value collect field placements
     * @return this model
     */
    public EnvelopeDocumentPayload setEntries(List<CollectAssignmentEntry> value) { entries = value; return this; }

    /**
     * Returns distinct envelope signer identifiers.
     * @return distinct envelope signer identifiers
     */
    public List<String> getSignerIds() { return signerIds; }

    /**
     * Sets distinct envelope signer identifiers.
     * @param value distinct envelope signer identifiers
     * @return this model
     */
    public EnvelopeDocumentPayload setSignerIds(List<String> value) { signerIds = value; return this; }

    /**
     * Returns ISO 8601 expiration or Unix seconds as text.
     * @return ISO 8601 expiration or Unix seconds as text
     */
    public String getExpiresAt() { return expiresAt; }

    /**
     * Sets ISO 8601 expiration or Unix seconds as text.
     * @param value ISO 8601 expiration or Unix seconds as text
     * @return this model
     */
    public EnvelopeDocumentPayload setExpiresAt(String value) { expiresAt = value; return this; }

    /**
     * Returns copy receiver identifiers.
     * @return copy receiver identifiers
     */
    public List<String> getCopyReceiverIds() { return copyReceiverIds; }

    /**
     * Sets copy receiver identifiers.
     * @param value copy receiver identifiers
     * @return this model
     */
    public EnvelopeDocumentPayload setCopyReceiverIds(List<String> value) { copyReceiverIds = value; return this; }

    /**
     * Returns spending ceiling when adding a document.
     * @return spending ceiling when adding a document
     */
    public BigDecimal getMaxTotalCredits() { return maxTotalCredits; }

    /**
     * Sets spending ceiling when adding a document.
     * @param value spending ceiling when adding a document
     * @return this model
     */
    public EnvelopeDocumentPayload setMaxTotalCredits(BigDecimal value) { maxTotalCredits = value; return this; }
}
