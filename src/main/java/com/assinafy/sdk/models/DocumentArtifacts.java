package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentArtifacts {

    private String original;
    private String certificated;

    @JsonProperty("certificate-page")
    private String certificatePage;

    private String bundle;
    private String thumbnail;

    public String getOriginal() { return original; }
    public void setOriginal(String original) { this.original = original; }

    public String getCertificated() { return certificated; }
    public void setCertificated(String certificated) { this.certificated = certificated; }

    public String getCertificatePage() { return certificatePage; }
    public void setCertificatePage(String certificatePage) { this.certificatePage = certificatePage; }

    public String getBundle() { return bundle; }
    public void setBundle(String bundle) { this.bundle = bundle; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
}
