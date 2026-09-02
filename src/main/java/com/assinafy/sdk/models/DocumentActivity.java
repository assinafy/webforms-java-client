package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** One event in a document's account-visible activity log. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentActivity {

    /** Creates an empty response model for deserialization. */
    public DocumentActivity() {}

    private long id;
    private String event;
    private String message;
    private Object payload;
    private Object origin;

    @JsonProperty("created_at")
    private String createdAt;

    /**
     * Returns the numeric activity identifier.
     *
     * @return the numeric activity identifier
     */
    public long getId() { return id; }

    /**
     * Sets numeric activity identifier.
     *
     * @param id numeric activity identifier
     */
    public void setId(long id) { this.id = id; }

    /**
     * Returns the event type code, such as {@code assignment_created}.
     *
     * @return the event type code, such as {@code assignment_created}
     */
    public String getEvent() { return event; }

    /**
     * Sets event type code.
     *
     * @param event event type code
     */
    public void setEvent(String event) { this.event = event; }

    /**
     * Returns the human-readable activity message.
     *
     * @return the human-readable activity message
     */
    public String getMessage() { return message; }

    /**
     * Sets human-readable activity message.
     *
     * @param message human-readable activity message
     */
    public void setMessage(String message) { this.message = message; }

    /**
     * Returns the event-specific payload snapshot, or {@code null}.
     *
     * <p>The shape depends on {@link #getEvent()} and is <strong>not</strong> uniform, which is why this is
     * typed as {@link Object} rather than a map. Most events carry a JSON object: {@code signature_requested}
     * carries {@code signer_full_name}, {@code signer_email}, {@code signer_whatsapp_phone_number}, and
     * {@code notification_method}; {@code assignment_created} carries {@code user_name}, {@code user_email},
     * and {@code user_telephone}. Other events, such as {@code document_prepared}, carry a JSON array
     * instead. Inspect the value before casting — deserializing it as a map fails for the array form.</p>
     *
     * @return a {@code Map<String, Object>} or a {@code List<Object>} depending on the event, or {@code null}
     */
    public Object getPayload() { return payload; }

    /**
     * Sets event-specific payload snapshot.
     *
     * @param payload event-specific payload snapshot
     */
    public void setPayload(Object payload) { this.payload = payload; }

    /**
     * Returns request-origin metadata, or {@code null} for a server-generated event such as
     * {@code signature_requested}. When present it is a JSON object carrying the {@code ip} and
     * {@code user-agent} of the request that produced the activity.
     *
     * @return a {@code Map<String, Object>} of origin metadata, or {@code null}
     */
    public Object getOrigin() { return origin; }

    /**
     * Sets request-origin metadata.
     *
     * @param origin request-origin metadata
     */
    public void setOrigin(Object origin) { this.origin = origin; }

    /**
     * Returns ISO 8601 {@code created_at} timestamp.
     *
     * @return ISO 8601 {@code created_at} timestamp
     */
    public String getCreatedAt() { return createdAt; }

    /**
     * Sets value of {@code created_at}.
     *
     * @param createdAt value of {@code created_at}
     */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
