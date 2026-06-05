package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.AcceptTermsResponse;
import com.assinafy.sdk.models.ConfirmSignerDataPayload;
import com.assinafy.sdk.models.DocumentDetails;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.models.VerifyEmailResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoints accessed by a signer using a short-lived {@code signer-access-code}.
 *
 * <p>These endpoints intentionally do not require the account-level API key. They are
 * authorised via the {@code signer-access-code} query parameter delivered to the signer.</p>
 */
public final class SignerSelfResource extends BaseResource {

    private static final MediaType PNG = MediaType.get("image/png");
    private static final MediaType JPEG = MediaType.get("image/jpeg");
    private static final String SIGNER_ACCESS_CODE = "signer-access-code";

    public SignerSelfResource(OkHttpClient httpClient, String baseUrl) {
        super(httpClient, baseUrl, null);
    }

    /** {@code GET /signers/self} — return the calling signer's profile. */
    public Signer getSelf(String signerAccessCode) {
        return httpGet("/signers/self", accessCodeQuery(signerAccessCode), Signer.class);
    }

    /** {@code GET /sign} - retrieve the signer-facing document and assignment details. */
    public DocumentDetails getSign(String signerAccessCode, Boolean hasAcceptedTerms) {
        Map<String, String> query = new LinkedHashMap<>(accessCodeQuery(signerAccessCode));
        if (hasAcceptedTerms != null) {
            query.put("has_accepted_terms", String.valueOf(hasAcceptedTerms));
        }
        return httpGet("/sign", query, DocumentDetails.class);
    }

    public DocumentDetails getSign(String signerAccessCode) {
        return getSign(signerAccessCode, null);
    }

    /** {@code PUT /signers/accept-terms} — accept the platform terms of use. */
    public AcceptTermsResponse acceptTerms(String signerAccessCode) {
        String code = requireAccessCode(signerAccessCode);
        return httpPut("/signers/accept-terms", Map.of(SIGNER_ACCESS_CODE, code), AcceptTermsResponse.class);
    }

    /** {@code POST /verify} — exchange a verification code for confirmation that the signer's email matches. */
    public VerifyEmailResponse verifyEmail(String verificationCode, String signerAccessCode) {
        String code = requireAccessCode(signerAccessCode);
        requireId(verificationCode, "Verification code");
        Map<String, Object> body = Map.of(
                "verification-code", verificationCode,
                SIGNER_ACCESS_CODE, code
        );
        return httpPost("/verify", body, VerifyEmailResponse.class);
    }

    /** {@code PUT /documents/{documentId}/signers/confirm-data} — confirm signer data before signing. */
    public void confirmSignerData(String documentId, String signerAccessCode, ConfirmSignerDataPayload payload) {
        String docId = requireId(documentId, "Document ID");
        if (payload == null) {
            throw new ValidationException("Payload is required");
        }
        httpPut("/documents/" + docId + "/signers/confirm-data", payload, Map.class,
                accessCodeQuery(signerAccessCode));
    }

