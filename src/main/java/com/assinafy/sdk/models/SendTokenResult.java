package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Result of {@code PUT /public/documents/{documentId}/send-token}:
 * {@code {"document": {"resource", "id", "name", "page_count", "created_by"}, "channel": "email",
 * "recipient": "signer@example.com"}}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SendTokenResult {

    private DocumentDetails document;
    private String channel;
    private String recipient;

    /** Creates an empty result for JSON deserialization. */
    public SendTokenResult() {}

    /**
     * Returns the public document summary: {@code resource}, {@code id}, {@code name}, {@code page_count},
     * and {@code created_by}.
     *
     * @return public document summary
     */
    public DocumentDetails getDocument() { return document; }
    /**
     * Sets the public document summary.
     *
     * @param document public document summary
     */
    public void setDocument(DocumentDetails document) { this.document = document; }

    /**
     * Returns the channel the access token was sent through; always {@code email}.
     *
     * @return delivery channel
     */
    public String getChannel() { return channel; }
    /**
     * Sets the delivery channel.
     *
     * @param channel delivery channel
     */
    public void setChannel(String channel) { this.channel = channel; }

    /**
     * Returns the signer email address the access token was sent to.
     *
     * @return recipient email address
     */
    public String getRecipient() { return recipient; }
    /**
     * Sets the recipient email address.
     *
     * @param recipient recipient email address
     */
    public void setRecipient(String recipient) { this.recipient = recipient; }
}
