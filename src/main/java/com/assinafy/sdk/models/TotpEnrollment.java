package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An unconfirmed authenticator-app enrollment. The secret is returned only once and cannot be retrieved again.
 *
 * @param id method identifier to pass when confirming the enrollment
 * @param secret base32 shared secret for manual entry in the authenticator app
 * @param provisioningUri wire {@code provisioning_uri}: the {@code otpauth://} URI to render as a QR code
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TotpEnrollment(
        String id,
        String secret,
        @JsonProperty("provisioning_uri") String provisioningUri) {}
