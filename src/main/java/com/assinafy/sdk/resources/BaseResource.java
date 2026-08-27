package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.NetworkException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.PaginationMeta;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Shared HTTP transport, JSON-envelope parsing, validation, and pagination support for endpoint resources. */
public abstract class BaseResource {

    private static final Pattern PATH_SEGMENT = Pattern.compile("[A-Za-z0-9._~-]+");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /** JSON request-body media type used by resource methods. */
    protected static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /** Shared HTTP client configured by {@link com.assinafy.sdk.AssinafyClient}. */
    protected final OkHttpClient httpClient;

    /** Normalized API base URL without a trailing slash. */
    protected final String baseUrl;
    private final String defaultAccountId;

    /** Shared, thread-safe JSON mapper configured to tolerate unknown response properties. */
    protected static final ObjectMapper MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * Creates transport support for one endpoint resource.
     *
     * @param httpClient required shared HTTP client
     * @param baseUrl required HTTPS API base URL, or a loopback HTTP URL for local tests
     * @param defaultAccountId default account identifier, or {@code null}
     * @throws ValidationException when the client or base URL is invalid
     */
    protected BaseResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        if (httpClient == null) {
            throw new ValidationException("HTTP client is required");
        }
        this.httpClient = httpClient;
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.defaultAccountId = defaultAccountId;
    }

    /**
     * Validates an API base URL and returns it without a trailing slash. HTTPS is required except for a
     * loopback host, which keeps local tests able to use a plain-HTTP mock server. Credentials, a query, and a
     * fragment are rejected because they would leak into, or be silently dropped from, every request URL.
     *
     * <p>This is the single definition used both by {@link com.assinafy.sdk.AssinafyClient} and by every
     * resource, so the client and its resources can never disagree about what a valid base URL is.</p>
     *
     * @param baseUrl candidate API base URL
     * @return the normalized URL, with any trailing slash removed
     * @throws ValidationException when the URL is blank, unparseable, non-HTTPS, or carries extra URL parts
     */
    public static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new ValidationException("Base URL is required");
        }
        HttpUrl parsed;
        try {
            parsed = HttpUrl.get(baseUrl);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Base URL must be a valid HTTP or HTTPS URL", e);
        }
        if (!parsed.username().isEmpty() || !parsed.password().isEmpty()
                || parsed.query() != null || parsed.fragment() != null) {
            throw new ValidationException("Base URL must not contain user information, a query, or a fragment");
        }
        String host = parsed.host();
        boolean loopback = "localhost".equalsIgnoreCase(host) || "::1".equals(host) || "127.0.0.1".equals(host);
        if (!"https".equals(parsed.scheme()) && !loopback) {
            throw new ValidationException("Base URL must use HTTPS except for loopback HTTP in local tests");
        }
        String normalized = parsed.toString();
        return normalized.endsWith("/")
                ? normalized.substring(0, normalized.length() - 1)
                : normalized;
    }

    /**
     * Resolves and validates an explicit account identifier or the configured default.
     *
     * @param explicit explicit account identifier, or {@code null} for the default
     * @return validated account identifier
     * @throws ValidationException when neither a valid explicit nor default identifier is available
     */
    protected String accountId(String explicit) {
        String id = explicit != null ? explicit : defaultAccountId;
        if (id == null || id.isBlank()) {
            throw new ValidationException(
                    "Account ID is required. Provide it as a parameter or set a default in the client.");
        }
        return requireId(id, "Account ID");
    }

    /**
     * Validates a value before inserting it into a URL path segment.
     *
     * @param value identifier to validate
     * @param name human-readable field name used in validation messages
     * @return the validated identifier
     * @throws ValidationException when the value is blank or not URL-unreserved
     */
    protected String requireId(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(name + " is required");
        }
        if (".".equals(value) || "..".equals(value) || !PATH_SEGMENT.matcher(value).matches()) {
            throw new ValidationException(name + " must be a URL-safe path segment");
        }
        return value;
    }

    /**
     * Validates an email address used at an API request boundary.
     *
     * @param value email address to validate
     * @param name human-readable field name used in validation messages
     * @return the validated email address
     * @throws ValidationException when the value is not a basic email address
     */
    protected String requireEmail(String value, String name) {
        if (value == null || !EMAIL.matcher(value).matches()) {
            throw new ValidationException(name + " must be a valid email address");
        }
        return value;
    }

    /**
     * Executes a GET request without query parameters and unwraps its JSON {@code data}.
     *
     * @param <T> response model type
     * @param path API path beginning with {@code /}
     * @param dataType response model class
     * @return unwrapped response data, or {@code null} for null/empty data
     */
    protected <T> T httpGet(String path, Class<T> dataType) {
        return httpGet(path, Collections.emptyMap(), dataType);
    }

    /**
     * Executes a GET request and unwraps its JSON {@code data}.
     *
     * @param <T> response model type
     * @param path API path beginning with {@code /}
     * @param queryParams query parameters; {@code null} values are omitted
     * @param dataType response model class
     * @return unwrapped response data, or {@code null} for null/empty data
     */
    protected <T> T httpGet(String path, Map<String, String> queryParams, Class<T> dataType) {
        Request request = buildGetRequest(path, queryParams);
        return execute(request, MAPPER.getTypeFactory().constructType(dataType));
    }

    /**
     * Executes a GET request for a generic response type without query parameters.
     *
     * @param <T> response type
     * @param path API path beginning with {@code /}
     * @param typeRef generic response type reference
     * @return unwrapped response data, or {@code null} for null/empty data
     */
    protected <T> T httpGet(String path, TypeReference<T> typeRef) {
        Request request = buildGetRequest(path, Collections.emptyMap());
        return execute(request, MAPPER.getTypeFactory().constructType(typeRef));
    }

    /**
     * Executes a GET request for a generic response type.
     *
     * @param <T> response type
     * @param path API path beginning with {@code /}
     * @param queryParams query parameters; {@code null} values are omitted
     * @param typeRef generic response type reference
     * @return unwrapped response data, or {@code null} for null/empty data
     */
    protected <T> T httpGet(String path, Map<String, String> queryParams, TypeReference<T> typeRef) {
        Request request = buildGetRequest(path, queryParams);
        return execute(request, MAPPER.getTypeFactory().constructType(typeRef));
    }

    /**
     * Executes a JSON POST without query parameters.
     *
     * @param <T> response model type
     * @param path API path beginning with {@code /}
     * @param body request body, or {@code null} for an empty body
     * @param dataType response model class
     * @return unwrapped response data, or {@code null} for null/empty data
     */
    protected <T> T httpPost(String path, Object body, Class<T> dataType) {
        Request request = buildRequest("POST", path, body);
        return execute(request, MAPPER.getTypeFactory().constructType(dataType));
    }

    /**
     * Executes a JSON POST with query parameters.
     *
     * @param <T> response model type
     * @param path API path beginning with {@code /}
     * @param body request body, or {@code null} for an empty body
     * @param dataType response model class
     * @param queryParams query parameters; {@code null} values are omitted
     * @return unwrapped response data, or {@code null} for null/empty data
     */
    protected <T> T httpPost(String path, Object body, Class<T> dataType, Map<String, String> queryParams) {
        Request request = buildRequest("POST", path, body, queryParams);
        return execute(request, MAPPER.getTypeFactory().constructType(dataType));
    }

    /**
     * Executes a JSON POST for a generic response type.
     *
     * @param <T> response type
     * @param path API path beginning with {@code /}
     * @param body request body, or {@code null} for an empty body
     * @param typeRef generic response type reference
     * @param queryParams query parameters; {@code null} values are omitted
     * @return unwrapped response data, or {@code null} for null/empty data
     */
    protected <T> T httpPost(String path, Object body, TypeReference<T> typeRef, Map<String, String> queryParams) {
        Request request = buildRequest("POST", path, body, queryParams);
        return execute(request, MAPPER.getTypeFactory().constructType(typeRef));
    }

    /**
     * Executes a JSON PUT without query parameters.
     *
     * @param <T> response model type
     * @param path API path beginning with {@code /}
     * @param body request body, or {@code null} for an empty body
     * @param dataType response model class
     * @return unwrapped response data, or {@code null} for null/empty data
     */
    protected <T> T httpPut(String path, Object body, Class<T> dataType) {
        Request request = buildRequest("PUT", path, body);
        return execute(request, MAPPER.getTypeFactory().constructType(dataType));
    }

    /**
     * Executes a JSON PUT with query parameters.
     *
     * @param <T> response model type
     * @param path API path beginning with {@code /}
     * @param body request body, or {@code null} for an empty body
     * @param dataType response model class
     * @param queryParams query parameters; {@code null} values are omitted
     * @return unwrapped response data, or {@code null} for null/empty data
     */
    protected <T> T httpPut(String path, Object body, Class<T> dataType, Map<String, String> queryParams) {
        Request request = buildRequest("PUT", path, body, queryParams);
        return execute(request, MAPPER.getTypeFactory().constructType(dataType));
    }

    /**
     * Executes a JSON PUT for a generic response type.
     *
     * @param <T> response type
     * @param path API path beginning with {@code /}
     * @param body request body, or {@code null} for an empty body
     * @param typeRef generic response type reference
     * @param queryParams query parameters; {@code null} values are omitted
     * @return unwrapped response data, or {@code null} for null/empty data
     */
    protected <T> T httpPut(String path, Object body, TypeReference<T> typeRef, Map<String, String> queryParams) {
        Request request = buildRequest("PUT", path, body, queryParams);
        return execute(request, MAPPER.getTypeFactory().constructType(typeRef));
    }

    /**
     * Executes a JSON PATCH without query parameters.
     *
     * @param <T> response model type
     * @param path API path beginning with {@code /}
     * @param body request body, or {@code null} for an empty body
     * @param dataType response model class
     * @return unwrapped response data, or {@code null} for null/empty data
     */
    protected <T> T httpPatch(String path, Object body, Class<T> dataType) {
        Request request = buildRequest("PATCH", path, body);
        return execute(request, MAPPER.getTypeFactory().constructType(dataType));
    }

    /**
     * Executes a DELETE without query parameters and validates its success response.
     *
     * @param path API path beginning with {@code /}
     */
    protected void httpDelete(String path) {
        Request request = buildRequest("DELETE", path, null);
        executeVoid(request);
    }

    /**
     * Executes a DELETE carrying a JSON request body.
     *
     * @param path API path beginning with {@code /}
     * @param body request body
     */
    protected void httpDeleteBody(String path, Object body) {
        executeVoid(buildRequest("DELETE", path, body));
    }

    /**
     * Executes a DELETE and unwraps a generic JSON response.
     *
     * @param <T> response type
     * @param path API path beginning with {@code /}
     * @param typeRef generic response type reference
     * @return unwrapped response data, or {@code null} for null/empty data
     */
    protected <T> T httpDelete(String path, TypeReference<T> typeRef) {
        Request request = buildRequest("DELETE", path, null);
        return execute(request, MAPPER.getTypeFactory().constructType(typeRef));
    }

    /**
     * Executes a DELETE with query parameters and unwraps a generic JSON response.
     *
     * @param <T> response type
     * @param path API path beginning with {@code /}
     * @param typeRef generic response type reference
     * @param queryParams query parameters; {@code null} values are omitted
     * @return unwrapped response data, or {@code null} for null/empty data
     */
    protected <T> T httpDelete(String path, TypeReference<T> typeRef, Map<String, String> queryParams) {
        Request request = buildRequest("DELETE", path, null, queryParams);
        return execute(request, MAPPER.getTypeFactory().constructType(typeRef));
    }

    /**
     * Executes a JSON POST without query parameters and validates its success response.
     *
     * @param path API path beginning with {@code /}
     * @param body request body, or {@code null} for an empty body
     */
    protected void httpPostVoid(String path, Object body) {
        executeVoid(buildRequest("POST", path, body));
    }

    /**
     * Executes a JSON POST with query parameters and validates its success response.
     *
     * @param path API path beginning with {@code /}
     * @param body request body, or {@code null} for an empty body
     * @param queryParams query parameters; {@code null} values are omitted
     */
    protected void httpPostVoid(String path, Object body, Map<String, String> queryParams) {
        executeVoid(buildRequest("POST", path, body, queryParams));
    }

    /**
     * Executes a JSON PUT with query parameters and validates its success response.
     *
     * @param path API path beginning with {@code /}
     * @param body request body, or {@code null} for an empty body
     * @param queryParams query parameters; {@code null} values are omitted
     */
    protected void httpPutVoid(String path, Object body, Map<String, String> queryParams) {
        executeVoid(buildRequest("PUT", path, body, queryParams));
    }

    /**
     * Executes a binary GET without query parameters.
     *
     * @param path API path beginning with {@code /}
     * @return raw response bytes, never {@code null}
     */
    protected byte[] httpGetBinary(String path) {
        return httpGetBinary(path, Collections.emptyMap());
    }

    /**
     * Executes a binary GET with query parameters.
     *
     * @param path API path beginning with {@code /}
     * @param queryParams query parameters; {@code null} values are omitted
     * @return raw response bytes, never {@code null}
     */
    protected byte[] httpGetBinary(String path, Map<String, String> queryParams) {
        Request request = buildGetRequest(path, queryParams);
        return executeBinary(request);
    }

    /**
     * POST a binary (e.g. image) request body to an endpoint that responds with a JSON envelope rather than a
     * binary artifact. The envelope is parsed so envelope-level errors ({@code status >= 400}) surface as
     * {@link ApiException}; the success payload is discarded.
     *
     * @param path API path beginning with {@code /}
     * @param queryParams query parameters; {@code null} values are omitted
     * @param body binary request body
     */
    protected void httpPostBinaryEnvelope(String path, Map<String, String> queryParams, RequestBody body) {
        Request request = new Request.Builder().url(buildUrl(path, queryParams)).post(body).build();
        execute(request, MAPPER.getTypeFactory().constructType(Object.class));
    }

    /**
     * Executes a multipart POST and unwraps a typed JSON response.
     *
     * @param <T> response model type
     * @param path API path beginning with {@code /}
     * @param multipartBody multipart request body
     * @param dataType response model class
     * @return unwrapped response data, or {@code null} for null/empty data
     */
    protected <T> T httpPostMultipart(String path, RequestBody multipartBody, Class<T> dataType) {
        Request request = new Request.Builder()
                .url(buildUrl(path, Collections.emptyMap()))
                .post(multipartBody)
                .build();
        return execute(request, MAPPER.getTypeFactory().constructType(dataType));
    }

    /**
     * Executes a paginated GET with query parameters.
     *
     * @param <T> list item model type
     * @param path API path beginning with {@code /}
     * @param queryParams query parameters; {@code null} values are omitted
     * @param itemType list item model class
     * @return page data and parsed pagination headers
     */
    protected <T> PaginatedResult<T> httpGetList(String path, Map<String, String> queryParams, Class<T> itemType) {
        Request request = buildGetRequest(path, queryParams);
        JavaType listType = MAPPER.getTypeFactory().constructCollectionType(List.class, itemType);
        return executeList(request, listType);
    }

    private Request buildGetRequest(String path, Map<String, String> queryParams) {
        return new Request.Builder().url(buildUrl(path, queryParams)).get().build();
    }

    private HttpUrl buildUrl(String path, Map<String, String> queryParams) {
        HttpUrl base = HttpUrl.get(baseUrl + path);
        Map<String, String> normalizedParams = normalizeQueryParams(queryParams);
        if (normalizedParams.isEmpty()) {
            return base;
        }
        HttpUrl.Builder builder = base.newBuilder();
        for (Map.Entry<String, String> entry : normalizedParams.entrySet()) {
            if (entry.getValue() != null) {
                builder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

    private Map<String, String> normalizeQueryParams(Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> normalized = new HashMap<>();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            String key = normalizeQueryParamName(entry.getKey());
            normalized.put(key, entry.getValue());
        }
        return normalized;
    }

    private String normalizeQueryParamName(String key) {
        if ("per_page".equals(key) || "perPage".equals(key)) {
            return "per-page";
        }
        return key;
    }

    private Request buildRequest(String method, String path, Object body) {
        return buildRequest(method, path, body, Collections.emptyMap());
    }

    private Request buildRequest(String method, String path, Object body, Map<String, String> queryParams) {
        RequestBody requestBody = null;
        if (body != null) {
            try {
                String json = MAPPER.writeValueAsString(body);
                requestBody = RequestBody.create(json, JSON);
            } catch (Exception e) {
                throw new ValidationException("Failed to serialize request body: " + e.getMessage(), e);
            }
        } else if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
            requestBody = RequestBody.create("", JSON);
        }
        return new Request.Builder()
                .url(buildUrl(path, queryParams))
                .method(method, requestBody)
                .build();
    }

    private <T> T execute(Request request, JavaType dataType) {
        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            String json = responseBody != null ? responseBody.string() : "";
            try {
                return parseEnvelope(json, response.code(), dataType);
            } catch (ApiException e) {
                throw attachRetryAfter(e, response);
            }
        } catch (ApiException e) {
            throw e;
        } catch (ValidationException e) {
            throw e;
        } catch (IOException e) {
            throw new NetworkException("Network error: " + e.getMessage(), e);
        }
    }

    private void executeVoid(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            String json = responseBody != null ? responseBody.string() : "";
            try {
                // The API can return an error envelope (status >= 400) under an HTTP 2xx status, so the
                // envelope must be inspected even on a "successful" HTTP response.
                if (!json.isBlank()) {
                    throwIfEnvelopeError(json, response.code());
                }
                if (!response.isSuccessful()) {
                    throw new ApiException(response.code());
                }
            } catch (ApiException e) {
                throw attachRetryAfter(e, response);
            }
        } catch (ApiException e) {
            throw e;
        } catch (IOException e) {
            throw new NetworkException("Network error: " + e.getMessage(), e);
        }
    }

    private byte[] executeBinary(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            MediaType contentType = responseBody != null ? responseBody.contentType() : null;
            boolean jsonBody = contentType != null
                    && "application".equalsIgnoreCase(contentType.type())
                    && ("json".equalsIgnoreCase(contentType.subtype())
                    || contentType.subtype().toLowerCase(java.util.Locale.ROOT).endsWith("+json"));

            // A real binary artifact is never JSON. When the body is JSON (an error envelope returned under
            // HTTP 200) or the HTTP status is an error, route through envelope inspection instead of returning
            // the JSON as if it were the artifact.
            if (!response.isSuccessful() || jsonBody) {
                String json = responseBody != null ? responseBody.string() : "";
                try {
                    if (!json.isBlank()) {
                        throwIfEnvelopeError(json, response.code());
                    }
                    if (!response.isSuccessful()) {
                        throw new ApiException(response.code());
                    }
                } catch (ApiException e) {
                    throw attachRetryAfter(e, response);
                }
                throw new NetworkException("Expected a binary response but received JSON");
            }
            return responseBody != null ? responseBody.bytes() : new byte[0];
        } catch (ApiException e) {
            throw e;
        } catch (IOException e) {
            throw new NetworkException("Network error: " + e.getMessage(), e);
        }
    }

    private <T> PaginatedResult<T> executeList(Request request, JavaType listType) {
        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            String json = responseBody != null ? responseBody.string() : "";
            List<T> data;
            try {
                data = parseEnvelope(json, response.code(), listType);
            } catch (ApiException e) {
                throw attachRetryAfter(e, response);
            }
            PaginationMeta meta = parsePaginationMeta(response);
            return new PaginatedResult<>(data != null ? data : Collections.emptyList(), meta);
        } catch (ApiException e) {
            throw e;
        } catch (IOException e) {
            throw new NetworkException("Network error: " + e.getMessage(), e);
        }
    }

    /**
     * Captures the server's retry hint into the exception so callers can back off on a rate-limit/temporary
     * error. Only attached for retryable statuses (HTTP 429 / 503): the {@code X-Rate-Limit-Reset} header is
     * present on virtually every response (including successful ones and permanent 4xx errors), so attaching it
     * unconditionally would wrongly signal that a permanent 400/401 is worth retrying. Reads {@code Retry-After}
     * first, then falls back to {@code X-Rate-Limit-Reset}; only the delta-seconds form is surfaced (an
     * HTTP-date {@code Retry-After} is ignored). Header lookup is case-insensitive (OkHttp).
     */
    private ApiException attachRetryAfter(ApiException e, Response response) {
        if (e.getStatusCode() != 429 && e.getStatusCode() != 503) {
            return e;
        }
        Integer seconds = parseRetryAfterSeconds(response.header("Retry-After"));
        if (seconds == null) {
            seconds = parseRetryAfterSeconds(response.header("X-Rate-Limit-Reset"));
        }
        return seconds != null ? e.withRetryAfterSeconds(seconds) : e;
    }

    private Integer parseRetryAfterSeconds(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T parseEnvelope(String json, int httpStatus, JavaType dataType) {
        if (json == null || json.isBlank()) {
            if (httpStatus < 200 || httpStatus >= 300) {
                throw new ApiException(httpStatus);
            }
            return null;
        }
        try {
            JsonNode root = MAPPER.readTree(json);
            if (root.has("status") && root.get("status").isNumber()) {
                int envelopeStatus = root.get("status").asInt();
                String message = root.has("message") ? root.get("message").asText(null) : null;
                if (envelopeStatus >= 400) {
                    throw new ApiException(envelopeStatus, message, json);
                }
                if (httpStatus < 200 || httpStatus >= 300) {
                    throw new ApiException(httpStatus, message, json);
                }
                if (root.has("data")) {
                    JsonNode dataNode = root.get("data");
                    if (dataNode.isNull()) {
                        return null;
                    }
                    return MAPPER.convertValue(dataNode, dataType);
                }
                return null;
            }
            if (httpStatus < 200 || httpStatus >= 300) {
                String message = root.has("message") ? root.get("message").asText(null) : null;
                throw new ApiException(httpStatus, message, json);
            }
            return MAPPER.convertValue(root, dataType);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            if (httpStatus < 200 || httpStatus >= 300) {
                throw new ApiException(httpStatus, null, json);
            }
            throw new NetworkException("Failed to parse response: " + e.getMessage(), e);
        }
    }

    /**
     * Inspects a response body used by the void/binary paths and throws an {@link ApiException} when it
     * represents an error — either an envelope whose {@code status >= 400} (even under an HTTP 2xx status) or a
     * non-envelope body accompanied by an HTTP error status. Returns normally when the body is a successful
     * envelope or non-error content, so callers can treat the response as a success.
     */
    private void throwIfEnvelopeError(String json, int httpStatus) {
        Integer envelopeStatus = null;
        String message = null;
        try {
            JsonNode root = MAPPER.readTree(json);
            if (root.has("status") && root.get("status").isNumber()) {
                envelopeStatus = root.get("status").asInt();
            }
            if (root.has("message")) {
                message = root.get("message").asText(null);
            }
        } catch (Exception ignored) {
            // Not JSON (e.g. a plain-text error) — fall through to the HTTP-status check below.
        }
        if (envelopeStatus != null && envelopeStatus >= 400) {
            throw new ApiException(envelopeStatus, message, json);
        }
        if (httpStatus < 200 || httpStatus >= 300) {
            throw new ApiException(httpStatus, message, json);
        }
    }

    private PaginationMeta parsePaginationMeta(Response response) {
        String currentPageStr = response.header("x-pagination-current-page");
        String perPageStr = response.header("x-pagination-per-page");
        String totalStr = response.header("x-pagination-total-count");
        String lastPageStr = response.header("x-pagination-page-count");

        if (currentPageStr == null && perPageStr == null && totalStr == null && lastPageStr == null) {
            return null;
        }

        PaginationMeta meta = new PaginationMeta();
        if (currentPageStr != null) meta.setCurrentPage(parseInt(currentPageStr));
        if (perPageStr != null) meta.setPerPage(parseInt(perPageStr));
        if (totalStr != null) meta.setTotal(parseInt(totalStr));
        if (lastPageStr != null) meta.setLastPage(parseInt(lastPageStr));
        return meta;
    }

    private Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Builds string query parameters from alternating keys and values, omitting null values.
     *
     * @param keyValues alternating {@link String} keys and arbitrary values
     * @return mutable query-parameter map
     * @throws IllegalArgumentException when an odd number of arguments is supplied
     * @throws ClassCastException when a key is not a {@link String}
     */
    protected Map<String, String> queryParams(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Key-value pairs must be even");
        }
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            String key = (String) keyValues[i];
            Object value = keyValues[i + 1];
            if (value != null) {
                params.put(key, String.valueOf(value));
            }
        }
        return params;
    }
}
