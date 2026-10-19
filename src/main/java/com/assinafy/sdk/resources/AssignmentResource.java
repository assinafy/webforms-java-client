package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.AssignmentSignEntry;
import com.assinafy.sdk.models.CostEstimate;
import com.assinafy.sdk.models.CreateAssignmentPayload;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.ResendCostEstimate;
import com.assinafy.sdk.models.ResendResult;
import com.assinafy.sdk.models.SignerRef;
import com.assinafy.sdk.models.SignerIdentityReview;
import com.assinafy.sdk.models.WhatsappNotification;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.OkHttpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Client for account-wide assignment lists and document-scoped assignment operations. */
public final class AssignmentResource extends BaseResource {

    /** Stable validation code used when a certificate signer is not guaranteed a virtual assignment. */
    public static final String DIGITAL_CERTIFICATE_REQUIRES_VIRTUAL_METHOD =
            "digital_certificate_requires_virtual_method";

    /**
     * Creates an instance.
     *
     * @param httpClient shared HTTP client
     * @param baseUrl API base URL
     * @param defaultAccountId default account identifier, or {@code null}
     */
    public AssignmentResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    /**
     * {@code GET /assignments} — list assignments across the account.
     *
     * <p>Unlike the other assignment routes this one is not document-scoped; the API takes the account context
     * as the camelCase {@code accountId} query parameter (not a path segment). The SDK supplies it from the
     * client's default account id (or the explicit {@code accountId}). Supports {@code page} / {@code per-page}
     * and returns pagination metadata via {@link PaginatedResult#getMeta()}.</p>
     *
     * @param params optional list filters and pagination query values
     * @param accountId account override, or {@code null} for the client default
     * @return assignment page and response-header pagination metadata
     */
    public PaginatedResult<Assignment> list(Map<String, String> params, String accountId) {
        String id = accountId(accountId);
        Map<String, String> query = new LinkedHashMap<>(params != null ? params : Map.of());
        query.put("accountId", id);
        return httpGetList("/assignments", query, Assignment.class);
    }

    /**
     * {@code GET /assignments}.
     * Returns default account's assignment page.
     *
     * @param params optional list filters and pagination query values
     * @return default account's assignment page
     */
    public PaginatedResult<Assignment> list(Map<String, String> params) {
        return list(params, null);
    }

    /**
     * {@code GET /assignments}.
     * Returns default account's first assignment page.
     *
     * @return default account's first assignment page
     */
    public PaginatedResult<Assignment> list() {
        return list(null, null);
    }

    /**
     * {@code POST /documents/{documentId}/assignments} — request signatures. Request body:
     * {@code {"method": "virtual"|"collect", "signers": [{"id", "verification_method":
     * "Email"|"Whatsapp"|"DigitalCertificate"|"IdDocumentFaceMatchBasic",
     * "notification_methods": ["Email"|"Whatsapp"], "step"}],
     * "entries", "message", "expires_at", "copy_receivers"}}; {@code method} and each signer {@code id} are
     * required, and {@code collect} also requires {@code entries}. Response {@code data}: the {@link Assignment}.
     *
     * @param documentId required document identifier
     * @param payload required virtual or collect assignment body
     * @return created assignment
     * @throws ValidationException when the payload is invalid; certificate signers on {@code collect} use
     *         {@link #DIGITAL_CERTIFICATE_REQUIRES_VIRTUAL_METHOD} in {@link ValidationException#getErrors()}
     */
    public Assignment create(String documentId, CreateAssignmentPayload payload) {
        String docId = requireId(documentId, "Document ID");
        Map<String, Object> body = buildPayload(payload, false);
        return httpPost("/documents/" + docId + "/assignments", body, Assignment.class);
    }

