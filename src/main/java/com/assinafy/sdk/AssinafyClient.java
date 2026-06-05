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
import com.assinafy.sdk.resources.AuthenticationResource;
import com.assinafy.sdk.resources.DocumentResource;
import com.assinafy.sdk.resources.FieldResource;
import com.assinafy.sdk.resources.SignerResource;
import com.assinafy.sdk.resources.SignerSelfResource;
import com.assinafy.sdk.resources.TagResource;
import com.assinafy.sdk.resources.TemplateResource;
import com.assinafy.sdk.resources.WebhookResource;
import okhttp3.OkHttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class AssinafyClient {

    private final OkHttpClient httpClient;
    private final String baseUrl;

    public final DocumentResource documents;
    public final SignerResource signers;
    public final AssignmentResource assignments;
    public final WebhookResource webhooks;
    public final TemplateResource templates;
    public final TagResource tags;
    public final FieldResource fields;
    public final SignerSelfResource signerSelf;
    public final AuthenticationResource auth;

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
        this.baseUrl = rawBaseUrl.endsWith("/")
                ? rawBaseUrl.substring(0, rawBaseUrl.length() - 1)
                : rawBaseUrl;

        final int maxRetries = Math.max(0, options.getMaxRetries());
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(options.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(options.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .writeTimeout(options.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .addInterceptor(chain -> {
                    okhttp3.Request.Builder requestBuilder = chain.request().newBuilder()
                            .header("Accept", "application/json")
                            .header("User-Agent", "assinafy-webforms-java-client-sdk");
                    if (options.getApiKey() != null && !options.getApiKey().isBlank()) {
                        requestBuilder.header("X-Api-Key", options.getApiKey());
                    } else if (options.getToken() != null && !options.getToken().isBlank()) {
                        requestBuilder.header("Authorization", "Bearer " + options.getToken());
                    }
                    okhttp3.Request request = requestBuilder.build();
                    okhttp3.Response response = chain.proceed(request);

                    int attempts = 0;
                    while (isRetryable(response.code()) && attempts < maxRetries) {
                        long waitMs = retryDelayMs(response, attempts);
                        response.close();
                        try {
                            Thread.sleep(waitMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return chain.proceed(request);
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
    }

    public static AssinafyClient create(String apiKey, String accountId) {
        return new AssinafyClient(new AssinafyClientOptions()
                .setApiKey(apiKey)
                .setAccountId(accountId));
    }

    public static AssinafyClient create(String apiKey, String accountId, Consumer<AssinafyClientOptions> configure) {
        AssinafyClientOptions opts = new AssinafyClientOptions()
                .setApiKey(apiKey)
                .setAccountId(accountId);
        if (configure != null) {
            configure.accept(opts);
        }
        return new AssinafyClient(opts);
    }

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

    public UploadAndRequestSignaturesResult uploadAndRequestSignatures(
            UploadAndRequestSignaturesOptions options) {
        if (options == null) {
            throw new ValidationException("Upload and request signatures options are required");
        }
        if (options.getSigners() == null || options.getSigners().isEmpty()) {
            throw new ValidationException("At least one signer is required");
        }

        DocumentDetails document;
        if (options.getFile() != null) {
            document = documents.upload(options.getFile(), options.getAccountId());
        } else {
            document = documents.upload(options.getFileBytes(), options.getFileName(), options.getAccountId());
        }

        if (!Boolean.FALSE.equals(options.getWaitForReady())) {
            documents.waitUntilReady(document.getId());
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
                ? headerSeconds * 1000L
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

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
