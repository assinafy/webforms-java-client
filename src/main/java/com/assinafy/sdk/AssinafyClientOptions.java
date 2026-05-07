package com.assinafy.sdk;

public final class AssinafyClientOptions {

    private String apiKey;
    private String token;
    private String accountId;
    private String baseUrl = "https://api.assinafy.com.br/v1";
    private int timeoutMs = 30_000;

    public String getApiKey() { return apiKey; }
    public AssinafyClientOptions setApiKey(String apiKey) { this.apiKey = apiKey; return this; }

    public String getToken() { return token; }
    public AssinafyClientOptions setToken(String token) { this.token = token; return this; }

    public String getAccountId() { return accountId; }
    public AssinafyClientOptions setAccountId(String accountId) { this.accountId = accountId; return this; }

    public String getBaseUrl() { return baseUrl; }
    public AssinafyClientOptions setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }

    public int getTimeoutMs() { return timeoutMs; }
    public AssinafyClientOptions setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; return this; }
}
