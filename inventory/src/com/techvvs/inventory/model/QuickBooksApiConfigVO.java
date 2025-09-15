package com.techvvs.inventory.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quickbooks_api_config")
public class QuickBooksApiConfigVO {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "app_id", length = 255)
    private String appId;
    
    @Column(name = "client_id", length = 255)
    private String clientId;
    
    @Column(name = "client_secret", length = 255)
    private String clientSecret;
    
    @Column(name = "sandbox_base_url", length = 500)
    private String sandboxBaseUrl;
    
    @Column(name = "prod_base_url", length = 500)
    private String prodBaseUrl;
    
    @Column(name = "redirect_uri", length = 500)
    private String redirectUri;
    
    @Column(name = "scope", length = 500)
    private String scope;
    
    @Column(name = "access_token", length = 1000)
    private String accessToken;
    
    @Column(name = "refresh_token", length = 1000)
    private String refreshToken;
    
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;
    
    @Column(name = "environment", length = 20, nullable = true)
    private String environment;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public QuickBooksApiConfigVO() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Constructor with parameters
    public QuickBooksApiConfigVO(String appId, String clientId, String clientSecret, 
                               String sandboxBaseUrl, String prodBaseUrl,
                               String redirectUri, String scope, 
                               String accessToken, String refreshToken, 
                               LocalDateTime tokenExpiresAt, String environment) {
        this();
        this.appId = appId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.sandboxBaseUrl = sandboxBaseUrl;
        this.prodBaseUrl = prodBaseUrl;
        this.redirectUri = redirectUri;
        this.scope = scope;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = tokenExpiresAt;
        this.environment = environment;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getAppId() {
        return appId;
    }
    
    public void setAppId(String appId) {
        this.appId = appId;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getClientSecret() {
        return clientSecret;
    }
    
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    
    public String getSandboxBaseUrl() {
        return sandboxBaseUrl;
    }
    
    public void setSandboxBaseUrl(String sandboxBaseUrl) {
        this.sandboxBaseUrl = sandboxBaseUrl;
    }
    
    public String getProdBaseUrl() {
        return prodBaseUrl;
    }
    
    public void setProdBaseUrl(String prodBaseUrl) {
        this.prodBaseUrl = prodBaseUrl;
    }
    
    public String getRedirectUri() {
        return redirectUri;
    }
    
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }
    
    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }
    
    public String getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Pre-persist and pre-update hooks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "QuickBooksApiConfigVO{" +
                "id=" + id +
                ", appId='" + appId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", sandboxBaseUrl='" + sandboxBaseUrl + '\'' +
                ", prodBaseUrl='" + prodBaseUrl + '\'' +
                ", redirectUri='" + redirectUri + '\'' +
                ", scope='" + scope + '\'' +
                ", environment='" + environment + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
