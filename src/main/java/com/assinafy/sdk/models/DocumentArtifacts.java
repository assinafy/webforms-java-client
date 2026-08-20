package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Download URLs returned with a document; absent artifacts remain {@code null}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentArtifacts {

    /** Creates an empty response model for deserialization. */
    public DocumentArtifacts() {}

    private String original;
    private String certificated;

    @JsonProperty("certificate-page")
    private String certificatePage;

    private String bundle;
    private String thumbnail;
    private String pades;

    /**
     * Returns URL for the uploaded original, or {@code null}.
     *
     * @return URL for the uploaded original, or {@code null}
     */
    public String getOriginal() { return original; }

    /**
     * Sets original-artifact URL.
     *
     * @param original original-artifact URL
     */
    public void setOriginal(String original) { this.original = original; }

    /**
     * Returns URL for the certificated document, or {@code null}.
     *
     * @return URL for the certificated document, or {@code null}
     */
    public String getCertificated() { return certificated; }

    /**
     * Sets certificated-artifact URL.
     *
     * @param certificated certificated-artifact URL
     */
    public void setCertificated(String certificated) { this.certificated = certificated; }

    /**
     * Returns URL stored under {@code certificate-page}, or {@code null}.
     *
     * @return URL stored under {@code certificate-page}, or {@code null}
     */
    public String getCertificatePage() { return certificatePage; }

    /**
     * Sets value of the {@code certificate-page} property.
     *
     * @param certificatePage value of the {@code certificate-page} property
     */
    public void setCertificatePage(String certificatePage) { this.certificatePage = certificatePage; }

    /**
     * Returns URL for the downloadable artifact bundle, or {@code null}.
     *
     * @return URL for the downloadable artifact bundle, or {@code null}
     */
    public String getBundle() { return bundle; }

    /**
     * Sets bundle-artifact URL.
     *
     * @param bundle bundle-artifact URL
     */
    public void setBundle(String bundle) { this.bundle = bundle; }

    /**
     * Returns URL for the document thumbnail, or {@code null}.
     *
     * @return URL for the document thumbnail, or {@code null}
     */
    public String getThumbnail() { return thumbnail; }

    /**
     * Sets thumbnail-artifact URL.
     *
     * @param thumbnail thumbnail-artifact URL
     */
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    /**
     * Returns PAdES artifact URL when generated, or {@code null}.
     *
     * @return PAdES artifact URL when generated, or {@code null}
     */
    public String getPades() { return pades; }

    /**
     * Sets PAdES artifact URL.
     *
     * @param pades PAdES artifact URL
     */
    public void setPades(String pades) { this.pades = pades; }
}
