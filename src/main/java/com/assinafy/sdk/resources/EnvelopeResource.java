package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.CostEstimate;
import com.assinafy.sdk.models.CreateEnvelopePayload;
import com.assinafy.sdk.models.DocumentActivity;
import com.assinafy.sdk.models.DocumentEnvelope;
import com.assinafy.sdk.models.EnvelopeCancellation;
import com.assinafy.sdk.models.EnvelopeDocumentPayload;
import com.assinafy.sdk.models.EnvelopeResend;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.SignerRef;
import com.assinafy.sdk.models.UpdateEnvelopePayload;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.OkHttpClient;

import java.util.List;
import java.util.Map;

/** Workspace envelopes: multiple documents with shared invitations and individual signatures. */
public final class EnvelopeResource extends BaseResource {

    /**
     * Creates the resource.
     * @param httpClient shared transport
     * @param baseUrl API base URL
     * @param defaultAccountId default workspace
     */
    public EnvelopeResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    /**
     * {@code GET /accounts/{accountId}/envelopes}. Supports search, status, page and per-page.
     * @param params optional query filters
     * @param accountId workspace override, or null
     * @return envelope page
     */
    public PaginatedResult<DocumentEnvelope> list(Map<String, String> params, String accountId) {
        return httpGetList(path(accountId), params, DocumentEnvelope.class);
    }

    /**
     * {@code GET /accounts/{accountId}/envelopes} in the default workspace.
     * @param params optional query filters
     * @return envelope page
     */
    public PaginatedResult<DocumentEnvelope> list(Map<String, String> params) { return list(params, null); }

    /**
     * {@code GET /accounts/{accountId}/envelopes} in the default workspace.
     * @return first envelope page
     */
    public PaginatedResult<DocumentEnvelope> list() { return list(null, null); }

    /**
     * {@code POST /accounts/{accountId}/envelopes}. A successful response does not promise notification delivery.
     * Reuse an idempotency key only when intentionally recovering the same creation after a lost response.
     * @param payload documents and signers
     * @param idempotencyKey optional workspace-scoped key that does not expire
     * @param accountId workspace override, or null
     * @return created envelope
     */
    public DocumentEnvelope create(CreateEnvelopePayload payload, String idempotencyKey, String accountId) {
        validate(payload);
        return httpPostIdempotent(path(accountId), payload, DocumentEnvelope.class, idempotencyKey);
    }

    /**
     * {@code POST /accounts/{accountId}/envelopes} in the default workspace.
     * @param payload documents and signers
     * @param idempotencyKey optional creation key
     * @return created envelope
     */
    public DocumentEnvelope create(CreateEnvelopePayload payload, String idempotencyKey) {
        return create(payload, idempotencyKey, null);
    }

    /**
     * {@code POST /accounts/{accountId}/envelopes} without an idempotency key.
     * @param payload documents and signers
     * @return created envelope
     */
    public DocumentEnvelope create(CreateEnvelopePayload payload) { return create(payload, null, null); }

    /**
     * {@code POST /accounts/{accountId}/envelopes/estimate-cost}. Creates and charges nothing.
     * @param payload creation values; the spending ceiling is ignored
     * @param accountId workspace override, or null
     * @return price and balances
     */
    public CostEstimate estimateCost(CreateEnvelopePayload payload, String accountId) {
        validate(payload);
        return httpPost(path(accountId) + "/estimate-cost", payload, CostEstimate.class);
    }

    /**
     * {@code POST /accounts/{accountId}/envelopes/estimate-cost} in the default workspace.
     * @param payload creation values
     * @return price and balances
     */
    public CostEstimate estimateCost(CreateEnvelopePayload payload) { return estimateCost(payload, null); }

    /**
     * {@code GET /accounts/{accountId}/envelopes/{envelopeId}}.
     * @param envelopeId envelope identifier
     * @param accountId workspace override, or null
     * @return envelope with document progress
     */
    public DocumentEnvelope get(String envelopeId, String accountId) {
        return httpGet(path(envelopeId, accountId), DocumentEnvelope.class);
    }

    /**
     * {@code GET /accounts/{accountId}/envelopes/{envelopeId}} in the default workspace.
     * @param envelopeId envelope identifier
     * @return envelope
     */
    public DocumentEnvelope get(String envelopeId) { return get(envelopeId, null); }

