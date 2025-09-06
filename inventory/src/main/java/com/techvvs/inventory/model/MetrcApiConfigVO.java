package com.techvvs.inventory.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "metrc_api_config")
public class MetrcApiConfigVO {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "api_key_username", length = 255)
    private String apiKeyUsername;
    
    @Column(name = "api_key_password", length = 255)
    private String apiKeyPassword;
    
    @Column(name = "test_api_key_base_uri", length = 500)
    private String testApiKeyBaseUri;
    
    @Column(name = "prod_api_key_base_uri", length = 500)
    private String prodApiKeyBaseUri;
    
    @Column(name = "environment", length = 20, nullable = true)
    private String environment;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public MetrcApiConfigVO() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Constructor with parameters
    public MetrcApiConfigVO(String apiKeyUsername, String apiKeyPassword, 
                           String testApiKeyBaseUri, String prodApiKeyBaseUri, String environment) {
        this();
        this.apiKeyUsername = apiKeyUsername;
        this.apiKeyPassword = apiKeyPassword;
        this.testApiKeyBaseUri = testApiKeyBaseUri;
        this.prodApiKeyBaseUri = prodApiKeyBaseUri;
        this.environment = environment;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getApiKeyUsername() {
        return apiKeyUsername;
    }
    
    public void setApiKeyUsername(String apiKeyUsername) {
        this.apiKeyUsername = apiKeyUsername;
    }
    
    public String getApiKeyPassword() {
        return apiKeyPassword;
    }
    
    public void setApiKeyPassword(String apiKeyPassword) {
        this.apiKeyPassword = apiKeyPassword;
    }
    
    public String getTestApiKeyBaseUri() {
        return testApiKeyBaseUri;
    }
    
    public void setTestApiKeyBaseUri(String testApiKeyBaseUri) {
        this.testApiKeyBaseUri = testApiKeyBaseUri;
    }
    
    public String getProdApiKeyBaseUri() {
        return prodApiKeyBaseUri;
    }
    
    public void setProdApiKeyBaseUri(String prodApiKeyBaseUri) {
        this.prodApiKeyBaseUri = prodApiKeyBaseUri;
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
        return "MetrcApiConfigVO{" +
                "id=" + id +
                ", apiKeyUsername='" + apiKeyUsername + '\'' +
                ", testApiKeyBaseUri='" + testApiKeyBaseUri + '\'' +
                ", prodApiKeyBaseUri='" + prodApiKeyBaseUri + '\'' +
                ", environment='" + environment + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
