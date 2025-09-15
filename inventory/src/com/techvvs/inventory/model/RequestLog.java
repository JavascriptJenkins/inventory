package com.techvvs.inventory.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entity to track HTTP request metadata in a relational database structure
 */
@Entity
@Table(name = "request_logs")
public class RequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    @JsonProperty
    private UUID id;

    // Basic request information
    @Column(name = "request_timestamp", nullable = false)
    @JsonProperty
    private LocalDateTime requestTimestamp;

    @Column(name = "http_method", length = 10, nullable = false)
    @JsonProperty
    private String httpMethod;

    @Column(name = "request_uri", length = 2000, nullable = false)
    @JsonProperty
    private String requestUri;

    @Column(name = "query_string", length = 2000)
    @JsonProperty
    private String queryString;

    @Column(name = "protocol", length = 20)
    @JsonProperty
    private String protocol;

    // Client information
    @Column(name = "client_ip", length = 45)
    @JsonProperty
    private String clientIp;

    @Column(name = "client_host", length = 255)
    @JsonProperty
    private String clientHost;

    @Column(name = "client_port")
    @JsonProperty
    private Integer clientPort;

    // Server information
    @Column(name = "server_name", length = 255)
    @JsonProperty
    private String serverName;

    @Column(name = "server_port")
    @JsonProperty
    private Integer serverPort;

    @Column(name = "context_path", length = 500)
    @JsonProperty
    private String contextPath;

    @Column(name = "servlet_path", length = 500)
    @JsonProperty
    private String servletPath;

    @Column(name = "path_info", length = 500)
    @JsonProperty
    private String pathInfo;

    // User agent and device information
    @Column(name = "user_agent", length = 2000)
    @JsonProperty
    private String userAgent;

    @Column(name = "browser", length = 100)
    @JsonProperty
    private String browser;

    @Column(name = "browser_version", length = 50)
    @JsonProperty
    private String browserVersion;

    @Column(name = "operating_system", length = 100)
    @JsonProperty
    private String operatingSystem;

    @Column(name = "os_version", length = 50)
    @JsonProperty
    private String osVersion;

    @Column(name = "device_type", length = 20)
    @JsonProperty
    private String deviceType;

    @Column(name = "device_model", length = 100)
    @JsonProperty
    private String deviceModel;

    @Column(name = "is_bot")
    @JsonProperty
    private Boolean isBot;

    @Column(name = "is_touch_device")
    @JsonProperty
    private Boolean isTouchDevice;

    @Column(name = "is_64bit")
    @JsonProperty
    private Boolean is64Bit;

    // Content information
    @Column(name = "content_type", length = 255)
    @JsonProperty
    private String contentType;

    @Column(name = "content_length")
    @JsonProperty
    private Long contentLength;

    @Column(name = "character_encoding", length = 50)
    @JsonProperty
    private String characterEncoding;

    // Session information
    @Column(name = "session_id", length = 100)
    @JsonProperty
    private String sessionId;

    @Column(name = "requested_session_id", length = 100)
    @JsonProperty
    private String requestedSessionId;

    @Column(name = "session_from_cookie")
    @JsonProperty
    private Boolean sessionFromCookie;

    @Column(name = "session_from_url")
    @JsonProperty
    private Boolean sessionFromUrl;

    @Column(name = "session_valid")
    @JsonProperty
    private Boolean sessionValid;

    // Locale information
    @Column(name = "locale", length = 20)
    @JsonProperty
    private String locale;

    @Column(name = "accept_language", length = 500)
    @JsonProperty
    private String acceptLanguage;

    // Security information
    @Column(name = "is_secure")
    @JsonProperty
    private Boolean isSecure;

    @Column(name = "auth_type", length = 50)
    @JsonProperty
    private String authType;

    @Column(name = "remote_user", length = 255)
    @JsonProperty
    private String remoteUser;

    @Column(name = "referer", length = 2000)
    @JsonProperty
    private String referer;

    @Column(name = "origin", length = 500)
    @JsonProperty
    private String origin;

    // Response information
    @Column(name = "response_timestamp")
    @JsonProperty
    private LocalDateTime responseTimestamp;

    @Column(name = "response_status")
    @JsonProperty
    private Integer responseStatus;

    @Column(name = "response_content_type", length = 255)
    @JsonProperty
    private String responseContentType;

    @Column(name = "response_character_encoding", length = 50)
    @JsonProperty
    private String responseCharacterEncoding;

    // Request duration in milliseconds
    @Column(name = "duration_ms")
    @JsonProperty
    private Long durationMs;

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @JsonProperty
    private LocalDateTime updatedAt;

    // Constructors
    public RequestLog() {
        this.requestTimestamp = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public RequestLog(String httpMethod, String requestUri) {
        this();
        this.httpMethod = httpMethod;
        this.requestUri = requestUri;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(LocalDateTime requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getClientHost() {
        return clientHost;
    }

    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }

    public Integer getClientPort() {
        return clientPort;
    }

    public void setClientPort(Integer clientPort) {
        this.clientPort = clientPort;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getServletPath() {
        return servletPath;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public Boolean getIsBot() {
        return isBot;
    }

    public void setIsBot(Boolean isBot) {
        this.isBot = isBot;
    }

    public Boolean getIsTouchDevice() {
        return isTouchDevice;
    }

    public void setIsTouchDevice(Boolean isTouchDevice) {
        this.isTouchDevice = isTouchDevice;
    }

    public Boolean getIs64Bit() {
        return is64Bit;
    }

    public void setIs64Bit(Boolean is64Bit) {
        this.is64Bit = is64Bit;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRequestedSessionId() {
        return requestedSessionId;
    }

    public void setRequestedSessionId(String requestedSessionId) {
        this.requestedSessionId = requestedSessionId;
    }

    public Boolean getSessionFromCookie() {
        return sessionFromCookie;
    }

    public void setSessionFromCookie(Boolean sessionFromCookie) {
        this.sessionFromCookie = sessionFromCookie;
    }

    public Boolean getSessionFromUrl() {
        return sessionFromUrl;
    }

    public void setSessionFromUrl(Boolean sessionFromUrl) {
        this.sessionFromUrl = sessionFromUrl;
    }

    public Boolean getSessionValid() {
        return sessionValid;
    }

    public void setSessionValid(Boolean sessionValid) {
        this.sessionValid = sessionValid;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    public void setAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
    }

    public Boolean getIsSecure() {
        return isSecure;
    }

    public void setIsSecure(Boolean isSecure) {
        this.isSecure = isSecure;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public LocalDateTime getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(LocalDateTime responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public void setResponseContentType(String responseContentType) {
        this.responseContentType = responseContentType;
    }

    public String getResponseCharacterEncoding() {
        return responseCharacterEncoding;
    }

    public void setResponseCharacterEncoding(String responseCharacterEncoding) {
        this.responseCharacterEncoding = responseCharacterEncoding;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public void calculateDuration() {
        if (requestTimestamp != null && responseTimestamp != null) {
            this.durationMs = java.time.Duration.between(requestTimestamp, responseTimestamp).toMillis();
        }
    }

    public void setDeviceInfo(Map<String, Object> deviceInfo) {
        if (deviceInfo != null) {
            this.browser = (String) deviceInfo.get("browser");
            this.browserVersion = (String) deviceInfo.get("browserVersion");
            this.operatingSystem = (String) deviceInfo.get("os");
            this.osVersion = (String) deviceInfo.get("osVersion");
            this.deviceType = (String) deviceInfo.get("device");
            this.deviceModel = (String) deviceInfo.get("deviceModel");
            this.isBot = (Boolean) deviceInfo.get("isBot");
            this.isTouchDevice = (Boolean) deviceInfo.get("isTouchDevice");
            this.is64Bit = (Boolean) deviceInfo.get("is64Bit");
        }
    }

    @Override
    public String toString() {
        return "RequestLog{" +
                "id=" + id +
                ", requestTimestamp=" + requestTimestamp +
                ", httpMethod='" + httpMethod + '\'' +
                ", requestUri='" + requestUri + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", browser='" + browser + '\'' +
                ", operatingSystem='" + operatingSystem + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", responseStatus=" + responseStatus +
                ", durationMs=" + durationMs +
                '}';
    }
}