    /**
     * {@code PATCH /accounts/{accountId}/envelopes/{envelopeId}}. Sends no notifications.
     * @param envelopeId envelope identifier
     * @param payload name, message or complete document order
     * @param accountId workspace override, or null
     * @return updated envelope
     */
    public DocumentEnvelope update(String envelopeId, UpdateEnvelopePayload payload, String accountId) {
        if (payload == null || payload.isEmpty()) throw new ValidationException("Envelope update is required");
        return httpPatch(path(envelopeId, accountId), payload, DocumentEnvelope.class);
    }

    /**
     * {@code PATCH /accounts/{accountId}/envelopes/{envelopeId}} in the default workspace.
     * @param envelopeId envelope identifier
     * @param payload partial update
     * @return updated envelope
     */
    public DocumentEnvelope update(String envelopeId, UpdateEnvelopePayload payload) {
        return update(envelopeId, payload, null);
    }

    /**
     * {@code DELETE /accounts/{accountId}/envelopes/{envelopeId}}. All documents must be closed; they remain.
     * @param envelopeId envelope identifier
     * @param accountId workspace override, or null
     */
    public void delete(String envelopeId, String accountId) { httpDelete(path(envelopeId, accountId)); }

    /**
     * {@code DELETE /accounts/{accountId}/envelopes/{envelopeId}} in the default workspace.
     * @param envelopeId envelope identifier
     */
    public void delete(String envelopeId) { delete(envelopeId, null); }

    /**
     * {@code GET /accounts/{accountId}/envelopes/{envelopeId}/activities}. Includes removed documents.
     * @param envelopeId envelope identifier
     * @param params optional sort, page and per-page
     * @param accountId workspace override, or null
     * @return activities with document identifiers and pagination
     */
    public PaginatedResult<DocumentActivity> activities(String envelopeId, Map<String, String> params,
                                                       String accountId) {
        return httpGetList(path(envelopeId, accountId) + "/activities", params, DocumentActivity.class);
    }

    /**
     * {@code GET /accounts/{accountId}/envelopes/{envelopeId}/activities} in the default workspace.
     * @param envelopeId envelope identifier
     * @param params optional pagination and sort
     * @return activity page
     */
    public PaginatedResult<DocumentActivity> activities(String envelopeId, Map<String, String> params) {
        return activities(envelopeId, params, null);
    }

    /**
     * {@code GET /accounts/{accountId}/envelopes/{envelopeId}/download/{artifactName}}.
     * @param envelopeId envelope identifier
     * @param artifactName original, certificated, certificate-page, pades or bundle
     * @param accountId workspace override, or null
     * @return ZIP bytes; 404 when no document has the artifact
     */
    public byte[] download(String envelopeId, String artifactName, String accountId) {
        return httpGetBinary(path(envelopeId, accountId) + "/download/" + requireId(artifactName, "Artifact"));
    }

    /**
     * {@code GET /accounts/{accountId}/envelopes/{envelopeId}/download/{artifactName}} in the default workspace.
     * @param envelopeId envelope identifier
     * @param artifactName artifact name
     * @return ZIP bytes
     */
    public byte[] download(String envelopeId, String artifactName) { return download(envelopeId, artifactName, null); }

    /**
     * {@code PUT /accounts/{accountId}/envelopes/{envelopeId}/cancel}. Notifies affected owners and recipients.
     * @param envelopeId envelope identifier
     * @param accountId workspace override, or null
     * @return each document's cancellation result, never null
     */
    public List<EnvelopeCancellation> cancel(String envelopeId, String accountId) {
        return orEmpty(httpPut(path(envelopeId, accountId) + "/cancel", null,
                new TypeReference<List<EnvelopeCancellation>>() {}, Map.of()));
    }

    /**
     * {@code PUT /accounts/{accountId}/envelopes/{envelopeId}/cancel} in the default workspace.
     * @param envelopeId envelope identifier
     * @return cancellation results
     */
    public List<EnvelopeCancellation> cancel(String envelopeId) { return cancel(envelopeId, null); }