    /**
     * {@code POST /signature} — upload the signer's signature or initial image.
     *
     * <p>The image is sent as the raw request body (content type auto-detected as {@code image/png} or
     * {@code image/jpeg} from the file header). The API responds with a JSON envelope
     * ({@code {"status":200,"message":"","data":[]}}), so this method returns {@code void} and raises an
     * {@link com.assinafy.sdk.exceptions.ApiException} on an error envelope or non-2xx status.</p>
     *
     * @param signerAccessCode access code for the signer
     * @param imageBytes raw PNG or JPEG image bytes
     * @param type either {@code "signature"} or {@code "initial"}; defaults to {@code "signature"}
     */
    public void uploadSignature(String signerAccessCode, byte[] imageBytes, String type) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new ValidationException("Signature image bytes are required");
        }
        String signatureType = resolveSignatureType(type);
        MediaType mediaType = detectImageMediaType(imageBytes);
        RequestBody body = RequestBody.create(imageBytes, mediaType);
        Map<String, String> params = signatureQuery(signerAccessCode, signatureType);
        httpPostBinaryEnvelope("/signature", params, body);
    }

    /** {@code GET /signature/{type}} — download the signer's signature or initial image. */
    public byte[] downloadSignature(String signerAccessCode, String type) {
        return httpGetBinary("/signature/" + resolveSignatureType(type), accessCodeQuery(signerAccessCode));
    }

    /**
     * {@code GET /signers/{signer_id}/document} — fetch the document currently associated with the signer
     * access code, without exposing other signers' pages or fields.
     */
    public DocumentDetails getCurrentDocument(String signerId, String signerAccessCode) {
        String sid = requireId(signerId, "Signer ID");
        return httpGet("/signers/" + sid + "/document", accessCodeQuery(signerAccessCode), DocumentDetails.class);
    }

    /** {@code GET /signers/{signer_id}/documents} — list all documents waiting for the signer. */
    public PaginatedResult<DocumentDetails> listDocuments(String signerId, String signerAccessCode) {
        String sid = requireId(signerId, "Signer ID");
        return httpGetList("/signers/" + sid + "/documents",
                accessCodeQuery(signerAccessCode), DocumentDetails.class);
    }

    /** {@code GET /signers/{signer_id}/documents} with documented filters such as status, method and search. */
    public PaginatedResult<DocumentDetails> listDocuments(String signerId, String signerAccessCode,
            Map<String, String> params) {
        String sid = requireId(signerId, "Signer ID");
        Map<String, String> query = new LinkedHashMap<>(params != null ? params : Map.of());
        query.put(SIGNER_ACCESS_CODE, requireAccessCode(signerAccessCode));
        return httpGetList("/signers/" + sid + "/documents", query, DocumentDetails.class);
    }

    /** {@code PUT /signers/documents/sign-multiple} — sign several virtual-method documents in one call. */
    public void signMultiple(String signerAccessCode, List<String> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            throw new ValidationException("At least one document ID is required");
        }
        httpPutVoid("/signers/documents/sign-multiple", Map.of("document_ids", documentIds),
                accessCodeQuery(signerAccessCode));
    }

    /** {@code PUT /signers/documents/decline-multiple} — decline several documents in one call. */
    public void declineMultiple(String signerAccessCode, List<String> documentIds, String declineReason) {
        if (documentIds == null || documentIds.isEmpty()) {
            throw new ValidationException("At least one document ID is required");
        }
        if (declineReason == null || declineReason.isBlank()) {
            throw new ValidationException("Decline reason is required");
        }
        Map<String, Object> body = Map.of(
                "document_ids", documentIds,
                "decline_reason", declineReason
        );
        httpPutVoid("/signers/documents/decline-multiple", body, accessCodeQuery(signerAccessCode));
    }

    /** {@code GET /signers/{signer_id}/documents/{document_id}/download/{artifact_name}} */
    public byte[] downloadDocument(String signerId, String documentId, String artifactName,
            String signerAccessCode) {
        String sid = requireId(signerId, "Signer ID");
        String docId = requireId(documentId, "Document ID");
        String artifact = artifactName != null ? artifactName : "original";
        return httpGetBinary("/signers/" + sid + "/documents/" + docId + "/download/" + artifact,
                accessCodeQuery(signerAccessCode));
    }

    private Map<String, String> accessCodeQuery(String signerAccessCode) {
        return Map.of(SIGNER_ACCESS_CODE, requireAccessCode(signerAccessCode));
    }

    private Map<String, String> signatureQuery(String signerAccessCode, String type) {
        return Map.of(
                SIGNER_ACCESS_CODE, requireAccessCode(signerAccessCode),
                "type", type
        );
    }

    private String resolveSignatureType(String type) {
        if (type == null || type.isBlank()) return "signature";
        if (!"signature".equals(type) && !"initial".equals(type)) {
            throw new ValidationException("Signature type must be 'signature' or 'initial'");
        }
        return type;
    }

    private MediaType detectImageMediaType(byte[] bytes) {
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return JPEG;
        }
        return PNG;
    }

    private String requireAccessCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ValidationException("Signer access code is required");
        }
        return code;
    }
}
