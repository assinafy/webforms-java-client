package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class TemplatePage {

    private String id;
    private int number;
    private int height;
    private int width;

    @JsonProperty("download_url")
    private String downloadUrl;

    private List<TemplateFieldPlacement> fields;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    /** Page height in pixels. The API returns an integer. */
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    /** Page width in pixels. The API returns an integer. */
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public List<TemplateFieldPlacement> getFields() { return fields; }
    public void setFields(List<TemplateFieldPlacement> fields) { this.fields = fields; }
}
