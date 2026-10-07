package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The authenticated user's enrolled two-factor methods.
 *
 * @param methods enrolled methods
 * @param recoveryCodesRemaining wire {@code recovery_codes_remaining}: unused recovery codes left
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MfaMethods(
        List<Method> methods,
        @JsonProperty("recovery_codes_remaining") int recoveryCodesRemaining) {

    /**
     * Normalises an absent method list to an empty one.
     *
     * @param methods enrolled methods, or {@code null}
     * @param recoveryCodesRemaining unused recovery codes left
     */
    public MfaMethods {
        methods = methods == null ? List.of() : List.copyOf(methods);
    }

    /**
     * One enrolled two-factor method.
     *
     * @param id method identifier, used to remove it
     * @param type method type, such as {@code Totp}
     * @param label user-chosen label, or {@code null}
     * @param confirmedAt wire {@code confirmed_at} ISO-8601 timestamp, or {@code null} while unconfirmed
     * @param lastUsedAt wire {@code last_used_at} ISO-8601 timestamp, or {@code null} before first use
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Method(
            String id,
            String type,
            String label,
            @JsonProperty("confirmed_at") String confirmedAt,
            @JsonProperty("last_used_at") String lastUsedAt) {}
}
