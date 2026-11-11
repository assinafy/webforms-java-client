package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Certified-copy availability and optional delivery result.
 *
 * @param available whether another copy can be requested
 * @param notificationMethod Email, Whatsapp, or null
 * @param resendsLeft remaining WhatsApp resends; null for email
 * @param sentTo masked delivery contact, present after sending
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CertifiedCopy(
        @JsonProperty("available") Boolean available,
        @JsonProperty("notification_method") String notificationMethod,
        @JsonProperty("resends_left") Integer resendsLeft,
        @JsonProperty("sent_to") String sentTo) {}
