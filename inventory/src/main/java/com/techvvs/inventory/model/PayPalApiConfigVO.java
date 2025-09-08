package com.techvvs.inventory.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "paypal_api_config")
public class PayPalApiConfigVO {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "client_id", length = 255)
    private String clientId;
    
    @Column(name = "client_secret", length = 255)
    private String clientSecret;
    
    @Column(name = "sandbox_base_url", length = 500)
    private String sandboxBaseUrl;
    
    @Column(name = "prod_base_url", length = 500)
    private String prodBaseUrl;
    
    @Column(name = "return_url", length = 500)
    private String returnUrl;
    
    @Column(name = "cancel_url", length = 500)
    private String cancelUrl;
    
    @Column(name = "brand_name", length = 255)
    private String brandName;
    
    @Column(name = "environment", length = 20, nullable = true)
    private String environment;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public PayPalApiConfigVO() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Constructor with parameters
    public PayPalApiConfigVO(String clientId, String clientSecret, 
                           String sandboxBaseUrl, String prodBaseUrl,
                           String returnUrl, String cancelUrl, String brandName, String environment) {
        this();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.sandboxBaseUrl = sandboxBaseUrl;
        this.prodBaseUrl = prodBaseUrl;
        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
        this.brandName = brandName;
        this.environment = environment;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getReturnUrl() {
        return returnUrl;
    }
    
    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
    
    public String getCancelUrl() {
        return cancelUrl;
    }
    
    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }
    
    public String getBrandName() {
        return brandName;
    }
    
    public void setBrandName(String brandName) {
        this.brandName = brandName;
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
        return "PayPalApiConfigVO{" +
                "id=" + id +
                ", clientId='" + clientId + '\'' +
                ", sandboxBaseUrl='" + sandboxBaseUrl + '\'' +
                ", prodBaseUrl='" + prodBaseUrl + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", cancelUrl='" + cancelUrl + '\'' +
                ", brandName='" + brandName + '\'' +
                ", environment='" + environment + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

