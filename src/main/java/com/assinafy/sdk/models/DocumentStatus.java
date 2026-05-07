package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentStatus {

    private String code;
    private Boolean deletable;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Boolean getDeletable() { return deletable; }
    public void setDeletable(Boolean deletable) { this.deletable = deletable; }
}
