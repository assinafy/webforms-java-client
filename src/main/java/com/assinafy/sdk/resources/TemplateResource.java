package com.assinafy.sdk.resources;

import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.TemplateDetails;
import com.assinafy.sdk.models.TemplateListItem;
import okhttp3.OkHttpClient;

import java.util.Map;

public final class TemplateResource extends BaseResource {

    public TemplateResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    /**
     * {@code GET /accounts/{account_id}/templates} — list the workspace's templates. Supports the documented
     * {@code search} query parameter plus pagination.
     */
    public PaginatedResult<TemplateListItem> list(Map<String, String> params, String accountId) {
        String id = accountId(accountId);
        return httpGetList("/accounts/" + id + "/templates",
                params != null ? params : Map.of(), TemplateListItem.class);
    }

    public PaginatedResult<TemplateListItem> list(Map<String, String> params) {
        return list(params, null);
    }

    public PaginatedResult<TemplateListItem> list() {
        return list(null, null);
    }

    /**
     * {@code GET /accounts/{account_id}/templates/{template_id}} — retrieve a single template, including its
     * roles, pages, field placements, tags, and default document tags.
     */
    public TemplateDetails get(String templateId, String accountId) {
        String id = accountId(accountId);
        String tmplId = requireId(templateId, "Template ID");
        return httpGet("/accounts/" + id + "/templates/" + tmplId, TemplateDetails.class);
    }

    public TemplateDetails get(String templateId) {
        return get(templateId, null);
    }
}
