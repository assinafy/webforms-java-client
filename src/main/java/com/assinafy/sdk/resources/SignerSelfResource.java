package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.AcceptTermsResponse;
import com.assinafy.sdk.models.ConfirmSignerDataPayload;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.models.VerifyEmailResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.Map;

public final class SignerSelfResource extends BaseResource {

    public SignerSelfResource(OkHttpClient httpClient, String baseUrl) {
        super(httpClient, baseUrl, null);
    }

    public Signer getSelf(String signerAccessCode) {
        String code = requireAccessCode(signerAccessCode);
        return httpGet("/signers/self?signer-access-code=" + code, Signer.class);
    }

    public AcceptTermsResponse acceptTerms(String signerAccessCode) {
        String code = requireAccessCode(signerAccessCode);
        Map<String, Object> body = Map.of("signer-access-code", code);
        return httpPut("/signers/accept-terms", body, AcceptTermsResponse.class);
    }

    public VerifyEmailResponse verifyEmail(String verificationCode, String signerAccessCode) {
        String code = requireAccessCode(signerAccessCode);
        requireId(verificationCode, "Verification code");
        Map<String, Object> body = Map.of(
                "verification-code", verificationCode,
                "signer-access-code", code
        );
        Request request = new Request.Builder()
                .url(baseUrl + "/verify")
                .post(RequestBody.create(toJson(body), JSON))
                .build();
        return execute(request, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance()
                .constructRawType(VerifyEmailResponse.class));
    }

    public void confirmSignerData(String documentId, String signerAccessCode, ConfirmSignerDataPayload payload) {
        String docId = requireId(documentId, "Document ID");
        String code = requireAccessCode(signerAccessCode);
        if (payload == null) {
            throw new ValidationException("Payload is required");
        }
        httpPut("/documents/" + docId + "/signers/confirm-data?signer-access-code=" + code, payload, Map.class);
    }

    public byte[] uploadSignature(String signerAccessCode, byte[] imageBytes, String type) {
        String code = requireAccessCode(signerAccessCode);
        String signatureType = type != null ? type : "signature";
        MediaType imageType = MediaType.get("image/png");
        RequestBody body = RequestBody.create(imageBytes, imageType);
        Request request = new Request.Builder()
                .url(baseUrl + "/signature?signer-access-code=" + code + "&type=" + signatureType)
                .post(body)
                .build();
        return executeBinary(request);
    }

    public byte[] downloadSignature(String signerAccessCode, String type) {
        String code = requireAccessCode(signerAccessCode);
        String signatureType = type != null ? type : "signature";
        return httpGetBinary("/signature/" + signatureType + "?signer-access-code=" + code);
    }

    private String requireAccessCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ValidationException("Signer access code is required");
        }
        return code;
    }

    private String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new ValidationException("Failed to serialize request body: " + e.getMessage());
        }
    }
}