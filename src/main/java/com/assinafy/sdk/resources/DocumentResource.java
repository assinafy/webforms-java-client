package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.CreateDocumentFromTemplateOptions;
import com.assinafy.sdk.models.DocumentActivity;
import com.assinafy.sdk.models.DocumentDetails;
import com.assinafy.sdk.models.DocumentListItem;
import com.assinafy.sdk.models.DocumentStatus;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.SigningProgress;
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

public final class DocumentResource extends BaseResource {

    private static final long MAX_UPLOAD_BYTES = 25L * 1024 * 1024;

    private static final Set<String> READY_STATUSES = Set.of(
            "metadata_ready", "pending_signature", "certificated"
    );

    private static final Set<String> FAILED_STATUSES = Set.of(
            "failed", "rejected_by_signer", "rejected_by_user", "expired"
    );

    public DocumentResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    /**
     * {@code POST /accounts/{account_id}/documents} — upload a PDF (multipart {@code file} field) to create a
     * document. The file must be a readable {@code .pdf} no larger than 25&nbsp;MB.
     */
    public DocumentDetails upload(File file, String accountId) {
        validateFile(file);
        String id = accountId(accountId);
        return uploadMultipart(file.getName(), file, null, id);
    }

    public DocumentDetails upload(File file) {
        return upload(file, null);
    }

    public DocumentDetails upload(byte[] bytes, String fileName, String accountId) {
        validateBytes(bytes, fileName);
        String id = accountId(accountId);
        return uploadMultipart(fileName, null, bytes, id);
    }

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
     * {@code GET /accounts/{account_id}/documents} — list the workspace's documents. Supports documented
     * filters (e.g. {@code status}, {@code search}) plus pagination ({@code page}, {@code per-page}, {@code sort}).
     */
    public PaginatedResult<DocumentListItem> list(Map<String, String> params, String accountId) {
        String id = accountId(accountId);
        return httpGetList("/accounts/" + id + "/documents",
                params != null ? params : Map.of(), DocumentListItem.class);
    }

    public PaginatedResult<DocumentListItem> list(Map<String, String> params) {
        return list(params, null);
    }

    public PaginatedResult<DocumentListItem> list() {
        return list(null, null);
    }

    /** {@code GET /documents/statuses} — list the catalogue of document statuses and whether each is deletable. */
    public List<DocumentStatus> statuses() {
        List<DocumentStatus> result = httpGet("/documents/statuses",
                new TypeReference<List<DocumentStatus>>() {});
        return result != null ? result : Collections.emptyList();
    }

    /** {@code GET /documents/{document_id}} — retrieve full document details, including pages and assignment. */
    public DocumentDetails details(String documentId) {
        String id = requireId(documentId, "Document ID");
        return httpGet("/documents/" + id, DocumentDetails.class);
    }

    public DocumentDetails get(String documentId) {
        return details(documentId);
    }

