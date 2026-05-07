package com.assinafy.sdk.exceptions;

import java.util.Collections;
import java.util.Map;

public class ValidationException extends AssinafyException {

    private final Map<String, Object> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = Collections.emptyMap();
    }

    public ValidationException(String message, Map<String, Object> errors) {
        super(message);
        this.errors = errors != null ? Collections.unmodifiableMap(errors) : Collections.emptyMap();
    }

    public Map<String, Object> getErrors() {
        return errors;
    }
}
