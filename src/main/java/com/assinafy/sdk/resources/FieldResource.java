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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Client for custom field definitions, field types, and value-validation endpoints. */
public final class FieldResource extends BaseResource {

    /**
     * Creates an instance.
     *
     * @param httpClient shared HTTP client
     * @param baseUrl API base URL
     * @param defaultAccountId default account identifier, or {@code null}
     */
    public FieldResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    /**
     * {@code POST /accounts/{account_id}/fields}.
     *
     * @param payload required field definition
     * @param accountId account override, or {@code null} for the client default
     * @return created field definition
     */
    public FieldDefinition create(CreateFieldPayload payload, String accountId) {
        validateCreatePayload(payload);
        String id = accountId(accountId);
        return httpPost("/accounts/" + id + "/fields", payload, FieldDefinition.class);
    }

    /**
     * Returns created field definition in the default account.
     *
     * @param payload required field definition
     * @return created field definition in the default account
     */
    public FieldDefinition create(CreateFieldPayload payload) {
        return create(payload, null);
    }

    /**
     * {@code GET /accounts/{account_id}/fields} — list field definitions for the workspace. Supported query
     * parameters: {@code include_standard} (add the built-in signature/initial/signatureDate types) and
     * {@code include_inactive}. Note this endpoint returns all matching fields and does not paginate.
     *
     * @param params optional {@code include_standard} and {@code include_inactive} values
     * @param accountId account override, or {@code null} for the client default
     * @return matching fields wrapped in a page result
     */
    public PaginatedResult<FieldDefinition> list(Map<String, String> params, String accountId) {
        String id = accountId(accountId);
        return httpGetList("/accounts/" + id + "/fields",
                params != null ? params : Map.of(), FieldDefinition.class);
    }

    /**
     * Returns default account's matching fields.
     *
     * @param params optional inclusion filters
     * @return default account's matching fields
     */
    public PaginatedResult<FieldDefinition> list(Map<String, String> params) {
        return list(params, null);
    }

    /**
     * Returns default account's active custom field definitions.
     *
     * @return default account's active custom field definitions
     */
    public PaginatedResult<FieldDefinition> list() {
        return list(null, null);
    }

    /**
     * {@code GET /accounts/{account_id}/fields/{field_id}}.
     *
     * @param fieldId required field identifier
     * @param accountId account override, or {@code null} for the client default
     * @return field definition
     */
    public FieldDefinition get(String fieldId, String accountId) {
        String id = accountId(accountId);
        String fid = requireId(fieldId, "Field ID");
        return httpGet("/accounts/" + id + "/fields/" + fid, FieldDefinition.class);
    }

    /**
     * Returns field definition in the default account.
     *
     * @param fieldId required field identifier
     * @return field definition in the default account
     */
    public FieldDefinition get(String fieldId) {
        return get(fieldId, null);
    }

    /**
     * {@code PUT /accounts/{account_id}/fields/{field_id}}.
     *
     * @param fieldId required field identifier
     * @param payload required partial update
     * @param accountId account override, or {@code null} for the client default
     * @return updated field definition
     */
    public FieldDefinition update(String fieldId, UpdateFieldPayload payload, String accountId) {
        String id = accountId(accountId);
        String fid = requireId(fieldId, "Field ID");
        return httpPut("/accounts/" + id + "/fields/" + fid, buildUpdateBody(payload), FieldDefinition.class);
    }

    /**
     * Returns updated field definition in the default account.
     *
     * @param fieldId required field identifier
     * @param payload required partial update
     * @return updated field definition in the default account
     */
    public FieldDefinition update(String fieldId, UpdateFieldPayload payload) {
        return update(fieldId, payload, null);
    }

    /**
     * {@code DELETE /accounts/{account_id}/fields/{field_id}}.
     *
     * @param fieldId required field identifier
     * @param accountId account override, or {@code null} for the client default
     */
    public void delete(String fieldId, String accountId) {
        String id = accountId(accountId);
        String fid = requireId(fieldId, "Field ID");
        httpDelete("/accounts/" + id + "/fields/" + fid);
    }

    /**
     * Deletes the selected resource.
     *
     * @param fieldId required field identifier in the default account
     */
    public void delete(String fieldId) {
        delete(fieldId, null);
    }

