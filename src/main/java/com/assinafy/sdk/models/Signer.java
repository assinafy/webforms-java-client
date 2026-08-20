package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Signer response. Assignment and signer-self endpoints add contextual verification, delivery, and signature
 * state fields to the base signer properties.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Signer {

    private String id;
    private String resource;

    @JsonProperty("full_name")
    private String fullName;

    private String email;

    @JsonProperty("whatsapp_phone_number")
    private String whatsappPhoneNumber;

    @JsonProperty("government_id")
    private String governmentId;

    @JsonProperty("has_accepted_terms")
    private Boolean hasAcceptedTerms;

    @JsonProperty("has_signature")
    private Boolean hasSignature;

    @JsonProperty("has_initial")
    private Boolean hasInitial;

    @JsonProperty("is_signature_reusable")
    private Boolean signatureReusable;

    @JsonProperty("verification_method")
    private String verificationMethod;

    @JsonProperty("notification_methods")
    private List<String> notificationMethods;

    private Integer step;

    private Boolean notified;

    private Boolean completed;

    @JsonProperty("notification_history")
    private List<AssignmentSignerNotification> notificationHistory;

    /** Creates an empty signer model for JSON deserialization. */
    public Signer() {}

    /**
     * Returns signer identifier.
     *
     * @return signer identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Sets signer identifier.
     *
     * @param id signer identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns resource discriminator, normally {@code signer}.
     *
     * @return resource discriminator, normally {@code signer}
     */
    public String getResource() {
        return resource;
    }

    /**
     * Sets resource discriminator.
     *
     * @param resource resource discriminator
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * Returns wire {@code full_name}.
     *
     * @return wire {@code full_name}
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets wire {@code full_name}.
     *
     * @param fullName wire {@code full_name}
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Returns email address, or {@code null}.
     *
     * @return email address, or {@code null}
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets email address.
     *
     * @param email email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns wire {@code whatsapp_phone_number} in E.164 form, or {@code null}.
     *
     * @return wire {@code whatsapp_phone_number} in E.164 form, or {@code null}
     */
    public String getWhatsappPhoneNumber() {
        return whatsappPhoneNumber;
    }

    /**
     * Sets wire {@code whatsapp_phone_number}.
     *
     * @param whatsappPhoneNumber wire {@code whatsapp_phone_number}
     */
    public void setWhatsappPhoneNumber(String whatsappPhoneNumber) {
        this.whatsappPhoneNumber = whatsappPhoneNumber;
    }

    /**
     * Returns wire {@code government_id} CPF/CNPJ, or {@code null}.
     *
     * @return wire {@code government_id} CPF/CNPJ, or {@code null}
     */
    public String getGovernmentId() {
        return governmentId;
    }

    /**
     * Sets wire {@code government_id} CPF/CNPJ.
     *
     * @param governmentId wire {@code government_id} CPF/CNPJ
     */
    public void setGovernmentId(String governmentId) {
        this.governmentId = governmentId;
    }

    /**
     * Returns wire {@code has_accepted_terms}, or {@code null} outside signer-facing contexts.
     *
     * @return wire {@code has_accepted_terms}, or {@code null} outside signer-facing contexts
     */
    public Boolean getHasAcceptedTerms() {
        return hasAcceptedTerms;
    }

    /**
     * Sets wire {@code has_accepted_terms}.
     *
     * @param hasAcceptedTerms wire {@code has_accepted_terms}
     */
    public void setHasAcceptedTerms(Boolean hasAcceptedTerms) {
        this.hasAcceptedTerms = hasAcceptedTerms;
    }

    /**
     * Returns wire {@code has_signature}, populated by {@code GET /signers/self}.
     *
     * @return wire {@code has_signature}, populated by {@code GET /signers/self}
     */
    public Boolean getHasSignature() {
        return hasSignature;
    }

    /**
     * Sets wire {@code has_signature}.
     *
     * @param hasSignature wire {@code has_signature}
     */
    public void setHasSignature(Boolean hasSignature) {
        this.hasSignature = hasSignature;
    }

    /**
     * Returns wire {@code has_initial}, populated by {@code GET /signers/self}.
     *
     * @return wire {@code has_initial}, populated by {@code GET /signers/self}
     */
    public Boolean getHasInitial() {
        return hasInitial;
    }

    /**
     * Sets wire {@code has_initial}.
     *
     * @param hasInitial wire {@code has_initial}
     */
    public void setHasInitial(Boolean hasInitial) {
        this.hasInitial = hasInitial;
    }

    /**
     * Returns whether the saved signature may be reused. When {@code false}, the signer must draw a new one.
     *
     * @return wire {@code is_signature_reusable}, or {@code null} outside signer-self responses
     */
    public Boolean getSignatureReusable() {
        return signatureReusable;
    }

    /**
     * Sets wire {@code is_signature_reusable}.
     *
     * @param signatureReusable wire {@code is_signature_reusable}
     */
    public void setSignatureReusable(Boolean signatureReusable) {
        this.signatureReusable = signatureReusable;
    }

    /**
     * Returns assignment verification method, or {@code null} outside assignment contexts.
     *
     * @return assignment verification method, or {@code null} outside assignment contexts
     */
    public String getVerificationMethod() {
        return verificationMethod;
    }

    /**
     * Sets wire {@code verification_method}.
     *
     * @param verificationMethod wire {@code verification_method}
     */
    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    /**
     * Returns assignment notification method codes, or {@code null} outside assignment contexts.
     *
     * @return assignment notification method codes, or {@code null} outside assignment contexts
     */
    public List<String> getNotificationMethods() {
        return notificationMethods;
    }

    /**
     * Sets wire {@code notification_methods}.
     *
     * @param notificationMethods wire {@code notification_methods}
     */
    public void setNotificationMethods(List<String> notificationMethods) {
        this.notificationMethods = notificationMethods;
    }

    /**
     * Returns whether this signer completed the assignment, or {@code null} when not contextual.
     *
     * @return whether this signer completed the assignment, or {@code null} when not contextual
     */
    public Boolean getCompleted() {
        return completed;
    }

    /**
     * Sets assignment completion state.
     *
     * @param completed assignment completion state
     */
    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    /**
     * Returns the sequential signing step. Signers in one step sign in parallel before the next step activates.
     *
     * @return the step, or {@code null} outside assignment contexts
     */
    public Integer getStep() {
        return step;
    }

    /**
     * Sets sequential signing step.
     *
     * @param step sequential signing step
     */
    public void setStep(Integer step) {
        this.step = step;
    }

    /**
     * Returns whether the active-step invitation was dispatched.
     *
     * @return the dispatch state, or {@code null} outside assignment contexts
     */
    public Boolean getNotified() {
        return notified;
    }

    /**
     * Sets invitation dispatch state.
     *
     * @param notified invitation dispatch state
     */
    public void setNotified(Boolean notified) {
        this.notified = notified;
    }

    /**
     * Returns tracked delivery attempts for this signer within an assignment.
     *
     * @return delivery history, or {@code null} outside account-owner assignment responses
     */
    public List<AssignmentSignerNotification> getNotificationHistory() {
        return notificationHistory;
    }

    /**
     * Sets wire {@code notification_history}.
     *
     * @param notificationHistory wire {@code notification_history}
     */
    public void setNotificationHistory(List<AssignmentSignerNotification> notificationHistory) {
        this.notificationHistory = notificationHistory;
    }

}
