package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


/** Personal push switches, separate from email preferences; null fields are omitted. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PushPreferences {

    @JsonProperty("SignerViewed")
    private Boolean signerViewed;

    @JsonProperty("SignerSigned")
    private Boolean signerSigned;

    @JsonProperty("DocumentCompleted")
    private Boolean documentCompleted;

    @JsonProperty("SignerDeclined")
    private Boolean signerDeclined;

    @JsonProperty("DocumentAboutToExpire")
    private Boolean documentAboutToExpire;

    @JsonProperty("DocumentExpired")
    private Boolean documentExpired;

    @JsonProperty("SignerIdentityReviewRequired")
    private Boolean signerIdentityReviewRequired;

    @JsonProperty("SignerWhatsappFailed")
    private Boolean signerWhatsappFailed;

    /** Creates an empty model. */
    public PushPreferences() {}

    /**
     * Returns SignerViewed push switch.
     * @return SignerViewed push switch
     */
    public Boolean getSignerViewed() { return signerViewed; }

    /**
     * Sets SignerViewed push switch.
     * @param value SignerViewed push switch
     * @return this model
     */
    public PushPreferences setSignerViewed(Boolean value) { signerViewed = value; return this; }

    /**
     * Returns SignerSigned push switch.
     * @return SignerSigned push switch
     */
    public Boolean getSignerSigned() { return signerSigned; }

    /**
     * Sets SignerSigned push switch.
     * @param value SignerSigned push switch
     * @return this model
     */
    public PushPreferences setSignerSigned(Boolean value) { signerSigned = value; return this; }

    /**
     * Returns DocumentCompleted push switch.
     * @return DocumentCompleted push switch
     */
    public Boolean getDocumentCompleted() { return documentCompleted; }

    /**
     * Sets DocumentCompleted push switch.
     * @param value DocumentCompleted push switch
     * @return this model
     */
    public PushPreferences setDocumentCompleted(Boolean value) { documentCompleted = value; return this; }

    /**
     * Returns SignerDeclined push switch.
     * @return SignerDeclined push switch
     */
    public Boolean getSignerDeclined() { return signerDeclined; }

    /**
     * Sets SignerDeclined push switch.
     * @param value SignerDeclined push switch
     * @return this model
     */
    public PushPreferences setSignerDeclined(Boolean value) { signerDeclined = value; return this; }

    /**
     * Returns DocumentAboutToExpire push switch.
     * @return DocumentAboutToExpire push switch
     */
    public Boolean getDocumentAboutToExpire() { return documentAboutToExpire; }

    /**
     * Sets DocumentAboutToExpire push switch.
     * @param value DocumentAboutToExpire push switch
     * @return this model
     */
    public PushPreferences setDocumentAboutToExpire(Boolean value) { documentAboutToExpire = value; return this; }

    /**
     * Returns DocumentExpired push switch.
     * @return DocumentExpired push switch
     */
    public Boolean getDocumentExpired() { return documentExpired; }

    /**
     * Sets DocumentExpired push switch.
     * @param value DocumentExpired push switch
     * @return this model
     */
    public PushPreferences setDocumentExpired(Boolean value) { documentExpired = value; return this; }

    /**
     * Returns SignerIdentityReviewRequired push switch.
     * @return SignerIdentityReviewRequired push switch
     */
    public Boolean getSignerIdentityReviewRequired() { return signerIdentityReviewRequired; }

    /**
     * Sets SignerIdentityReviewRequired push switch.
     * @param value SignerIdentityReviewRequired push switch
     * @return this model
     */
    public PushPreferences setSignerIdentityReviewRequired(Boolean value) { signerIdentityReviewRequired = value; return this; }

    /**
     * Returns SignerWhatsappFailed push switch.
     * @return SignerWhatsappFailed push switch
     */
    public Boolean getSignerWhatsappFailed() { return signerWhatsappFailed; }

    /**
     * Sets SignerWhatsappFailed push switch.
     * @param value SignerWhatsappFailed push switch
     * @return this model
     */
    public PushPreferences setSignerWhatsappFailed(Boolean value) { signerWhatsappFailed = value; return this; }
}
