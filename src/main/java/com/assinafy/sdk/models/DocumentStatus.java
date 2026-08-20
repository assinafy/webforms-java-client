package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** A document lifecycle status and whether documents in that state may be deleted. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentStatus {

    /** Creates an empty response model for deserialization. */
    public DocumentStatus() {}

    private String code;
    private Boolean deletable;

    /**
     * Returns the status code, such as {@code metadata_ready}.
     *
     * @return the status code, such as {@code metadata_ready}
     */
    public String getCode() { return code; }

    /**
     * Sets document status code.
     *
     * @param code document status code
     */
    public void setCode(String code) { this.code = code; }

    /**
     * Returns whether a document in this status can be deleted.
     *
     * @return whether a document in this status can be deleted
     */
    public Boolean getDeletable() { return deletable; }

    /**
     * Sets status deletion capability.
     *
     * @param deletable status deletion capability
     */
    public void setDeletable(Boolean deletable) { this.deletable = deletable; }
}
