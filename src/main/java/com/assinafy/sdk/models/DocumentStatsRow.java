package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonProperty("signature_requests_notification_email")
    @JsonAlias("signature_requests_email")
    private int signatureRequestsNotificationEmail;

    @JsonProperty("signature_requests_notification_whatsapp")
    @JsonAlias("signature_requests_whatsapp")
    private int signatureRequestsNotificationWhatsapp;

    @JsonProperty("signature_requests_notification_bypass")
    private int signatureRequestsNotificationBypass;

    @JsonProperty("signature_requests_verification_email")
    private int signatureRequestsVerificationEmail;

    @JsonProperty("signature_requests_verification_whatsapp")
    private int signatureRequestsVerificationWhatsapp;

    @JsonProperty("signature_requests_verification_bypass")
    private int signatureRequestsVerificationBypass;

    @JsonProperty("signature_requests_verification_digital_certificate")
    private int signatureRequestsVerificationDigitalCertificate;

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
     * Returns email notification requests.
     *
     * @return number of {@code signature_requests_notification_email}
     */
    public int getSignatureRequestsNotificationEmail() { return signatureRequestsNotificationEmail; }

    /**
     * Sets email notification requests.
     *
     * @param value value of {@code signature_requests_notification_email}
     */
    public void setSignatureRequestsNotificationEmail(int value) { this.signatureRequestsNotificationEmail = value; }

    /**
     * Returns WhatsApp notification requests.
     *
     * @return number of {@code signature_requests_notification_whatsapp}
     */
    public int getSignatureRequestsNotificationWhatsapp() { return signatureRequestsNotificationWhatsapp; }

    /**
     * Sets WhatsApp notification requests.
     *
     * @param value value of {@code signature_requests_notification_whatsapp}
     */
    public void setSignatureRequestsNotificationWhatsapp(int value) {
        this.signatureRequestsNotificationWhatsapp = value;
    }

    /**
     * Returns requests for which notification was bypassed.
     *
     * @return number of {@code signature_requests_notification_bypass}
     */
    public int getSignatureRequestsNotificationBypass() { return signatureRequestsNotificationBypass; }

    /**
     * Sets requests for which notification was bypassed.
     *
     * @param value value of {@code signature_requests_notification_bypass}
     */
    public void setSignatureRequestsNotificationBypass(int value) {
        this.signatureRequestsNotificationBypass = value;
    }

    /**
     * Returns email verification requests.
     *
     * @return number of {@code signature_requests_verification_email}
     */
    public int getSignatureRequestsVerificationEmail() { return signatureRequestsVerificationEmail; }

    /**
     * Sets email verification requests.
     *
     * @param value value of {@code signature_requests_verification_email}
     */
    public void setSignatureRequestsVerificationEmail(int value) { this.signatureRequestsVerificationEmail = value; }

    /**
     * Returns WhatsApp verification requests.
     *
     * @return number of {@code signature_requests_verification_whatsapp}
     */
    public int getSignatureRequestsVerificationWhatsapp() { return signatureRequestsVerificationWhatsapp; }

    /**
     * Sets WhatsApp verification requests.
     *
     * @param value value of {@code signature_requests_verification_whatsapp}
     */
    public void setSignatureRequestsVerificationWhatsapp(int value) {
        this.signatureRequestsVerificationWhatsapp = value;
    }

    /**
     * Returns requests for which verification was bypassed.
     *
     * @return number of {@code signature_requests_verification_bypass}
     */
    public int getSignatureRequestsVerificationBypass() { return signatureRequestsVerificationBypass; }

    /**
     * Sets requests for which verification was bypassed.
     *
     * @param value value of {@code signature_requests_verification_bypass}
     */
    public void setSignatureRequestsVerificationBypass(int value) {
        this.signatureRequestsVerificationBypass = value;
    }

    /**
     * Returns digital-certificate verification requests.
     *
     * @return number of {@code signature_requests_verification_digital_certificate}
     */
    public int getSignatureRequestsVerificationDigitalCertificate() {
        return signatureRequestsVerificationDigitalCertificate;
    }

    /**
     * Sets digital-certificate verification requests.
     *
     * @param value value of {@code signature_requests_verification_digital_certificate}
     */
    public void setSignatureRequestsVerificationDigitalCertificate(int value) {
        this.signatureRequestsVerificationDigitalCertificate = value;
    }

    /**
     * Returns email notification requests through the concise accessor.
     *
     * @return number of {@code signature_requests_notification_email}
     */
    @JsonIgnore
    public int getSignatureRequestsEmail() { return signatureRequestsNotificationEmail; }

    /**
     * Sets email notification requests through the concise accessor.
     *
     * @param value value of {@code signature_requests_notification_email}
     */
    @JsonIgnore
    public void setSignatureRequestsEmail(int value) { this.signatureRequestsNotificationEmail = value; }

    /**
     * Returns WhatsApp notification requests through the concise accessor.
     *
     * @return number of {@code signature_requests_notification_whatsapp}
     */
    @JsonIgnore
    public int getSignatureRequestsWhatsapp() { return signatureRequestsNotificationWhatsapp; }

    /**
     * Sets WhatsApp notification requests through the concise accessor.
     *
     * @param value value of {@code signature_requests_notification_whatsapp}
     */
    @JsonIgnore
    public void setSignatureRequestsWhatsapp(int value) { this.signatureRequestsNotificationWhatsapp = value; }

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
