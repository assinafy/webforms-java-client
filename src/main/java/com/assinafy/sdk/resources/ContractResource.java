package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.ContractRenewalPayload;
import com.assinafy.sdk.models.ContractTerminationPayload;
import com.assinafy.sdk.models.DocumentContract;
import com.assinafy.sdk.models.DocumentContractAmendment;
import com.assinafy.sdk.models.DocumentContractInput;
import com.assinafy.sdk.models.DocumentContractResult;
import com.assinafy.sdk.models.PaginatedResult;
import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.OkHttpClient;

import java.util.Map;

/** Client for contract terms tracked on workspace documents. */
public final class ContractResource extends BaseResource {

    /**
     * Creates an instance.
     *
     * @param httpClient shared HTTP client
     * @param baseUrl API base URL
     * @param defaultAccountId default account identifier, or {@code null}
     */
    public ContractResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    /**
     * {@code GET /accounts/{accountId}/contracts} — list tracked contracts, newest update first by default.
     * Supported filters are {@code status}, {@code counterparty}, {@code search}, {@code end_date_from},
     * {@code end_date_to}, {@code ending_within_days}, {@code external_system}, {@code external_id},
     * {@code sort}, {@code expand}, {@code page}, and {@code per-page}.
     *
     * @param params optional filters, expansion, sorting, and pagination
     * @param accountId account override, or {@code null} for the client default
     * @return contract page and response-header pagination metadata
     */
    public PaginatedResult<DocumentContract> list(Map<String, String> params, String accountId) {
        String id = accountId(accountId);
        return httpGetList("/accounts/" + id + "/contracts", params != null ? params : Map.of(),
                DocumentContract.class);
    }

    /**
     * {@code GET /accounts/{accountId}/contracts} in the default account.
     *
     * @param params optional filters, expansion, sorting, and pagination
     * @return contract page and response-header pagination metadata
     */
    public PaginatedResult<DocumentContract> list(Map<String, String> params) { return list(params, null); }

    /**
     * {@code GET /accounts/{accountId}/contracts} in the default account with no filters.
     *
     * @return first contract page and response-header pagination metadata
     */
    public PaginatedResult<DocumentContract> list() { return list(null, null); }

    /**
     * {@code GET /accounts/{accountId}/documents/{documentId}/contract} — read a document's own contract terms
     * or, for an amendment instrument, its pending/applied link and delta.
     *
     * @param documentId required document identifier
     * @param accountId account override, or {@code null} for the client default
     * @return {@link DocumentContract} or {@link com.assinafy.sdk.models.DocumentContractAmendment}
     */
    public DocumentContractResult get(String documentId, String accountId) {
        return contractResult(httpGet(contractPath(documentId, accountId), JsonNode.class));
    }

    /**
     * {@code GET /accounts/{accountId}/documents/{documentId}/contract} in the default account.
     *
     * @param documentId required document identifier
     * @return document contract terms or amendment link
     */
    public DocumentContractResult get(String documentId) { return get(documentId, null); }

    /**
     * {@code PUT /accounts/{accountId}/documents/{documentId}/contract} — create or partially update terms,
     * or link the document as an amendment, renewal, or termination instrument. An explicitly supplied
     * {@code null} clears the corresponding property where supported; {@code updated_at} enables optimistic
     * concurrency and produces HTTP 409 when stale.
     *
     * @param documentId required document identifier
     * @param input required non-empty partial terms or amendment link
     * @param accountId account override, or {@code null} for the client default
     * @return saved contract terms or amendment link
     * @throws ValidationException when the input is null or empty
     */
    public DocumentContractResult set(String documentId, DocumentContractInput input, String accountId) {
        if (input == null || input.isEmpty()) {
            throw new ValidationException("At least one contract property is required");
        }
        return contractResult(httpPut(contractPath(documentId, accountId), input, JsonNode.class));
    }

    /**
     * {@code PUT /accounts/{accountId}/documents/{documentId}/contract} in the default account.
     *
     * @param documentId required document identifier
     * @param input required non-empty partial terms or amendment link
     * @return saved contract terms or amendment link
     */
    public DocumentContractResult set(String documentId, DocumentContractInput input) {
        return set(documentId, input, null);
    }

