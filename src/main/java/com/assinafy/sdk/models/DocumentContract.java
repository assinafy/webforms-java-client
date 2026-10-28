package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Contract terms tracked on a document. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentContract implements DocumentContractResult {

    private String resource;

    @JsonProperty("document_id")
    private String documentId;

    private String status;

    @JsonProperty("counterparty_name")
    private String counterpartyName;

    @JsonProperty("counterparty_taxpayer_id")
    private String counterpartyTaxpayerId;

    private BigDecimal value;
    private String currency;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("auto_renew")
    private boolean autoRenew;

    @JsonProperty("renewal_term_months")
    private Integer renewalTermMonths;

    @JsonProperty("notice_period_days")
    private Integer noticePeriodDays;

    private int term;

    @JsonProperty("owner_id")
    private String ownerId;

    private Map<String, String> metadata;

    @JsonProperty("external_references")
    private List<ContractExternalReference> externalReferences;

    private DocumentDetails document;
    private List<DocumentContractAmendment> amendments;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    /** Creates an empty contract model for JSON deserialization. */
    public DocumentContract() {}

    /**
     * Returns the resource discriminator.
     *
     * @return resource discriminator, normally {@code document_contract}
     */
    public String getResource() { return resource; }
    /**
     * Sets the resource discriminator.
     *
     * @param resource resource discriminator
     */
    public void setResource(String resource) { this.resource = resource; }

    /**
     * Returns the document that owns these contract terms.
     *
     * @return document that owns these contract terms
     */
    public String getDocumentId() { return documentId; }
    /**
     * Sets the document that owns these contract terms.
     *
     * @param documentId document that owns these contract terms
     */
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    /**
     * Returns the contract lifecycle status.
     *
     * @return {@code draft}, {@code active}, {@code expired}, or {@code terminated}
     */
    public String getStatus() { return status; }
    /**
     * Sets the contract lifecycle status.
     *
     * @param status contract lifecycle status
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Returns the counterparty name.
     *
     * @return counterparty name, or {@code null}
     */
    public String getCounterpartyName() { return counterpartyName; }
    /**
     * Sets the counterparty name.
     *
     * @param counterpartyName counterparty name
     */
    public void setCounterpartyName(String counterpartyName) { this.counterpartyName = counterpartyName; }

    /**
     * Returns the counterparty CPF/CNPJ.
     *
     * @return counterparty CPF/CNPJ as digits, or {@code null}
     */
    public String getCounterpartyTaxpayerId() { return counterpartyTaxpayerId; }
    /**
     * Sets the counterparty CPF/CNPJ.
     *
     * @param counterpartyTaxpayerId counterparty CPF/CNPJ
     */
    public void setCounterpartyTaxpayerId(String counterpartyTaxpayerId) {
        this.counterpartyTaxpayerId = counterpartyTaxpayerId;
    }

    /**
     * Returns the monetary contract value.
     *
     * @return contract value, or {@code null}
     */
    public BigDecimal getValue() { return value; }
    /**
     * Sets the monetary contract value.
     *
     * @param value contract value
     */
    public void setValue(BigDecimal value) { this.value = value; }

    /**
     * Returns the currency.
     *
     * @return ISO 4217 currency code, or {@code null}
     */
    public String getCurrency() { return currency; }
    /**
     * Sets the currency.
     *
     * @param currency ISO 4217 currency code
     */
    public void setCurrency(String currency) { this.currency = currency; }

    /**
     * Returns the start date.
     *
     * @return {@code start_date} as {@code YYYY-MM-DD}, or {@code null}
     */
    public String getStartDate() { return startDate; }
    /**
     * Sets the start date.
     *
     * @param startDate {@code start_date} as {@code YYYY-MM-DD}
     */
    public void setStartDate(String startDate) { this.startDate = startDate; }

    /**
     * Returns the end date.
     *
     * @return {@code end_date} as {@code YYYY-MM-DD}, or {@code null}
     */
    public String getEndDate() { return endDate; }
    /**
     * Sets the end date.
     *
     * @param endDate {@code end_date} as {@code YYYY-MM-DD}
     */
    public void setEndDate(String endDate) { this.endDate = endDate; }

    /**
     * Returns whether the hourly lifecycle renews the contract.
     *
     * @return whether the hourly lifecycle renews the contract after its end date
     */
    public boolean isAutoRenew() { return autoRenew; }
    /**
     * Sets whether the hourly lifecycle renews the contract.
     *
     * @param autoRenew whether the hourly lifecycle renews the contract
     */
    public void setAutoRenew(boolean autoRenew) { this.autoRenew = autoRenew; }

    /**
     * Returns the renewal length.
     *
     * @return months added by a term renewal, or {@code null} for the API default
     */
    public Integer getRenewalTermMonths() { return renewalTermMonths; }
    /**
     * Sets the renewal length.
     *
     * @param renewalTermMonths months added by a term renewal
     */
    public void setRenewalTermMonths(Integer renewalTermMonths) { this.renewalTermMonths = renewalTermMonths; }

    /**
     * Returns the notice-period length.
     *
     * @return days before the end date when the notice period starts, or {@code null}
     */
    public Integer getNoticePeriodDays() { return noticePeriodDays; }
    /**
     * Sets the notice-period length.
     *
     * @param noticePeriodDays notice-period length in days
     */
    public void setNoticePeriodDays(Integer noticePeriodDays) { this.noticePeriodDays = noticePeriodDays; }

    /**
     * Returns the current contract term.
     *
     * @return current contract term, starting at {@code 1}
     */
    public int getTerm() { return term; }
    /**
     * Sets the current contract term.
     *
     * @param term current contract term
     */
    public void setTerm(int term) { this.term = term; }

    /**
     * Returns the contract owner.
     *
     * @return workspace member who owns the contract, or {@code null}
     */
    public String getOwnerId() { return ownerId; }
    /**
     * Sets the contract owner.
     *
     * @param ownerId workspace member who owns the contract
     */
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    /**
     * Returns application metadata.
     *
     * @return application metadata, or {@code null}
     */
    public Map<String, String> getMetadata() { return metadata; }
    /**
     * Sets application metadata.
     *
     * @param metadata application metadata
     */
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }

    /**
     * Returns the external references.
     *
     * @return identifiers for this contract in external systems
     */
    public List<ContractExternalReference> getExternalReferences() { return externalReferences; }
    /**
     * Sets the external references.
     *
     * @param externalReferences identifiers for this contract in external systems
     */
    public void setExternalReferences(List<ContractExternalReference> externalReferences) {
        this.externalReferences = externalReferences;
    }

    /**
     * Returns the expanded document.
     *
     * @return embedded document when requested with {@code expand=document}, otherwise {@code null}
     */
    public DocumentDetails getDocument() { return document; }
    /**
     * Sets the expanded document.
     *
     * @param document embedded document
     */
    public void setDocument(DocumentDetails document) { this.document = document; }

    /**
     * Returns the related instruments.
     *
     * @return amendment, renewal, and termination instruments on a single-contract read
     */
    public List<DocumentContractAmendment> getAmendments() { return amendments; }
    /**
     * Sets the related instruments.
     *
     * @param amendments amendment, renewal, and termination instruments
     */
    public void setAmendments(List<DocumentContractAmendment> amendments) { this.amendments = amendments; }

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
