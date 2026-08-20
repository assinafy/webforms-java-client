package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Page metadata and field placements belonging to a template. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TemplatePage {

    private String id;
    private int number;
    private int height;
    private int width;

    @JsonProperty("download_url")
    private String downloadUrl;

    private List<TemplateFieldPlacement> fields;

    /** Creates an empty template-page model for JSON deserialization. */
    public TemplatePage() {}

    /**
     * Returns page identifier.
     *
     * @return page identifier
     */
    public String getId() { return id; }
    /**
     * Sets page identifier.
     *
     * @param id page identifier
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns one-based page number.
     *
     * @return one-based page number
     */
    public int getNumber() { return number; }
    /**
     * Sets one-based page number.
     *
     * @param number one-based page number
     */
    public void setNumber(int number) { this.number = number; }

    /**
     * Returns page height in pixels.
     *
     * @return page height in pixels
     */
    public int getHeight() { return height; }
    /**
     * Sets page height in pixels.
     *
     * @param height page height in pixels
     */
    public void setHeight(int height) { this.height = height; }

    /**
     * Returns page width in pixels.
     *
     * @return page width in pixels
     */
    public int getWidth() { return width; }
    /**
     * Sets page width in pixels.
     *
     * @param width page width in pixels
     */
    public void setWidth(int width) { this.width = width; }

    /**
     * Returns wire {@code download_url} for the rendered page.
     *
     * @return wire {@code download_url} for the rendered page
     */
    public String getDownloadUrl() { return downloadUrl; }
    /**
     * Sets wire {@code download_url}.
     *
     * @param downloadUrl wire {@code download_url}
     */
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    /**
     * Returns fields placed on this page.
     *
     * @return fields placed on this page
     */
    public List<TemplateFieldPlacement> getFields() { return fields; }
    /**
     * Sets fields placed on this page.
     *
     * @param fields fields placed on this page
     */
    public void setFields(List<TemplateFieldPlacement> fields) { this.fields = fields; }
}
