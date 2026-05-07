package com.assinafy.sdk.models;

public final class UploadAndRequestSignaturesSigner {

    private final String name;
    private final String email;
    private String whatsappPhoneNumber;

    public UploadAndRequestSignaturesSigner(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }

    public String getWhatsappPhoneNumber() { return whatsappPhoneNumber; }
    public UploadAndRequestSignaturesSigner setWhatsappPhoneNumber(String whatsappPhoneNumber) {
        this.whatsappPhoneNumber = whatsappPhoneNumber;
        return this;
    }
}
