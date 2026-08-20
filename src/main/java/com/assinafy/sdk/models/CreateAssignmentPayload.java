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

    /** Creates an empty assignment payload. */
    public CreateAssignmentPayload() {}

    private String method;
    private List<SignerRef> signers;
    private List<String> signerIds;
    private String message;
    private String expiresAt;
    private List<String> copyReceivers;
    private List<?> entries;

    /**
     * Returns {@code virtual}, {@code collect}, or {@code null} for the default {@code virtual}.
     *
     * @return {@code virtual}, {@code collect}, or {@code null} for the default {@code virtual}
     */
    public String getMethod() { return method; }

    /**
     * Sets assignment method: {@code virtual} or {@code collect}.
     *
     * @param method assignment method: {@code virtual} or {@code collect}
     * @return this payload
     */
    public CreateAssignmentPayload setMethod(String method) { this.method = method; return this; }

    /**
     * Returns structured signer references, or {@code null} when legacy signer IDs are set.
     *
     * @return structured signer references, or {@code null} when legacy signer IDs are set
     */
    public List<SignerRef> getSigners() { return signers; }

    /**
     * Sets structured {@code signers} and clears values set through {@link #setSignerIds(List)}.
     *
     * @param signers signer references
     * @return this payload
     */
    public CreateAssignmentPayload setSigners(List<SignerRef> signers) {
        this.signers = signers;
        this.signerIds = null;
        return this;
    }

    /**
     * Converts signer identifiers to {@link SignerRef} values.
     *
     * @param ids signer identifiers
     * @return this payload
     */
    public CreateAssignmentPayload setSignerStrings(List<String> ids) {
        return setSigners(ids == null ? null : ids.stream().map(SignerRef::of).collect(Collectors.toList()));
    }

    /**
     * Converts signer identifiers to {@link SignerRef} values.
     *
     * @param ids signer identifiers
     * @return this payload
     */
    public CreateAssignmentPayload setSignerStrings(String... ids) {
        return setSignerStrings(ids == null ? null : Arrays.asList(ids));
    }

    /**
     * Returns legacy signer IDs, or {@code null} when structured signer references are set.
     *
     * @return legacy signer IDs, or {@code null} when structured signer references are set
     */
    public List<String> getSignerIds() { return signerIds; }

    /**
     * Sets legacy signer IDs and clears values set through {@link #setSigners(List)}.
     *
     * @param signerIds signer identifiers
     * @return this payload
     */
    public CreateAssignmentPayload setSignerIds(List<String> signerIds) {
        this.signerIds = signerIds;
        this.signers = null;
        return this;
    }

    /**
     * Returns the optional signer message.
     *
     * @return the optional signer message
     */
    public String getMessage() { return message; }

    /**
     * Sets message sent to signers.
     *
     * @param message message sent to signers
     * @return this payload
     */
    public CreateAssignmentPayload setMessage(String message) { this.message = message; return this; }

    /**
     * Returns optional ISO 8601 {@code expires_at} value.
     *
     * @return optional ISO 8601 {@code expires_at} value
     */
    public String getExpiresAt() { return expiresAt; }

    /**
     * Sets ISO 8601 expiration serialized as {@code expires_at}.
     *
     * @param expiresAt ISO 8601 expiration serialized as {@code expires_at}
     * @return this payload
     */
    public CreateAssignmentPayload setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; return this; }

    /**
     * Returns optional signer IDs serialized as {@code copy_receivers}.
     *
     * @return optional signer IDs serialized as {@code copy_receivers}
     */
    public List<String> getCopyReceivers() { return copyReceivers; }

    /**
     * Sets signer IDs that receive completed-document copies.
     *
     * @param copyReceivers signer IDs that receive completed-document copies
     * @return this payload
     */
    public CreateAssignmentPayload setCopyReceivers(List<String> copyReceivers) { this.copyReceivers = copyReceivers; return this; }

    /**
     * Returns collect page entries; prefer {@link CollectAssignmentEntry} over raw maps.
     *
     * @return collect page entries; prefer {@link CollectAssignmentEntry} over raw maps
     */
    public List<?> getEntries() { return entries; }

    /**
     * Sets collect page entries, normally a {@code List<CollectAssignmentEntry>}.
     *
     * @param entries collect page entries
     * @return this payload
     */
    public CreateAssignmentPayload setEntries(List<?> entries) { this.entries = entries; return this; }

    /**
     * Sets strongly typed collect-assignment page entries.
     *
     * @param entries collect page entries
     * @return this payload
     */
    public CreateAssignmentPayload setCollectEntries(List<CollectAssignmentEntry> entries) {
        return setEntries(entries);
    }

    /**
     * Resolves either structured references or legacy signer IDs into the wire-ready signer list.
     *
     * @return signer references, or an empty list when none are configured
     */
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