    /**
     * {@code POST /accounts/{account_id}/fields/{field_id}/validate} — validate a single value against a field
     * definition's type/regex rules. Returns {@code {type, success, error_message}}. A {@code null} value is
     * forwarded to the API (which decides validity) rather than rejected client-side. When provided, the
     * {@code signerAccessCode} is sent as the {@code signer-access-code} query parameter.
     *
     * @param fieldId required field identifier
     * @param value value to validate; may be {@code null}
     * @param signerAccessCode optional signer access code
     * @param accountId account override, or {@code null} for the client default
     * @return validation result
     */
    public FieldValidationResult validate(String fieldId, Object value, String signerAccessCode, String accountId) {
        String id = accountId(accountId);
        String fid = requireId(fieldId, "Field ID");
        Map<String, String> query = optionalSignerAccessCodeQuery(signerAccessCode);
        Map<String, Object> body = new HashMap<>();
        body.put("value", value);
        return httpPost("/accounts/" + id + "/fields/" + fid + "/validate",
                body, FieldValidationResult.class, query);
    }

    /**
     * Returns validation result in the default account.
     *
     * @param fieldId required field identifier
     * @param value value to validate; may be {@code null}
     * @param signerAccessCode optional signer access code
     * @return validation result in the default account
     */
    public FieldValidationResult validate(String fieldId, Object value, String signerAccessCode) {
        return validate(fieldId, value, signerAccessCode, null);
    }

    /**
     * Returns validation result in the default account.
     *
     * @param fieldId required field identifier
     * @param value value to validate; may be {@code null}
     * @return validation result in the default account
     */
    public FieldValidationResult validate(String fieldId, Object value) {
        return validate(fieldId, value, null, null);
    }

    /**
     * {@code POST /accounts/{account_id}/fields/validate-multiple} — validate several field/value pairs in one
     * call. The request body is a JSON array of {@code {field_id, value}} objects; the response is an array of
     * {@code {field_id, type, success, error_message}} results.
     *
     * @param values one or more field/value pairs
     * @param signerAccessCode optional signer access code
     * @param accountId account override, or {@code null} for the client default
     * @return validation results in request order
     */
    public List<FieldValidationResult> validateMultiple(List<FieldValidationPayload> values,
            String signerAccessCode, String accountId) {
        if (values == null || values.isEmpty()) {
            throw new ValidationException("At least one field validation value is required");
        }
        if (values.stream().anyMatch(value -> value == null || value.getFieldId() == null
                || value.getFieldId().isBlank())) {
            throw new ValidationException("Every field validation value must have a field ID");
        }
        String id = accountId(accountId);
        Map<String, String> query = optionalSignerAccessCodeQuery(signerAccessCode);
        return orEmpty(httpPost("/accounts/" + id + "/fields/validate-multiple", values,
                new TypeReference<List<FieldValidationResult>>() {}, query));
    }

    /**
     * Returns validation results in the default account.
     *
     * @param values one or more field/value pairs
     * @param signerAccessCode optional signer access code
     * @return validation results in the default account
     */
    public List<FieldValidationResult> validateMultiple(List<FieldValidationPayload> values,
            String signerAccessCode) {
        return validateMultiple(values, signerAccessCode, null);
    }

    /**
     * Returns validation results in the default account.
     *
     * @param values one or more field/value pairs
     * @return validation results in the default account
     */
    public List<FieldValidationResult> validateMultiple(List<FieldValidationPayload> values) {
        return validateMultiple(values, null, null);
    }

    /**
     * {@code GET /field-types}.
     *
     * @return available field types, never {@code null}
     */
    public List<FieldTypeInfo> listTypes() {
        return orEmpty(httpGet("/field-types", new TypeReference<List<FieldTypeInfo>>() {}));
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

    private Map<String, Object> buildUpdateBody(UpdateFieldPayload payload) {
        if (payload == null) {
            throw new ValidationException("Field update payload is required");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        if (payload.getType() != null) {
            if (payload.getType().isBlank()) throw new ValidationException("Field type cannot be blank");
            body.put("type", payload.getType());
        }
        if (payload.getName() != null) {
            if (payload.getName().isBlank()) throw new ValidationException("Field name cannot be blank");
            body.put("name", payload.getName());
        }
        if (payload.isRegexSet()) body.put("regex", payload.getRegex());
        if (payload.getRequired() != null) body.put("is_required", payload.getRequired());
        if (payload.getActive() != null) body.put("is_active", payload.getActive());
        if (body.isEmpty()) throw new ValidationException("At least one field attribute is required");
        return body;
    }
}
