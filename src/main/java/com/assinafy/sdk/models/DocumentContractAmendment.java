package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/** A signed or pending instrument that changes another document's contract terms. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentContractAmendment implements DocumentContractResult {

    private String resource;

    @JsonProperty("document_id")
    private String documentId;

    @JsonProperty("document_name")
    private String documentName;

    @JsonProperty("parent_document_id")
    private String parentDocumentId;

    @JsonProperty("relation_type")
    private String relationType;

    private Integer number;
    private String status;

    @JsonProperty("document_status")
    private String documentStatus;

    @JsonProperty("apply_error")
    private String applyError;

    private String currency;
    private Map<String, Object> terms;
    private Map<String, Object> changes;

    @JsonProperty("applied_at")
    private String appliedAt;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    /** Creates an empty amendment model for JSON deserialization. */
    public DocumentContractAmendment() {}

    /**
     * Returns the resource discriminator.
     *
     * @return resource discriminator, normally {@code document_contract_amendment}
     */
    public String getResource() { return resource; }
    /**
     * Sets the resource discriminator.
     *
     * @param resource resource discriminator
     */
    public void setResource(String resource) { this.resource = resource; }

    /**
     * Returns the instrument document identifier.
     *
     * @return instrument document identifier
     */
    public String getDocumentId() { return documentId; }
    /**
     * Sets the instrument document identifier.
     *
     * @param documentId instrument document identifier
     */
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    /**
     * Returns the instrument document name.
     *
     * @return instrument document name
     */
    public String getDocumentName() { return documentName; }
    /**
     * Sets the instrument document name.
     *
     * @param documentName instrument document name
     */
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    /**
     * Returns the root contract document identifier.
     *
     * @return root contract document identifier
     */
    public String getParentDocumentId() { return parentDocumentId; }
    /**
     * Sets the root contract document identifier.
     *
     * @param parentDocumentId root contract document identifier
     */
    public void setParentDocumentId(String parentDocumentId) { this.parentDocumentId = parentDocumentId; }

    /**
     * Returns the relationship to the parent contract.
     *
     * @return {@code amendment}, {@code renewal}, or {@code termination}
     */
    public String getRelationType() { return relationType; }
    /**
     * Sets the relationship to the parent contract.
     *
     * @param relationType relationship to the parent contract
     */
    public void setRelationType(String relationType) { this.relationType = relationType; }

    /**
     * Returns the applied sequence number.
     *
     * @return applied sequence number, or {@code null} while pending
     */
    public Integer getNumber() { return number; }
    /**
     * Sets the applied sequence number.
     *
     * @param number applied sequence number
     */
    public void setNumber(Integer number) { this.number = number; }

    /**
     * Returns the amendment status.
     *
     * @return {@code pending} or {@code applied}
     */
    public String getStatus() { return status; }
    /**
     * Sets the amendment status.
     *
     * @param status amendment status
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Returns the instrument document's lifecycle status.
     *
     * @return lifecycle status of the instrument document
     */
    public String getDocumentStatus() { return documentStatus; }
    /**
     * Sets the instrument document's lifecycle status.
     *
     * @param documentStatus lifecycle status of the instrument document
     */
    public void setDocumentStatus(String documentStatus) { this.documentStatus = documentStatus; }

    /**
     * Returns why a signed instrument has not applied.
     *
     * @return reason a signed instrument has not applied, or {@code null}
     */
    public String getApplyError() { return applyError; }
    /**
     * Sets why a signed instrument has not applied.
     *
     * @param applyError reason a signed instrument has not applied
     */
    public void setApplyError(String applyError) { this.applyError = applyError; }

    /**
     * Returns the currency of monetary changes.
     *
     * @return currency of monetary values in {@link #getTerms()} and {@link #getChanges()}
     */
    public String getCurrency() { return currency; }
    /**
     * Sets the currency of monetary changes.
     *
     * @param currency currency of monetary changes
     */
    public void setCurrency(String currency) { this.currency = currency; }

    /**
     * Returns the terms changed by the instrument.
     *
     * @return terms changed by the instrument, or {@code null}
     */
    public Map<String, Object> getTerms() { return terms; }
    /**
     * Sets the terms changed by the instrument.
     *
     * @param terms terms changed by the instrument
     */
    public void setTerms(Map<String, Object> terms) { this.terms = terms; }

    /**
     * Returns the parent fields moved by the instrument.
     *
     * @return parent fields moved as {@code {from,to}}, or {@code null}
     */
    public Map<String, Object> getChanges() { return changes; }
    /**
     * Sets the parent fields moved by the instrument.
     *
     * @param changes parent fields moved as {@code {from,to}}
     */
    public void setChanges(Map<String, Object> changes) { this.changes = changes; }

    /**
     * Returns the application time.
     *
     * @return ISO 8601 application time, or {@code null} while pending
     */
    public String getAppliedAt() { return appliedAt; }
    /**
     * Sets the application time.
     *
     * @param appliedAt ISO 8601 application time
     */
    public void setAppliedAt(String appliedAt) { this.appliedAt = appliedAt; }

    /**
     * Returns the creation time.
     *
     * @return ISO 8601 creation time
     */
    public String getCreatedAt() { return createdAt; }
    /**
     * Sets the creation time.
     *
     * @param createdAt ISO 8601 creation time
     */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    /**
     * Returns the last-change time.
     *
     * @return ISO 8601 last-change time used for optimistic concurrency
     */
    public String getUpdatedAt() { return updatedAt; }
    /**
     * Sets the last-change time.
     *
     * @param updatedAt ISO 8601 last-change time
     */
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
