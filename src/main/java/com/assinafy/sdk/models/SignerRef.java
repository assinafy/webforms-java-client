package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Signer reference accepted by assignment requests. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class SignerRef {

    private String id;

    @JsonProperty("verification_method")
    private String verificationMethod;

    @JsonProperty("notification_methods")
    private List<String> notificationMethods;

    private Integer step;

    /** Creates an empty signer reference. */
    public SignerRef() {}

    /**
     * Creates a reference containing only the required signer ID.
     *
     * @param id signer identifier
     * @return a new signer reference
     */
    public static SignerRef of(String id) {
        SignerRef ref = new SignerRef();
        ref.id = id;
        return ref;
    }

    /**
     * Returns required signer identifier.
     *
     * @return required signer identifier
     */
    public String getId() { return id; }

    /**
     * Sets required signer identifier.
     *
     * @param id required signer identifier
     * @return this reference
     */
    public SignerRef setId(String id) { this.id = id; return this; }

    /**
     * Returns wire {@code verification_method}, or {@code null} for the API default.
     *
     * @return wire {@code verification_method}, or {@code null} for the API default
     */
    public String getVerificationMethod() { return verificationMethod; }

    /**
     * Sets optional wire {@code verification_method}.
     *
     * @param verificationMethod optional wire {@code verification_method}
     * @return this reference
     */
    public SignerRef setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
        return this;
    }

    /**
     * Returns wire {@code notification_methods}, or {@code null} for the API default.
     *
     * @return wire {@code notification_methods}, or {@code null} for the API default
     */
    public List<String> getNotificationMethods() { return notificationMethods; }

    /**
     * Sets optional wire {@code notification_methods}.
     *
     * @param notificationMethods optional wire {@code notification_methods}
     * @return this reference
     */
    public SignerRef setNotificationMethods(List<String> notificationMethods) {
        this.notificationMethods = notificationMethods;
        return this;
    }

    /**
     * Returns sequential signing step, or {@code null} for step {@code 1}.
     *
     * @return sequential signing step, or {@code null} for step {@code 1}
     */
    public Integer getStep() { return step; }

    /**
     * Sets optional positive sequential signing step.
     *
     * @param step optional positive sequential signing step
     * @return this reference
     */
    public SignerRef setStep(Integer step) {
        this.step = step;
        return this;
    }
}
