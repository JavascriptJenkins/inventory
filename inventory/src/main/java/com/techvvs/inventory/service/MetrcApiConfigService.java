package com.techvvs.inventory.service;

import com.techvvs.inventory.model.MetrcApiConfigVO;
import com.techvvs.inventory.repository.MetrcApiConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class MetrcApiConfigService {
    
    @Autowired
    private MetrcApiConfigRepository metrcApiConfigRepository;
    
    private static final String SANDBOX_ENV = "SANDBOX";
    private static final String PROD_ENV = "PROD";
    
    /**
     * Initialize default configurations for both environments if they don't exist
     */
    @Transactional
    public void initializeDefaultConfigurations() {
        // Migrate existing records with null environment to SANDBOX
        migrateExistingRecords();
        
        // Initialize SANDBOX configuration
        if (!metrcApiConfigRepository.existsByEnvironment(SANDBOX_ENV)) {
            MetrcApiConfigVO sandboxConfig = new MetrcApiConfigVO();
            sandboxConfig.setApiKeyUsername("");
            sandboxConfig.setApiKeyPassword("");
            sandboxConfig.setTestApiKeyBaseUri("https://sandbox-api-il.metrc.com");
            sandboxConfig.setProdApiKeyBaseUri("https://api-il.metrc.com");
            sandboxConfig.setEnvironment(SANDBOX_ENV);
            metrcApiConfigRepository.save(sandboxConfig);
        }
        
        // Initialize PROD configuration
        if (!metrcApiConfigRepository.existsByEnvironment(PROD_ENV)) {
            MetrcApiConfigVO prodConfig = new MetrcApiConfigVO();
            prodConfig.setApiKeyUsername("");
            prodConfig.setApiKeyPassword("");
            prodConfig.setTestApiKeyBaseUri("https://sandbox-api-il.metrc.com");
            prodConfig.setProdApiKeyBaseUri("https://api-il.metrc.com");
            prodConfig.setEnvironment(PROD_ENV);
            metrcApiConfigRepository.save(prodConfig);
        }
    }
    
    /**
     * Migrate existing records that have null environment to SANDBOX environment
     */
    private void migrateExistingRecords() {
        List<MetrcApiConfigVO> recordsWithNullEnvironment = metrcApiConfigRepository.findByEnvironmentIsNull();
        if (!recordsWithNullEnvironment.isEmpty()) {
            System.out.println("Found " + recordsWithNullEnvironment.size() + " records with null environment, migrating to SANDBOX");
            for (MetrcApiConfigVO record : recordsWithNullEnvironment) {
                record.setEnvironment(SANDBOX_ENV);
                metrcApiConfigRepository.save(record);
            }
        }
    }
    
    /**
     * Get all configurations for both environments
     */
    public Map<String, MetrcApiConfigVO> getAllConfigurations() {
        initializeDefaultConfigurations(); // Ensure both configs exist
        
        Map<String, MetrcApiConfigVO> configs = new HashMap<>();
        
        // Get SANDBOX config
        Optional<MetrcApiConfigVO> sandboxConfig = metrcApiConfigRepository.findByEnvironment(SANDBOX_ENV);
        sandboxConfig.ifPresent(config -> configs.put(SANDBOX_ENV, config));
        
        // Get PROD config
        Optional<MetrcApiConfigVO> prodConfig = metrcApiConfigRepository.findByEnvironment(PROD_ENV);
        prodConfig.ifPresent(config -> configs.put(PROD_ENV, config));
        
        return configs;
    }
    
    /**
     * Get configuration by environment
     */
    public MetrcApiConfigVO getConfigByEnvironment(String environment) {
        initializeDefaultConfigurations(); // Ensure both configs exist
        
        Optional<MetrcApiConfigVO> config = metrcApiConfigRepository.findByEnvironment(environment);
        if (config.isPresent()) {
            return config.get();
        } else {
            // Create default config for environment if it doesn't exist
            MetrcApiConfigVO newConfig = new MetrcApiConfigVO();
            newConfig.setApiKeyUsername("");
            newConfig.setApiKeyPassword("");
            newConfig.setTestApiKeyBaseUri("https://sandbox-api-il.metrc.com");
            newConfig.setProdApiKeyBaseUri("https://api-il.metrc.com");
            newConfig.setEnvironment(environment);
            return metrcApiConfigRepository.save(newConfig);
        }
    }
    
    /**
     * Save or update configuration for a specific environment
     */
    public MetrcApiConfigVO saveConfigForEnvironment(String environment, MetrcApiConfigVO config) {
        initializeDefaultConfigurations(); // Ensure both configs exist
        
        // Find existing config for environment
        Optional<MetrcApiConfigVO> existingConfig = metrcApiConfigRepository.findByEnvironment(environment);
        
        if (existingConfig.isPresent()) {
            // Update existing config
            MetrcApiConfigVO existing = existingConfig.get();
            existing.setApiKeyUsername(config.getApiKeyUsername());
            existing.setApiKeyPassword(config.getApiKeyPassword());
            existing.setTestApiKeyBaseUri(config.getTestApiKeyBaseUri());
            existing.setProdApiKeyBaseUri(config.getProdApiKeyBaseUri());
            existing.setUpdatedAt(LocalDateTime.now());
            return metrcApiConfigRepository.save(existing);
        } else {
            // Create new config for environment
            config.setEnvironment(environment);
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            return metrcApiConfigRepository.save(config);
        }
    }
    
    /**
     * Update configuration with individual parameters for a specific environment
     */
    public MetrcApiConfigVO updateConfigForEnvironment(String environment, String apiKeyUsername, 
                                                      String apiKeyPassword, String testApiKeyBaseUri, 
                                                      String prodApiKeyBaseUri) {
        initializeDefaultConfigurations(); // Ensure both configs exist
        
        Optional<MetrcApiConfigVO> existingConfig = metrcApiConfigRepository.findByEnvironment(environment);
        
        if (existingConfig.isPresent()) {
            MetrcApiConfigVO config = existingConfig.get();
            config.setApiKeyUsername(apiKeyUsername);
            config.setApiKeyPassword(apiKeyPassword);
            config.setTestApiKeyBaseUri(testApiKeyBaseUri);
            config.setProdApiKeyBaseUri(prodApiKeyBaseUri);
            config.setUpdatedAt(LocalDateTime.now());
            return metrcApiConfigRepository.save(config);
        } else {
            // Create new config for environment
            MetrcApiConfigVO newConfig = new MetrcApiConfigVO(apiKeyUsername, apiKeyPassword, 
                                                            testApiKeyBaseUri, prodApiKeyBaseUri, environment);
            return metrcApiConfigRepository.save(newConfig);
        }
    }
    
    /**
     * Reset configuration to default values for a specific environment
     */
    public MetrcApiConfigVO resetConfigForEnvironment(String environment) {
        MetrcApiConfigVO defaultConfig = new MetrcApiConfigVO();
        defaultConfig.setApiKeyUsername("");
        defaultConfig.setApiKeyPassword("");
        defaultConfig.setTestApiKeyBaseUri("https://sandbox-api-il.metrc.com");
        defaultConfig.setProdApiKeyBaseUri("https://api-il.metrc.com");
        defaultConfig.setEnvironment(environment);
        
        return saveConfigForEnvironment(environment, defaultConfig);
    }
    
    /**
     * Get configuration by ID
     */
    public Optional<MetrcApiConfigVO> getConfigById(Long id) {
        return metrcApiConfigRepository.findById(id);
    }
    
    /**
     * Get all configurations (for admin purposes)
     */
    public List<MetrcApiConfigVO> getAllConfigs() {
        initializeDefaultConfigurations(); // Ensure both configs exist
        return metrcApiConfigRepository.findAllByOrderByEnvironment();
    }
    
    /**
     * Delete configuration by ID (only if not SANDBOX or PROD)
     */
    public void deleteConfig(Long id) {
        Optional<MetrcApiConfigVO> config = metrcApiConfigRepository.findById(id);
        if (config.isPresent()) {
            String environment = config.get().getEnvironment();
            if (!SANDBOX_ENV.equals(environment) && !PROD_ENV.equals(environment)) {
                metrcApiConfigRepository.deleteById(id);
            } else {
                throw new IllegalArgumentException("Cannot delete SANDBOX or PROD configurations. Use reset instead.");
            }
        }
    }
    
    /**
     * Check if configuration exists for environment
     */
    public boolean configExistsForEnvironment(String environment) {
        return metrcApiConfigRepository.existsByEnvironment(environment);
    }
    
    /**
     * Get available environments
     */
    public String[] getAvailableEnvironments() {
        return new String[]{SANDBOX_ENV, PROD_ENV};
    }
}