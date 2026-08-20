package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** One monthly ({@code YYYY-MM}) or daily ({@code YYYY-MM-DD}) document KPI period. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentStatsRow {

    /** Creates an empty response model for deserialization. */
    public DocumentStatsRow() {}

    private String period;

    @JsonProperty("documents_uploaded")
    private int documentsUploaded;

    @JsonProperty("documents_sent")
    private int documentsSent;

    @JsonProperty("signature_requests")
    private int signatureRequests;

    @JsonProperty("signature_requests_email")
    private int signatureRequestsEmail;

    @JsonProperty("signature_requests_whatsapp")
    private int signatureRequestsWhatsapp;

    @JsonProperty("signature_requests_viewed")
    private int signatureRequestsViewed;

    @JsonProperty("signature_requests_completed")
    private int signatureRequestsCompleted;

    @JsonProperty("documents_certified")
    private int documentsCertified;

    /**
     * Returns {@code YYYY-MM} for monthly data or {@code YYYY-MM-DD} for daily data.
     *
     * @return {@code YYYY-MM} for monthly data or {@code YYYY-MM-DD} for daily data
     */
    public String getPeriod() { return period; }

    /**
     * Sets monthly or daily period label.
     *
     * @param period monthly or daily period label
     */
    public void setPeriod(String period) { this.period = period; }

    /**
     * Returns number of {@code documents_uploaded}.
     *
     * @return number of {@code documents_uploaded}
     */
    public int getDocumentsUploaded() { return documentsUploaded; }

    /**
     * Sets value of {@code documents_uploaded}.
     *
     * @param documentsUploaded value of {@code documents_uploaded}
     */
    public void setDocumentsUploaded(int documentsUploaded) { this.documentsUploaded = documentsUploaded; }

    /**
     * Returns number of {@code documents_sent}.
     *
     * @return number of {@code documents_sent}
     */
    public int getDocumentsSent() { return documentsSent; }

    /**
     * Sets value of {@code documents_sent}.
     *
     * @param documentsSent value of {@code documents_sent}
     */
    public void setDocumentsSent(int documentsSent) { this.documentsSent = documentsSent; }

    /**
     * Returns total {@code signature_requests}.
     *
     * @return total {@code signature_requests}
     */
    public int getSignatureRequests() { return signatureRequests; }

    /**
     * Sets value of {@code signature_requests}.
     *
     * @param signatureRequests value of {@code signature_requests}
     */
    public void setSignatureRequests(int signatureRequests) { this.signatureRequests = signatureRequests; }

    /**
     * Returns number of {@code signature_requests_email}.
     *
     * @return number of {@code signature_requests_email}
     */
    public int getSignatureRequestsEmail() { return signatureRequestsEmail; }

    /**
     * Sets value of {@code signature_requests_email}.
     *
     * @param value value of {@code signature_requests_email}
     */
    public void setSignatureRequestsEmail(int value) { this.signatureRequestsEmail = value; }

    /**
     * Returns number of {@code signature_requests_whatsapp}.
     *
     * @return number of {@code signature_requests_whatsapp}
     */
    public int getSignatureRequestsWhatsapp() { return signatureRequestsWhatsapp; }

    /**
     * Sets value of {@code signature_requests_whatsapp}.
     *
     * @param value value of {@code signature_requests_whatsapp}
     */
    public void setSignatureRequestsWhatsapp(int value) { this.signatureRequestsWhatsapp = value; }

    /**
     * Returns signature requests first viewed during the period.
     *
     * @return signature requests first viewed during the period
     */
    public int getSignatureRequestsViewed() { return signatureRequestsViewed; }

    /**
     * Sets value of {@code signature_requests_viewed}.
     *
     * @param value value of {@code signature_requests_viewed}
     */
    public void setSignatureRequestsViewed(int value) { this.signatureRequestsViewed = value; }

    /**
     * Returns individual signer requests completed during the period.
     *
     * @return individual signer requests completed during the period
     */
    public int getSignatureRequestsCompleted() { return signatureRequestsCompleted; }

    /**
     * Sets value of {@code signature_requests_completed}.
     *
     * @param value value of {@code signature_requests_completed}
     */
    public void setSignatureRequestsCompleted(int value) { this.signatureRequestsCompleted = value; }

    /**
     * Returns number of {@code documents_certified}.
     *
     * @return number of {@code documents_certified}
     */
    public int getDocumentsCertified() { return documentsCertified; }

    /**
     * Sets value of {@code documents_certified}.
     *
     * @param documentsCertified value of {@code documents_certified}
     */
    public void setDocumentsCertified(int documentsCertified) { this.documentsCertified = documentsCertified; }
}
