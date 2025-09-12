package com.techvvs.inventory.model;

/**
 * Simple POJO to handle OAuth2 callback parameters from Google.
 */
public class OAuth2CallbackRequest {
    
    private String state;
    private String code;
    private String scope;
    private String authuser;
    private String prompt;
    
    // Default constructor
    public OAuth2CallbackRequest() {}
    
    // Constructor with parameters
    public OAuth2CallbackRequest(String state, String code, String scope, String authuser, String prompt) {
        this.state = state;
        this.code = code;
        this.scope = scope;
        this.authuser = authuser;
        this.prompt = prompt;
    }
    
    // Getters and setters
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public String getAuthuser() {
        return authuser;
    }
    
    public void setAuthuser(String authuser) {
        this.authuser = authuser;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    
    @Override
    public String toString() {
        return "OAuth2CallbackRequest{" +
                "state='" + state + '\'' +
                ", code='" + code + '\'' +
                ", scope='" + scope + '\'' +
                ", authuser='" + authuser + '\'' +
                ", prompt='" + prompt + '\'' +
                '}';
    }
}
