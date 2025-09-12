package com.techvvs.inventory.service.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GoogleIdTokenPayload {

    private String iss;
    private String azp;
    private String aud;
    private String sub;
    private String email;

    @JsonProperty("email_verified")
    private boolean emailVerified;

    @JsonProperty("at_hash")
    private String atHash;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email_verified")
    private String picture;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;

    private long iat;
    private long exp;

    // Getters and setters
    public String getIss() { return iss; }
    public void setIss(String iss) { this.iss = iss; }

    public String getAzp() { return azp; }
    public void setAzp(String azp) { this.azp = azp; }

    public String getAud() { return aud; }
    public void setAud(String aud) { this.aud = aud; }

    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getAtHash() { return atHash; }
    public void setAtHash(String atHash) { this.atHash = atHash; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }

    public String getGivenName() { return givenName; }
    public void setGivenName(String givenName) { this.givenName = givenName; }

    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }

    public long getIat() { return iat; }
    public void setIat(long iat) { this.iat = iat; }

    public long getExp() { return exp; }
    public void setExp(long exp) { this.exp = exp; }
}
