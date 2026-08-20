package com.assinafy.sdk.exceptions;

/** Base unchecked exception for SDK validation, API, and transport failures. */
public class AssinafyException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an instance.
     *
     * @param message failure description
     */
    public AssinafyException(String message) {
        super(message);
    }

    /**
     * Creates an instance.
     *
     * @param message failure description
     * @param cause underlying cause
     */
    public AssinafyException(String message, Throwable cause) {
        super(message, cause);
    }
}
