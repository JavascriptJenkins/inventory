package com.techvvs.inventory.model;

/**
 * Simple POJO to hold Google user information retrieved from OAuth2.
 */
public class GoogleUserInfo {
    
    private String googleId;
    private String email;
    private String name;
    private String picture;
    private String givenName;
    private String familyName;
    
    // Default constructor
    public GoogleUserInfo() {}
    
    // Constructor with parameters
    public GoogleUserInfo(String googleId, String email, String name, String picture, String givenName, String familyName) {
        this.googleId = googleId;
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.givenName = givenName;
        this.familyName = familyName;
    }
    
    // Getters and setters
    public String getGoogleId() {
        return googleId;
    }
    
    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPicture() {
        return picture;
    }
    
    public void setPicture(String picture) {
        this.picture = picture;
    }
    
    public String getGivenName() {
        return givenName;
    }
    
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }
    
    public String getFamilyName() {
        return familyName;
    }
    
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
    
    @Override
    public String toString() {
        return "GoogleUserInfo{" +
                "googleId='" + googleId + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", picture='" + picture + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                '}';
    }
}
