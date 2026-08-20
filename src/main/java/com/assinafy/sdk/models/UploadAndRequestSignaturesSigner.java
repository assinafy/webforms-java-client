package com.assinafy.sdk.models;

import com.assinafy.sdk.exceptions.ValidationException;

import java.util.regex.Pattern;

/** A signer created or reused by the high-level upload-and-request workflow. */
public final class UploadAndRequestSignaturesSigner {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final String name;
    private final String email;
    private String whatsappPhoneNumber;

    /**
     * Creates a signer definition for this helper's email-based virtual assignment.
     *
     * @param name required signer full name
     * @param email required signer email
     * @throws ValidationException when the name or email is invalid
     */
    public UploadAndRequestSignaturesSigner(String name, String email) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Signer name is required");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid signer email address");
        }
        this.name = name;
        this.email = email;
    }

    /**
     * Returns signer full name.
     *
     * @return signer full name
     */
    public String getName() { return name; }

    /**
     * Returns required signer email.
     *
     * @return required signer email
     */
    public String getEmail() { return email; }

    /**
     * Returns optional E.164 WhatsApp number.
     *
     * @return optional E.164 WhatsApp number
     */
    public String getWhatsappPhoneNumber() { return whatsappPhoneNumber; }

    /**
     * Sets optional E.164 WhatsApp number.
     *
     * @param whatsappPhoneNumber optional E.164 WhatsApp number
     * @return this signer definition
     */
    public UploadAndRequestSignaturesSigner setWhatsappPhoneNumber(String whatsappPhoneNumber) {
        this.whatsappPhoneNumber = whatsappPhoneNumber;
        return this;
    }
}
