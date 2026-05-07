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

    private Boolean completed;

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

}
