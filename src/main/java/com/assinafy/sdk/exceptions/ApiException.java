package com.assinafy.sdk.exceptions;

/**
 * Thrown when the Assinafy API returns an error, either via a non-2xx HTTP status or via an error envelope
 * ({@code {"status": >=400, "message": "...", "data": null}}) returned with any HTTP status.
 *
 * <p>The {@link #getStatusCode() status code} reflects the envelope {@code status} when present, otherwise the
 * HTTP status. {@link #getMessage()} surfaces the server-provided {@code message} when available, and
 * {@link #getResponseBody()} carries the raw response body for diagnostics.</p>
 *
 * <p>On a retryable status (HTTP 429 Too Many Requests or 503 Service Unavailable) the server's
 * {@code Retry-After} / {@code X-Rate-Limit-Reset} hint is captured into {@link #getRetryAfterSeconds()} so
 * callers can implement their own backoff. It is left {@code null} on permanent errors (e.g. 400/401) so a
 * caller keying retries on its presence never backs off on a non-retryable failure.</p>
 *
 * <p>OAuth failures additionally populate {@link #getOAuthError()}, and an {@code insufficient_scope}
 * rejection also populates {@link #getRequiredScope()}, so an application can branch on the machine-readable
 * code instead of re-parsing the response body or the {@code WWW-Authenticate} header.</p>
 */
public class ApiException extends AssinafyException {

    private static final long serialVersionUID = 1L;

    /** HTTP or response-envelope status code. */
    private final int statusCode;
    /** Raw response body, or {@code null}. */
    private final String responseBody;
    /** Server-suggested retry delay in seconds, or {@code null}. */
    private Integer retryAfterSeconds;
    /** RFC 6749 error code, or {@code null}. */
    private String oauthError;
    /** Scope named by an {@code insufficient_scope} challenge, or {@code null}. */
    private String requiredScope;

    /**
     * Creates an API failure with its parsed message and raw response body.
     *
     * @param statusCode HTTP or response-envelope status code
     * @param apiMessage server-provided error message, or {@code null}
     * @param responseBody raw response body, or {@code null}
     */
    public ApiException(int statusCode, String apiMessage, String responseBody) {
        super(buildMessage(statusCode, apiMessage));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    /**
     * Creates an API failure containing only a status code.
     *
     * @param statusCode HTTP or response-envelope status code
     */
    public ApiException(int statusCode) {
        this(statusCode, null, null);
    }

    /**
     * Returns HTTP or response-envelope status code.
     *
     * @return HTTP or response-envelope status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns raw response body, or {@code null}.
     *
     * @return raw response body, or {@code null}
     */
    public String getResponseBody() {
        return responseBody;
    }

    /**
     * Number of seconds the caller should wait before retrying, derived from the {@code Retry-After} or
     * {@code X-Rate-Limit-Reset} response header. Populated only on retryable statuses (HTTP 429 / 503);
     * {@code null} on permanent errors and when the server provided no such hint.
     *
     * @return suggested retry delay in seconds, or {@code null}
     */
    public Integer getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    /**
     * Attaches a retry-after hint for fluent throw-site usage.
     *
     * @param retryAfterSeconds suggested retry delay in seconds, or {@code null}
     * @return this exception
     */
    public ApiException withRetryAfterSeconds(Integer retryAfterSeconds) {
        this.retryAfterSeconds = retryAfterSeconds;
        return this;
    }

    /**
     * RFC 6749 error code identifying an OAuth failure, read from a flat {@code {error, error_description}}
     * body returned by the token or revoke endpoint, or from the {@code WWW-Authenticate} challenge on a
     * 401/403. Typical values are {@code invalid_grant}, {@code invalid_client}, {@code invalid_target},
     * {@code unsupported_grant_type}, and {@code insufficient_scope}. {@code null} on every non-OAuth failure.
     *
     * @return the OAuth error code, or {@code null}
     */
    public String getOAuthError() {
        return oauthError;
    }

    /**
     * Scope named by an {@code insufficient_scope} challenge. Treat it as a prompt to send the user through
     * the authorization flow again with that scope added, not as a reason to retry the same request.
     *
     * @return the missing scope, or {@code null}
     */
    public String getRequiredScope() {
        return requiredScope;
    }

    /**
     * Attaches an OAuth error code and the scope a challenge named, for fluent throw-site usage.
     *
     * @param oauthError RFC 6749 error code, or {@code null}
     * @param requiredScope scope named by an {@code insufficient_scope} challenge, or {@code null}
     * @return this exception
     */
    public ApiException withOAuthChallenge(String oauthError, String requiredScope) {
        this.oauthError = oauthError;
        this.requiredScope = requiredScope;
        return this;
    }

    private static String buildMessage(int statusCode, String apiMessage) {
        if (apiMessage != null && !apiMessage.isBlank()) {
            return apiMessage;
        }
        return "API request failed with status " + statusCode;
    }
}
