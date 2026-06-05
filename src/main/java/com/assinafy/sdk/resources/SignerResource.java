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

    /**
     * {@code POST /accounts/{account_id}/signers} — create a signer.
     *
     * <p><b>Idempotent by email.</b> When the payload carries an email, this first looks the signer up by email
     * and, if one already exists, returns the existing record <em>without</em> sending the POST. As a safety net
     * for a race, a duplicate-email error from the API (the live API reports this as HTTP 400, historically 409)
     * is recovered by re-querying and returning the existing signer. Because of this, {@code create} will not
     * apply changed {@code full_name}/{@code whatsapp_phone_number} values to an already-existing signer — use
     * {@link #update} to modify an existing signer.</p>
     */
    public Signer create(CreateSignerPayload payload, String accountId) {
        validateCreatePayload(payload);
        String id = accountId(accountId);

        boolean hasEmail = payload.getEmail() != null && !payload.getEmail().isBlank();
        if (hasEmail) {
            Signer existing = findByEmail(payload.getEmail(), id);
            if (existing != null) {
                return existing;
            }
        }

        try {
            return httpPost("/accounts/" + id + "/signers", normalisePayload(payload), Signer.class);
        } catch (ApiException e) {
            // The API rejects a duplicate email (observed as HTTP 400, historically 409). Recover by
            // returning the pre-existing signer when we can find it; otherwise surface the original error.
            if ((e.getStatusCode() == 400 || e.getStatusCode() == 409) && hasEmail) {
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

    /** {@code GET /accounts/{account_id}/signers/{signer_id}} — retrieve a signer's information. */
    public Signer get(String signerId, String accountId) {
        String id = accountId(accountId);
        String sid = requireId(signerId, "Signer ID");
        return httpGet("/accounts/" + id + "/signers/" + sid, Signer.class);
    }

    public Signer get(String signerId) {
        return get(signerId, null);
    }

    /**
     * {@code GET /accounts/{account_id}/signers} — list signers of the workspace. Supports the documented
     * {@code search} query parameter (filters by {@code full_name} or {@code email}) plus pagination params.
     */
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

    /** {@code PUT /accounts/{account_id}/signers/{signer_id}} — update a signer's information. */
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

    /** {@code DELETE /accounts/{account_id}/signers/{signer_id}} — delete a signer. */
    public void delete(String signerId, String accountId) {
        String id = accountId(accountId);
        String sid = requireId(signerId, "Signer ID");
        httpDelete("/accounts/" + id + "/signers/" + sid);
    }

    public void delete(String signerId) {
        delete(signerId, null);
    }

    /**
     * Finds a signer by exact email using {@code GET /accounts/{account_id}/signers?search=...} and matching
     * case-insensitively on the email. Returns {@code null} when no signer matches. Note the lookup pages
     * through up to 100 search results, so it may miss a match if the workspace has many same-prefix emails.
     */
    public Signer findByEmail(String email, String accountId) {
        assertEmail(email);
        String id = accountId(accountId);
        try {
            PaginatedResult<Signer> result = list(
                    queryParams("search", email, "per_page", "100"), id);
            return result.getData().stream()
                    .filter(s -> s.getEmail() != null && s.getEmail().equalsIgnoreCase(email))
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
