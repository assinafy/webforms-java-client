package com.assinafy.sdk.resources;

import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.TemplateDetails;
import com.assinafy.sdk.models.TemplateListItem;
import okhttp3.OkHttpClient;

import java.util.Map;

/** Client for listing and retrieving workspace templates. */
public final class TemplateResource extends BaseResource {

    /**
     * Creates an instance.
     *
     * @param httpClient shared HTTP client
     * @param baseUrl API base URL
     * @param defaultAccountId default account identifier, or {@code null}
     */
    public TemplateResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    /**
     * {@code GET /accounts/{account_id}/templates} — list the workspace's templates. Supports the documented
     * {@code search} query parameter plus pagination.
     *
     * @param params optional search and pagination values
     * @param accountId account override, or {@code null} for the client default
     * @return template page
     */
    public PaginatedResult<TemplateListItem> list(Map<String, String> params, String accountId) {
        String id = accountId(accountId);
        return httpGetList("/accounts/" + id + "/templates",
                params != null ? params : Map.of(), TemplateListItem.class);
    }

    /**
     * Returns default account's template page.
     *
     * @param params optional search and pagination values
     * @return default account's template page
     */
    public PaginatedResult<TemplateListItem> list(Map<String, String> params) {
        return list(params, null);
    }

    /**
     * Returns default account's first template page.
     *
     * @return default account's first template page
     */
    public PaginatedResult<TemplateListItem> list() {
        return list(null, null);
    }

    /**
     * {@code GET /accounts/{account_id}/templates/{template_id}} — retrieve a single template, including its
     * roles, pages, field placements, tags, and default document tags.
     *
     * @param templateId required template identifier
     * @param accountId account override, or {@code null} for the client default
     * @return full template details
     */
    public TemplateDetails get(String templateId, String accountId) {
        String id = accountId(accountId);
        String tmplId = requireId(templateId, "Template ID");
        return httpGet("/accounts/" + id + "/templates/" + tmplId, TemplateDetails.class);
    }

    /**
     * Returns full template details in the default account.
     *
     * @param templateId required template identifier
     * @return full template details in the default account
     */
    public TemplateDetails get(String templateId) {
        return get(templateId, null);
    }
}
