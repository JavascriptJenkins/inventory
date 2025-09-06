package com.techvvs.inventory.service.metrc;

import com.techvvs.inventory.model.MetrcApiConfigVO;
import com.techvvs.inventory.service.MetrcApiConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MetrcConfigurationService {
    
    private final MetrcApiConfigService metrcApiConfigService;
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    public MetrcConfigurationService(MetrcApiConfigService metrcApiConfigService) {
        this.metrcApiConfigService = metrcApiConfigService;
    }
    
    /**
     * Get METRC API credentials based on current environment
     * If environment is "prod", use PROD credentials, otherwise use SANDBOX
     */
    public MetrcApiConfigVO getMetrcCredentials() {
        String environment = "prod".equalsIgnoreCase(activeProfile) ? "PROD" : "SANDBOX";
        return metrcApiConfigService.getConfigByEnvironment(environment);
    }
    
    /**
     * Get the appropriate base URI for METRC API calls
     * If environment is "prod", use PROD URI, otherwise use SANDBOX URI
     */
    public String getMetrcBaseUri() {
        MetrcApiConfigVO config = getMetrcCredentials();
        return "prod".equalsIgnoreCase(activeProfile) 
            ? config.getProdApiKeyBaseUri() 
            : config.getTestApiKeyBaseUri();
    }
    
    /**
     * Get API key username for current environment
     */
    public String getApiKeyUsername() {
        return getMetrcCredentials().getApiKeyUsername();
    }
    
    /**
     * Get API key password for current environment
     */
    public String getApiKeyPassword() {
        return getMetrcCredentials().getApiKeyPassword();
    }
}
