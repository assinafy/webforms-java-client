package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.CreateFieldPayload;
import com.assinafy.sdk.models.FieldDefinition;
import com.assinafy.sdk.models.FieldTypeInfo;
import com.assinafy.sdk.models.FieldValidationPayload;
import com.assinafy.sdk.models.FieldValidationResult;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.UpdateFieldPayload;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.OkHttpClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class FieldResource extends BaseResource {

    public FieldResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    public FieldDefinition create(CreateFieldPayload payload, String accountId) {
        validateCreatePayload(payload);
        String id = accountId(accountId);
        return httpPost("/accounts/" + id + "/fields", payload, FieldDefinition.class);
    }

    public FieldDefinition create(CreateFieldPayload payload) {
        return create(payload, null);
    }

    public PaginatedResult<FieldDefinition> list(Map<String, String> params, String accountId) {
        String id = accountId(accountId);
        return httpGetList("/accounts/" + id + "/fields",
                params != null ? params : Map.of(), FieldDefinition.class);
    }

    public PaginatedResult<FieldDefinition> list(Map<String, String> params) {
        return list(params, null);
    }

    public PaginatedResult<FieldDefinition> list() {
        return list(null, null);
    }

    public FieldDefinition get(String fieldId, String accountId) {
        String id = accountId(accountId);
        String fid = requireId(fieldId, "Field ID");
        return httpGet("/accounts/" + id + "/fields/" + fid, FieldDefinition.class);
    }

    public FieldDefinition get(String fieldId) {
        return get(fieldId, null);
    }

    public FieldDefinition update(String fieldId, UpdateFieldPayload payload, String accountId) {
        if (payload == null) {
            throw new ValidationException("Field update payload is required");
        }
        String id = accountId(accountId);
        String fid = requireId(fieldId, "Field ID");
        return httpPut("/accounts/" + id + "/fields/" + fid, payload, FieldDefinition.class);
    }

    public FieldDefinition update(String fieldId, UpdateFieldPayload payload) {
        return update(fieldId, payload, null);
    }

    public void delete(String fieldId, String accountId) {
        String id = accountId(accountId);
        String fid = requireId(fieldId, "Field ID");
        httpDelete("/accounts/" + id + "/fields/" + fid);
    }

    public void delete(String fieldId) {
        delete(fieldId, null);
    }

    public FieldValidationResult validate(String fieldId, Object value, String signerAccessCode, String accountId) {
        String id = accountId(accountId);
        String fid = requireId(fieldId, "Field ID");
        Map<String, String> query = signerAccessCode != null && !signerAccessCode.isBlank()
                ? Map.of("signer-access-code", signerAccessCode)
                : Map.of();
        return httpPost("/accounts/" + id + "/fields/" + fid + "/validate",
                Map.of("value", value), FieldValidationResult.class, query);
    }

    public FieldValidationResult validate(String fieldId, Object value, String signerAccessCode) {
        return validate(fieldId, value, signerAccessCode, null);
    }

    public FieldValidationResult validate(String fieldId, Object value) {
        return validate(fieldId, value, null, null);
    }

    public List<FieldValidationResult> validateMultiple(List<FieldValidationPayload> values,
            String signerAccessCode, String accountId) {
        if (values == null || values.isEmpty()) {
            throw new ValidationException("At least one field validation value is required");
        }
        String id = accountId(accountId);
        Map<String, String> query = signerAccessCode != null && !signerAccessCode.isBlank()
                ? Map.of("signer-access-code", signerAccessCode)
                : Map.of();
        List<FieldValidationResult> result = httpPost("/accounts/" + id + "/fields/validate-multiple",
                values, new TypeReference<List<FieldValidationResult>>() {}, query);
        return result != null ? result : Collections.emptyList();
    }

    public List<FieldValidationResult> validateMultiple(List<FieldValidationPayload> values,
            String signerAccessCode) {
        return validateMultiple(values, signerAccessCode, null);
    }

    public List<FieldValidationResult> validateMultiple(List<FieldValidationPayload> values) {
        return validateMultiple(values, null, null);
    }

    public List<FieldTypeInfo> listTypes() {
        List<FieldTypeInfo> result = httpGet("/field-types", new TypeReference<List<FieldTypeInfo>>() {});
        return result != null ? result : Collections.emptyList();
    }

    private void validateCreatePayload(CreateFieldPayload payload) {
        if (payload == null) {
            throw new ValidationException("Field payload is required");
        }
        if (payload.getType() == null || payload.getType().isBlank()) {
            throw new ValidationException("Field type is required");
        }
        if (payload.getName() == null || payload.getName().isBlank()) {
            throw new ValidationException("Field name is required");
        }
    }
}
