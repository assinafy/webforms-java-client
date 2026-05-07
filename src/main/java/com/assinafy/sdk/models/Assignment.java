package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Assignment {

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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<Signer> getSigners() { return signers; }
    public void setSigners(List<Signer> signers) { this.signers = signers; }

    public List<Signer> getCopyReceivers() { return copyReceivers; }
    public void setCopyReceivers(List<Signer> copyReceivers) { this.copyReceivers = copyReceivers; }

    public List<AssignmentItem> getItems() { return items; }
    public void setItems(List<AssignmentItem> items) { this.items = items; }

    public AssignmentSummary getSummary() { return summary; }
    public void setSummary(AssignmentSummary summary) { this.summary = summary; }

    public List<SigningUrl> getSigningUrls() { return signingUrls; }
    public void setSigningUrls(List<SigningUrl> signingUrls) { this.signingUrls = signingUrls; }
}
