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

    public List<DocumentStatus> statuses() {
        List<DocumentStatus> result = httpGet("/documents/statuses",
                new TypeReference<List<DocumentStatus>>() {});
        return result != null ? result : Collections.emptyList();
    }

    public DocumentDetails details(String documentId) {
        String id = requireId(documentId, "Document ID");
        return httpGet("/documents/" + id, DocumentDetails.class);
    }

    public DocumentDetails get(String documentId) {
        return details(documentId);
    }

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

    public byte[] download(String documentId, String artifactName) {
        String id = requireId(documentId, "Document ID");
        String artifact = artifactName != null ? artifactName : "certificated";
        return httpGetBinary("/documents/" + id + "/download/" + artifact);
    }

    public byte[] download(String documentId) {
        return download(documentId, "certificated");
    }

    public byte[] thumbnail(String documentId) {
        String id = requireId(documentId, "Document ID");
        return httpGetBinary("/documents/" + id + "/thumbnail");
    }

    public byte[] downloadPage(String documentId, String pageId) {
        String docId = requireId(documentId, "Document ID");
        String pid = requireId(pageId, "Page ID");
        return httpGetBinary("/documents/" + docId + "/pages/" + pid + "/download");
    }

    public List<DocumentActivity> activities(String documentId) {
        String id = requireId(documentId, "Document ID");
        List<DocumentActivity> result = httpGet("/documents/" + id + "/activities",
                new TypeReference<List<DocumentActivity>>() {});
        return result != null ? result : Collections.emptyList();
    }

    public void delete(String documentId) {
        String id = requireId(documentId, "Document ID");
        httpDelete("/documents/" + id);
    }

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
        }
        return httpPost("/accounts/" + accId + "/templates/" + tmplId + "/documents", body,
                DocumentDetails.class);
    }

    public DocumentDetails createFromTemplate(String templateId, List<TemplateSigner> signers) {
        return createFromTemplate(templateId, signers, null, null);
    }

    public Map<String, Object> estimateCostFromTemplate(String templateId, List<TemplateSigner> signers,
            String accountId) {
        String tmplId = requireId(templateId, "Template ID");
        String accId = accountId(accountId);
        validateTemplateSigners(signers);
        return httpPost("/accounts/" + accId + "/templates/" + tmplId + "/documents/estimate-cost",
                Map.of("signers", signers), Map.class);
    }

    public Map<String, Object> estimateCostFromTemplate(String templateId, List<TemplateSigner> signers) {
        return estimateCostFromTemplate(templateId, signers, null);
    }

    public Map<String, Object> verify(String hash) {
        String h = requireId(hash, "Signature hash");
        return httpGet("/documents/" + h + "/verify", Map.class);
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
}
