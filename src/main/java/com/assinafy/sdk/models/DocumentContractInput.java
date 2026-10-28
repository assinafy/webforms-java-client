package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Partial contract terms or amendment link sent to {@code PUT .../documents/{documentId}/contract}.
 * Calling a setter includes that property even when its value is {@code null}, which clears it where the API
 * permits. Uncalled setters remain absent and leave the stored value unchanged.
 */
public final class DocumentContractInput {

    private final Map<String, Object> properties = new LinkedHashMap<>();

    /** Creates an empty partial contract update. */
    public DocumentContractInput() {}

    /**
     * Returns the exact wire properties accumulated by the setters.
     *
     * @return immutable wire-property map, including explicitly supplied nulls
     */
    @JsonAnyGetter
    public Map<String, Object> getProperties() { return Collections.unmodifiableMap(properties); }

    /**
     * Returns whether no contract property has been supplied.
     *
     * @return whether the update is empty
     */
    @JsonIgnore
    public boolean isEmpty() { return properties.isEmpty(); }

    /**
     * Sets the contract lifecycle status.
     *
     * @param status {@code draft}, {@code active}, {@code expired}, or {@code terminated}
     * @return this input
     */
    public DocumentContractInput setStatus(String status) { return put("status", status); }

    /**
     * Sets or clears the counterparty name.
     *
     * @param name counterparty name, or {@code null} to clear it
     * @return this input
     */
    public DocumentContractInput setCounterpartyName(String name) { return put("counterparty_name", name); }

    /**
     * Sets or clears the counterparty CPF/CNPJ.
     *
     * @param taxpayerId counterparty CPF/CNPJ, or {@code null} to clear it
     * @return this input
     */
    public DocumentContractInput setCounterpartyTaxpayerId(String taxpayerId) {
        return put("counterparty_taxpayer_id", taxpayerId);
    }

    /**
     * Sets or clears the monetary value.
     *
     * @param value monetary value, or {@code null} to clear it
     * @return this input
     */
    public DocumentContractInput setValue(BigDecimal value) { return put("value", value); }

    /**
     * Sets or clears the ISO 4217 currency.
     *
     * @param currency ISO 4217 currency, or {@code null} for the API default
     * @return this input
     */
    public DocumentContractInput setCurrency(String currency) { return put("currency", currency); }

    /**
     * Sets or clears the start date.
     *
     * @param startDate start date as {@code YYYY-MM-DD}, or {@code null} to clear it
     * @return this input
     */
    public DocumentContractInput setStartDate(String startDate) { return put("start_date", startDate); }

    /**
     * Sets or clears the end date.
     *
     * @param endDate end date as {@code YYYY-MM-DD}, or {@code null} to clear it
     * @return this input
     */
    public DocumentContractInput setEndDate(String endDate) { return put("end_date", endDate); }

    /**
     * Enables or disables automatic renewal.
     *
     * @param autoRenew whether the hourly lifecycle renews after the end date
     * @return this input
     */
    public DocumentContractInput setAutoRenew(boolean autoRenew) { return put("auto_renew", autoRenew); }

    /**
     * Sets or clears the renewal length.
     *
     * @param months renewal length, or {@code null} for the API default
     * @return this input
     */
    public DocumentContractInput setRenewalTermMonths(Integer months) {
        return put("renewal_term_months", months);
    }

    /**
     * Sets or clears the notice-period length.
     *
     * @param days notice-period length, or {@code null} to remove it
     * @return this input
     */
    public DocumentContractInput setNoticePeriodDays(Integer days) { return put("notice_period_days", days); }

    /**
     * Sets or clears the contract owner.
     *
     * @param ownerId active workspace member ID, or {@code null} to clear it
     * @return this input
     */
    public DocumentContractInput setOwnerId(String ownerId) { return put("owner_id", ownerId); }

    /**
     * Sets or clears application metadata.
     *
     * @param metadata application metadata, or {@code null} to clear it
     * @return this input
     */
    public DocumentContractInput setMetadata(Map<String, String> metadata) { return put("metadata", metadata); }

    /**
     * Replaces or clears the external references.
     *
     * @param references complete replacement list; empty or {@code null} clears it
     * @return this input
     */
    public DocumentContractInput setExternalReferences(List<ContractExternalReference> references) {
        return put("external_references", references);
    }

    /**
     * Links or unlinks this document as a contract instrument.
     *
     * @param documentId contract document to amend, or {@code null} to unlink a pending amendment
     * @return this input
     */
    public DocumentContractInput setParentDocumentId(String documentId) {
        return put("parent_document_id", documentId);
    }

    /**
     * Sets the instrument relationship.
     *
     * @param relationType {@code amendment}, {@code renewal}, or {@code termination}
     * @return this input
     */
    public DocumentContractInput setRelationType(String relationType) {
        return put("relation_type", relationType);
    }

    /**
     * Sets the optimistic-concurrency timestamp.
     *
     * @param updatedAt last-read ISO 8601 timestamp used as a write precondition
     * @return this input
     */
    public DocumentContractInput setUpdatedAt(String updatedAt) { return put("updated_at", updatedAt); }

    private DocumentContractInput put(String key, Object value) {
        properties.put(key, value);
        return this;
    }
}
