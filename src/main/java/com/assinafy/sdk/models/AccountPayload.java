package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Request fields shared by account creation and update. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class AccountPayload {

    private String name;

    @JsonProperty("notification_sender_type")
    private String notificationSenderType;

    /** Creates an empty payload for a partial account update. */
    public AccountPayload() {}

    /**
     * Creates a payload with the required account name.
     *
     * @param name account display name
     */
    public AccountPayload(String name) {
        this.name = name;
    }

    /**
     * Returns the account display name.
     *
     * @return the account display name
     */
    public String getName() { return name; }

    /**
     * Sets account display name.
     *
     * @param name account display name
     * @return this payload
     */
    public AccountPayload setName(String name) { this.name = name; return this; }

    /**
     * Returns the {@code notification_sender_type}, or {@code null} when omitted.
     *
     * @return the {@code notification_sender_type}, or {@code null} when omitted
     */
    public String getNotificationSenderType() { return notificationSenderType; }

    /**
     * Sets who signers see as the notification sender.
     *
     * @param notificationSenderType {@code User} or {@code Account}
     * @return this payload
     */
    public AccountPayload setNotificationSenderType(String notificationSenderType) {
        this.notificationSenderType = notificationSenderType;
        return this;
    }
}
