package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.CostEstimate;
import com.assinafy.sdk.models.CreateDocumentFromTemplateOptions;
import com.assinafy.sdk.models.DocumentActivity;
import com.assinafy.sdk.models.DocumentDetails;
import com.assinafy.sdk.models.DocumentListItem;
import com.assinafy.sdk.models.DocumentStatus;
import com.assinafy.sdk.models.DocumentVerification;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.SigningProgress;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.models.Tag;
import com.assinafy.sdk.models.TemplateSigner;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Client for document upload, retrieval, artifacts, templates, verification, and document tags. */
public final class DocumentResource extends BaseResource {

    private static final long MAX_UPLOAD_BYTES = 25L * 1024 * 1024;

    private static final Set<String> READY_STATUSES = Set.of(
            "metadata_ready", "pending_signature", "certificated"
    );

    private static final Set<String> FAILED_STATUSES = Set.of(
            "failed", "rejected_by_signer", "rejected_by_user", "expired"
    );

    /**
     * Creates an instance.
     *
     * @param httpClient shared HTTP client
     * @param baseUrl API base URL
     * @param defaultAccountId default account identifier, or {@code null}
     */
    public DocumentResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    /**
     * {@code POST /accounts/{account_id}/documents} — upload a PDF (multipart {@code file} field) to create a
     * document. The file must be a readable {@code .pdf} no larger than 25&nbsp;MB.
     *
     * @param file required readable PDF
     * @param accountId account override, or {@code null} for the client default
     * @return created document
     */
    public DocumentDetails upload(File file, String accountId) {
        validateFile(file);
        String id = accountId(accountId);
        return uploadMultipart(file.getName(), file, null, id);
    }

    /**
     * Returns created document in the default account.
     *
     * @param file required readable PDF
     * @return created document in the default account
     */
    public DocumentDetails upload(File file) {
        return upload(file, null);
    }

    /**
     * Uploads an in-memory PDF.
     *
     * @param bytes required PDF bytes, at most 25 MB
     * @param fileName required filename ending in {@code .pdf}
     * @param accountId account override, or {@code null} for the client default
     * @return created document
     */
    public DocumentDetails upload(byte[] bytes, String fileName, String accountId) {
        validateBytes(bytes, fileName);
        String id = accountId(accountId);
        return uploadMultipart(fileName, null, bytes, id);
    }

    /**
     * Returns created document in the default account.
     *
     * @param bytes required PDF bytes, at most 25 MB
     * @param fileName required filename ending in {@code .pdf}
     * @return created document in the default account
     */
    public DocumentDetails upload(byte[] bytes, String fileName) {
        return upload(bytes, fileName, null);
    }

