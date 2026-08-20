package com.assinafy.sdk.exceptions;

/** Wraps I/O failures and malformed/unparseable API responses. */
public class NetworkException extends AssinafyException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an instance.
     *
     * @param message transport or response-parsing failure description
     */
    public NetworkException(String message) {
        super(message);
    }

    /**
     * Creates an instance.
     *
     * @param message transport or response-parsing failure description
     * @param cause underlying I/O or parsing cause
     */
    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