    /**
     * Polls {@code GET /documents/{document_id}} until the document reaches a ready status
     * ({@code metadata_ready}, {@code pending_signature}, or {@code certificated}), throwing a
     * {@link ValidationException} if it enters a failed status or the {@code maxWaitMs} budget elapses.
     */
    public DocumentDetails waitUntilReady(String documentId, long maxWaitMs, long pollIntervalMs) {
        String id = requireId(documentId, "Document ID");
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < maxWaitMs) {
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
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ValidationException("Interrupted while waiting for document to be ready");
            }
        }
        throw new ValidationException("Timeout waiting for document to be ready");
    }

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
     */
    public byte[] download(String documentId, String artifactName) {
        String id = requireId(documentId, "Document ID");
        String artifact = artifactName != null ? artifactName : "certificated";
        return httpGetBinary("/documents/" + id + "/download/" + artifact);
    }

    /**
     * Convenience for {@link #download(String, String)} with the {@code "certificated"} artifact — the final
     * signed PDF. Use {@code download(id, "original")} to fetch the originally uploaded PDF instead.
     */
    public byte[] download(String documentId) {
        return download(documentId, "certificated");
    }

    /** {@code GET /documents/{document_id}/thumbnail} — download the document's thumbnail image (JPEG). */
    public byte[] thumbnail(String documentId) {
        String id = requireId(documentId, "Document ID");
        return httpGetBinary("/documents/" + id + "/thumbnail");
    }

    /** {@code GET /documents/{document_id}/pages/{page_id}/download} — download a single page image. */
    public byte[] downloadPage(String documentId, String pageId) {
        String docId = requireId(documentId, "Document ID");
        String pid = requireId(pageId, "Page ID");
        return httpGetBinary("/documents/" + docId + "/pages/" + pid + "/download");
    }

    /** {@code GET /documents/{document_id}/activities} — list the document's activity/audit-trail entries. */
    public List<DocumentActivity> activities(String documentId) {
        String id = requireId(documentId, "Document ID");
        List<DocumentActivity> result = httpGet("/documents/" + id + "/activities",
                new TypeReference<List<DocumentActivity>>() {});
        return result != null ? result : Collections.emptyList();
    }

    /** {@code DELETE /documents/{document_id}} — delete a document (only allowed for deletable statuses). */
    public void delete(String documentId) {
        String id = requireId(documentId, "Document ID");
        httpDelete("/documents/" + id);
    }

    /**
     * {@code POST /accounts/{account_id}/templates/{template_id}/documents} — generate a document from a
     * template. Each {@link TemplateSigner} binds a template role to a signer; optional {@code name},
     * {@code message}, {@code expires_at}, {@code editor_fields}, and {@code tags} are taken from {@code options}.
     */
    public DocumentDetails createFromTemplate(String templateId, List<TemplateSigner> signers,
            CreateDocumentFromTemplateOptions options, String accountId) {
        String tmplId = requireId(templateId, "Template ID");
        String accId = accountId(accountId);
        validateTemplateSigners(signers);
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

    public DocumentDetails createFromTemplate(String templateId, List<TemplateSigner> signers) {
        return createFromTemplate(templateId, signers, null, null);
    }

    /**
     * {@code POST /accounts/{account_id}/templates/{template_id}/documents/estimate-cost} — estimate the credit
     * cost of generating a document from a template for the given signers, without creating it. Returns the raw
     * cost-estimate object ({@code documents}, {@code credits}, {@code total_credits}, {@code document_balance},
     * {@code has_sufficient_resources}, …).
     */
    public Map<String, Object> estimateCostFromTemplate(String templateId, List<TemplateSigner> signers,
            String accountId) {
        String tmplId = requireId(templateId, "Template ID");
        String accId = accountId(accountId);
        validateTemplateSigners(signers);
        return httpPost("/accounts/" + accId + "/templates/" + tmplId + "/documents/estimate-cost",
                Map.of("signers", signers), new TypeReference<Map<String, Object>>() {}, Map.of());
    }

    public Map<String, Object> estimateCostFromTemplate(String templateId, List<TemplateSigner> signers) {
        return estimateCostFromTemplate(templateId, signers, null);
    }

    /**
     * {@code GET /documents/{signature_hash}/verify} — verify a document by its signature hash and return the
     * verification details. This endpoint is public (no API key required).
     */
    public Map<String, Object> verify(String hash) {
        String h = requireId(hash, "Signature hash");
        return httpGet("/documents/" + h + "/verify", new TypeReference<Map<String, Object>>() {});
    }

    /**
     * {@code GET /public/documents/{document_id}} — unauthenticated lookup that returns minimal document
     * info ({@code id}, {@code name}, {@code page_count}, {@code created_by}). Useful for signer landing pages.
     */
    public DocumentDetails getPublic(String documentId) {
        String id = requireId(documentId, "Document ID");
        return httpGet("/public/documents/" + id, DocumentDetails.class);
    }

    /**
     * {@code PUT /public/documents/{document_id}/send-token} — request that a fresh signing token be sent
     * to the given recipient. Unauthenticated endpoint used by signer landing pages.
     */
    public Map<String, Object> sendToken(String documentId, String recipient, String channel) {
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
        return httpPut("/public/documents/" + id + "/send-token", body,
                new TypeReference<Map<String, Object>>() {}, Map.of());
    }

    /** {@code GET /accounts/{account_id}/documents/{document_id}/tags} */
    public List<Tag> listTags(String documentId, String accountId) {
        String docId = requireId(documentId, "Document ID");
        String accId = accountId(accountId);
        List<Tag> result = httpGet("/accounts/" + accId + "/documents/" + docId + "/tags",
                new TypeReference<List<Tag>>() {});
        return result != null ? result : Collections.emptyList();
    }

    public List<Tag> listTags(String documentId) {
        return listTags(documentId, null);
    }

    /** {@code PUT /accounts/{account_id}/documents/{document_id}/tags} */
    public List<Tag> replaceTags(String documentId, List<String> tags, String accountId) {
        String docId = requireId(documentId, "Document ID");
        String accId = accountId(accountId);
        validateTagNames(tags, true);
        List<Tag> result = httpPut("/accounts/" + accId + "/documents/" + docId + "/tags",
                Map.of("tags", tags), new TypeReference<List<Tag>>() {}, Map.of());
        return result != null ? result : Collections.emptyList();
    }

    public List<Tag> replaceTags(String documentId, List<String> tags) {
        return replaceTags(documentId, tags, null);
    }

    /** {@code POST /accounts/{account_id}/documents/{document_id}/tags} */
    public List<Tag> appendTags(String documentId, List<String> tags, String accountId) {
        String docId = requireId(documentId, "Document ID");
        String accId = accountId(accountId);
        validateTagNames(tags, false);
        List<Tag> result = httpPost("/accounts/" + accId + "/documents/" + docId + "/tags",
                Map.of("tags", tags), new TypeReference<List<Tag>>() {}, Map.of());
        return result != null ? result : Collections.emptyList();
    }

    public List<Tag> appendTags(String documentId, List<String> tags) {
        return appendTags(documentId, tags, null);
    }

    /** {@code DELETE /accounts/{account_id}/documents/{document_id}/tags/{tag_id}} */
    public Map<String, Object> detachTag(String documentId, String tagId, String accountId) {
        String docId = requireId(documentId, "Document ID");
        String tid = requireId(tagId, "Tag ID");
        String accId = accountId(accountId);
        return httpDelete("/accounts/" + accId + "/documents/" + docId + "/tags/" + tid,
                new TypeReference<Map<String, Object>>() {});
    }

    public Map<String, Object> detachTag(String documentId, String tagId) {
        return detachTag(documentId, tagId, null);
    }

    public boolean isFullySigned(String documentId) {
        DocumentDetails d = details(documentId);
        if ("certificated".equals(d.getStatus())) return true;
        if (d.getAssignment() != null && d.getAssignment().getSummary() != null) {
            int total = d.getAssignment().getSummary().getSignerCount();
            int completed = d.getAssignment().getSummary().getCompletedCount();
            return total > 0 && total == completed;
        }
        return false;
    }

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
            }
        }
        int pending = Math.max(total - signed, 0);
        double percentage = total > 0 ? Math.round((double) signed / total * 10_000.0) / 100.0 : 0;
        return new SigningProgress(signed, total, percentage, pending);
    }

    private void validateTemplateSigners(List<TemplateSigner> signers) {
        if (signers == null || signers.isEmpty()) {
            throw new ValidationException("At least one template signer is required");
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
