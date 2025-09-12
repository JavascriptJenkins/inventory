# Tenant Configuration JSON Implementation Summary

## Overview
Implemented JSON storage for TenantConfig values and automatic creation of default configuration when a new tenant is created through the tenant/admin UI.

## Requirements Fulfilled
1. ✅ Store TenantConfig string-to-string value JSON maps in the `configValue` column
2. ✅ Automatically create a default configuration when a new tenant is inserted
3. ✅ Default configuration has key "default" with JSON map `{"uimode": "RETRO"}`

## Implementation Details

### 1. **TenantConfig Entity Enhancements** (`TenantConfig.java`)

#### New Imports Added:
```java
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
```

#### JSON Handling Methods Added:
```java
// Static ObjectMapper for JSON operations
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
```

### 2. **TenantService Enhancements** (`TenantService.groovy`)

#### New Imports Added:
```groovy
import java.util.ArrayList
import java.util.HashMap
import java.util.Map
```

#### Enhanced createTenant Method:
```groovy
Tenant createTenant(Tenant tenant) {
    try {
        // Set timestamps
        tenant.createTimeStamp = LocalDateTime.now()
        tenant.updateTimeStamp = LocalDateTime.now()
        if (tenant.createdAt == null) {
            tenant.createdAt = LocalDateTime.now()
        }
        
        // Save the tenant first to get the ID
        tenant = tenantRepo.save(tenant)
        
        // Create default configuration
        createDefaultTenantConfig(tenant)
        
        return tenant
    } catch (Exception ex) {
        throw new RuntimeException("Failed to create tenant: " + ex.message, ex)
    }
}
```

#### New Default Configuration Creation Method:
```groovy
private void createDefaultTenantConfig(Tenant tenant) {
    try {
        // Create the default configuration map
        Map<String, String> defaultConfig = new HashMap<>()
        defaultConfig.put("uimode", "RETRO")
        
        // Create TenantConfig entity
        TenantConfig defaultTenantConfig = new TenantConfig()
        defaultTenantConfig.setTenant(tenant)
        defaultTenantConfig.setConfigKey("default")
        defaultTenantConfig.setConfigValueFromMap(defaultConfig)
        
        // Save the configuration
        tenantConfigRepo.save(defaultTenantConfig)
        
        // Add to tenant's config list
        if (tenant.tenantConfigs == null) {
            tenant.tenantConfigs = new ArrayList<>()
        }
        tenant.tenantConfigs.add(defaultTenantConfig)
        
    } catch (Exception ex) {
        // Log error but don't fail tenant creation
        System.err.println("Failed to create default tenant config: " + ex.message)
    }
}
```

## Usage Examples

### 1. **Creating a New Tenant (Automatic Default Config)**
When a user creates a new tenant through the UI:
```java
// This happens automatically when createTenant() is called
Tenant newTenant = new Tenant("My Company", "mycompany.com", "premium", "billing@mycompany.com");
newTenant = tenantService.createTenant(newTenant);

// Result: A TenantConfig is automatically created with:
// - configKey: "default"
// - configValue: '{"uimode":"RETRO"}'
```

### 2. **Working with Configuration Values**
```java
// Get a specific config value
TenantConfig config = tenantConfigRepo.findByTenantAndConfigKey(tenantId, "default");
String uiMode = config.getConfigValue("uimode"); // Returns "RETRO"

// Set a specific config value
config.setConfigValue("uimode", "MODERN");
config.setConfigValue("theme", "dark");
tenantConfigRepo.save(config);

// Get the entire config map
Map<String, String> allConfigs = config.getConfigValueAsMap();
// Returns: {"uimode": "MODERN", "theme": "dark"}

// Set the entire config map
Map<String, String> newConfigs = new HashMap<>();
newConfigs.put("uimode", "CLASSIC");
newConfigs.put("language", "en");
newConfigs.put("timezone", "UTC");
config.setConfigValueFromMap(newConfigs);
```

### 3. **Database Storage Format**
The `configValue` column in the database stores JSON strings:
```sql
-- Example of what gets stored in tenant_configs table
INSERT INTO tenant_configs (id, tenant_id, config_key, config_value, createTimeStamp, updateTimeStamp)
VALUES (
    gen_random_uuid(),
    'tenant-uuid-here',
    'default',
    '{"uimode":"RETRO"}',
    NOW(),
    NOW()
);
```

## Error Handling

### 1. **JSON Parsing Errors**
- If `configValue` contains invalid JSON, `getConfigValueAsMap()` returns an empty HashMap
- If JSON serialization fails, `setConfigValueFromMap()` sets `configValue` to null

### 2. **Default Config Creation Errors**
- If default config creation fails, it's logged but doesn't prevent tenant creation
- This ensures tenant creation is robust even if config creation has issues

## Database Schema
The existing `tenant_configs` table structure is used:
```sql
CREATE TABLE tenant_configs (
    id UUID PRIMARY KEY,
    tenant_id UUID REFERENCES tenants(id) ON DELETE CASCADE,
    config_key VARCHAR(100),
    config_value TEXT,  -- Stores JSON strings
    createTimeStamp TIMESTAMP,
    updateTimeStamp TIMESTAMP
);
```

## Testing Results
- ✅ Compilation successful
- ✅ JSON serialization/deserialization working
- ✅ Default configuration automatically created
- ✅ Error handling implemented
- ✅ Backward compatibility maintained

## Future Enhancements
1. **Configuration Validation**: Add validation for specific config keys and values
2. **Configuration Templates**: Support for different default configurations based on tenant type
3. **Configuration History**: Track changes to configurations over time
4. **Bulk Configuration**: Support for importing/exporting configurations
5. **Configuration Inheritance**: Support for tenant-specific configs that inherit from defaults

The tenant management system now supports flexible JSON-based configuration storage with automatic default configuration creation for new tenants.





