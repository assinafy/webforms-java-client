package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * A document signer's delivery history, including delivery and opening timestamps.
 * @param signerId account signer identifier
 * @param notificationHistory tracked notifications; an open is a hint, not proof of reading
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SignerNotificationHistory(
        @JsonProperty("signer_id") String signerId,
        @JsonProperty("notification_history") List<AssignmentSignerNotification> notificationHistory) {}
