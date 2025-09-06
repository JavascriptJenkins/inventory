package com.techvvs.inventory.service;

import com.techvvs.inventory.model.MetrcApiConfigVO;
import com.techvvs.inventory.repository.MetrcApiConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MetrcApiConfigService {
    
    @Autowired
    private MetrcApiConfigRepository metrcApiConfigRepository;
    
    /**
     * Get the current METRC API configuration
     * If no configuration exists, returns a new empty configuration
     */
    public MetrcApiConfigVO getCurrentConfig() {
        Optional<MetrcApiConfigVO> config = metrcApiConfigRepository.findFirstByOrderByUpdatedAtDesc();
        return config.orElse(new MetrcApiConfigVO());
    }
    
    /**
     * Save or update the METRC API configuration
     */
    public MetrcApiConfigVO saveConfig(MetrcApiConfigVO config) {
        // If this is an update (has ID), update the existing record
        if (config.getId() != null && metrcApiConfigRepository.existsById(config.getId())) {
            config.setUpdatedAt(LocalDateTime.now());
            return metrcApiConfigRepository.save(config);
        } else {
            // If this is a new configuration, create it
            config.setId(null); // Ensure it's treated as new
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            return metrcApiConfigRepository.save(config);
        }
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
        return metrcApiConfigRepository.findAll();
    }
    
    /**
     * Delete configuration by ID
     */
    public void deleteConfig(Long id) {
        metrcApiConfigRepository.deleteById(id);
    }
    
    /**
     * Check if configuration exists
     */
    public boolean configExists() {
        return metrcApiConfigRepository.findFirstByOrderByUpdatedAtDesc().isPresent();
    }
    
    /**
     * Update configuration with new values
     */
    public MetrcApiConfigVO updateConfig(Long id, String apiKeyUsername, String apiKeyPassword, 
                                        String testApiKeyBaseUri, String prodApiKeyBaseUri) {
        Optional<MetrcApiConfigVO> existingConfig = metrcApiConfigRepository.findById(id);
        
        if (existingConfig.isPresent()) {
            MetrcApiConfigVO config = existingConfig.get();
            config.setApiKeyUsername(apiKeyUsername);
            config.setApiKeyPassword(apiKeyPassword);
            config.setTestApiKeyBaseUri(testApiKeyBaseUri);
            config.setProdApiKeyBaseUri(prodApiKeyBaseUri);
            config.setUpdatedAt(LocalDateTime.now());
            
            return metrcApiConfigRepository.save(config);
        } else {
            // If ID doesn't exist, create new configuration
            MetrcApiConfigVO newConfig = new MetrcApiConfigVO(apiKeyUsername, apiKeyPassword, 
                                                             testApiKeyBaseUri, prodApiKeyBaseUri);
            return metrcApiConfigRepository.save(newConfig);
        }
    }
}
