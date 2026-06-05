package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single tracked notification-delivery record for a signer within an assignment.
 *
 * <p>Returned inside {@code assignment.signers[].notification_history}. This data is only present in
 * account-owner contexts; in signer-facing contexts the array is absent and deserializes to {@code null}.</p>
 *
 * <p>Documented supported {@link #getEvent() event} values include {@code signature_request},
 * {@code document_about_to_expire}, {@code document_expired}, {@code document_canceled},
 * {@code document_declined}, {@code signed_delivery}, and {@code unknown} for unmapped legacy/provider data.
 * {@link #getStatus() status} is {@code sent} on success or {@code failed} on a delivery problem.</p>
 *
 * @see <a href="https://api.assinafy.com.br/v1/docs">Assignment Signer Notification Object</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class AssignmentSignerNotification {

    private String event;
    private String status;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("sent_at")
    private String sentAt;

    @JsonProperty("failed_at")
    private String failedAt;

    /** Normalized API event value for the tracked notification record. */
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    /** Normalized delivery status: {@code failed} on a delivery problem, otherwise {@code sent}. */
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /** Provider error code when the notification fails, or {@code null}. */
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    /** Provider error message when the notification fails, or {@code null}. */
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    /** ISO 8601 timestamp when the notification was sent, or {@code null}. */
    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }

    /** ISO 8601 timestamp when the notification failed, or {@code null}. */
    public String getFailedAt() { return failedAt; }
    public void setFailedAt(String failedAt) { this.failedAt = failedAt; }
}
