package com.assinafy.sdk.models;

import java.util.List;

/** Persistent resources produced by the high-level upload-and-request workflow. */
public final class UploadAndRequestSignaturesResult {

    private final DocumentDetails document;
    private final Assignment assignment;
    private final List<String> signerIds;

    /**
     * Creates a workflow result from its persistent resources.
     *
     * @param document uploaded document
     * @param assignment created virtual assignment
     * @param signerIds created or reused signer IDs in request order
     */
    public UploadAndRequestSignaturesResult(DocumentDetails document, Assignment assignment,
            List<String> signerIds) {
        this.document = document;
        this.assignment = assignment;
        this.signerIds = List.copyOf(signerIds);
    }

    /**
     * Returns ready document, or the upload response when waiting was disabled.
     *
     * @return ready document, or the upload response when waiting was disabled
     */
    public DocumentDetails getDocument() { return document; }

    /**
     * Returns created virtual assignment.
     *
     * @return created virtual assignment
     */
    public Assignment getAssignment() { return assignment; }

    /**
     * Returns signer IDs in request order.
     *
     * @return signer IDs in request order
     */
    public List<String> getSignerIds() { return signerIds; }
}
