package com.techvvs.inventory.service;

import com.techvvs.inventory.model.PayPalApiConfigVO;
import com.techvvs.inventory.repository.PayPalApiConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PayPalApiConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(PayPalApiConfigService.class);
    
    @Autowired
    private PayPalApiConfigRepository paypalApiConfigRepository;
    
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
        if (!paypalApiConfigRepository.existsByEnvironment(SANDBOX_ENV)) {
            PayPalApiConfigVO sandboxConfig = new PayPalApiConfigVO();
            sandboxConfig.setClientId("");
            sandboxConfig.setClientSecret("");
            sandboxConfig.setSandboxBaseUrl("https://api-m.sandbox.paypal.com");
            sandboxConfig.setProdBaseUrl("https://api-m.paypal.com");
            sandboxConfig.setReturnUrl("/payment/paypal/return");
            sandboxConfig.setCancelUrl("/payment/paypal/cancel");
            sandboxConfig.setBrandName("TechVVS Inventory");
            sandboxConfig.setEnvironment(SANDBOX_ENV);
            paypalApiConfigRepository.save(sandboxConfig);
        }
        
        // Initialize PROD configuration
        if (!paypalApiConfigRepository.existsByEnvironment(PROD_ENV)) {
            PayPalApiConfigVO prodConfig = new PayPalApiConfigVO();
            prodConfig.setClientId("");
            prodConfig.setClientSecret("");
            prodConfig.setSandboxBaseUrl("https://api-m.sandbox.paypal.com");
            prodConfig.setProdBaseUrl("https://api-m.paypal.com");
            prodConfig.setReturnUrl("/payment/paypal/return");
            prodConfig.setCancelUrl("/payment/paypal/cancel");
            prodConfig.setBrandName("TechVVS Inventory");
            prodConfig.setEnvironment(PROD_ENV);
            paypalApiConfigRepository.save(prodConfig);
        }
    }
    
    /**
     * Migrate existing records that have null environment to SANDBOX environment
     */
    private void migrateExistingRecords() {
        List<PayPalApiConfigVO> recordsWithNullEnvironment = paypalApiConfigRepository.findByEnvironmentIsNull();
        if (!recordsWithNullEnvironment.isEmpty()) {
            System.out.println("Found " + recordsWithNullEnvironment.size() + " records with null environment, migrating to SANDBOX");
            for (PayPalApiConfigVO record : recordsWithNullEnvironment) {
                record.setEnvironment(SANDBOX_ENV);
                paypalApiConfigRepository.save(record);
            }
        }
    }
    
    /**
     * Get all configurations for both environments
     */
    public Map<String, PayPalApiConfigVO> getAllConfigurations() {
        initializeDefaultConfigurations(); // Ensure both configs exist
        
        Map<String, PayPalApiConfigVO> configs = new HashMap<>();
        
        // Get SANDBOX config
        Optional<PayPalApiConfigVO> sandboxConfig = paypalApiConfigRepository.findByEnvironment(SANDBOX_ENV);
        sandboxConfig.ifPresent(config -> configs.put(SANDBOX_ENV, config));
        
        // Get PROD config
        Optional<PayPalApiConfigVO> prodConfig = paypalApiConfigRepository.findByEnvironment(PROD_ENV);
        prodConfig.ifPresent(config -> configs.put(PROD_ENV, config));
        
        return configs;
    }
    
    /**
     * Get configuration by environment
     */
    public PayPalApiConfigVO getConfigByEnvironment(String environment) {
        initializeDefaultConfigurations(); // Ensure both configs exist
        
        Optional<PayPalApiConfigVO> config = paypalApiConfigRepository.findByEnvironment(environment);
        if (config.isPresent()) {
            return config.get();
        } else {
            // Create default config for environment if it doesn't exist
            PayPalApiConfigVO newConfig = new PayPalApiConfigVO();
            newConfig.setClientId("");
            newConfig.setClientSecret("");
            newConfig.setSandboxBaseUrl("https://api-m.sandbox.paypal.com");
            newConfig.setProdBaseUrl("https://api-m.paypal.com");
            newConfig.setReturnUrl("/payment/paypal/return");
            newConfig.setCancelUrl("/payment/paypal/cancel");
            newConfig.setBrandName("TechVVS Inventory");
            newConfig.setEnvironment(environment);
            return paypalApiConfigRepository.save(newConfig);
        }
    }
    
    /**
     * Save configuration for a specific environment
     */
    public PayPalApiConfigVO saveConfiguration(PayPalApiConfigVO config) {
        initializeDefaultConfigurations(); // Ensure both configs exist
        
        // Set environment if not already set
        if (config.getEnvironment() == null || config.getEnvironment().trim().isEmpty()) {
            config.setEnvironment(SANDBOX_ENV);
        }
        
        // Find existing config for this environment
        Optional<PayPalApiConfigVO> existingConfig = paypalApiConfigRepository.findByEnvironment(config.getEnvironment());
        if (existingConfig.isPresent()) {
            PayPalApiConfigVO existing = existingConfig.get();
            existing.setClientId(config.getClientId());
            existing.setClientSecret(config.getClientSecret());
            existing.setSandboxBaseUrl(config.getSandboxBaseUrl());
            existing.setProdBaseUrl(config.getProdBaseUrl());
            existing.setReturnUrl(config.getReturnUrl());
            existing.setCancelUrl(config.getCancelUrl());
            existing.setBrandName(config.getBrandName());
            return paypalApiConfigRepository.save(existing);
        } else {
            return paypalApiConfigRepository.save(config);
        }
    }
    
    /**
     * Get SANDBOX configuration
     */
    public PayPalApiConfigVO getSandboxConfig() {
        return getConfigByEnvironment(SANDBOX_ENV);
    }
    
    /**
     * Get PROD configuration
     */
    public PayPalApiConfigVO getProdConfig() {
        return getConfigByEnvironment(PROD_ENV);
    }
}

