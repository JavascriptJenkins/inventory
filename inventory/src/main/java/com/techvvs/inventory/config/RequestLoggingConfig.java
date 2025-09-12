package com.techvvs.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for request logging
 */
@Configuration
@ConfigurationProperties(prefix = "request.logging")
public class RequestLoggingConfig {

    private boolean enabled = true;
    private boolean logHeaders = true;
    private boolean logDeviceInfo = true;
    private boolean logSessionInfo = true;
    private boolean logSecurityInfo = false;
    private String[] excludePaths = {"/static/**", "/css/**", "/js/**", "/images/**", "/favicon.ico", "/actuator/**"};
    private String[] sensitiveHeaders = {
        "authorization", "cookie", "x-api-key", "x-auth-token", 
        "x-access-token", "x-csrf-token", "x-forwarded-for", "x-real-ip"
    };

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isLogHeaders() {
        return logHeaders;
    }

    public void setLogHeaders(boolean logHeaders) {
        this.logHeaders = logHeaders;
    }

    public boolean isLogDeviceInfo() {
        return logDeviceInfo;
    }

    public void setLogDeviceInfo(boolean logDeviceInfo) {
        this.logDeviceInfo = logDeviceInfo;
    }

    public boolean isLogSessionInfo() {
        return logSessionInfo;
    }

    public void setLogSessionInfo(boolean logSessionInfo) {
        this.logSessionInfo = logSessionInfo;
    }

    public boolean isLogSecurityInfo() {
        return logSecurityInfo;
    }

    public void setLogSecurityInfo(boolean logSecurityInfo) {
        this.logSecurityInfo = logSecurityInfo;
    }

    public String[] getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(String[] excludePaths) {
        this.excludePaths = excludePaths;
    }

    public String[] getSensitiveHeaders() {
        return sensitiveHeaders;
    }

    public void setSensitiveHeaders(String[] sensitiveHeaders) {
        this.sensitiveHeaders = sensitiveHeaders;
    }
}





