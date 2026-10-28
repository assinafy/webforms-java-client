package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Maps a template role to an optional existing signer and assignment settings. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TemplateSigner {

    @JsonProperty("role_id")
    private final String roleId;

    private final String id;

    private String email;
    private String name;

    @JsonProperty("whatsapp_phone_number")
    private String whatsappPhoneNumber;

    @JsonProperty("verification_method")
    private String verificationMethod;

    @JsonProperty("notification_methods")
    private List<String> notificationMethods;

    private Integer step;

    /**
     * Creates a template signer that will be resolved for the supplied role.
     *
     * @param roleId required template role identifier
     */
    public TemplateSigner(String roleId) {
        this(roleId, null);
    }

    /**
     * Creates a template signer mapped to an existing signer.
     *
     * @param roleId required template role identifier
     * @param id optional existing signer identifier
     */
    public TemplateSigner(String roleId, String id) {
        this.roleId = roleId;
        this.id = id;
    }

    /**
     * Returns required wire {@code role_id}.
     *
     * @return required wire {@code role_id}
     */
    public String getRoleId() { return roleId; }

    /**
     * Returns existing signer ID, or {@code null}.
     *
     * @return existing signer ID, or {@code null}
     */
    public String getId() { return id; }

    /**
     * Returns the email used to find or create a signer when {@link #getId()} is absent.
     *
     * @return signer email, or {@code null}
     */
    public String getEmail() { return email; }

    /**
     * Sets the email used to find or create a signer when no ID is supplied.
     *
     * @param email signer email
     * @return this mapping
     */
    public TemplateSigner setEmail(String email) { this.email = email; return this; }

    /**
     * Returns the full name used when the email does not match an existing account signer.
     *
     * @return signer full name, or {@code null}
     */
    public String getName() { return name; }

    /**
     * Sets the full name used when the email does not match an existing account signer.
     *
     * @param name signer full name
     * @return this mapping
     */
    public TemplateSigner setName(String name) { this.name = name; return this; }

    /**
     * Returns the WhatsApp number used when creating a signer by email.
     *
     * @return WhatsApp number, or {@code null}
     */
    public String getWhatsappPhoneNumber() { return whatsappPhoneNumber; }

    /**
     * Sets the WhatsApp number used when creating a signer by email.
     *
     * @param whatsappPhoneNumber WhatsApp number
     * @return this mapping
     */
    public TemplateSigner setWhatsappPhoneNumber(String whatsappPhoneNumber) {
        this.whatsappPhoneNumber = whatsappPhoneNumber;
        return this;
    }

    /**
     * Returns wire {@code verification_method}, or {@code null}.
     *
     * @return wire {@code verification_method}, or {@code null}
     */
    public String getVerificationMethod() { return verificationMethod; }

    /**
     * Sets optional wire {@code verification_method}: Email, Whatsapp, DigitalCertificate, or
     * IdDocumentFaceMatchBasic.
     *
     * @param verificationMethod optional wire {@code verification_method}
     * @return this mapping
     */
    public TemplateSigner setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
        return this;
    }

    /**
     * Returns wire {@code notification_methods}, or {@code null}.
     *
     * @return wire {@code notification_methods}, or {@code null}
     */
    public List<String> getNotificationMethods() { return notificationMethods; }

    /**
     * Sets optional wire {@code notification_methods}.
     *
     * @param notificationMethods optional wire {@code notification_methods}
     * @return this mapping
     */
    public TemplateSigner setNotificationMethods(List<String> notificationMethods) {
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
     * @return this mapping
     */
    public TemplateSigner setStep(Integer step) {
        this.step = step;
        return this;
    }
}
