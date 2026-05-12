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

public abstract class BaseResource {

    protected static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    protected final OkHttpClient httpClient;
    protected final String baseUrl;
    private final String defaultAccountId;

    protected static final ObjectMapper MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    protected BaseResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.defaultAccountId = defaultAccountId;
    }

    protected String accountId(String explicit) {
        String id = explicit != null ? explicit : defaultAccountId;
        if (id == null || id.isBlank()) {
            throw new ValidationException(
                    "Account ID is required. Provide it as a parameter or set a default in the client.");
        }
        return id;
    }

    protected String requireId(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(name + " is required");
        }
        return value;
    }

    protected <T> T httpGet(String path, Class<T> dataType) {
        return httpGet(path, Collections.emptyMap(), dataType);
    }

    protected <T> T httpGet(String path, Map<String, String> queryParams, Class<T> dataType) {
        Request request = buildGetRequest(path, queryParams);
        return execute(request, MAPPER.getTypeFactory().constructType(dataType));
    }

    protected <T> T httpGet(String path, TypeReference<T> typeRef) {
        Request request = buildGetRequest(path, Collections.emptyMap());
        return execute(request, MAPPER.getTypeFactory().constructType(typeRef));
    }

    protected <T> T httpGet(String path, Map<String, String> queryParams, TypeReference<T> typeRef) {
        Request request = buildGetRequest(path, queryParams);
        return execute(request, MAPPER.getTypeFactory().constructType(typeRef));
    }

    protected <T> T httpPost(String path, Object body, Class<T> dataType) {
        Request request = buildRequest("POST", path, body);
        return execute(request, MAPPER.getTypeFactory().constructType(dataType));
    }

    protected <T> T httpPost(String path, Object body, Class<T> dataType, Map<String, String> queryParams) {
        Request request = buildRequest("POST", path, body, queryParams);
        return execute(request, MAPPER.getTypeFactory().constructType(dataType));
    }

    protected <T> T httpPost(String path, Object body, TypeReference<T> typeRef, Map<String, String> queryParams) {
        Request request = buildRequest("POST", path, body, queryParams);
        return execute(request, MAPPER.getTypeFactory().constructType(typeRef));
    }

    protected <T> T httpPut(String path, Object body, Class<T> dataType) {
        Request request = buildRequest("PUT", path, body);
        return execute(request, MAPPER.getTypeFactory().constructType(dataType));
    }

    protected <T> T httpPut(String path, Object body, Class<T> dataType, Map<String, String> queryParams) {
        Request request = buildRequest("PUT", path, body, queryParams);
        return execute(request, MAPPER.getTypeFactory().constructType(dataType));
    }

    protected void httpDelete(String path) {
        Request request = buildRequest("DELETE", path, null);
        executeVoid(request);
    }

    protected void httpPostVoid(String path, Object body) {
        executeVoid(buildRequest("POST", path, body));
    }

    protected void httpPostVoid(String path, Object body, Map<String, String> queryParams) {
        executeVoid(buildRequest("POST", path, body, queryParams));
    }

    protected void httpPutVoid(String path, Object body) {
        executeVoid(buildRequest("PUT", path, body));
    }

    protected void httpPutVoid(String path, Object body, Map<String, String> queryParams) {
        executeVoid(buildRequest("PUT", path, body, queryParams));
    }

    protected byte[] httpGetBinary(String path) {
        return httpGetBinary(path, Collections.emptyMap());
    }

    protected byte[] httpGetBinary(String path, Map<String, String> queryParams) {
        Request request = buildGetRequest(path, queryParams);
        return executeBinary(request);
    }

    protected byte[] httpPostBinary(String path, RequestBody body) {
        return httpPostBinary(path, Collections.emptyMap(), body);
    }

    protected byte[] httpPostBinary(String path, Map<String, String> queryParams, RequestBody body) {
        HttpUrl url = buildUrl(path, queryParams);
        Request request = new Request.Builder().url(url).post(body).build();
        return executeBinary(request);
    }

    protected <T> T httpPostMultipart(String path, RequestBody multipartBody, Class<T> dataType) {
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .post(multipartBody)
                .build();
        return execute(request, MAPPER.getTypeFactory().constructType(dataType));
    }

    protected <T> PaginatedResult<T> httpGetList(String path, Class<T> itemType) {
        return httpGetList(path, Collections.emptyMap(), itemType);
    }

    protected <T> PaginatedResult<T> httpGetList(String path, Map<String, String> queryParams, Class<T> itemType) {
        Request request = buildGetRequest(path, queryParams);
        JavaType listType = MAPPER.getTypeFactory().constructCollectionType(List.class, itemType);
        return executeList(request, listType);
    }

    protected <T> T httpGetOptional(String path, Class<T> dataType) {
        try {
            return httpGet(path, dataType);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                return null;
            }
            throw e;
        }
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
                throw new ValidationException("Failed to serialize request body: " + e.getMessage());
            }
        } else if ("POST".equals(method) || "PUT".equals(method)) {
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
            return parseEnvelope(json, response.code(), dataType);
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
            if (!response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                String json = responseBody != null ? responseBody.string() : "";
                if (!json.isBlank()) {
                    tryThrowEnvelopeError(json, response.code());
                }
                throw new ApiException(response.code());
            }
        } catch (ApiException e) {
            throw e;
        } catch (IOException e) {
            throw new NetworkException("Network error: " + e.getMessage(), e);
        }
    }

    private byte[] executeBinary(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new ApiException(response.code());
            }
            ResponseBody responseBody = response.body();
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
            List<T> data = parseEnvelope(json, response.code(), listType);
            PaginationMeta meta = parsePaginationMeta(response);
            return new PaginatedResult<>(data != null ? data : Collections.emptyList(), meta);
        } catch (ApiException e) {
            throw e;
        } catch (IOException e) {
            throw new NetworkException("Network error: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T parseEnvelope(String json, int httpStatus, JavaType dataType) {
        if (json == null || json.isBlank()) {
            if (httpStatus >= 400) {
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
                if (root.has("data")) {
                    JsonNode dataNode = root.get("data");
                    if (dataNode.isNull()) {
                        return null;
                    }
                    return MAPPER.convertValue(dataNode, dataType);
                }
                return null;
            }
            if (httpStatus >= 400) {
                String message = root.has("message") ? root.get("message").asText(null) : null;
                throw new ApiException(httpStatus, message, json);
            }
            return MAPPER.convertValue(root, dataType);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new NetworkException("Failed to parse response: " + e.getMessage(), e);
        }
    }

    private void tryThrowEnvelopeError(String json, int httpStatus) {
        try {
            JsonNode root = MAPPER.readTree(json);
            if (root.has("status") && root.get("status").isNumber()) {
                int status = root.get("status").asInt();
                String message = root.has("message") ? root.get("message").asText(null) : null;
                if (status >= 400) {
                    throw new ApiException(status, message, json);
                }
            }
            String message = root.has("message") ? root.get("message").asText(null) : null;
            throw new ApiException(httpStatus, message, json);
        } catch (ApiException e) {
            throw e;
        } catch (Exception ignored) {
            throw new ApiException(httpStatus);
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
