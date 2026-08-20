package com.assinafy.sdk;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.CreateAssignmentPayload;
import com.assinafy.sdk.models.CreateSignerPayload;
import com.assinafy.sdk.models.DocumentDetails;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.models.UploadAndRequestSignaturesOptions;
import com.assinafy.sdk.models.UploadAndRequestSignaturesResult;
import com.assinafy.sdk.models.UploadAndRequestSignaturesSigner;
import com.assinafy.sdk.resources.AssignmentResource;
import com.assinafy.sdk.resources.AccountResource;
import com.assinafy.sdk.resources.AuthenticationResource;
import com.assinafy.sdk.resources.DocumentResource;
import com.assinafy.sdk.resources.FieldResource;
import com.assinafy.sdk.resources.SignerResource;
import com.assinafy.sdk.resources.SignerSelfResource;
import com.assinafy.sdk.resources.TagResource;
import com.assinafy.sdk.resources.TemplateResource;
import com.assinafy.sdk.resources.WebhookResource;
import com.assinafy.sdk.resources.UserResource;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Thread-safe entry point for the Assinafy Webforms API.
 *
 * <p>Create one instance per credential/account and reuse it. The client snapshots its options, adds the
 * required authentication header, and exposes endpoint groups as immutable resource fields. JSON responses
 * are unwrapped from Assinafy's {@code {status,message,data}} envelope; binary download methods return bytes.</p>
 */
public final class AssinafyClient {

    private final OkHttpClient httpClient;
    private final String baseUrl;

    /** Document upload, lookup, download, template-generation, verification, and tag operations. */
    public final DocumentResource documents;
    /** Account-owner signer CRUD operations. */
    public final SignerResource signers;
    /** Assignment creation, pricing, signing, expiration, resend, and notification operations. */
    public final AssignmentResource assignments;
    /** Webhook subscription, event catalogue, delivery history, and retry operations. */
    public final WebhookResource webhooks;
    /** Template list and single-template operations. */
    public final TemplateResource templates;
    /** Account tag CRUD operations. */
    public final TagResource tags;
    /** Account field CRUD, validation, and type-catalogue operations. */
    public final FieldResource fields;
    /** Signer-access-code and public signer-facing operations. */
    public final SignerSelfResource signerSelf;
    /** Login, password, social-login, and API-key operations. */
    public final AuthenticationResource auth;
    /** Account CRUD, theme, logo, and account-statistics operations. */
    public final AccountResource accounts;
    /** Current-user profile, notification-preference, and user-statistics operations. */
    public final UserResource users;

    /**
     * Builds a client from validated connection, authentication, retry, and timeout options.
     *
     * @param options required client configuration
     * @throws ValidationException when the options or base URL are invalid
     */
    public AssinafyClient(AssinafyClientOptions options) {
        if (options == null) {
            throw new ValidationException("Client options are required");
        }
        if (options.getTimeoutMs() <= 0) {
            throw new ValidationException("Request timeout must be greater than zero");
        }

        String rawBaseUrl = options.getBaseUrl() != null
                ? options.getBaseUrl()
                : "https://api.assinafy.com.br/v1";
        if (rawBaseUrl.isBlank()) {
            throw new ValidationException("Base URL is required");
        }
        final HttpUrl apiOrigin;
        try {
            apiOrigin = HttpUrl.get(rawBaseUrl);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Base URL must be a valid HTTP or HTTPS URL", e);
        }
        if (!apiOrigin.username().isEmpty() || !apiOrigin.password().isEmpty()
                || apiOrigin.query() != null || apiOrigin.fragment() != null) {
            throw new ValidationException("Base URL must not contain user information, a query, or a fragment");
        }
        if (!"https".equals(apiOrigin.scheme()) && !isLoopbackHost(apiOrigin.host())) {
            throw new ValidationException("Base URL must use HTTPS except for loopback HTTP in local tests");
        }
        String normalizedBaseUrl = apiOrigin.toString();
        this.baseUrl = normalizedBaseUrl.endsWith("/")
                ? normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1)
                : normalizedBaseUrl;

