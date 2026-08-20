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

    /** Creates an empty response model for deserialization. */
    public AssignmentSignerNotification() {}

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

    /**
     * Returns normalized API event value for the tracked notification record.
     *
     * @return normalized API event value for the tracked notification record
     */
    public String getEvent() { return event; }

    /**
     * Sets normalized notification event.
     *
     * @param event normalized notification event
     */
    public void setEvent(String event) { this.event = event; }

    /**
     * Returns {@code failed} on a delivery problem, otherwise {@code sent}.
     *
     * @return {@code failed} on a delivery problem, otherwise {@code sent}
     */
    public String getStatus() { return status; }

    /**
     * Sets normalized delivery status.
     *
     * @param status normalized delivery status
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Returns provider {@code error_code} when delivery fails, or {@code null}.
     *
     * @return provider {@code error_code} when delivery fails, or {@code null}
     */
    public String getErrorCode() { return errorCode; }

    /**
     * Sets provider {@code error_code}.
     *
     * @param errorCode provider {@code error_code}
     */
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    /**
     * Returns provider {@code error_message} when delivery fails, or {@code null}.
     *
     * @return provider {@code error_message} when delivery fails, or {@code null}
     */
    public String getErrorMessage() { return errorMessage; }

    /**
     * Sets provider {@code error_message}.
     *
     * @param errorMessage provider {@code error_message}
     */
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    /**
     * Returns ISO 8601 {@code sent_at} timestamp, or {@code null}.
     *
     * @return ISO 8601 {@code sent_at} timestamp, or {@code null}
     */
    public String getSentAt() { return sentAt; }

    /**
     * Sets value of {@code sent_at}.
     *
     * @param sentAt value of {@code sent_at}
     */
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }

    /**
     * Returns ISO 8601 {@code failed_at} timestamp, or {@code null}.
     *
     * @return ISO 8601 {@code failed_at} timestamp, or {@code null}
     */
    public String getFailedAt() { return failedAt; }

    /**
     * Sets value of {@code failed_at}.
     *
     * @param failedAt value of {@code failed_at}
     */
    public void setFailedAt(String failedAt) { this.failedAt = failedAt; }
}
