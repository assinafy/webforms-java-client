package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.CreateTagPayload;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Tag;
import com.assinafy.sdk.models.UpdateTagPayload;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.OkHttpClient;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TagResource extends BaseResource {

    public TagResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    /** {@code GET /accounts/{account_id}/tags} - list workspace tags. */
    public PaginatedResult<Tag> list(Map<String, String> params, String accountId) {
        String id = accountId(accountId);
        return httpGetList("/accounts/" + id + "/tags", params != null ? params : Map.of(), Tag.class);
    }

    public PaginatedResult<Tag> list(Map<String, String> params) {
        return list(params, null);
    }

    public PaginatedResult<Tag> list() {
        return list(null, null);
    }

    /** {@code POST /accounts/{account_id}/tags} - create a workspace tag. */
    public Tag create(CreateTagPayload payload, String accountId) {
        validateCreatePayload(payload);
        String id = accountId(accountId);
        return httpPost("/accounts/" + id + "/tags", payload, Tag.class);
    }

    public Tag create(CreateTagPayload payload) {
        return create(payload, null);
    }

    /** {@code PUT /accounts/{account_id}/tags/{tag_id}} - update a workspace tag. */
    public Tag update(String tagId, UpdateTagPayload payload, String accountId) {
        String id = accountId(accountId);
        String tid = requireId(tagId, "Tag ID");
        Map<String, Object> body = buildUpdateBody(payload);
        return httpPut("/accounts/" + id + "/tags/" + tid, body, Tag.class);
    }

    public Tag update(String tagId, UpdateTagPayload payload) {
        return update(tagId, payload, null);
    }

    /** {@code DELETE /accounts/{account_id}/tags/{tag_id}} - delete a workspace tag. */
    public Map<String, Object> delete(String tagId, boolean force, String accountId) {
        String id = accountId(accountId);
        String tid = requireId(tagId, "Tag ID");
        Map<String, String> query = force ? Map.of("force", "true") : Map.of();
        return httpDelete("/accounts/" + id + "/tags/" + tid,
                new TypeReference<Map<String, Object>>() {}, query);
    }

    public Map<String, Object> delete(String tagId, boolean force) {
        return delete(tagId, force, null);
    }

    public Map<String, Object> delete(String tagId) {
        return delete(tagId, false, null);
    }

    private void validateCreatePayload(CreateTagPayload payload) {
        if (payload == null) {
            throw new ValidationException("Tag payload is required");
        }
        if (payload.getName() == null || payload.getName().isBlank()) {
            throw new ValidationException("Tag name is required");
        }
    }

    private Map<String, Object> buildUpdateBody(UpdateTagPayload payload) {
        if (payload == null) {
            throw new ValidationException("Tag update payload is required");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        if (payload.getName() != null) {
            if (payload.getName().isBlank()) {
                throw new ValidationException("Tag name cannot be blank");
            }
            body.put("name", payload.getName());
        }
        if (payload.isColorSet()) {
            body.put("color", payload.getColor());
        }
        if (body.isEmpty()) {
            throw new ValidationException("At least one tag attribute is required");
        }
        return body;
    }
}