    /**
     * {@code GET /accounts/{accountId}/documents/{documentId}/contract/related} — read the root contract and
     * every amendment, renewal, and termination instrument in its family.
     *
     * @param documentId contract or amendment document identifier
     * @param accountId account override, or {@code null} for the client default
     * @return root contract and its amendment family
     */
    public DocumentContract related(String documentId, String accountId) {
        return httpGet(contractPath(documentId, accountId) + "/related", DocumentContract.class);
    }

    /**
     * {@code GET /accounts/{accountId}/documents/{documentId}/contract/related} in the default account.
     *
     * @param documentId contract or amendment document identifier
     * @return root contract and its amendment family
     */
    public DocumentContract related(String documentId) { return related(documentId, null); }

    /**
     * {@code POST /accounts/{accountId}/documents/{documentId}/contract/renew} — start the next term. Supply
     * either the current {@code term} or an explicit later {@code end_date}; {@code updated_at} rejects a stale
     * write with HTTP 409.
     *
     * @param documentId required contract document identifier
     * @param payload optional renewal values, or {@code null} for an empty body
     * @param accountId account override, or {@code null} for the client default
     * @return renewed contract
     */
    public DocumentContract renew(String documentId, ContractRenewalPayload payload, String accountId) {
        return httpPost(contractPath(documentId, accountId) + "/renew", payload, DocumentContract.class);
    }

    /**
     * {@code POST /accounts/{accountId}/documents/{documentId}/contract/renew} in the default account.
     *
     * @param documentId required contract document identifier
     * @param payload optional renewal values, or {@code null} for an empty body
     * @return renewed contract
     */
    public DocumentContract renew(String documentId, ContractRenewalPayload payload) {
        return renew(documentId, payload, null);
    }

    /**
     * {@code POST /accounts/{accountId}/documents/{documentId}/contract/renew} in the default account with an
     * empty body. This is valid only when the server can advance from the stored renewal term.
     *
     * @param documentId required contract document identifier
     * @return renewed contract
     */
    public DocumentContract renew(String documentId) { return renew(documentId, null, null); }

    /**
     * {@code POST /accounts/{accountId}/documents/{documentId}/contract/terminate} — close the contract and
     * stop lifecycle alerts.
     *
     * @param documentId required contract document identifier
     * @param payload optional reason and optimistic-concurrency timestamp, or {@code null}
     * @param accountId account override, or {@code null} for the client default
     * @return terminated contract
     * @throws ValidationException when the reason is longer than 500 characters
     */
    public DocumentContract terminate(String documentId, ContractTerminationPayload payload, String accountId) {
        if (payload != null && payload.getReason() != null && payload.getReason().length() > 500) {
            throw new ValidationException("Contract termination reason must be at most 500 characters");
        }
        return httpPost(contractPath(documentId, accountId) + "/terminate", payload, DocumentContract.class);
    }

    /**
     * {@code POST /accounts/{accountId}/documents/{documentId}/contract/terminate} in the default account.
     *
     * @param documentId required contract document identifier
     * @param payload optional reason and optimistic-concurrency timestamp, or {@code null}
     * @return terminated contract
     */
    public DocumentContract terminate(String documentId, ContractTerminationPayload payload) {
        return terminate(documentId, payload, null);
    }

    /**
     * {@code POST /accounts/{accountId}/documents/{documentId}/contract/terminate} in the default account with
     * an empty body.
     *
     * @param documentId required contract document identifier
     * @return terminated contract
     */
    public DocumentContract terminate(String documentId) { return terminate(documentId, null, null); }

    private String contractPath(String documentId, String accountId) {
        String id = accountId(accountId);
        String docId = requireId(documentId, "Document ID");
        return "/accounts/" + id + "/documents/" + docId + "/contract";
    }

    private static DocumentContractResult contractResult(JsonNode data) {
        if (data == null || data.isNull()) {
            return null;
        }
        Class<? extends DocumentContractResult> type = "document_contract_amendment".equals(
                data.path("resource").asText()) || data.has("parent_document_id")
                ? DocumentContractAmendment.class : DocumentContract.class;
        return MAPPER.convertValue(data, type);
    }
}
