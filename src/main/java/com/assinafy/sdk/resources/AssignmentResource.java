package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.CreateAssignmentPayload;
import com.assinafy.sdk.models.SignerRef;
import com.assinafy.sdk.models.WhatsappNotification;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.OkHttpClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AssignmentResource extends BaseResource {

    public AssignmentResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    public Assignment create(String documentId, CreateAssignmentPayload payload) {
        String docId = requireId(documentId, "Document ID");
        Map<String, Object> body = buildPayload(payload, false);
        return httpPost("/documents/" + docId + "/assignments", body, Assignment.class);
    }

    public Map<String, Object> estimateCost(String documentId, CreateAssignmentPayload payload) {
        String docId = requireId(documentId, "Document ID");
        Map<String, Object> body = buildPayload(payload, true);
        return httpPost("/documents/" + docId + "/assignments/estimate-cost", body, Map.class);
    }

    public Assignment resetExpiration(String documentId, String assignmentId, String expiresAt) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        return httpPut("/documents/" + docId + "/assignments/" + asgId + "/reset-expiration",
                Map.of("expires_at", expiresAt), Assignment.class);
    }

    public Map<String, Object> resendNotification(String documentId, String assignmentId, String signerId) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        String sid = requireId(signerId, "Signer ID");
        return httpPut("/documents/" + docId + "/assignments/" + asgId + "/signers/" + sid + "/resend",
                null, Map.class);
    }

    public Map<String, Object> estimateResendCost(String documentId, String assignmentId, String signerId) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        String sid = requireId(signerId, "Signer ID");
        return httpPost("/documents/" + docId + "/assignments/" + asgId + "/signers/" + sid
                + "/estimate-resend-cost", null, Map.class);
    }

    public List<WhatsappNotification> whatsappNotifications(String documentId, String assignmentId) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        List<WhatsappNotification> result = httpGet("/documents/" + docId + "/assignments/" + asgId
                + "/whatsapp-notifications", new TypeReference<List<WhatsappNotification>>() {});
        return result != null ? result : List.of();
    }

    private static Map<String, Object> buildPayload(CreateAssignmentPayload payload, boolean allowSignersWithoutId) {
        if (payload == null) {
            throw new ValidationException("Assignment payload is required");
        }
        List<SignerRef> signerRefs = payload.resolveSignerRefs();
        if (signerRefs.isEmpty()) {
            throw new ValidationException("At least one signer is required");
        }

        List<Map<String, Object>> normalisedSigners = new ArrayList<>();
        for (SignerRef ref : signerRefs) {
            normalisedSigners.add(normaliseSignerRef(ref, allowSignersWithoutId));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("method", payload.getMethod() != null ? payload.getMethod() : "virtual");
        body.put("signers", normalisedSigners);
        if (payload.getMessage() != null) body.put("message", payload.getMessage());
        if (payload.getExpiresAt() != null) body.put("expires_at", payload.getExpiresAt());
        if (payload.getCopyReceivers() != null && !payload.getCopyReceivers().isEmpty()) {
            body.put("copy_receivers", payload.getCopyReceivers());
        }
        if (payload.getEntries() != null && !payload.getEntries().isEmpty()) {
            body.put("entries", payload.getEntries());
        }
        return body;
    }

    private static Map<String, Object> normaliseSignerRef(SignerRef ref, boolean allowWithoutId) {
        String id = ref.getId();
        Map<String, Object> result = new LinkedHashMap<>();
        if (id != null && !id.isBlank()) {
            result.put("id", id);
        }
        if (ref.getVerificationMethod() != null) {
            result.put("verification_method", ref.getVerificationMethod());
        }
        if (ref.getNotificationMethods() != null && !ref.getNotificationMethods().isEmpty()) {
            result.put("notification_methods", ref.getNotificationMethods());
        }
        if ((id == null || id.isBlank()) && !allowWithoutId) {
            throw new ValidationException("Invalid signer reference: ID is required for this operation");
        }
        return result;
    }
}
