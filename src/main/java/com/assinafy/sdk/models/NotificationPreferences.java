package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The nine configurable owner-facing email notification switches. Null fields are omitted on update. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class NotificationPreferences {

    @JsonProperty("DocumentCompleted")
    private Boolean documentCompleted;
    @JsonProperty("SignerDeclined")
    private Boolean signerDeclined;
    @JsonProperty("DocumentCancelled")
    private Boolean documentCancelled;
    @JsonProperty("DocumentAboutToExpire")
    private Boolean documentAboutToExpire;
    @JsonProperty("DocumentExpired")
    private Boolean documentExpired;
    @JsonProperty("DocumentExpirationReset")
    private Boolean documentExpirationReset;
    @JsonProperty("DocumentProcessingFailed")
    private Boolean documentProcessingFailed;
    @JsonProperty("TemplateProcessingFailed")
    private Boolean templateProcessingFailed;
    @JsonProperty("SignerWhatsappFailed")
    private Boolean signerWhatsappFailed;

    /** Creates an empty preference set for either deserialization or a partial update. */
    public NotificationPreferences() {}

    /**
     * Returns wire {@code DocumentCompleted}, or {@code null} when unset.
     *
     * @return wire {@code DocumentCompleted}, or {@code null} when unset
     */
    public Boolean getDocumentCompleted() { return documentCompleted; }
    /**
     * Sets wire {@code DocumentCompleted}; {@code null} omits it.
     *
     * @param value wire {@code DocumentCompleted}; {@code null} omits it
     * @return this payload
     */
    public NotificationPreferences setDocumentCompleted(Boolean value) { documentCompleted = value; return this; }

    /**
     * Returns wire {@code SignerDeclined}, or {@code null} when unset.
     *
     * @return wire {@code SignerDeclined}, or {@code null} when unset
     */
    public Boolean getSignerDeclined() { return signerDeclined; }
    /**
     * Sets wire {@code SignerDeclined}; {@code null} omits it.
     *
     * @param value wire {@code SignerDeclined}; {@code null} omits it
     * @return this payload
     */
    public NotificationPreferences setSignerDeclined(Boolean value) { signerDeclined = value; return this; }

    /**
     * Returns wire {@code DocumentCancelled}, or {@code null} when unset.
     *
     * @return wire {@code DocumentCancelled}, or {@code null} when unset
     */
    public Boolean getDocumentCancelled() { return documentCancelled; }
    /**
     * Sets wire {@code DocumentCancelled}; {@code null} omits it.
     *
     * @param value wire {@code DocumentCancelled}; {@code null} omits it
     * @return this payload
     */
    public NotificationPreferences setDocumentCancelled(Boolean value) { documentCancelled = value; return this; }

    /**
     * Returns wire {@code DocumentAboutToExpire}, or {@code null} when unset.
     *
     * @return wire {@code DocumentAboutToExpire}, or {@code null} when unset
     */
    public Boolean getDocumentAboutToExpire() { return documentAboutToExpire; }
    /**
     * Sets wire {@code DocumentAboutToExpire}; {@code null} omits it.
     *
     * @param value wire {@code DocumentAboutToExpire}; {@code null} omits it
     * @return this payload
     */
    public NotificationPreferences setDocumentAboutToExpire(Boolean value) {
        documentAboutToExpire = value; return this;
    }

    /**
     * Returns wire {@code DocumentExpired}, or {@code null} when unset.
     *
     * @return wire {@code DocumentExpired}, or {@code null} when unset
     */
    public Boolean getDocumentExpired() { return documentExpired; }
    /**
     * Sets wire {@code DocumentExpired}; {@code null} omits it.
     *
     * @param value wire {@code DocumentExpired}; {@code null} omits it
     * @return this payload
     */
    public NotificationPreferences setDocumentExpired(Boolean value) { documentExpired = value; return this; }

    /**
     * Returns wire {@code DocumentExpirationReset}, or {@code null} when unset.
     *
     * @return wire {@code DocumentExpirationReset}, or {@code null} when unset
     */
    public Boolean getDocumentExpirationReset() { return documentExpirationReset; }
    /**
     * Sets wire {@code DocumentExpirationReset}; {@code null} omits it.
     *
     * @param value wire {@code DocumentExpirationReset}; {@code null} omits it
     * @return this payload
     */
    public NotificationPreferences setDocumentExpirationReset(Boolean value) {
        documentExpirationReset = value; return this;
    }

    /**
     * Returns wire {@code DocumentProcessingFailed}, or {@code null} when unset.
     *
     * @return wire {@code DocumentProcessingFailed}, or {@code null} when unset
     */
    public Boolean getDocumentProcessingFailed() { return documentProcessingFailed; }
    /**
     * Sets wire {@code DocumentProcessingFailed}; {@code null} omits it.
     *
     * @param value wire {@code DocumentProcessingFailed}; {@code null} omits it
     * @return this payload
     */
    public NotificationPreferences setDocumentProcessingFailed(Boolean value) {
        documentProcessingFailed = value; return this;
    }

    /**
     * Returns wire {@code TemplateProcessingFailed}, or {@code null} when unset.
     *
     * @return wire {@code TemplateProcessingFailed}, or {@code null} when unset
     */
    public Boolean getTemplateProcessingFailed() { return templateProcessingFailed; }
    /**
     * Sets wire {@code TemplateProcessingFailed}; {@code null} omits it.
     *
     * @param value wire {@code TemplateProcessingFailed}; {@code null} omits it
     * @return this payload
     */
    public NotificationPreferences setTemplateProcessingFailed(Boolean value) {
        templateProcessingFailed = value; return this;
    }

    /**
     * Returns wire {@code SignerWhatsappFailed}, or {@code null} when unset.
     *
     * @return wire {@code SignerWhatsappFailed}, or {@code null} when unset
     */
    public Boolean getSignerWhatsappFailed() { return signerWhatsappFailed; }
    /**
     * Sets wire {@code SignerWhatsappFailed}; {@code null} omits it.
     *
     * @param value wire {@code SignerWhatsappFailed}; {@code null} omits it
     * @return this payload
     */
    public NotificationPreferences setSignerWhatsappFailed(Boolean value) {
        signerWhatsappFailed = value; return this;
    }
}
