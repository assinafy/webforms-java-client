package com.assinafy.sdk;

/** Mutable builder-style configuration consumed and snapshotted by {@link AssinafyClient}. */
public final class AssinafyClientOptions {

    private String apiKey;
    private String token;
    private String accountId;
    private String baseUrl = "https://api.assinafy.com.br/v1";
    private int timeoutMs = 30_000;
    private int maxRetries = 0;

    /** Creates options with production URL, 30-second timeout, and retries disabled. */
    public AssinafyClientOptions() {}

    /**
     * Returns API key sent as {@code X-Api-Key}, or {@code null}.
     *
     * @return API key sent as {@code X-Api-Key}, or {@code null}
     */
    public String getApiKey() { return apiKey; }

    /**
     * Sets the API key; when both credentials are set, the API key takes precedence.
     *
     * @param apiKey API key, or {@code null} to clear it
     * @return this options object
     */
    public AssinafyClientOptions setApiKey(String apiKey) { this.apiKey = apiKey; return this; }

    /**
     * Returns bearer access token, or {@code null}.
     *
     * @return bearer access token, or {@code null}
     */
    public String getToken() { return token; }

    /**
     * Sets the bearer access token used when no API key is configured.
     *
     * @param token bearer access token, or {@code null} to clear it
     * @return this options object
     */
    public AssinafyClientOptions setToken(String token) { this.token = token; return this; }

    /**
     * Returns default account ID used by account-scoped overloads, or {@code null}.
     *
     * @return default account ID used by account-scoped overloads, or {@code null}
     */
    public String getAccountId() { return accountId; }

    /**
     * Sets default account ID, or {@code null} to require per-call overrides.
     *
     * @param accountId default account ID, or {@code null} to require per-call overrides
     * @return this options object
     */
    public AssinafyClientOptions setAccountId(String accountId) { this.accountId = accountId; return this; }

    /**
     * Returns full API base URL; defaults to the production {@code /v1} URL.
     *
     * @return full API base URL; defaults to the production {@code /v1} URL
     */
    public String getBaseUrl() { return baseUrl; }

    /**
     * Sets the full HTTPS API base URL, including {@code /v1}. Loopback HTTP is accepted for local tests.
     *
     * @param baseUrl full HTTPS API base URL, including {@code /v1}, or a loopback HTTP test URL
     * @return this options object
     */
    public AssinafyClientOptions setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }

    /**
     * Returns connect, read, and write timeout in milliseconds.
     *
     * @return connect, read, and write timeout in milliseconds
     */
    public int getTimeoutMs() { return timeoutMs; }

    /**
     * Sets positive connect, read, and write timeout in milliseconds.
     *
     * @param timeoutMs positive connect, read, and write timeout in milliseconds
     * @return this options object
     */
    public AssinafyClientOptions setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; return this; }

    /**
     * Maximum number of automatic retries for safe read requests on a rate-limit/temporary error (HTTP 429 or
     * 503). Defaults to {@code 0}. Mutating requests are never replayed because doing so could duplicate an
     * upload, notification, signature, or other side effect. Retry waits honor the server hint and are capped.
     *
     * @return configured safe-read retry limit
     */
    public int getMaxRetries() { return maxRetries; }

    /**
     * Sets non-negative safe-read retry limit.
     *
     * @param maxRetries non-negative safe-read retry limit
     * @return this options object
     */
    public AssinafyClientOptions setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; return this; }
}
