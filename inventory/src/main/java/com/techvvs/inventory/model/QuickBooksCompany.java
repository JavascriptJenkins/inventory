package com.techvvs.inventory.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quickbooks_companies")
public class QuickBooksCompany {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "company_id", length = 100, nullable = false)
    private String companyId; // Realm ID
    
    @Column(name = "company_name", length = 255)
    private String companyName;
    
    @Column(name = "environment", length = 20, nullable = false)
    private String environment; // SANDBOX or PROD
    
    @Column(name = "is_active")
    private Boolean isActive = false;
    
    @Column(name = "is_sandbox_created")
    private Boolean isSandboxCreated = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public QuickBooksCompany() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Constructor with parameters
    public QuickBooksCompany(String companyId, String companyName, String environment, Boolean isSandboxCreated) {
        this();
        this.companyId = companyId;
        this.companyName = companyName;
        this.environment = environment;
        this.isSandboxCreated = isSandboxCreated;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIsSandboxCreated() {
        return isSandboxCreated;
    }
    
    public void setIsSandboxCreated(Boolean isSandboxCreated) {
        this.isSandboxCreated = isSandboxCreated;
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
        return "QuickBooksCompany{" +
                "id=" + id +
                ", companyId='" + companyId + '\'' +
                ", companyName='" + companyName + '\'' +
                ", environment='" + environment + '\'' +
                ", isActive=" + isActive +
                ", isSandboxCreated=" + isSandboxCreated +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
