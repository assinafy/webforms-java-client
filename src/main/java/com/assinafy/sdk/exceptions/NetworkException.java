package com.assinafy.sdk.exceptions;

public class NetworkException extends AssinafyException {

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
