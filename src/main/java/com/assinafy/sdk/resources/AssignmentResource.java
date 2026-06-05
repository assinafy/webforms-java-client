package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.CreateAssignmentPayload;
import com.assinafy.sdk.models.SignerRef;
import com.assinafy.sdk.models.WhatsappNotification;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.OkHttpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AssignmentResource extends BaseResource {

    public AssignmentResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    /** {@code POST /documents/{documentId}/assignments} — create an assignment (virtual or collect). */
    public Assignment create(String documentId, CreateAssignmentPayload payload) {
        String docId = requireId(documentId, "Document ID");
        Map<String, Object> body = buildPayload(payload, false);
        return httpPost("/documents/" + docId + "/assignments", body, Assignment.class);
    }

    /** {@code POST /documents/{documentId}/assignments/estimate-cost} — estimate cost without creating. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map<String, Object> estimateCost(String documentId, CreateAssignmentPayload payload) {
        String docId = requireId(documentId, "Document ID");
        Map<String, Object> body = buildPayload(payload, true);
        return (Map<String, Object>) httpPost("/documents/" + docId + "/assignments/estimate-cost",
                body, Map.class);
    }

    /**
     * {@code POST /documents/{documentId}/assignments/{assignmentId}} — sign a document for the
     * collect-method flow. {@code entries} is an array of {@code {itemId, fieldId, pageId, value}} objects.
     * The API returns an empty envelope payload on success; failure cases are surfaced as exceptions.
     */
    public void sign(String documentId, String assignmentId, String signerAccessCode,
            List<Map<String, Object>> entries) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        if (entries == null || entries.isEmpty()) {
            throw new ValidationException("At least one sign entry is required");
        }
        httpPostVoid("/documents/" + docId + "/assignments/" + asgId,
                entries, requireAccessCodeQuery(signerAccessCode));
    }

    /**
     * {@code PUT /documents/{documentId}/assignments/{assignmentId}/reject} — signer declines the assignment.
     */
    public void decline(String documentId, String assignmentId, String signerAccessCode,
            String declineReason) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        if (declineReason == null || declineReason.isBlank()) {
            throw new ValidationException("Decline reason is required");
        }
        httpPutVoid("/documents/" + docId + "/assignments/" + asgId + "/reject",
                Map.of("decline_reason", declineReason),
                requireAccessCodeQuery(signerAccessCode));
    }

    /**
     * {@code PUT /documents/{documentId}/assignments/{assignmentId}/reset-expiration} — set a new expiration
     * for the assignment.
     *
     * <p>Per the API, a {@code null} {@code expiresAt} is accepted and clears the expiration (the assignment no
     * longer expires); a blank/empty string is rejected as it is not a valid ISO-8601 datetime. Prefer
     * {@link #clearExpiration(String, String)} when the intent is to remove the expiration.</p>
     *
     * @param expiresAt new ISO-8601 expiration timestamp, or {@code null} to remove the expiration entirely
     */
    public Assignment resetExpiration(String documentId, String assignmentId, String expiresAt) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        if (expiresAt != null && expiresAt.isBlank()) {
            throw new ValidationException("expires_at must be a valid ISO-8601 timestamp or null");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("expires_at", expiresAt);
        return httpPut("/documents/" + docId + "/assignments/" + asgId + "/reset-expiration",
                body, Assignment.class);
    }

    /**
     * Convenience for {@code PUT /documents/{documentId}/assignments/{assignmentId}/reset-expiration} with a
     * {@code null} {@code expires_at}, which removes the assignment's expiration so it no longer expires.
     */
    public Assignment clearExpiration(String documentId, String assignmentId) {
        return resetExpiration(documentId, assignmentId, null);
    }

    /** {@code PUT /documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/resend} */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map<String, Object> resendNotification(String documentId, String assignmentId, String signerId) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        String sid = requireId(signerId, "Signer ID");
        return (Map<String, Object>) httpPut(
                "/documents/" + docId + "/assignments/" + asgId + "/signers/" + sid + "/resend",
                null, Map.class);
    }

    /**
     * {@code POST /documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/estimate-resend-cost}
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map<String, Object> estimateResendCost(String documentId, String assignmentId, String signerId) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        String sid = requireId(signerId, "Signer ID");
        return (Map<String, Object>) httpPost(
                "/documents/" + docId + "/assignments/" + asgId + "/signers/" + sid + "/estimate-resend-cost",
                null, Map.class);
    }

    /** {@code GET /documents/{documentId}/assignments/{assignmentId}/whatsapp-notifications} */
    public List<WhatsappNotification> whatsappNotifications(String documentId, String assignmentId) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        List<WhatsappNotification> result = httpGet("/documents/" + docId + "/assignments/" + asgId
                + "/whatsapp-notifications", new TypeReference<List<WhatsappNotification>>() {});
        return result != null ? result : List.of();
    }

    private Map<String, String> requireAccessCodeQuery(String signerAccessCode) {
        if (signerAccessCode == null || signerAccessCode.isBlank()) {
            throw new ValidationException("Signer access code is required");
        }
        return Map.of("signer-access-code", signerAccessCode);
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
        if (ref.getStep() != null) {
            result.put("step", ref.getStep());
        }
        if ((id == null || id.isBlank()) && !allowWithoutId) {
            throw new ValidationException("Invalid signer reference: ID is required for this operation");
        }
        return result;
    }
}
