package com.techvvs.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "tenant_configs")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    @JsonProperty
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", referencedColumnName = "id")
    @JsonProperty
    private Tenant tenant;

    @Column(name = "config_key", length = 100)
    @JsonProperty
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    @JsonProperty
    private String configValue;

    // Generic timestamp fields
    @Column(name = "updateTimeStamp")
    @JsonProperty
    private LocalDateTime updateTimeStamp;

    @Column(name = "createTimeStamp")
    @JsonProperty
    private LocalDateTime createTimeStamp;

    // Constructors
    public TenantConfig() {
        // Don't set ID in constructor - let JPA handle it
        this.createTimeStamp = LocalDateTime.now();
        this.updateTimeStamp = LocalDateTime.now();
    }

    public TenantConfig(Tenant tenant, String configKey, String configValue) {
        this();
        this.tenant = tenant;
        this.configKey = configKey;
        this.configValue = configValue;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
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

    @PreUpdate
    public void preUpdate() {
        this.updateTimeStamp = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        // Let JPA handle ID generation with @GeneratedValue
        if (this.createTimeStamp == null) {
            this.createTimeStamp = LocalDateTime.now();
        }
        if (this.updateTimeStamp == null) {
            this.updateTimeStamp = LocalDateTime.now();
        }
    }

    // JSON handling methods for configValue
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get the config value as a Map<String, String>
     * @return Map representation of the JSON config value
     */
    @Transient
    public Map<String, String> getConfigValueAsMap() {
        if (configValue == null || configValue.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(configValue, Map.class);
        } catch (JsonProcessingException e) {
            // If JSON parsing fails, return empty map
            return new HashMap<>();
        }
    }

    /**
     * Set the config value from a Map<String, String>
     * @param configMap Map to convert to JSON and store
     */
    @Transient
    public void setConfigValueFromMap(Map<String, String> configMap) {
        if (configMap == null) {
            this.configValue = null;
            return;
        }
        try {
            this.configValue = objectMapper.writeValueAsString(configMap);
        } catch (JsonProcessingException e) {
            // If JSON serialization fails, store as null
            this.configValue = null;
        }
    }

    /**
     * Get a specific value from the config map
     * @param key The key to retrieve
     * @return The value for the key, or null if not found
     */
    @Transient
    public String getConfigValue(String key) {
        Map<String, String> configMap = getConfigValueAsMap();
        return configMap.get(key);
    }

    /**
     * Set a specific value in the config map
     * @param key The key to set
     * @param value The value to set
     */
    @Transient
    public void setConfigValue(String key, String value) {
        Map<String, String> configMap = getConfigValueAsMap();
        configMap.put(key, value);
        setConfigValueFromMap(configMap);
    }

    @Override
    public String toString() {
        return "TenantConfig{" +
                "id=" + id +
                ", configKey='" + configKey + '\'' +
                ", configValue='" + configValue + '\'' +
                '}';
    }
}