    private void validateFile(File file) {
        if (file == null) {
            throw new ValidationException("File is required");
        }
        if (!file.exists() || !file.isFile()) {
            throw new ValidationException("File does not exist or is not a regular file");
        }
        if (!file.canRead()) {
            throw new ValidationException("File is not readable");
        }
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            throw new ValidationException("Only PDF files are supported");
        }
        if (file.length() > MAX_UPLOAD_BYTES) {
            throw new ValidationException("File size exceeds maximum allowed (25MB)");
        }
    }

    private void validateBytes(byte[] bytes, String fileName) {
        if (bytes == null || bytes.length == 0) {
            throw new ValidationException("File bytes are required");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new ValidationException("File name is required");
        }
        if (!fileName.toLowerCase().endsWith(".pdf")) {
            throw new ValidationException("Only PDF files are supported");
        }
        if (bytes.length > MAX_UPLOAD_BYTES) {
            throw new ValidationException("File size exceeds maximum allowed (25MB)");
        }
    }

    private DocumentDetails uploadMultipart(String fileName, File file, byte[] bytes, String accountId) {
        MediaType pdfType = MediaType.get("application/pdf");
        RequestBody fileBody = file != null
                ? RequestBody.create(file, pdfType)
                : RequestBody.create(bytes, pdfType);

        RequestBody multipart = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, fileBody)
                .build();

        DocumentDetails document = httpPostMultipart(
                "/accounts/" + accountId + "/documents", multipart, DocumentDetails.class);
        if (document == null || document.getId() == null) {
            throw new ValidationException("Upload succeeded but no document ID was returned");
        }
        return document;
    }

    /**
     * {@code GET /accounts/{account_id}/documents} — list the workspace's documents. Supported query
     * parameters: {@code status}, {@code method}, {@code search}, {@code tags}, {@code sort}, plus pagination
     * ({@code page}, {@code per-page}). The {@code per_page}/{@code perPage} key is normalized to {@code per-page}.
     *
     * @param params optional filters, sorting, and pagination values
     * @param accountId account override, or {@code null} for the client default
     * @return document page and response-header pagination metadata
     */
    public PaginatedResult<DocumentListItem> list(Map<String, String> params, String accountId) {
        String id = accountId(accountId);
        return httpGetList("/accounts/" + id + "/documents",
                params != null ? params : Map.of(), DocumentListItem.class);
    }

    /**
     * Returns default account's document page.
     *
     * @param params optional filters, sorting, and pagination values
     * @return default account's document page
     */
    public PaginatedResult<DocumentListItem> list(Map<String, String> params) {
        return list(params, null);
    }

    /**
     * Returns default account's first document page.
     *
     * @return default account's first document page
     */
    public PaginatedResult<DocumentListItem> list() {
        return list(null, null);
    }

    /**
     * {@code GET /accounts/{account_id}/documents/search} — lightweight document search. Returns the same
     * paginated {@link DocumentListItem} shape as {@link #list(Map, String)} but without the expanded
     * {@code assignment}/{@code pages} sub-objects, so it is cheaper for autocomplete/typeahead. Accepts
     * {@code search}, {@code status}, and pagination ({@code page}, {@code per-page}) query parameters.
     *
     * @param params optional search, status, and pagination values
     * @param accountId account override, or {@code null} for the client default
     * @return lightweight document page
     */
    public PaginatedResult<DocumentListItem> search(Map<String, String> params, String accountId) {
        String id = accountId(accountId);
        return httpGetList("/accounts/" + id + "/documents/search",
                params != null ? params : Map.of(), DocumentListItem.class);
    }

    /**
     * Returns default account's lightweight document page.
     *
     * @param params optional search, status, and pagination values
     * @return default account's lightweight document page
     */
    public PaginatedResult<DocumentListItem> search(Map<String, String> params) {
        return search(params, null);
    }

    /**
     * {@code GET /documents/statuses}.
     *
     * @return document status catalogue, never {@code null}
     */
    public List<DocumentStatus> statuses() {
        List<DocumentStatus> result = httpGet("/documents/statuses",
                new TypeReference<List<DocumentStatus>>() {});
        return result != null ? result : Collections.emptyList();
    }

    /**
     * {@code GET /documents/{document_id}}.
     *
     * @param documentId required document identifier
     * @return full document details, including pages and assignment
     */
    public DocumentDetails details(String documentId) {
        String id = requireId(documentId, "Document ID");
        return httpGet("/documents/" + id, DocumentDetails.class);
    }

    /**
     * Alias for {@link #details(String)}.
     *
     * @param documentId required document identifier
     * @return full document details
     */
    public DocumentDetails get(String documentId) {
        return details(documentId);
    }

    /**
     * Polls {@code GET /documents/{document_id}} until the document reaches a ready status
     * ({@code metadata_ready}, {@code pending_signature}, or {@code certificated}), throwing a
     * {@link ValidationException} if it enters a failed status or the {@code maxWaitMs} budget elapses.
     *
     * @param documentId required document identifier
     * @param maxWaitMs positive maximum wait in milliseconds
     * @param pollIntervalMs positive polling interval in milliseconds
     * @return first ready document response
     * @throws ValidationException on invalid timing, failed status, interruption, or timeout
     */
    public DocumentDetails waitUntilReady(String documentId, long maxWaitMs, long pollIntervalMs) {
        String id = requireId(documentId, "Document ID");
        if (maxWaitMs <= 0) {
            throw new ValidationException("Maximum wait must be greater than zero");
        }
        if (pollIntervalMs <= 0) {
            throw new ValidationException("Poll interval must be greater than zero");
        }
        long start = System.nanoTime();
        long maxWaitNanos = TimeUnit.MILLISECONDS.toNanos(maxWaitMs);

        while (System.nanoTime() - start < maxWaitNanos) {
            try {
                DocumentDetails d = details(id);
                String status = d.getStatus() != null ? d.getStatus() : "unknown";
                if (READY_STATUSES.contains(status)) {
                    return d;
                }
                if (FAILED_STATUSES.contains(status)) {
                    throw new ValidationException("Document processing failed with status: " + status);
                }
            } catch (ValidationException | ApiException e) {
                throw e;
            }
            try {
                long remainingNanos = maxWaitNanos - (System.nanoTime() - start);
                if (remainingNanos <= 0) break;
                long sleepNanos = Math.min(TimeUnit.MILLISECONDS.toNanos(pollIntervalMs), remainingNanos);
                TimeUnit.NANOSECONDS.sleep(sleepNanos);
                if (sleepNanos == remainingNanos) break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ValidationException("Interrupted while waiting for document to be ready", e);
            }
        }
        throw new ValidationException("Timeout waiting for document to be ready");
    }

    /**
     * Waits up to 30 seconds, polling every two seconds.
     *
     * @param documentId required document identifier
     * @return first ready document response
     */
    public DocumentDetails waitUntilReady(String documentId) {
        return waitUntilReady(documentId, 30_000, 2_000);
    }

    /**
     * {@code GET /documents/{document_id}/download/{artifact_name}} — download a document artifact as bytes.
     *
     * <p>Common artifact names are {@code "original"} (always available after upload) and {@code "certificated"}
     * (the final signed PDF, available only once the document is certificated). When {@code artifactName} is
     * {@code null} this defaults to {@code "certificated"}; requesting an artifact that does not yet exist
     * raises an {@link com.assinafy.sdk.exceptions.ApiException} (HTTP 404, "Artefato não está disponível").</p>
     *
     * @param documentId required document identifier
     * @param artifactName artifact name, or {@code null} for {@code certificated}
     * @return artifact bytes
     */
    public byte[] download(String documentId, String artifactName) {
        String id = requireId(documentId, "Document ID");
        String artifact = requireId(artifactName != null ? artifactName : "certificated", "Artifact name");
        return httpGetBinary("/documents/" + id + "/download/" + artifact);
    }

    /**
     * Convenience for {@link #download(String, String)} with the {@code "certificated"} artifact — the final
     * signed PDF. Use {@code download(id, "original")} to fetch the originally uploaded PDF instead.
     *
     * @param documentId required document identifier
     * @return certificated PDF bytes
     */
    public byte[] download(String documentId) {
        return download(documentId, "certificated");
    }

    /**
     * {@code GET /documents/{document_id}/thumbnail}.
     *
     * @param documentId required document identifier
     * @return thumbnail image bytes
     */
    public byte[] thumbnail(String documentId) {
        String id = requireId(documentId, "Document ID");
        return httpGetBinary("/documents/" + id + "/thumbnail");
    }

    /**
     * {@code GET /documents/{document_id}/pages/{page_id}/download}.
     *
     * @param documentId required document identifier
     * @param pageId required page identifier
     * @return rendered page image bytes
     */
    public byte[] downloadPage(String documentId, String pageId) {
        String docId = requireId(documentId, "Document ID");
        String pid = requireId(pageId, "Page ID");
        return httpGetBinary("/documents/" + docId + "/pages/" + pid + "/download");
    }

    /**
     * {@code GET /documents/{document_id}/activities}.
     *
     * @param documentId required document identifier
     * @return activity entries, never {@code null}
     */
    public List<DocumentActivity> activities(String documentId) {
        String id = requireId(documentId, "Document ID");
        List<DocumentActivity> result = httpGet("/documents/" + id + "/activities",
                new TypeReference<List<DocumentActivity>>() {});
        return result != null ? result : Collections.emptyList();
    }

    /**
     * {@code DELETE /documents/{document_id}}.
     *
     * @param documentId required identifier of a document in a deletable status
     */
    public void delete(String documentId) {
        String id = requireId(documentId, "Document ID");
        httpDelete("/documents/" + id);
    }

    private static final int MAX_DOCUMENT_NAME_LENGTH = 255;

    /**
     * {@code PATCH /documents/{document_id}} — rename a document. The request body is {@code {"name": name}} and
     * the updated {@link DocumentDetails} is returned. {@code name} is required and limited to 255 characters;
     * the server normalizes unsupported characters/diacritics.
     *
     * @param documentId required document identifier
     * @param name required new name, at most 255 characters
     * @return updated document
     */
    public DocumentDetails rename(String documentId, String name) {
        String id = requireId(documentId, "Document ID");
        if (name == null || name.isBlank()) {
            throw new ValidationException("Document name is required");
        }
        if (name.length() > MAX_DOCUMENT_NAME_LENGTH) {
            throw new ValidationException("Document name must be at most " + MAX_DOCUMENT_NAME_LENGTH + " characters");
        }
        return httpPatch("/documents/" + id, Map.of("name", name), DocumentDetails.class);
    }

    /**
     * {@code POST /accounts/{account_id}/templates/{template_id}/documents} — generate a document from a
     * template. Each {@link TemplateSigner} binds a template role to a signer; optional {@code name},
     * {@code message}, {@code expires_at}, {@code editor_fields}, and {@code tags} are taken from {@code options}.
     *
     * @param templateId required template identifier
     * @param signers required template role-to-signer mappings
     * @param options optional document, invitation, editor-field, and tag values
     * @param accountId account override, or {@code null} for the client default
     * @return created document
     */
    public DocumentDetails createFromTemplate(String templateId, List<TemplateSigner> signers,
            CreateDocumentFromTemplateOptions options, String accountId) {
        String tmplId = requireId(templateId, "Template ID");
        String accId = accountId(accountId);
        validateTemplateSigners(signers, true);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("signers", signers);
        if (options != null) {
            if (options.getName() != null) body.put("name", options.getName());
            if (options.getMessage() != null) body.put("message", options.getMessage());
            if (options.getExpiresAt() != null) body.put("expires_at", options.getExpiresAt());
            if (options.getEditorFields() != null) body.put("editor_fields", options.getEditorFields());
            if (options.getTags() != null) body.put("tags", options.getTags());
        }
        return httpPost("/accounts/" + accId + "/templates/" + tmplId + "/documents", body,
                DocumentDetails.class);
    }

    /**
     * Generates from a template in the client's default account.
     *
     * @param templateId required template identifier
     * @param signers required template role-to-signer mappings
     * @param options optional document, invitation, editor-field, and tag values
     * @return created document
     */
    public DocumentDetails createFromTemplate(String templateId, List<TemplateSigner> signers,
            CreateDocumentFromTemplateOptions options) {
        return createFromTemplate(templateId, signers, options, null);
    }

    /**
     * Generates from a template with no optional values in the client's default account.
     *
     * @param templateId required template identifier
     * @param signers required template role-to-signer mappings
     * @return created document
     */
    public DocumentDetails createFromTemplate(String templateId, List<TemplateSigner> signers) {
        return createFromTemplate(templateId, signers, null, null);
    }

    /**
     * {@code POST /accounts/{account_id}/templates/{template_id}/documents/estimate-cost} — estimate the credit
     * cost of generating a document from a template for the given signers, without creating it. Inspect
     * {@link CostEstimate#getNeedsExtraDocument()}, {@link CostEstimate#getBlockingReason()}
     * ({@code PendingPayment} / {@code InsufficientDocuments} / {@code InsufficientCredits}),
     * {@link CostEstimate#getHasSufficientResources()}, and {@link CostEstimate#getBreakdown()} to decide.
     *
     * @param templateId required template identifier
     * @param signers required role and optional assignment settings
     * @param accountId account override, or {@code null} for the client default
     * @return template-generation credit estimate
     */
    public CostEstimate estimateCostFromTemplate(String templateId, List<TemplateSigner> signers,
            String accountId) {
        String tmplId = requireId(templateId, "Template ID");
        String accId = accountId(accountId);
        validateTemplateSigners(signers, false);
        return httpPost("/accounts/" + accId + "/templates/" + tmplId + "/documents/estimate-cost",
                Map.of("signers", signers), CostEstimate.class, Map.of());
    }

    /**
     * Returns default account's template-generation credit estimate.
     *
     * @param templateId required template identifier
     * @param signers required role and optional assignment settings
     * @return default account's template-generation credit estimate
     */
    public CostEstimate estimateCostFromTemplate(String templateId, List<TemplateSigner> signers) {
        return estimateCostFromTemplate(templateId, signers, null);
    }

    /**
     * {@code GET /documents/{signature_hash}/verify} — verify a document by its signature hash. This endpoint
     * is public (no API key required) and always responds 200; check {@link DocumentVerification#getIsValid()}.
     * When the hash does not resolve to a signed document, {@code isValid} is {@code false} and
     * {@link DocumentVerification#getMessage()} explains why.
     *
     * @param hash required document signature hash
     * @return verification result
     */
    public DocumentVerification verify(String hash) {
        String h = requireId(hash, "Signature hash");
        return httpGet("/documents/" + h + "/verify", DocumentVerification.class);
    }

    /**
     * {@code GET /public/documents/{document_id}} — unauthenticated lookup that returns minimal document
     * info ({@code id}, {@code name}, {@code page_count}, {@code created_by}). Useful for signer landing pages.
     *
     * @param documentId required document identifier
     * @return public document metadata
     */
    public DocumentDetails getPublic(String documentId) {
        String id = requireId(documentId, "Document ID");
        return httpGet("/public/documents/" + id, DocumentDetails.class);
    }

    /**
     * {@code PUT /public/documents/{document_id}/send-token} — request that a fresh signing token be sent to a
     * signer. Unauthenticated endpoint used by signer landing pages.
     *
     * <p>The request body is {@code {"recipient": ..., "channel": ...}}, where {@code channel} is {@code "email"}
     * or {@code "whatsapp"} (any other value yields a {@code "Canal inválido"} error) and {@code recipient} is
     * the target email/phone. The target document must be in {@code pending_signature} status. (The OpenAPI spec
     * documents an {@code {"email": ...}} body, but the live API rejects that and requires
     * {@code recipient}+{@code channel}; the SDK follows the live behavior.) The success envelope carries no
     * data, so this returns {@code void}.</p>
     *
     * @param documentId required pending-signature document identifier
     * @param recipient required email address or phone number
     * @param channel required {@code email} or {@code whatsapp}
     */
    public void sendToken(String documentId, String recipient, String channel) {
        String id = requireId(documentId, "Document ID");
        if (recipient == null || recipient.isBlank()) {
            throw new ValidationException("Recipient is required");
        }
        if (channel == null || channel.isBlank()) {
            throw new ValidationException("Channel is required");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("recipient", recipient);
        body.put("channel", channel);
        httpPutVoid("/public/documents/" + id + "/send-token", body, Map.of());
    }

    /**
     * {@code GET /accounts/{account_id}/documents/{document_id}/tags}.
     *
     * @param documentId required document identifier
     * @param accountId account override, or {@code null} for the client default
     * @return attached tags, never {@code null}
     */
    public List<Tag> listTags(String documentId, String accountId) {
        String docId = requireId(documentId, "Document ID");
        String accId = accountId(accountId);
        List<Tag> result = httpGet("/accounts/" + accId + "/documents/" + docId + "/tags",
                new TypeReference<List<Tag>>() {});
        return result != null ? result : Collections.emptyList();
    }

    /**
     * Returns tags attached in the default account.
     *
     * @param documentId required document identifier
     * @return tags attached in the default account
     */
    public List<Tag> listTags(String documentId) {
        return listTags(documentId, null);
    }

    /**
     * {@code PUT /accounts/{account_id}/documents/{document_id}/tags}.
     *
     * @param documentId required document identifier
     * @param tags replacement tag names; an empty list removes all tags
     * @param accountId account override, or {@code null} for the client default
     * @return attached tags after replacement
     */
    public List<Tag> replaceTags(String documentId, List<String> tags, String accountId) {
        String docId = requireId(documentId, "Document ID");
        String accId = accountId(accountId);
        validateTagNames(tags, true);
        List<Tag> result = httpPut("/accounts/" + accId + "/documents/" + docId + "/tags",
                Map.of("tags", tags), new TypeReference<List<Tag>>() {}, Map.of());
        return result != null ? result : Collections.emptyList();
    }

    /**
     * Returns attached tags after replacement in the default account.
     *
     * @param documentId required document identifier
     * @param tags replacement tag names; an empty list removes all tags
     * @return attached tags after replacement in the default account
     */
    public List<Tag> replaceTags(String documentId, List<String> tags) {
        return replaceTags(documentId, tags, null);
    }

    /**
     * {@code POST /accounts/{account_id}/documents/{document_id}/tags}.
     *
     * @param documentId required document identifier
     * @param tags one or more tag names to append
     * @param accountId account override, or {@code null} for the client default
     * @return attached tags after append
     */
    public List<Tag> appendTags(String documentId, List<String> tags, String accountId) {
        String docId = requireId(documentId, "Document ID");
        String accId = accountId(accountId);
        validateTagNames(tags, false);
        List<Tag> result = httpPost("/accounts/" + accId + "/documents/" + docId + "/tags",
                Map.of("tags", tags), new TypeReference<List<Tag>>() {}, Map.of());
        return result != null ? result : Collections.emptyList();
    }

    /**
     * Returns attached tags after append in the default account.
     *
     * @param documentId required document identifier
     * @param tags one or more tag names to append
     * @return attached tags after append in the default account
     */
    public List<Tag> appendTags(String documentId, List<String> tags) {
        return appendTags(documentId, tags, null);
    }

    /**
     * {@code DELETE /accounts/{account_id}/documents/{document_id}/tags/{tag_id}} — detach a single tag from a
     * document (the tag itself is not deleted from the workspace). Returns the response's {@code detached}
     * flag; an error is raised if the association or IDs are invalid.
     *
     * @param documentId required document identifier
     * @param tagId required tag identifier
     * @param accountId account override, or {@code null} for the client default
     * @return response {@code detached} flag
     */
    public boolean detachTag(String documentId, String tagId, String accountId) {
        String docId = requireId(documentId, "Document ID");
        String tid = requireId(tagId, "Tag ID");
        String accId = accountId(accountId);
        Map<String, Boolean> result = httpDelete(
                "/accounts/" + accId + "/documents/" + docId + "/tags/" + tid,
                new TypeReference<Map<String, Boolean>>() {});
        return result != null && Boolean.TRUE.equals(result.get("detached"));
    }

    /**
     * Returns response {@code detached} flag in the default account.
     *
     * @param documentId required document identifier
     * @param tagId required tag identifier
     * @return response {@code detached} flag in the default account
     */
    public boolean detachTag(String documentId, String tagId) {
        return detachTag(documentId, tagId, null);
    }

    /**
     * Checks the certificated status or strict signer-count equality.
     *
     * @param documentId required document identifier
     * @return {@code true} when certificated or when total signer count equals completed count and is positive
     */
    public boolean isFullySigned(String documentId) {
        DocumentDetails d = details(documentId);
        if ("certificated".equals(d.getStatus())) return true;
        if (d.getAssignment() != null && d.getAssignment().getSummary() != null) {
            int total = d.getAssignment().getSummary().getSignerCount();
            int completed = d.getAssignment().getSummary().getCompletedCount();
            return total > 0 && total == completed;
        }
        if (d.getAssignment() != null && d.getAssignment().getSigners() != null) {
            List<Signer> signers = d.getAssignment().getSigners();
            return !signers.isEmpty() && completedSignerCount(signers) == signers.size();
        }
        return false;
    }

    /**
     * Calculates completion counts and percentage from document assignment data.
     *
     * @param documentId required document identifier
     * @return immutable signing progress
     */
    public SigningProgress getSigningProgress(String documentId) {
        DocumentDetails d = details(documentId);
        int total = 0;
        int signed = 0;
        if (d.getAssignment() != null) {
            if (d.getAssignment().getSummary() != null) {
                total = d.getAssignment().getSummary().getSignerCount();
                signed = d.getAssignment().getSummary().getCompletedCount();
            } else if (d.getAssignment().getSigners() != null) {
                total = d.getAssignment().getSigners().size();
                signed = completedSignerCount(d.getAssignment().getSigners());
            }
        }
        int pending = Math.max(total - signed, 0);
        double percentage = total > 0 ? Math.round((double) signed / total * 10_000.0) / 100.0 : 0;
        return new SigningProgress(signed, total, percentage, pending);
    }

    private static int completedSignerCount(List<Signer> signers) {
        return (int) signers.stream().filter(signer -> Boolean.TRUE.equals(signer.getCompleted())).count();
    }

    private void validateTemplateSigners(List<TemplateSigner> signers, boolean requireSignerId) {
        if (signers == null || signers.isEmpty()) {
            throw new ValidationException("At least one template signer is required");
        }
        for (TemplateSigner signer : signers) {
            if (signer == null || signer.getRoleId() == null || signer.getRoleId().isBlank()) {
                throw new ValidationException("Template signer role ID is required");
            }
            if (requireSignerId && (signer.getId() == null || signer.getId().isBlank())) {
                throw new ValidationException("Template signer ID is required when creating a document");
            }
        }
    }

    private void validateTagNames(List<String> tags, boolean allowEmpty) {
        if (tags == null) {
            throw new ValidationException("Tag names are required");
        }
        if (!allowEmpty && tags.isEmpty()) {
            throw new ValidationException("At least one tag name is required");
        }
        for (String tag : tags) {
            if (tag == null || tag.isBlank()) {
                throw new ValidationException("Tag names cannot be blank");
            }
        }
    }
}
