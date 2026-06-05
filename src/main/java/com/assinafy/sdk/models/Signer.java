package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Signer {

    private String id;
    private String resource;

    @JsonProperty("full_name")
    private String fullName;

    private String email;

    @JsonProperty("whatsapp_phone_number")
    private String whatsappPhoneNumber;

    @JsonProperty("has_accepted_terms")
    private Boolean hasAcceptedTerms;

    @JsonProperty("has_signature")
    private Boolean hasSignature;

    @JsonProperty("has_initial")
    private Boolean hasInitial;

    @JsonProperty("verification_method")
    private String verificationMethod;

    @JsonProperty("notification_methods")
    private List<String> notificationMethods;

    private Integer step;

    private Boolean notified;

    private Boolean completed;

    @JsonProperty("notification_history")
    private List<AssignmentSignerNotification> notificationHistory;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWhatsappPhoneNumber() {
        return whatsappPhoneNumber;
    }

    public void setWhatsappPhoneNumber(String whatsappPhoneNumber) {
        this.whatsappPhoneNumber = whatsappPhoneNumber;
    }

    public Boolean getHasAcceptedTerms() {
        return hasAcceptedTerms;
    }

    public void setHasAcceptedTerms(Boolean hasAcceptedTerms) {
        this.hasAcceptedTerms = hasAcceptedTerms;
    }

    public Boolean getHasSignature() {
        return hasSignature;
    }

    public void setHasSignature(Boolean hasSignature) {
        this.hasSignature = hasSignature;
    }

    public Boolean getHasInitial() {
        return hasInitial;
    }

    public void setHasInitial(Boolean hasInitial) {
        this.hasInitial = hasInitial;
    }

    public String getVerificationMethod() {
        return verificationMethod;
    }

    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    public List<String> getNotificationMethods() {
        return notificationMethods;
    }

    public void setNotificationMethods(List<String> notificationMethods) {
        this.notificationMethods = notificationMethods;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    /**
     * Step the signer belongs to in the sequential signing flow. Signers in the same step sign in parallel;
     * the next step activates once every signer in the previous step has signed. Only populated inside
     * {@code assignment.signers}; {@code null} for legacy records (new assignments default to {@code 1}).
     */
    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    /**
     * {@code true} once the signature-request notification has been dispatched to this signer; {@code false}
     * for signers in steps not yet activated. {@code null} for legacy records. Only populated inside
     * {@code assignment.signers}.
     */
    public Boolean getNotified() {
        return notified;
    }

    public void setNotified(Boolean notified) {
        this.notified = notified;
    }

    /**
     * Tracked notification-delivery history for this signer within an assignment. Only present in
     * account-owner contexts (inside {@code assignment.signers}); {@code null} otherwise. May be an empty
     * list when the configured channel does not persist delivery history.
     */
    public List<AssignmentSignerNotification> getNotificationHistory() {
        return notificationHistory;
    }

    public void setNotificationHistory(List<AssignmentSignerNotification> notificationHistory) {
        this.notificationHistory = notificationHistory;
    }

}
