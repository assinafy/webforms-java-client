package com.assinafy.sdk.models;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Options for creating an assignment (or estimating its cost). This is a builder-style holder; the resource
 * serializes it into the wire body ({@code method}, {@code signers[]}, {@code message}, {@code expires_at},
 * {@code copy_receivers}, {@code entries}) itself, so no Jackson field mapping is needed here.
 */
public final class CreateAssignmentPayload {

    private String method;
    private List<SignerRef> signers;
    private List<String> signerIds;
    private String message;
    private String expiresAt;
    private List<String> copyReceivers;
    private List<Object> entries;

    public String getMethod() { return method; }
    public CreateAssignmentPayload setMethod(String method) { this.method = method; return this; }

    public List<SignerRef> getSigners() { return signers; }
    public CreateAssignmentPayload setSigners(List<SignerRef> signers) { this.signers = signers; return this; }

    public CreateAssignmentPayload setSignerStrings(List<String> ids) {
        this.signers = ids == null ? null : ids.stream().map(SignerRef::of).collect(Collectors.toList());
        return this;
    }

    public CreateAssignmentPayload setSignerStrings(String... ids) {
        return setSignerStrings(Arrays.asList(ids));
    }

    public List<String> getSignerIds() { return signerIds; }
    public CreateAssignmentPayload setSignerIds(List<String> signerIds) { this.signerIds = signerIds; return this; }

    public String getMessage() { return message; }
    public CreateAssignmentPayload setMessage(String message) { this.message = message; return this; }

    public String getExpiresAt() { return expiresAt; }
    public CreateAssignmentPayload setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; return this; }

    public List<String> getCopyReceivers() { return copyReceivers; }
    public CreateAssignmentPayload setCopyReceivers(List<String> copyReceivers) { this.copyReceivers = copyReceivers; return this; }

    public List<Object> getEntries() { return entries; }
    public CreateAssignmentPayload setEntries(List<Object> entries) { this.entries = entries; return this; }

    public List<SignerRef> resolveSignerRefs() {
        if (signers != null && !signers.isEmpty()) {
            return signers;
        }
        if (signerIds != null && !signerIds.isEmpty()) {
            return signerIds.stream().map(SignerRef::of).collect(Collectors.toList());
        }
        return List.of();
    }
}
