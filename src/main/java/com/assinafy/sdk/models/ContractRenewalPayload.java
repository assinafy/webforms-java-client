package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Optional values sent when starting a tracked contract's next term. */
public final class ContractRenewalPayload {

    private final Map<String, Object> properties = new LinkedHashMap<>();

    /** Creates an empty renewal that advances by the configured term. */
    public ContractRenewalPayload() {}

    /**
     * Returns the exact wire properties accumulated by the setters.
     *
     * @return immutable wire-property map
     */
    @JsonAnyGetter
    public Map<String, Object> getProperties() { return Collections.unmodifiableMap(properties); }

    /**
     * Sets an explicit next end date.
     *
     * @param endDate explicit new end date, or {@code null} to advance by the configured term
     * @return this body
     */
    public ContractRenewalPayload setEndDate(String endDate) { return put("end_date", endDate); }

    /**
     * Sets or clears the next term's value.
     *
     * @param value new contract value, or {@code null} to clear it
     * @return this body
     */
    public ContractRenewalPayload setValue(BigDecimal value) { return put("value", value); }

    /**
     * Sets the expected current term.
     *
     * @param term current term used to make the renewal idempotent
     * @return this body
     */
    public ContractRenewalPayload setTerm(Integer term) { return put("term", term); }

    /**
     * Sets the optimistic-concurrency timestamp.
     *
     * @param updatedAt last-read ISO 8601 timestamp used as a write precondition
     * @return this body
     */
    public ContractRenewalPayload setUpdatedAt(String updatedAt) { return put("updated_at", updatedAt); }

    private ContractRenewalPayload put(String key, Object value) {
        properties.put(key, value);
        return this;
    }
}
