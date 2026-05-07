package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class SignerRef {

    private String id;

    @JsonProperty("verification_method")
    private String verificationMethod;

    @JsonProperty("notification_methods")
    private List<String> notificationMethods;

    public SignerRef() {}

    public static SignerRef of(String id) {
        SignerRef ref = new SignerRef();
        ref.id = id;
        return ref;
    }

    public String getId() { return id; }
    public SignerRef setId(String id) { this.id = id; return this; }

    public String getVerificationMethod() { return verificationMethod; }
    public SignerRef setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
        return this;
    }

    public List<String> getNotificationMethods() { return notificationMethods; }
    public SignerRef setNotificationMethods(List<String> notificationMethods) {
        this.notificationMethods = notificationMethods;
        return this;
    }
}
