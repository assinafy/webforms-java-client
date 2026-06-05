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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FieldResource extends BaseResource {

    public FieldResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    /** {@code POST /accounts/{account_id}/fields} — create a field definition. */
    public FieldDefinition create(CreateFieldPayload payload, String accountId) {
        validateCreatePayload(payload);
        String id = accountId(accountId);
        return httpPost("/accounts/" + id + "/fields", payload, FieldDefinition.class);
    }

    public FieldDefinition create(CreateFieldPayload payload) {
        return create(payload, null);
    }

    /** {@code GET /accounts/{account_id}/fields} — list field definitions for the workspace. */
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

    /** {@code GET /accounts/{account_id}/fields/{field_id}} — retrieve a single field definition. */
    public FieldDefinition get(String fieldId, String accountId) {
        String id = accountId(accountId);
        String fid = requireId(fieldId, "Field ID");
        return httpGet("/accounts/" + id + "/fields/" + fid, FieldDefinition.class);
    }

    public FieldDefinition get(String fieldId) {
        return get(fieldId, null);
    }

    /** {@code PUT /accounts/{account_id}/fields/{field_id}} — update a field definition. */
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

    /** {@code DELETE /accounts/{account_id}/fields/{field_id}} — delete a field definition. */
    public void delete(String fieldId, String accountId) {
        String id = accountId(accountId);
        String fid = requireId(fieldId, "Field ID");
        httpDelete("/accounts/" + id + "/fields/" + fid);
    }

    public void delete(String fieldId) {
        delete(fieldId, null);
    }

    /**
     * {@code POST /accounts/{account_id}/fields/{field_id}/validate} — validate a single value against a field
     * definition's type/regex rules. Returns {@code {type, success, error_message}}. A {@code null} value is
     * forwarded to the API (which decides validity) rather than rejected client-side. When provided, the
     * {@code signerAccessCode} is sent as the {@code signer-access-code} query parameter.
     */
    public FieldValidationResult validate(String fieldId, Object value, String signerAccessCode, String accountId) {
        String id = accountId(accountId);
        String fid = requireId(fieldId, "Field ID");
        Map<String, String> query = signerAccessCode != null && !signerAccessCode.isBlank()
                ? Map.of("signer-access-code", signerAccessCode)
                : Map.of();
        Map<String, Object> body = new HashMap<>();
        body.put("value", value);
        return httpPost("/accounts/" + id + "/fields/" + fid + "/validate",
                body, FieldValidationResult.class, query);
    }

    public FieldValidationResult validate(String fieldId, Object value, String signerAccessCode) {
        return validate(fieldId, value, signerAccessCode, null);
    }

    public FieldValidationResult validate(String fieldId, Object value) {
        return validate(fieldId, value, null, null);
    }

    /**
     * {@code POST /accounts/{account_id}/fields/validate-multiple} — validate several field/value pairs in one
     * call. The request body is a JSON array of {@code {field_id, value}} objects; the response is an array of
     * {@code {field_id, type, success, error_message}} results.
     */
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

    /** {@code GET /field-types} — list the available field types (e.g. {@code text}, {@code cpf}, {@code email}). */
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
