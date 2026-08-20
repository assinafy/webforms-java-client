package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Metadata for one rendered document page. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentPage {

    /** Creates an empty response model for deserialization. */
    public DocumentPage() {}

    private String id;
    private int number;
    private int height;
    private int width;

    @JsonProperty("download_url")
    private String downloadUrl;

    /**
     * Returns the page identifier.
     *
     * @return the page identifier
     */
    public String getId() { return id; }

    /**
     * Sets page identifier.
     *
     * @param id page identifier
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns the one-based page number.
     *
     * @return the one-based page number
     */
    public int getNumber() { return number; }

    /**
     * Sets one-based page number.
     *
     * @param number one-based page number
     */
    public void setNumber(int number) { this.number = number; }

    /**
     * Returns page height in 150-DPI image pixels.
     *
     * @return page height in 150-DPI image pixels
     */
    public int getHeight() { return height; }

    /**
     * Sets page height in pixels.
     *
     * @param height page height in pixels
     */
    public void setHeight(int height) { this.height = height; }

    /**
     * Returns page width in 150-DPI image pixels.
     *
     * @return page width in 150-DPI image pixels
     */
    public int getWidth() { return width; }

    /**
     * Sets page width in pixels.
     *
     * @param width page width in pixels
     */
    public void setWidth(int width) { this.width = width; }

    /**
     * Returns the {@code download_url} for the rendered page image.
     *
     * @return the {@code download_url} for the rendered page image
     */
    public String getDownloadUrl() { return downloadUrl; }

    /**
     * Sets value of {@code download_url}.
     *
     * @param downloadUrl value of {@code download_url}
     */
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
}