    /**
     * {@code POST /documents/{documentId}/assignments/estimate-cost} — estimate the credit cost of an
     * assignment without creating it. Request body: {@code {"method", "signers": [{"verification_method",
     * "notification_methods"}], "entries"}} — signer IDs, steps, message, expiration, and copy receivers are
     * not part of an estimate and are not sent. Inspect {@link CostEstimate#getHasSufficientResources()} and
     * {@link CostEstimate#getBlockingReason()} before creating.
     *
     * @param documentId required document identifier
     * @param payload required virtual or collect estimate body
     * @return credit estimate
     * @throws ValidationException when the payload is invalid; certificate signers on {@code collect} use
     *         {@link #DIGITAL_CERTIFICATE_REQUIRES_VIRTUAL_METHOD} in {@link ValidationException#getErrors()}
     */
    public CostEstimate estimateCost(String documentId, CreateAssignmentPayload payload) {
        String docId = requireId(documentId, "Document ID");
        Map<String, Object> body = buildPayload(payload, true);
        return httpPost("/documents/" + docId + "/assignments/estimate-cost", body, CostEstimate.class);
    }

    /**
     * {@code POST /documents/{documentId}/assignments/{assignmentId}} — sign a document for the
     * collect-method flow. {@code entries} is an array of {@code {itemId, fieldId, pageId, value}} objects.
     * The API returns an empty envelope payload on success; failure cases are surfaced as exceptions.
     *
     * @param documentId required document identifier
     * @param assignmentId required assignment identifier
     * @param signerAccessCode required signer access code
     * @param entries one or more collect-field values
     */
    public void sign(String documentId, String assignmentId, String signerAccessCode, List<?> entries) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        if (entries == null || entries.isEmpty()) {
            throw new ValidationException("At least one sign entry is required");
        }
        httpPostVoid("/documents/" + docId + "/assignments/" + asgId,
                entries, signerAccessCodeQuery(signerAccessCode));
    }

    /**
     * {@code POST /documents/{documentId}/assignments/{assignmentId}}.
     * Strongly typed variant of {@link #sign(String, String, String, List)}.
     *
     * @param documentId required document identifier
     * @param assignmentId required assignment identifier
     * @param signerAccessCode required signer access code
     * @param entries one or more collect-field values
     */
    public void signEntries(String documentId, String assignmentId, String signerAccessCode,
            List<AssignmentSignEntry> entries) {
        sign(documentId, assignmentId, signerAccessCode, entries);
    }

    /**
     * {@code PUT /documents/{documentId}/assignments/{assignmentId}/reject} — signer declines the assignment.
     * Request body: {@code {"decline_reason": "..."}} (required, at most 2000 characters).
     *
     * @param documentId required document identifier
     * @param assignmentId required assignment identifier
     * @param signerAccessCode required signer access code
     * @param declineReason required decline reason, at most 2000 characters
     */
    public void decline(String documentId, String assignmentId, String signerAccessCode,
            String declineReason) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        requireDeclineReason(declineReason);
        httpPutVoid("/documents/" + docId + "/assignments/" + asgId + "/reject",
                Map.of("decline_reason", declineReason),
                signerAccessCodeQuery(signerAccessCode));
    }

    /**
     * {@code PUT /documents/{documentId}/assignments/{assignmentId}/reset-expiration} — set a new expiration
     * for the assignment.
     *
     * <p>Per the API, a {@code null} {@code expiresAt} is accepted and clears the expiration (the assignment no
     * longer expires); a blank/empty string is rejected as it is not a valid ISO-8601 datetime. Prefer
     * {@link #clearExpiration(String, String)} when the intent is to remove the expiration.</p>
     *
     * @param documentId required document identifier
     * @param assignmentId required assignment identifier
     * @param expiresAt new ISO-8601 expiration timestamp, or {@code null} to remove the expiration entirely
     * @return updated assignment
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
     *
     * @param documentId required document identifier
     * @param assignmentId required assignment identifier
     * @return updated assignment
     */
    public Assignment clearExpiration(String documentId, String assignmentId) {
        return resetExpiration(documentId, assignmentId, null);
    }

    /**
     * {@code PUT /documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/resend} — resend the
     * signature-request notification to a signer. Response payload: {@code {is_sent, document_id, signer_id}}.
     *
     * @param documentId required document identifier
     * @param assignmentId required assignment identifier
     * @param signerId required signer identifier
     * @return resend result
     */
    public ResendResult resendNotification(String documentId, String assignmentId, String signerId) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        String sid = requireId(signerId, "Signer ID");
        return httpPut(
                "/documents/" + docId + "/assignments/" + asgId + "/signers/" + sid + "/resend",
                null, ResendResult.class);
    }

    /**
     * {@code POST /documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/estimate-resend-cost} —
     * estimate the credit cost of resending a request to a signer. {@link ResendCostEstimate} exposes the
     * complete cost fields and the compact {@code total}/{@code has_sufficient_credits} aliases.
     *
     * @param documentId required document identifier
     * @param assignmentId required assignment identifier
     * @param signerId required signer identifier
     * @return resend credit estimate
     */
    public ResendCostEstimate estimateResendCost(String documentId, String assignmentId, String signerId) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        String sid = requireId(signerId, "Signer ID");
        return httpPost(
                "/documents/" + docId + "/assignments/" + asgId + "/signers/" + sid + "/estimate-resend-cost",
                null, ResendCostEstimate.class);
    }

    /**
     * {@code GET /documents/{documentId}/assignments/{assignmentId}/whatsapp-notifications}.
     *
     * @param documentId required document identifier
     * @param assignmentId required assignment identifier
     * @return recorded WhatsApp notifications, never {@code null}
     */
    public List<WhatsappNotification> whatsappNotifications(String documentId, String assignmentId) {
        String docId = requireId(documentId, "Document ID");
        String asgId = requireId(assignmentId, "Assignment ID");
        return orEmpty(httpGet("/documents/" + docId + "/assignments/" + asgId + "/whatsapp-notifications",
                new TypeReference<List<WhatsappNotification>>() {}));
    }

    /**
     * {@code GET /documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/identity-verification}.
     * Returns the account's biometric review and evidence summary. Images are available only while InReview.
     *
     * @param documentId required document identifier
     * @param assignmentId required assignment identifier
     * @param signerId required signer identifier
     * @return verification, evidence summary, and account review state
     */
    public SignerIdentityReview getIdentityVerification(String documentId, String assignmentId, String signerId) {
        return httpGet(identityVerificationPath(documentId, assignmentId, signerId), SignerIdentityReview.class);
    }

    /**
     * {@code GET /documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/identity-verification/images/{name}}.
     * Fetches JPEG, PNG, or PDF evidence while InReview; a decided review returns 404. Keep these bytes private.
     *
     * @param documentId required document identifier
     * @param assignmentId required assignment identifier
     * @param signerId required signer identifier
     * @param name document_front, document_back, document_pdf, or face, as listed in the review
     * @return raw evidence bytes
     * @throws ValidationException when an identifier or image name is invalid
     */
    public byte[] downloadIdentityVerificationImage(String documentId, String assignmentId, String signerId,
            String name) {
        if (name == null || !List.of("document_front", "document_back", "document_pdf", "face").contains(name)) {
            throw new ValidationException("Image name must be document_front, document_back, document_pdf, or face");
        }
        return httpGetBinary(identityVerificationPath(documentId, assignmentId, signerId) + "/images/" + name);
    }

    /**
     * {@code POST /documents/{documentId}/assignments/{assignmentId}/signers/{signerId}/identity-verification/review}.
     * Sends {@code {decision,note?}}. Approval releases certification once all signers finish and all reviews
     * are approved. Rejection cancels the document and notifies signers and copy receivers; the note stays private.
     *
     * @param documentId required document identifier
     * @param assignmentId required assignment identifier
     * @param signerId required signer identifier
     * @param decision Approved or Rejected; the verification must be InReview
     * @param note optional private note of at most 2000 characters, recorded and sent to the provider
     * @return decided verification and review
     * @throws ValidationException when an identifier, decision, or note is invalid
     */
    public SignerIdentityReview reviewIdentityVerification(String documentId, String assignmentId, String signerId,
            String decision, String note) {
        if (!"Approved".equals(decision) && !"Rejected".equals(decision)) {
            throw new ValidationException("Identity review decision must be Approved or Rejected");
        }
        if (note != null && note.length() > 2000) {
            throw new ValidationException("Identity review note must be at most 2000 characters");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("decision", decision);
        if (note != null) body.put("note", note);
        return httpPost(identityVerificationPath(documentId, assignmentId, signerId) + "/review", body,
                SignerIdentityReview.class);
    }

    private String identityVerificationPath(String documentId, String assignmentId, String signerId) {
        return "/documents/" + requireId(documentId, "Document ID")
                + "/assignments/" + requireId(assignmentId, "Assignment ID")
                + "/signers/" + requireId(signerId, "Signer ID") + "/identity-verification";
    }

    private static Map<String, Object> buildPayload(CreateAssignmentPayload payload, boolean estimate) {
        if (payload == null) {
            throw new ValidationException("Assignment payload is required");
        }
        String method = payload.getMethod() != null ? payload.getMethod() : "virtual";
        if (!"virtual".equals(method) && !"collect".equals(method)) {
            throw new ValidationException("Assignment method must be 'virtual' or 'collect'");
        }
        boolean hasEntries = payload.getEntries() != null && !payload.getEntries().isEmpty();
        if ("collect".equals(method) && !hasEntries) {
            throw new ValidationException("At least one collect entry is required");
        }

        // Required for both methods: creation needs to know who signs, and an estimate is priced
        // per signer. The contract marks `signers` required only for `virtual`, but the API
        // answers a signer-less body with 400 "Pelo menos um signatários precisa ser informado."
        List<SignerRef> signerRefs = payload.resolveSignerRefs();
        if (signerRefs.isEmpty()) {
            throw new ValidationException("At least one signer is required");
        }

        List<Map<String, Object>> normalisedSigners = new ArrayList<>();
        for (SignerRef ref : signerRefs) {
            normalisedSigners.add(normaliseSignerRef(ref, estimate));
        }
        if (!"virtual".equals(method) && normalisedSigners.stream()
                .anyMatch(signer -> "DigitalCertificate".equals(signer.get("verification_method")))) {
            throw digitalCertificateRequiresVirtualMethod(method);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("method", method);
        body.put("signers", normalisedSigners);
        if (!estimate) {
            if (payload.getMessage() != null) body.put("message", payload.getMessage());
            if (payload.getExpiresAt() != null) body.put("expires_at", payload.getExpiresAt());
            if (payload.getCopyReceivers() != null && !payload.getCopyReceivers().isEmpty()) {
                body.put("copy_receivers", payload.getCopyReceivers());
            }
        }
        if (payload.getEntries() != null && !payload.getEntries().isEmpty()) {
            body.put("entries", payload.getEntries());
        }
        return body;
    }

    /**
     * Builds one wire signer. Creation sends {@code {id, verification_method, notification_methods, step}} and
     * requires {@code id}; an estimate prices channels only, so it sends {@code {verification_method,
     * notification_methods}}.
     */
    private static Map<String, Object> normaliseSignerRef(SignerRef ref, boolean estimate) {
        if (ref == null) {
            throw new ValidationException("Signer references cannot be null");
        }
        String id = ref.getId();
        Map<String, Object> result = new LinkedHashMap<>();
        if (!estimate) {
            if (id == null || id.isBlank()) {
                throw new ValidationException("Invalid signer reference: ID is required for this operation");
            }
            result.put("id", id);
        }
        if (ref.getVerificationMethod() != null) {
            result.put("verification_method", ref.getVerificationMethod());
        }
        if (ref.getNotificationMethods() != null && !ref.getNotificationMethods().isEmpty()) {
            result.put("notification_methods", ref.getNotificationMethods());
        }
        if (!estimate && ref.getStep() != null) {
            result.put("step", ref.getStep());
        }
        return result;
    }

    static ValidationException digitalCertificateRequiresVirtualMethod(String method) {
        Map<String, Object> errors = new LinkedHashMap<>();
        errors.put("code", DIGITAL_CERTIFICATE_REQUIRES_VIRTUAL_METHOD);
        errors.put("method", method);
        return new ValidationException("DigitalCertificate signers require method: virtual", errors);
    }
}
