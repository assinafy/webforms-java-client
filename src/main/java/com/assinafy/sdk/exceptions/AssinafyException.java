package com.assinafy.sdk.exceptions;

public class AssinafyException extends RuntimeException {

    public AssinafyException(String message) {
        super(message);
    }

    public AssinafyException(String message, Throwable cause) {
        super(message, cause);
    }
}
