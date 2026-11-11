package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Partial envelope update sent to {@code PATCH .../envelopes/{envelopeId}}.
 * Calling a setter includes that property even when its value is {@code null}, which clears it where the API
 * permits. Uncalled setters remain absent and leave the stored value unchanged.
 */
public final class UpdateEnvelopePayload {

    private final Map<String, Object> properties = new LinkedHashMap<>();

    /** Creates an empty partial envelope update. */
    public UpdateEnvelopePayload() {}

    /**
     * Returns the exact wire properties accumulated by the setters.
     *
     * @return immutable wire-property map, including explicitly supplied nulls
     */
    @JsonAnyGetter
    public Map<String, Object> getProperties() { return Collections.unmodifiableMap(properties); }

    /**
     * Returns whether no envelope property has been supplied.
     *
     * @return whether the update is empty
     */
    @JsonIgnore
    public boolean isEmpty() { return properties.isEmpty(); }

    /**
     * Sets the invitation name.
     * @param name non-empty name, at most 255 characters
     * @return this input
     */
    public UpdateEnvelopePayload setName(String name) { return put("name", name); }

    /**
     * Sets or clears the signer message.
     * @param message message, or null to clear
     * @return this input
     */
    public UpdateEnvelopePayload setMessage(String message) { return put("message", message); }

    /**
     * Reorders every document exactly once.
     * @param documentIds complete document order
     * @return this input
     */
    public UpdateEnvelopePayload setDocumentIds(List<String> documentIds) {
        return put("document_ids", documentIds);
    }

    private UpdateEnvelopePayload put(String key, Object value) {
        properties.put(key, value);
        return this;
    }
}
