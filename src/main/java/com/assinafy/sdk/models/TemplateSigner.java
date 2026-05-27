package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TemplateSigner {

    @JsonProperty("role_id")
    private final String roleId;

    private final String id;

    @JsonProperty("verification_method")
    private String verificationMethod;

    @JsonProperty("notification_methods")
    private List<String> notificationMethods;

    private Integer step;

    public TemplateSigner(String roleId) {
        this(roleId, null);
    }

    public TemplateSigner(String roleId, String id) {
        this.roleId = roleId;
        this.id = id;
    }

    public String getRoleId() { return roleId; }
    public String getId() { return id; }

    public String getVerificationMethod() { return verificationMethod; }
    public TemplateSigner setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
        return this;
    }

    public List<String> getNotificationMethods() { return notificationMethods; }
    public TemplateSigner setNotificationMethods(List<String> notificationMethods) {
        this.notificationMethods = notificationMethods;
        return this;
    }

    public Integer getStep() { return step; }
    public TemplateSigner setStep(Integer step) {
        this.step = step;
        return this;
    }
}
