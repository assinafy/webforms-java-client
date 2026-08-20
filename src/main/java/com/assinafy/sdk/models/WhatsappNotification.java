package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** WhatsApp delivery record returned by signer notification endpoints. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class WhatsappNotification {

    @JsonProperty("sent_at")
    private Long sentAt;

    private String header;
    private String body;
    private List<Button> buttons;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("signer_id")
    private String signerId;

    /** Creates an empty WhatsApp notification model for JSON deserialization. */
    public WhatsappNotification() {}

    /**
     * Returns wire {@code sent_at} timestamp.
     *
     * @return wire {@code sent_at} timestamp
     */
    public Long getSentAt() { return sentAt; }
    /**
     * Sets wire {@code sent_at} timestamp.
     *
     * @param sentAt wire {@code sent_at} timestamp
     */
    public void setSentAt(Long sentAt) { this.sentAt = sentAt; }

    /**
     * Returns rendered notification header.
     *
     * @return rendered notification header
     */
    public String getHeader() { return header; }
    /**
     * Sets rendered notification header.
     *
     * @param header rendered notification header
     */
    public void setHeader(String header) { this.header = header; }

    /**
     * Returns rendered notification body.
     *
     * @return rendered notification body
     */
    public String getBody() { return body; }
    /**
     * Sets rendered notification body.
     *
     * @param body rendered notification body
     */
    public void setBody(String body) { this.body = body; }

    /**
     * Returns rendered action buttons.
     *
     * @return rendered action buttons
     */
    public List<Button> getButtons() { return buttons; }
    /**
     * Sets rendered action buttons.
     *
     * @param buttons rendered action buttons
     */
    public void setButtons(List<Button> buttons) { this.buttons = buttons; }

    /**
     * Returns wire {@code phone_number} destination.
     *
     * @return wire {@code phone_number} destination
     */
    public String getPhoneNumber() { return phoneNumber; }
    /**
     * Sets wire {@code phone_number} destination.
     *
     * @param phoneNumber wire {@code phone_number} destination
     */
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    /**
     * Returns wire {@code signer_id}.
     *
     * @return wire {@code signer_id}
     */
    public String getSignerId() { return signerId; }
    /**
     * Sets wire {@code signer_id}.
     *
     * @param signerId wire {@code signer_id}
     */
    public void setSignerId(String signerId) { this.signerId = signerId; }

    /** Rendered action button contained in a WhatsApp notification. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Button {
        private String text;
        private String url;

        /** Creates an empty button model for JSON deserialization. */
        public Button() {}

        /**
         * Returns button text.
         *
         * @return button text
         */
        public String getText() { return text; }
        /**
         * Sets button text.
         *
         * @param text button text
         */
        public void setText(String text) { this.text = text; }

        /**
         * Returns button target URL.
         *
         * @return button target URL
         */
        public String getUrl() { return url; }
        /**
         * Sets button target URL.
         *
         * @param url button target URL
         */
        public void setUrl(String url) { this.url = url; }
    }
}
