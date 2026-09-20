package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OpenID Connect claims about the user who authorized an OAuth token.
 *
 * <p>OIDC Core §5.3.2 requires a flat claim object, so this response is not wrapped in the
 * {@code {status,message,data}} envelope. {@code name} requires the {@code profile} scope and {@code email}
 * requires the {@code email} scope; either is {@code null} when its scope was not granted.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class OAuthUserInfo {

    private String sub;
    private String name;
    private String email;

    @JsonProperty("email_verified")
    private Boolean emailVerified;

    /** Creates an empty claim model for JSON deserialization. */
    public OAuthUserInfo() {}

    /**
     * Returns wire {@code sub}, the user's stable identifier.
     *
     * @return wire {@code sub}, the user's stable identifier
     */
    public String getSub() { return sub; }

    /**
     * Sets wire {@code sub}.
     *
     * @param sub wire {@code sub}
     */
    public void setSub(String sub) { this.sub = sub; }

    /**
     * Returns wire {@code name}, present only with the {@code profile} scope.
     *
     * @return wire {@code name}, or {@code null}
     */
    public String getName() { return name; }

    /**
     * Sets wire {@code name}.
     *
     * @param name wire {@code name}
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns wire {@code email}, present only with the {@code email} scope.
     *
     * @return wire {@code email}, or {@code null}
     */
    public String getEmail() { return email; }

    /**
     * Sets wire {@code email}.
     *
     * @param email wire {@code email}
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns wire {@code email_verified}, present only with the {@code email} scope.
     *
     * @return wire {@code email_verified}, or {@code null}
     */
    public Boolean getEmailVerified() { return emailVerified; }

    /**
     * Sets wire {@code email_verified}.
     *
     * @param emailVerified wire {@code email_verified}
     */
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
}
