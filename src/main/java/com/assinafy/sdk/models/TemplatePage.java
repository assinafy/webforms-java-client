package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class TemplatePage {

    private String id;
    private int number;
    private double height;
    private double width;

    @JsonProperty("download_url")
    private String downloadUrl;

    private List<TemplateFieldPlacement> fields;

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

    public List<TemplateFieldPlacement> getFields() { return fields; }
    public void setFields(List<TemplateFieldPlacement> fields) { this.fields = fields; }
}
