package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentPage {

    private String id;
    private int number;
    private int height;
    private int width;

    @JsonProperty("download_url")
    private String downloadUrl;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    /** Page height in pixels. The API returns an integer (e.g. {@code 1651}). */
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    /** Page width in pixels. The API returns an integer (e.g. {@code 1275}). */
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
}
