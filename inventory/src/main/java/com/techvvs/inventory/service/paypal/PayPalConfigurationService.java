package com.techvvs.inventory.service.paypal;

import com.techvvs.inventory.model.PayPalApiConfigVO;
import com.techvvs.inventory.service.PayPalApiConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PayPalConfigurationService {
    
    private final PayPalApiConfigService paypalApiConfigService;
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    public PayPalConfigurationService(PayPalApiConfigService paypalApiConfigService) {
        this.paypalApiConfigService = paypalApiConfigService;
    }
    
    /**
     * Get PayPal API credentials based on current environment
     * If environment is "prod", use PROD credentials, otherwise use SANDBOX
     */
    public PayPalApiConfigVO getPayPalCredentials() {
        String environment = "prod".equalsIgnoreCase(activeProfile) ? "PROD" : "SANDBOX";
        return paypalApiConfigService.getConfigByEnvironment(environment);
    }
    
    /**
     * Get the appropriate base URL for PayPal API calls
     * If environment is "prod", use PROD URL, otherwise use SANDBOX URL
     */
    public String getPayPalBaseUrl() {
        PayPalApiConfigVO config = getPayPalCredentials();
        return "prod".equalsIgnoreCase(activeProfile) 
            ? config.getProdBaseUrl() 
            : config.getSandboxBaseUrl();
    }
    
    /**
     * Get client ID for current environment
     */
    public String getClientId() {
        return getPayPalCredentials().getClientId();
    }
    
    /**
     * Get client secret for current environment
     */
    public String getClientSecret() {
        return getPayPalCredentials().getClientSecret();
    }
    
    /**
     * Get return URL for current environment
     */
    public String getReturnUrl() {
        return getPayPalCredentials().getReturnUrl();
    }
    
    /**
     * Get cancel URL for current environment
     */
    public String getCancelUrl() {
        return getPayPalCredentials().getCancelUrl();
    }
    
    /**
     * Get brand name for current environment
     */
    public String getBrandName() {
        return getPayPalCredentials().getBrandName();
    }
}

