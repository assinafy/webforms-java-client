package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ApiException;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.CreateSignerPayload;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.models.UpdateSignerPayload;
import okhttp3.OkHttpClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class SignerResource extends BaseResource {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public SignerResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    public Signer create(CreateSignerPayload payload, String accountId) {
        validateCreatePayload(payload);
        String id = accountId(accountId);

        if (payload.getEmail() != null && !payload.getEmail().isBlank()) {
            Signer existing = findByEmail(payload.getEmail(), id);
            if (existing != null) {
                return existing;
            }
        }

        try {
            return httpPost("/accounts/" + id + "/signers", normalisePayload(payload), Signer.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 409 && payload.getEmail() != null && !payload.getEmail().isBlank()) {
                Signer duplicate = findByEmail(payload.getEmail(), id);
                if (duplicate != null) {
                    return duplicate;
                }
            }
            throw e;
        }
    }

    public Signer create(CreateSignerPayload payload) {
        return create(payload, null);
    }

    public Signer get(String signerId, String accountId) {
        String id = accountId(accountId);
        String sid = requireId(signerId, "Signer ID");
        return httpGet("/accounts/" + id + "/signers/" + sid, Signer.class);
    }

    public Signer get(String signerId) {
        return get(signerId, null);
    }

    public PaginatedResult<Signer> list(Map<String, String> params, String accountId) {
        String id = accountId(accountId);
        return httpGetList("/accounts/" + id + "/signers",
                params != null ? params : Map.of(), Signer.class);
    }

    public PaginatedResult<Signer> list(Map<String, String> params) {
        return list(params, null);
    }

    public PaginatedResult<Signer> list() {
        return list(null, null);
    }

    public Signer update(String signerId, UpdateSignerPayload payload, String accountId) {
        String id = accountId(accountId);
        String sid = requireId(signerId, "Signer ID");
        if (payload == null) {
            throw new ValidationException("Signer update payload is required");
        }
        if (payload.getEmail() != null && !payload.getEmail().isBlank()) {
            assertEmail(payload.getEmail());
        }
        return httpPut("/accounts/" + id + "/signers/" + sid, normaliseUpdatePayload(payload), Signer.class);
    }

    public Signer update(String signerId, UpdateSignerPayload payload) {
        return update(signerId, payload, null);
    }

    public void delete(String signerId, String accountId) {
        String id = accountId(accountId);
        String sid = requireId(signerId, "Signer ID");
        httpDelete("/accounts/" + id + "/signers/" + sid);
    }

    public void delete(String signerId) {
        delete(signerId, null);
    }

    public Signer findByEmail(String email, String accountId) {
        assertEmail(email);
        String id = accountId(accountId);
        try {
            PaginatedResult<Signer> result = list(
                    queryParams("search", email, "per_page", "100"), id);
            String lower = email.toLowerCase();
            return result.getData().stream()
                    .filter(s -> s.getEmail() != null && s.getEmail().equalsIgnoreCase(lower))
                    .findFirst()
                    .orElse(null);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                return null;
            }
            throw e;
        }
    }

    public Signer findByEmail(String email) {
        return findByEmail(email, null);
    }

    private void assertEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email address");
        }
    }

    private void validateCreatePayload(CreateSignerPayload payload) {
        if (payload == null) {
            throw new ValidationException("Signer payload is required");
        }
        if (payload.getFullName() == null || payload.getFullName().isBlank()) {
            throw new ValidationException("Signer full name is required");
        }
        if (payload.getEmail() != null && !payload.getEmail().isBlank()) {
            assertEmail(payload.getEmail());
        }
    }

    private Map<String, Object> normalisePayload(CreateSignerPayload payload) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("full_name", payload.getFullName());
        if (payload.getEmail() != null && !payload.getEmail().isBlank()) {
            body.put("email", payload.getEmail());
        }
        if (payload.getWhatsappPhoneNumber() != null && !payload.getWhatsappPhoneNumber().isBlank()) {
            body.put("whatsapp_phone_number", payload.getWhatsappPhoneNumber());
        }
        return body;
    }

    private Map<String, Object> normaliseUpdatePayload(UpdateSignerPayload payload) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (payload.getFullName() != null) body.put("full_name", payload.getFullName());
        if (payload.getEmail() != null && !payload.getEmail().isBlank()) body.put("email", payload.getEmail());
        if (payload.getWhatsappPhoneNumber() != null) {
            body.put("whatsapp_phone_number", payload.getWhatsappPhoneNumber());
        }
        return body;
    }
}
