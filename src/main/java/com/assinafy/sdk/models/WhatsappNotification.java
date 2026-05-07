package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class WhatsappNotification {

    @JsonProperty("sent_at")
    private Long sentAt;

    private String header;
    private String body;
    private List<Button> buttons;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("signer_id")
    private String signerId;

    public Long getSentAt() { return sentAt; }
    public void setSentAt(Long sentAt) { this.sentAt = sentAt; }

    public String getHeader() { return header; }
    public void setHeader(String header) { this.header = header; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public List<Button> getButtons() { return buttons; }
    public void setButtons(List<Button> buttons) { this.buttons = buttons; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getSignerId() { return signerId; }
    public void setSignerId(String signerId) { this.signerId = signerId; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Button {
        private String text;
        private String url;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}