    /**
     * {@code PUT /accounts/{accountId}/envelopes/{envelopeId}/signers/{signerId}/resend}. May spend credits.
     * @param envelopeId envelope identifier
     * @param signerId signer identifier
     * @param accountId workspace override, or null
     * @return reminder delivery result
     */
    public EnvelopeResend resend(String envelopeId, String signerId, String accountId) {
        return httpPut(path(envelopeId, accountId) + "/signers/" + requireId(signerId, "Signer ID") + "/resend",
                null, EnvelopeResend.class);
    }

    /**
     * {@code PUT /accounts/{accountId}/envelopes/{envelopeId}/signers/{signerId}/resend} in the default workspace.
     * @param envelopeId envelope identifier
     * @param signerId signer identifier
     * @return reminder delivery result
     */
    public EnvelopeResend resend(String envelopeId, String signerId) { return resend(envelopeId, signerId, null); }

    /**
     * {@code POST /accounts/{accountId}/envelopes/{envelopeId}/documents}. Notifies selected envelope signers.
     * @param envelopeId envelope identifier
     * @param payload unassigned document and existing envelope signers
     * @param accountId workspace override, or null
     * @return envelope including the new document
     */
    public DocumentEnvelope addDocument(String envelopeId, EnvelopeDocumentPayload payload, String accountId) {
        validateDocument(payload);
        return httpPost(path(envelopeId, accountId) + "/documents", payload, DocumentEnvelope.class);
    }

    /**
     * {@code POST /accounts/{accountId}/envelopes/{envelopeId}/documents} in the default workspace.
     * @param envelopeId envelope identifier
     * @param payload document values
     * @return updated envelope
     */
    public DocumentEnvelope addDocument(String envelopeId, EnvelopeDocumentPayload payload) {
        return addDocument(envelopeId, payload, null);
    }

    /**
     * {@code DELETE /accounts/{accountId}/envelopes/{envelopeId}/documents/{documentId}}. Cancels the document.
     * @param envelopeId envelope identifier
     * @param documentId document identifier
     * @param accountId workspace override, or null
     * @return envelope without the document
     */
    public DocumentEnvelope removeDocument(String envelopeId, String documentId, String accountId) {
        return httpDelete(path(envelopeId, accountId) + "/documents/" + requireId(documentId, "Document ID"),
                null, DocumentEnvelope.class);
    }

    /**
     * {@code DELETE /accounts/{accountId}/envelopes/{envelopeId}/documents/{documentId}} in the default workspace.
     * @param envelopeId envelope identifier
     * @param documentId document identifier
     * @return updated envelope
     */
    public DocumentEnvelope removeDocument(String envelopeId, String documentId) {
        return removeDocument(envelopeId, documentId, null);
    }

    private String path(String accountId) { return "/accounts/" + accountId(accountId) + "/envelopes"; }
    private String path(String envelopeId, String accountId) {
        return path(accountId) + "/" + requireId(envelopeId, "Envelope ID");
    }

    private void validate(CreateEnvelopePayload payload) {
        if (payload == null) throw new ValidationException("Envelope payload is required");
        requireValue(payload.getName(), "Envelope name");
        if (payload.getName().length() > 255 || payload.getName().contains("<")) {
            throw new ValidationException("Envelope name must be at most 255 characters and cannot contain '<'");
        }
        if (payload.getSigners() == null || payload.getSigners().isEmpty() || payload.getSigners().size() > 20
                || payload.getDocuments() == null || payload.getDocuments().isEmpty()
                || payload.getDocuments().size() > 20) {
            throw new ValidationException("An envelope requires 1–20 signers and 1–20 documents");
        }
        for (SignerRef signer : payload.getSigners()) {
            if (signer == null) throw new ValidationException("Envelope signer is required");
            requireId(signer.getId(), "Signer ID");
            if (signer.getStep() != null) throw new ValidationException("Envelope signers do not have steps");
        }
        for (EnvelopeDocumentPayload document : payload.getDocuments()) validateDocument(document);
    }

    private void validateDocument(EnvelopeDocumentPayload payload) {
        if (payload == null) throw new ValidationException("Envelope document is required");
        requireId(payload.getId(), "Document ID");
        if (payload.getSignerIds() == null || payload.getSignerIds().isEmpty()) {
            throw new ValidationException("At least one document signer is required");
        }
        for (String signer : payload.getSignerIds()) requireId(signer, "Signer ID");
    }
}
