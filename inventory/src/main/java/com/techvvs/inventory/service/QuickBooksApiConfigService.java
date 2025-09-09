package com.techvvs.inventory.service;

import com.techvvs.inventory.model.QuickBooksApiConfigVO;
import com.techvvs.inventory.repository.QuickBooksApiConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QuickBooksApiConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(QuickBooksApiConfigService.class);
    
    @Autowired
    private QuickBooksApiConfigRepository quickBooksApiConfigRepository;
    
    private static final String SANDBOX_ENV = "SANDBOX";
    private static final String PROD_ENV = "PROD";
    
    /**
     * Initialize default configurations for both SANDBOX and PROD environments if they don't exist
     */
    @Transactional
    public void initializeDefaultConfigurations() {
        // Migrate existing records with null environment to SANDBOX
        migrateExistingRecords();
        
        // Initialize SANDBOX configuration
        if (!quickBooksApiConfigRepository.existsByEnvironment(SANDBOX_ENV)) {
            QuickBooksApiConfigVO sandboxConfig = new QuickBooksApiConfigVO();
            sandboxConfig.setAppId("");
            sandboxConfig.setClientId("");
            sandboxConfig.setClientSecret("");
            sandboxConfig.setSandboxBaseUrl("https://sandbox-quickbooks.api.intuit.com");
            sandboxConfig.setProdBaseUrl("https://quickbooks.api.intuit.com");
            sandboxConfig.setRedirectUri("/quickbooks/oauth/callback");
            sandboxConfig.setScope("com.intuit.quickbooks.accounting");
            sandboxConfig.setAccessToken("");
            sandboxConfig.setRefreshToken("");
            sandboxConfig.setTokenExpiresAt(null);
            sandboxConfig.setEnvironment(SANDBOX_ENV);
            quickBooksApiConfigRepository.save(sandboxConfig);
        }
        
        // Initialize PROD configuration
        if (!quickBooksApiConfigRepository.existsByEnvironment(PROD_ENV)) {
            QuickBooksApiConfigVO prodConfig = new QuickBooksApiConfigVO();
            prodConfig.setAppId("");
            prodConfig.setClientId("");
            prodConfig.setClientSecret("");
            prodConfig.setSandboxBaseUrl("https://sandbox-quickbooks.api.intuit.com");
            prodConfig.setProdBaseUrl("https://quickbooks.api.intuit.com");
            prodConfig.setRedirectUri("/quickbooks/oauth/callback");
            prodConfig.setScope("com.intuit.quickbooks.accounting");
            prodConfig.setAccessToken("");
            prodConfig.setRefreshToken("");
            prodConfig.setTokenExpiresAt(null);
            prodConfig.setEnvironment(PROD_ENV);
            quickBooksApiConfigRepository.save(prodConfig);
        }
    }
    
    /**
     * Migrate existing records that have null environment to SANDBOX environment
     */
    private void migrateExistingRecords() {
        List<QuickBooksApiConfigVO> recordsWithNullEnvironment = quickBooksApiConfigRepository.findByEnvironmentIsNull();
        if (!recordsWithNullEnvironment.isEmpty()) {
            System.out.println("Found " + recordsWithNullEnvironment.size() + " records with null environment, migrating to SANDBOX");
            for (QuickBooksApiConfigVO record : recordsWithNullEnvironment) {
                record.setEnvironment(SANDBOX_ENV);
                quickBooksApiConfigRepository.save(record);
            }
        }
    }
    
    /**
     * Get all configurations for both environments
     */
    public Map<String, QuickBooksApiConfigVO> getAllConfigurations() {
        initializeDefaultConfigurations(); // Ensure both configs exist
        
        Map<String, QuickBooksApiConfigVO> configs = new HashMap<>();
        
        // Get SANDBOX config
        Optional<QuickBooksApiConfigVO> sandboxConfig = quickBooksApiConfigRepository.findByEnvironment(SANDBOX_ENV);
        sandboxConfig.ifPresent(config -> configs.put(SANDBOX_ENV, config));
        
        // Get PROD config
        Optional<QuickBooksApiConfigVO> prodConfig = quickBooksApiConfigRepository.findByEnvironment(PROD_ENV);
        prodConfig.ifPresent(config -> configs.put(PROD_ENV, config));
        
        return configs;
    }
    
    /**
     * Get configuration by environment
     */
    public QuickBooksApiConfigVO getConfigByEnvironment(String environment) {
        initializeDefaultConfigurations(); // Ensure both configs exist
        
        Optional<QuickBooksApiConfigVO> config = quickBooksApiConfigRepository.findByEnvironment(environment);
        if (config.isPresent()) {
            return config.get();
        } else {
            // Create default config for environment if it doesn't exist
            QuickBooksApiConfigVO newConfig = new QuickBooksApiConfigVO();
            newConfig.setAppId("");
            newConfig.setClientId("");
            newConfig.setClientSecret("");
            newConfig.setSandboxBaseUrl("https://sandbox-quickbooks.api.intuit.com");
            newConfig.setProdBaseUrl("https://quickbooks.api.intuit.com");
            newConfig.setRedirectUri("/quickbooks/oauth/callback");
            newConfig.setScope("com.intuit.quickbooks.accounting");
            newConfig.setAccessToken("");
            newConfig.setRefreshToken("");
            newConfig.setTokenExpiresAt(null);
            newConfig.setEnvironment(environment);
            return quickBooksApiConfigRepository.save(newConfig);
        }
    }
    
    /**
     * Save configuration for a specific environment
     */
    public QuickBooksApiConfigVO saveConfiguration(QuickBooksApiConfigVO config) {
        initializeDefaultConfigurations(); // Ensure both configs exist
        
        // Set environment if not already set
        if (config.getEnvironment() == null || config.getEnvironment().trim().isEmpty()) {
            config.setEnvironment(SANDBOX_ENV);
        }
        
        // Find existing config for this environment
        Optional<QuickBooksApiConfigVO> existingConfig = quickBooksApiConfigRepository.findByEnvironment(config.getEnvironment());
        if (existingConfig.isPresent()) {
            QuickBooksApiConfigVO existing = existingConfig.get();
            existing.setAppId(config.getAppId());
            existing.setClientId(config.getClientId());
            existing.setClientSecret(config.getClientSecret());
            existing.setSandboxBaseUrl(config.getSandboxBaseUrl());
            existing.setProdBaseUrl(config.getProdBaseUrl());
            existing.setRedirectUri(config.getRedirectUri());
            existing.setScope(config.getScope());
            // Only update tokens if they are provided (not empty)
            if (config.getAccessToken() != null && !config.getAccessToken().trim().isEmpty()) {
                existing.setAccessToken(config.getAccessToken());
            }
            if (config.getRefreshToken() != null && !config.getRefreshToken().trim().isEmpty()) {
                existing.setRefreshToken(config.getRefreshToken());
            }
            if (config.getTokenExpiresAt() != null) {
                existing.setTokenExpiresAt(config.getTokenExpiresAt());
            }
            return quickBooksApiConfigRepository.save(existing);
        } else {
            return quickBooksApiConfigRepository.save(config);
        }
    }
    
    /**
     * Get SANDBOX configuration
     */
    public QuickBooksApiConfigVO getSandboxConfig() {
        return getConfigByEnvironment(SANDBOX_ENV);
    }
    
    /**
     * Get PROD configuration
     */
    public QuickBooksApiConfigVO getProdConfig() {
        return getConfigByEnvironment(PROD_ENV);
    }
    
    /**
     * Update access token and refresh token for an environment
     */
    public QuickBooksApiConfigVO updateTokens(String environment, String accessToken, String refreshToken, LocalDateTime expiresAt) {
        QuickBooksApiConfigVO config = getConfigByEnvironment(environment);
        config.setAccessToken(accessToken);
        config.setRefreshToken(refreshToken);
        config.setTokenExpiresAt(expiresAt);
        return quickBooksApiConfigRepository.save(config);
    }
    
    /**
     * Check if access token is expired
     */
    public boolean isTokenExpired(String environment) {
        QuickBooksApiConfigVO config = getConfigByEnvironment(environment);
        if (config.getTokenExpiresAt() == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(config.getTokenExpiresAt());
    }
    
    /**
     * Get the appropriate base URL based on environment
     */
    public String getBaseUrl(String environment) {
        QuickBooksApiConfigVO config = getConfigByEnvironment(environment);
        if (SANDBOX_ENV.equals(environment)) {
            return config.getSandboxBaseUrl();
        } else {
            return config.getProdBaseUrl();
        }
    }
}
