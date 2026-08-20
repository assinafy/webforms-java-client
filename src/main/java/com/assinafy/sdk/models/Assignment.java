package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** A request for one or more signers to sign a document. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Assignment {

    /** Creates an empty response model for deserialization. */
    public Assignment() {}

    private String id;
    private String resource;
    private String method;

    @JsonProperty("expires_at")
    @JsonAlias("expiration")
    private String expiresAt;

    @JsonProperty("sender_email")
    private String senderEmail;

    private String message;

    private List<Signer> signers;

    @JsonProperty("copy_receivers")
    private List<Signer> copyReceivers;

    private List<AssignmentItem> items;

    private AssignmentSummary summary;

    @JsonProperty("signing_urls")
    private List<SigningUrl> signingUrls;

    /**
     * Returns the assignment identifier.
     *
     * @return the assignment identifier
     */
    public String getId() { return id; }

    /**
     * Sets assignment identifier.
     *
     * @param id assignment identifier
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns the API resource marker, normally {@code assignment}.
     *
     * @return the API resource marker, normally {@code assignment}
     */
    public String getResource() { return resource; }

    /**
     * Sets API resource marker.
     *
     * @param resource API resource marker
     */
    public void setResource(String resource) { this.resource = resource; }

    /**
     * Returns the signing method, {@code virtual} or {@code collect}.
     *
     * @return the signing method, {@code virtual} or {@code collect}
     */
    public String getMethod() { return method; }

    /**
     * Sets signing method returned by the API.
     *
     * @param method signing method returned by the API
     */
    public void setMethod(String method) { this.method = method; }

    /**
     * Returns the optional ISO 8601 {@code expires_at} timestamp.
     *
     * @return the optional ISO 8601 {@code expires_at} timestamp
     */
    public String getExpiresAt() { return expiresAt; }

    /**
     * Sets value of {@code expires_at}.
     *
     * @param expiresAt value of {@code expires_at}
     */
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    /**
     * Returns the {@code sender_email} used for the assignment.
     *
     * @return the {@code sender_email} used for the assignment
     */
    public String getSenderEmail() { return senderEmail; }

    /**
     * Sets value of {@code sender_email}.
     *
     * @param senderEmail value of {@code sender_email}
     */
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    /**
     * Returns the optional message sent to signers.
     *
     * @return the optional message sent to signers
     */
    public String getMessage() { return message; }

    /**
     * Sets assignment message.
     *
     * @param message assignment message
     */
    public void setMessage(String message) { this.message = message; }

    /**
     * Returns the signers participating in the assignment.
     *
     * @return the signers participating in the assignment
     */
    public List<Signer> getSigners() { return signers; }

    /**
     * Sets assignment signers.
     *
     * @param signers assignment signers
     */
    public void setSigners(List<Signer> signers) { this.signers = signers; }

    /**
     * Returns the optional {@code copy_receivers}.
     *
     * @return the optional {@code copy_receivers}
     */
    public List<Signer> getCopyReceivers() { return copyReceivers; }

    /**
     * Sets value of {@code copy_receivers}.
     *
     * @param copyReceivers value of {@code copy_receivers}
     */
    public void setCopyReceivers(List<Signer> copyReceivers) { this.copyReceivers = copyReceivers; }

    /**
     * Returns the collect-assignment items, possibly empty for virtual assignments.
     *
     * @return the collect-assignment items, possibly empty for virtual assignments
     */
    public List<AssignmentItem> getItems() { return items; }

    /**
     * Sets assignment items.
     *
     * @param items assignment items
     */
    public void setItems(List<AssignmentItem> items) { this.items = items; }

    /**
     * Returns signer completion totals and signer summaries.
     *
     * @return signer completion totals and signer summaries
     */
    public AssignmentSummary getSummary() { return summary; }

    /**
     * Sets assignment summary.
     *
     * @param summary assignment summary
     */
    public void setSummary(AssignmentSummary summary) { this.summary = summary; }

    /**
     * Returns per-signer URLs when the endpoint requested {@code return_signing_urls}.
     *
     * @return per-signer URLs when the endpoint requested {@code return_signing_urls}
     */
    public List<SigningUrl> getSigningUrls() { return signingUrls; }

    /**
     * Sets value of {@code signing_urls}.
     *
     * @param signingUrls value of {@code signing_urls}
     */
    public void setSigningUrls(List<SigningUrl> signingUrls) { this.signingUrls = signingUrls; }
}
