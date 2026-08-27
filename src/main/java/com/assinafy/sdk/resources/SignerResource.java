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

/** Account-owner client for signer creation, lookup, update, deletion, and search. */
public final class SignerResource extends BaseResource {

    /**
     * Creates an instance.
     *
     * @param httpClient shared HTTP client
     * @param baseUrl API base URL
     * @param defaultAccountId default account identifier, or {@code null}
     */
    public SignerResource(OkHttpClient httpClient, String baseUrl, String defaultAccountId) {
        super(httpClient, baseUrl, defaultAccountId);
    }

    /**
     * {@code POST /accounts/{account_id}/signers} — create a signer.
     * This method always sends the creation request; use {@link #findOrCreate(CreateSignerPayload, String)}
     * when email-based reuse is desired.
     *
     * @param payload required signer fields
     * @param accountId account override, or {@code null} for the client default
     * @return created signer
     */
    public Signer create(CreateSignerPayload payload, String accountId) {
        validateCreatePayload(payload);
        String id = accountId(accountId);
        return createValidated(payload, id);
    }

    /**
     * Finds a signer by email or creates one when no exact match exists.
     *
     * <p>This is the explicit idempotent alternative to {@link #create(CreateSignerPayload, String)}. Existing
     * signer fields are not updated. A duplicate-email response caused by a concurrent creator is recovered by
     * one final lookup.</p>
     *
     * @param payload required signer fields
     * @param accountId account override, or {@code null} for the client default
     * @return existing or newly created signer
     */
    public Signer findOrCreate(CreateSignerPayload payload, String accountId) {
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
            return createValidated(payload, id);
        } catch (ApiException e) {
            // Recover a concurrent duplicate response only when an exact email match can now be found.
            if ((e.getStatusCode() == 400 || e.getStatusCode() == 409) && hasEmail) {
                try {
                    Signer duplicate = findByEmail(payload.getEmail(), id);
                    if (duplicate != null) {
                        return duplicate;
                    }
                } catch (RuntimeException lookupFailure) {
                    e.addSuppressed(lookupFailure);
                }
            }
            throw e;
        }
    }

    /**
     * Finds or creates a signer in the default account.
     *
     * @param payload required signer fields
     * @return existing or newly created signer
     */
    public Signer findOrCreate(CreateSignerPayload payload) {
        return findOrCreate(payload, null);
    }

    /**
     * Creates a signer in the default account.
     *
     * @param payload required signer fields
     * @return created signer in the default account
     */
    public Signer create(CreateSignerPayload payload) {
        return create(payload, null);
    }

    private Signer createValidated(CreateSignerPayload payload, String accountId) {
        return httpPost("/accounts/" + accountId + "/signers", normalisePayload(payload), Signer.class);
    }

    /**
     * {@code GET /accounts/{account_id}/signers/{signer_id}}.
     *
     * @param signerId required signer identifier
     * @param accountId account override, or {@code null} for the client default
     * @return signer details
     */
    public Signer get(String signerId, String accountId) {
        String id = accountId(accountId);
        String sid = requireId(signerId, "Signer ID");
        return httpGet("/accounts/" + id + "/signers/" + sid, Signer.class);
    }

    /**
     * Returns signer details in the default account.
     *
     * @param signerId required signer identifier
     * @return signer details in the default account
     */
    public Signer get(String signerId) {
        return get(signerId, null);
    }

    /**
     * {@code GET /accounts/{account_id}/signers} — list signers of the workspace. Supports the documented
     * {@code search} query parameter (filters by {@code full_name} or {@code email}) plus pagination params.
     *
     * @param params optional search and pagination values
     * @param accountId account override, or {@code null} for the client default
     * @return signer page and response-header pagination metadata
     */
    public PaginatedResult<Signer> list(Map<String, String> params, String accountId) {
        String id = accountId(accountId);
        return httpGetList("/accounts/" + id + "/signers",
                params != null ? params : Map.of(), Signer.class);
    }

    /**
     * Returns default account's signer page.
     *
     * @param params optional search and pagination values
     * @return default account's signer page
     */
    public PaginatedResult<Signer> list(Map<String, String> params) {
        return list(params, null);
    }

    /**
     * Returns default account's first signer page.
     *
     * @return default account's first signer page
     */
    public PaginatedResult<Signer> list() {
        return list(null, null);
    }

    /**
     * {@code PUT /accounts/{account_id}/signers/{signer_id}} — update any supplied {@code full_name},
     * {@code email}, {@code whatsapp_phone_number}, or {@code government_id} field and return the signer.
     *
     * @param signerId required signer identifier
     * @param payload required partial signer update
     * @param accountId account override, or {@code null} for the client default
     * @return updated signer
     */
    public Signer update(String signerId, UpdateSignerPayload payload, String accountId) {
        String id = accountId(accountId);
        String sid = requireId(signerId, "Signer ID");
        if (payload == null) {
            throw new ValidationException("Signer update payload is required");
        }
        if (payload.getEmail() != null) {
            requireEmail(payload.getEmail(), "Signer email");
        }
        return httpPut("/accounts/" + id + "/signers/" + sid, normaliseUpdatePayload(payload), Signer.class);
    }

    /**
     * Returns updated signer in the default account.
     *
     * @param signerId required signer identifier
     * @param payload required partial signer update
     * @return updated signer in the default account
     */
    public Signer update(String signerId, UpdateSignerPayload payload) {
        return update(signerId, payload, null);
    }

    /**
     * {@code DELETE /accounts/{account_id}/signers/{signer_id}}.
     *
     * @param signerId required signer identifier
     * @param accountId account override, or {@code null} for the client default
     */
    public void delete(String signerId, String accountId) {
        String id = accountId(accountId);
        String sid = requireId(signerId, "Signer ID");
        httpDelete("/accounts/" + id + "/signers/" + sid);
    }

    /**
     * Deletes the selected resource.
     *
     * @param signerId required signer identifier in the default account
     */
    public void delete(String signerId) {
        delete(signerId, null);
    }

    /**
     * Finds a signer by exact email using {@code GET /accounts/{account_id}/signers?search=...} and matching
     * case-insensitively on the email. All pages reported by the API are searched. Returns {@code null} when no
     * signer matches.
     *
     * @param email required email address
     * @param accountId account override, or {@code null} for the client default
     * @return matching signer, or {@code null}
     */
    public Signer findByEmail(String email, String accountId) {
        requireEmail(email, "Signer email");
        String id = accountId(accountId);
        int page = 1;
        while (true) {
            PaginatedResult<Signer> result = list(
                    queryParams("search", email, "per_page", "100", "page", page), id);
            Signer match = result.getData().stream()
                    .filter(s -> s.getEmail() != null && s.getEmail().equalsIgnoreCase(email))
                    .findFirst()
                    .orElse(null);
            if (match != null) {
                return match;
            }
            if (result.getMeta() == null || result.getMeta().getLastPage() == null
                    || page >= result.getMeta().getLastPage()) {
                return null;
            }
            page++;
        }
    }

    /**
     * Returns matching signer in the default account, or {@code null}.
     *
     * @param email required email address
     * @return matching signer in the default account, or {@code null}
     */
    public Signer findByEmail(String email) {
        return findByEmail(email, null);
    }

    private void validateCreatePayload(CreateSignerPayload payload) {
        if (payload == null) {
            throw new ValidationException("Signer payload is required");
        }
        if (payload.getFullName() == null || payload.getFullName().isBlank()) {
            throw new ValidationException("Signer full name is required");
        }
        if (payload.getEmail() != null && !payload.getEmail().isBlank()) {
            requireEmail(payload.getEmail(), "Signer email");
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
        if (payload.getEmail() != null) body.put("email", payload.getEmail());
        if (payload.getWhatsappPhoneNumber() != null) {
            body.put("whatsapp_phone_number", payload.getWhatsappPhoneNumber());
        }
        if (payload.getGovernmentId() != null) body.put("government_id", payload.getGovernmentId());
        if (body.isEmpty()) {
            throw new ValidationException("At least one signer attribute is required");
        }
        return body;
    }
}
