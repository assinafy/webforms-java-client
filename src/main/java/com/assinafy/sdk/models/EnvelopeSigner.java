package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Signer progress on one envelope document.
 *
 * @param id signer identifier
 * @param fullName display name
 * @param email email or null
 * @param whatsappPhoneNumber WhatsApp number or null
 * @param completed whether this signer signed
 * @param signedAt signature timestamp or null
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EnvelopeSigner(
        @JsonProperty("id") String id,
        @JsonProperty("full_name") String fullName,
        @JsonProperty("email") String email,
        @JsonProperty("whatsapp_phone_number") String whatsappPhoneNumber,
        @JsonProperty("completed") Boolean completed,
        @JsonProperty("signed_at") String signedAt) {}
