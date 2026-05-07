package com.assinafy.sdk.exceptions;

public class ApiException extends AssinafyException {

    private final int statusCode;
    private final String responseBody;

    public ApiException(int statusCode, String apiMessage, String responseBody) {
        super(buildMessage(statusCode, apiMessage));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public ApiException(int statusCode) {
        this(statusCode, null, null);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    private static String buildMessage(int statusCode, String apiMessage) {
        if (apiMessage != null && !apiMessage.isBlank()) {
            return apiMessage;
        }
        return "API request failed with status " + statusCode;
    }
}
