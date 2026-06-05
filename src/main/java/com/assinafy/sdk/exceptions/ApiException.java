package com.assinafy.sdk.exceptions;

/**
 * Thrown when the Assinafy API returns an error, either via a non-2xx HTTP status or via an error envelope
 * ({@code {"status": >=400, "message": "...", "data": null}}) returned with any HTTP status.
 *
 * <p>The {@link #getStatusCode() status code} reflects the envelope {@code status} when present, otherwise the
 * HTTP status. {@link #getMessage()} surfaces the server-provided {@code message} when available, and
 * {@link #getResponseBody()} carries the raw response body for diagnostics.</p>
 *
 * <p>On HTTP 429 (Too Many Requests) the server's {@code Retry-After} / {@code X-Rate-Limit-Reset} hint is
 * captured into {@link #getRetryAfterSeconds()} so callers can implement their own backoff.</p>
 */
public class ApiException extends AssinafyException {

    private final int statusCode;
    private final String responseBody;
    private Integer retryAfterSeconds;

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

    /**
     * Number of seconds the caller should wait before retrying, derived from the {@code Retry-After} or
     * {@code X-Rate-Limit-Reset} response header (typically populated on HTTP 429); {@code null} when the
     * server provided no such hint.
     */
    public Integer getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    /** Attaches a retry-after hint (in seconds) and returns {@code this} for fluent throw-site usage. */
    public ApiException withRetryAfterSeconds(Integer retryAfterSeconds) {
        this.retryAfterSeconds = retryAfterSeconds;
        return this;
    }

    private static String buildMessage(int statusCode, String apiMessage) {
        if (apiMessage != null && !apiMessage.isBlank()) {
            return apiMessage;
        }
        return "API request failed with status " + statusCode;
    }
}
