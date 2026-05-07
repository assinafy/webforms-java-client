package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentPage {

    private String id;
    private int number;
    private double height;
    private double width;

    @JsonProperty("download_url")
    private String downloadUrl;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
}
