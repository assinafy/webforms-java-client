package com.assinafy.sdk.models;

import java.util.List;

public final class UploadAndRequestSignaturesResult {

    private final DocumentDetails document;
    private final Assignment assignment;
    private final List<String> signerIds;

    public UploadAndRequestSignaturesResult(DocumentDetails document, Assignment assignment,
            List<String> signerIds) {
        this.document = document;
        this.assignment = assignment;
        this.signerIds = signerIds;
    }

    public DocumentDetails getDocument() { return document; }
    public Assignment getAssignment() { return assignment; }
    public List<String> getSignerIds() { return signerIds; }
}
