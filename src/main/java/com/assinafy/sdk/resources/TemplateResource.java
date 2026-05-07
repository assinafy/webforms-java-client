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

    public TemplateDetails get(String templateId, String accountId) {
        String id = accountId(accountId);
        String tmplId = requireId(templateId, "Template ID");
        return httpGet("/accounts/" + id + "/templates/" + tmplId, TemplateDetails.class);
    }

    public TemplateDetails get(String templateId) {
        return get(templateId, null);
    }
}