        final int maxRetries = Math.max(0, options.getMaxRetries());
        final String apiKey = options.getApiKey();
        final String token = options.getToken();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(options.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(options.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .writeTimeout(options.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .addNetworkInterceptor(chain -> {
                    okhttp3.Request.Builder requestBuilder = chain.request().newBuilder()
                            .header("Accept", "application/json")
                            .header("User-Agent", "assinafy-webforms-java-client-sdk");
                    if (sameOrigin(chain.request().url(), apiOrigin)) {
                        if (apiKey != null && !apiKey.isBlank()) {
                            requestBuilder.header("X-Api-Key", apiKey);
                        } else if (token != null && !token.isBlank()) {
                            requestBuilder.header("Authorization", "Bearer " + token);
                        }
                    }
                    return chain.proceed(requestBuilder.build());
                })
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request();
                    okhttp3.Response response = chain.proceed(request);

                    int attempts = 0;
                    while (isRetryable(response.code()) && isRetrySafe(request.method())
                            && attempts < maxRetries) {
                        long waitMs = retryDelayMs(response, attempts);
                        response.close();
                        try {
                            Thread.sleep(waitMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new java.io.InterruptedIOException("Interrupted while waiting to retry");
                        }
                        attempts++;
                        response = chain.proceed(request);
                    }
                    return response;
                })
                .build();

        String accountId = options.getAccountId();
        this.documents = new DocumentResource(httpClient, baseUrl, accountId);
        this.signers = new SignerResource(httpClient, baseUrl, accountId);
        this.assignments = new AssignmentResource(httpClient, baseUrl, accountId);
        this.webhooks = new WebhookResource(httpClient, baseUrl, accountId);
        this.templates = new TemplateResource(httpClient, baseUrl, accountId);
        this.tags = new TagResource(httpClient, baseUrl, accountId);
        this.fields = new FieldResource(httpClient, baseUrl, accountId);
        this.signerSelf = new SignerSelfResource(httpClient, baseUrl);
        this.auth = new AuthenticationResource(httpClient, baseUrl);
        this.accounts = new AccountResource(httpClient, baseUrl, accountId);
        this.users = new UserResource(httpClient, baseUrl);
    }

    /**
     * Creates a production-API client using an API key and default account ID.
     *
     * @param apiKey API key sent as {@code X-Api-Key}
     * @param accountId default account identifier
     * @return configured client
     */
    public static AssinafyClient create(String apiKey, String accountId) {
        return new AssinafyClient(new AssinafyClientOptions()
                .setApiKey(apiKey)
                .setAccountId(accountId));
    }

    /**
     * Creates a production-API client and applies optional configuration before construction.
     *
     * @param apiKey API key sent as {@code X-Api-Key}
     * @param accountId default account identifier
     * @param configure optional options customizer
     * @return configured client
     */
    public static AssinafyClient create(String apiKey, String accountId, Consumer<AssinafyClientOptions> configure) {
        AssinafyClientOptions opts = new AssinafyClientOptions()
                .setApiKey(apiKey)
                .setAccountId(accountId);
        if (configure != null) {
            configure.accept(opts);
        }
        return new AssinafyClient(opts);
    }

    /**
     * Creates a client from string configuration. Accepted aliases are {@code api_key|apiKey},
     * {@code token|access_token|accessToken}, {@code account_id|accountId}, and {@code base_url|baseUrl}.
     *
     * @param config required configuration map
     * @return configured client
     * @throws ValidationException when {@code config} is {@code null} or invalid
     */
    public static AssinafyClient fromConfig(Map<String, String> config) {
        if (config == null) {
            throw new ValidationException("Configuration map is required");
        }
        AssinafyClientOptions opts = new AssinafyClientOptions();

        String apiKey = config.getOrDefault("api_key", config.get("apiKey"));
        if (apiKey != null && !apiKey.isBlank()) {
            opts.setApiKey(apiKey);
        }

        if (opts.getApiKey() == null || opts.getApiKey().isBlank()) {
            String token = config.getOrDefault("token",
                    config.getOrDefault("access_token", config.get("accessToken")));
            if (token != null && !token.isBlank()) {
                opts.setToken(token);
            }
        }

        String accountId = config.getOrDefault("account_id", config.get("accountId"));
        if (accountId != null) opts.setAccountId(accountId);

        String url = config.getOrDefault("base_url", config.get("baseUrl"));
        if (url != null) opts.setBaseUrl(url);

        return new AssinafyClient(opts);
    }

    /**
     * High-level virtual-signature workflow: upload one PDF, optionally wait for processing, create or reuse
     * every signer by email, then create the assignment. Returns the ready document, assignment, and signer IDs.
     * Each successful stage is persistent; the API has no transaction spanning these calls.
     *
     * @param options required upload, signer, assignment, and account options
     * @return resources produced by the workflow
     * @throws ValidationException when required workflow options are absent or invalid
     */
    public UploadAndRequestSignaturesResult uploadAndRequestSignatures(
            UploadAndRequestSignaturesOptions options) {
        if (options == null) {
            throw new ValidationException("Upload and request signatures options are required");
        }
        if (options.getSigners() == null || options.getSigners().isEmpty()) {
            throw new ValidationException("At least one signer is required");
        }
        for (UploadAndRequestSignaturesSigner signer : options.getSigners()) {
            if (signer == null || signer.getName() == null || signer.getName().isBlank()) {
                throw new ValidationException("Every signer must have a name");
            }
        }

        DocumentDetails document;
        if (options.getFile() != null) {
            document = documents.upload(options.getFile(), options.getAccountId());
        } else {
            document = documents.upload(options.getFileBytes(), options.getFileName(), options.getAccountId());
        }

        if (!Boolean.FALSE.equals(options.getWaitForReady())) {
            document = documents.waitUntilReady(document.getId());
        }

        List<String> signerIds = new ArrayList<>();
        for (UploadAndRequestSignaturesSigner s : options.getSigners()) {
            CreateSignerPayload payload = new CreateSignerPayload(s.getName(), s.getEmail());
            if (s.getWhatsappPhoneNumber() != null) payload.setWhatsappPhoneNumber(s.getWhatsappPhoneNumber());
            Signer created = signers.create(payload, options.getAccountId());
            signerIds.add(created.getId());
        }

        CreateAssignmentPayload assignmentPayload = new CreateAssignmentPayload()
                .setMethod("virtual")
                .setSignerStrings(signerIds);
        if (options.getMessage() != null) assignmentPayload.setMessage(options.getMessage());
        if (options.getExpiresAt() != null) assignmentPayload.setExpiresAt(options.getExpiresAt());
        if (options.getCopyReceivers() != null) assignmentPayload.setCopyReceivers(options.getCopyReceivers());

        Assignment assignment = assignments.create(document.getId(), assignmentPayload);
        return new UploadAndRequestSignaturesResult(document, assignment, signerIds);
    }

    private static boolean isRetryable(int statusCode) {
        return statusCode == 429 || statusCode == 503;
    }

    private static boolean isRetrySafe(String method) {
        return "GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method);
    }

    private static boolean sameOrigin(HttpUrl requestUrl, HttpUrl apiUrl) {
        return requestUrl.scheme().equalsIgnoreCase(apiUrl.scheme())
                && requestUrl.host().equalsIgnoreCase(apiUrl.host())
                && requestUrl.port() == apiUrl.port();
    }

    private static boolean isLoopbackHost(String host) {
        return "localhost".equalsIgnoreCase(host) || "::1".equals(host) || "127.0.0.1".equals(host);
    }

    private static final long MAX_RETRY_WAIT_MS = 30_000L;

    /**
     * Computes how long to wait before the next retry. Honors the server's {@code Retry-After} (delta-seconds)
     * or {@code X-Rate-Limit-Reset} header when present; otherwise falls back to a simple linear backoff. The
     * delay is capped at {@link #MAX_RETRY_WAIT_MS}.
     */
    private static long retryDelayMs(okhttp3.Response response, int attempt) {
        Long headerSeconds = parseLong(response.header("Retry-After"));
        if (headerSeconds == null) {
            headerSeconds = parseLong(response.header("X-Rate-Limit-Reset"));
        }
        long waitMs = headerSeconds != null
                ? Math.min(MAX_RETRY_WAIT_MS / 1000L, Math.max(0L, headerSeconds)) * 1000L
                : Math.min(MAX_RETRY_WAIT_MS, 1000L * (attempt + 1));
        return Math.max(0L, Math.min(MAX_RETRY_WAIT_MS, waitMs));
    }

    private static Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Returns configured shared OkHttp client.
     *
     * @return configured shared OkHttp client
     */
    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Returns normalized API base URL without a trailing slash.
     *
     * @return normalized API base URL without a trailing slash
     */
    public String getBaseUrl() {
        return baseUrl;
    }
}
