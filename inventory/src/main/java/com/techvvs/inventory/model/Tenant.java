package com.techvvs.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    @JsonProperty
    private UUID id;

    @Column(name = "tenant_name", unique = true, length = 50)
    @JsonProperty
    private String tenantName;

    @Column(name = "domain_name", length = 100)
    @JsonProperty
    private String domainName;

    @Column(name = "subscription_tier", length = 20)
    @JsonProperty
    private String subscriptionTier;

    @Column(name = "status", length = 20)
    @JsonProperty
    private String status; // active, pending, suspended

    @Column(name = "created_at")
    @JsonProperty
    private LocalDateTime createdAt;

    @Column(name = "billing_email", length = 255)
    @JsonProperty
    private String billingEmail;

    @Column(name = "last_deployed")
    @JsonProperty
    private LocalDateTime lastDeployed;

    @Column(name = "deploy_flag")
    @JsonProperty
    private Integer deployflag;

    // One-to-many relationship with SystemUserDAO
    @OneToMany(mappedBy = "tenantEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonProperty
    private List<SystemUserDAO> systemUsers = new ArrayList<>();

    // One-to-many relationship with TenantConfig
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonProperty
    private List<TenantConfig> tenantConfigs = new ArrayList<>();

    // Generic timestamp fields
    @Column(name = "updateTimeStamp")
    @JsonProperty
    private LocalDateTime updateTimeStamp;

    @Column(name = "createTimeStamp")
    @JsonProperty
    private LocalDateTime createTimeStamp;

    // Constructors
    public Tenant() {
        // Don't set ID in constructor - let JPA handle it
        this.createdAt = LocalDateTime.now();
        this.createTimeStamp = LocalDateTime.now();
        this.updateTimeStamp = LocalDateTime.now();
        this.deployflag = 0; // Default deploy flag to 0 (not deployed)
        // lastDeployed remains null by default
    }

    public Tenant(String tenantName, String domainName, String subscriptionTier, String billingEmail) {
        this();
        this.tenantName = tenantName;
        this.domainName = domainName;
        this.subscriptionTier = subscriptionTier;
        this.billingEmail = billingEmail;
        this.status = "pending"; // Default status
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getSubscriptionTier() {
        return subscriptionTier;
    }

    public void setSubscriptionTier(String subscriptionTier) {
        this.subscriptionTier = subscriptionTier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getBillingEmail() {
        return billingEmail;
    }

    public void setBillingEmail(String billingEmail) {
        this.billingEmail = billingEmail;
    }

    public LocalDateTime getLastDeployed() {
        return lastDeployed;
    }

    public void setLastDeployed(LocalDateTime lastDeployed) {
        this.lastDeployed = lastDeployed;
    }

    public Integer getDeployflag() {
        return deployflag;
    }

    public void setDeployflag(Integer deployflag) {
        this.deployflag = deployflag;
    }

    public List<SystemUserDAO> getSystemUsers() {
        return systemUsers;
    }

    public void setSystemUsers(List<SystemUserDAO> systemUsers) {
        this.systemUsers = systemUsers;
    }

    public List<TenantConfig> getTenantConfigs() {
        return tenantConfigs;
    }

    public void setTenantConfigs(List<TenantConfig> tenantConfigs) {
        this.tenantConfigs = tenantConfigs;
    }

    public LocalDateTime getUpdateTimeStamp() {
        return updateTimeStamp;
    }

    public void setUpdateTimeStamp(LocalDateTime updateTimeStamp) {
        this.updateTimeStamp = updateTimeStamp;
    }

    public LocalDateTime getCreateTimeStamp() {
        return createTimeStamp;
    }

    public void setCreateTimeStamp(LocalDateTime createTimeStamp) {
        this.createTimeStamp = createTimeStamp;
    }

    // Helper methods
//    public void addSystemUser(SystemUserDAO systemUser) {
//        if (systemUsers == null) {
//            systemUsers = new ArrayList<>();
//        }
//        systemUsers.add(systemUser);
//        systemUser.setTenant(this);
//    }
//
//    public void removeSystemUser(SystemUserDAO systemUser) {
//        if (systemUsers != null) {
//            systemUsers.remove(systemUser);
//            systemUser.setTenant(null);
//        }
//    }

    public void addSystemUser(SystemUserDAO systemUser) {
        if (systemUsers == null) {
            systemUsers = new ArrayList<>();
        }
        systemUsers.add(systemUser);
        systemUser.setTenantEntity(this);
    }

    public void removeSystemUser(SystemUserDAO systemUser) {
        if (systemUsers != null) {
            systemUsers.remove(systemUser);
            systemUser.setTenantEntity(null);
        }
    }

    public void addTenantConfig(TenantConfig tenantConfig) {
        if (tenantConfigs == null) {
            tenantConfigs = new ArrayList<>();
        }
        tenantConfigs.add(tenantConfig);
        tenantConfig.setTenant(this);
    }

    public void removeTenantConfig(TenantConfig tenantConfig) {
        if (tenantConfigs != null) {
            tenantConfigs.remove(tenantConfig);
            tenantConfig.setTenant(null);
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTimeStamp = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        // Let JPA handle ID generation with @GeneratedValue
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.createTimeStamp == null) {
            this.createTimeStamp = LocalDateTime.now();
        }
        if (this.updateTimeStamp == null) {
            this.updateTimeStamp = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "Tenant{" +
                "id=" + id +
                ", tenantName='" + tenantName + '\'' +
                ", domainName='" + domainName + '\'' +
                ", subscriptionTier='" + subscriptionTier + '\'' +
                ", status='" + status + '\'' +
                ", billingEmail='" + billingEmail + '\'' +
                '}';
    }
}
