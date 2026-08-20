package com.assinafy.sdk.exceptions;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Thrown before a request when caller-supplied data cannot satisfy the SDK/API contract. */
public class ValidationException extends AssinafyException {

    private static final long serialVersionUID = 1L;

    /** Immutable field-specific errors. */
    private final transient Map<String, Object> errors;

    /**
     * Creates a validation failure without field-specific errors.
     *
     * @param message validation failure description
     */
    public ValidationException(String message) {
        super(message);
        this.errors = Collections.emptyMap();
    }

    /**
     * Creates a validation failure while retaining the underlying parsing or serialization cause.
     *
     * @param message validation failure description
     * @param cause underlying cause
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errors = Collections.emptyMap();
    }

    /**
     * Creates a validation failure with immutable field-specific error details.
     *
     * @param message validation failure description
     * @param errors field-specific error map, or {@code null}
     */
    public ValidationException(String message, Map<String, Object> errors) {
        super(message);
        this.errors = errors != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(errors))
                : Collections.emptyMap();
    }

    /**
     * Returns immutable field-specific errors, never {@code null}.
     *
     * @return immutable field-specific errors, never {@code null}
     */
    public Map<String, Object> getErrors() {
        return errors != null ? errors : Collections.emptyMap();
    }
}
