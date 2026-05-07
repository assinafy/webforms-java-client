package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreateAssignmentPayload {

    private String method;
    private List<SignerRef> signers;

    @JsonProperty("signer_ids")
    private List<String> signerIds;

    private String message;

    @JsonProperty("expires_at")
    private String expiresAt;

    @JsonProperty("copy_receivers")
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
