package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Branding returned by {@code GET /accounts/{accountId}/theme}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class AccountTheme {

    /** Creates an empty response model for deserialization. */
    public AccountTheme() {}

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("primary_color")
    private String primaryColor;

    @JsonProperty("secondary_color")
    private String secondaryColor;

    private String logo;

    /**
     * Returns the {@code account_name} shown in the branded interface.
     *
     * @return the {@code account_name} shown in the branded interface
     */
    public String getAccountName() { return accountName; }

    /**
     * Sets value of {@code account_name}.
     *
     * @param accountName value of {@code account_name}
     */
    public void setAccountName(String accountName) { this.accountName = accountName; }

    /**
     * Returns the primary hex color without a leading {@code #}.
     *
     * @return the primary hex color without a leading {@code #}
     */
    public String getPrimaryColor() { return primaryColor; }

    /**
     * Sets value of {@code primary_color}.
     *
     * @param primaryColor value of {@code primary_color}
     */
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }

    /**
     * Returns the optional secondary hex color without a leading {@code #}.
     *
     * @return the optional secondary hex color without a leading {@code #}
     */
    public String getSecondaryColor() { return secondaryColor; }

    /**
     * Sets value of {@code secondary_color}.
     *
     * @param secondaryColor value of {@code secondary_color}
     */
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }

    /**
     * Returns the account logo URL.
     *
     * @return the account logo URL
     */
    public String getLogo() { return logo; }

    /**
     * Sets account logo URL.
     *
     * @param logo account logo URL
     */
    public void setLogo(String logo) { this.logo = logo; }
}
