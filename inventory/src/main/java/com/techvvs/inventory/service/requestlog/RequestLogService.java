package com.techvvs.inventory.service.requestlog;

import com.techvvs.inventory.jparepo.RequestLogRepo;
import com.techvvs.inventory.model.RequestLog;
import com.techvvs.inventory.util.DeviceDetectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing request log operations
 */
@Service
@Transactional
public class RequestLogService {

    @Autowired
    private RequestLogRepo requestLogRepo;

    /**
     * Create a new request log entry from HTTP request
     */
    public RequestLog createRequestLog(HttpServletRequest request) {
        RequestLog requestLog = new RequestLog();
        
        // Basic request information
        requestLog.setRequestTimestamp(LocalDateTime.now());
        requestLog.setHttpMethod(request.getMethod());
        requestLog.setRequestUri(request.getRequestURI());
        requestLog.setQueryString(request.getQueryString());
        requestLog.setProtocol(request.getProtocol());
        
        // Client information
        requestLog.setClientIp(getClientIpAddress(request));
        requestLog.setClientHost(request.getRemoteHost());
        requestLog.setClientPort(request.getRemotePort());
        
        // Server information
        requestLog.setServerName(request.getServerName());
        requestLog.setServerPort(request.getServerPort());
        requestLog.setContextPath(request.getContextPath());
        requestLog.setServletPath(request.getServletPath());
        requestLog.setPathInfo(request.getPathInfo());
        
        // User agent and device information
        String userAgent = request.getHeader("User-Agent");
        requestLog.setUserAgent(userAgent);
        
        // Parse device information
        Map<String, Object> deviceInfo = DeviceDetectionUtil.parseDeviceInfo(userAgent);
        requestLog.setDeviceInfo(deviceInfo);
        
        // Content information
        requestLog.setContentType(request.getContentType());
        requestLog.setContentLength((long) request.getContentLength());
        requestLog.setCharacterEncoding(request.getCharacterEncoding());
        
        // Session information
        if (request.getSession(false) != null) {
            requestLog.setSessionId(request.getSession().getId());
        }
        requestLog.setRequestedSessionId(request.getRequestedSessionId());
        requestLog.setSessionFromCookie(request.isRequestedSessionIdFromCookie());
        requestLog.setSessionFromUrl(request.isRequestedSessionIdFromURL());
        requestLog.setSessionValid(request.isRequestedSessionIdValid());
        
        // Locale information
        requestLog.setLocale(request.getLocale() != null ? request.getLocale().toString() : null);
        requestLog.setAcceptLanguage(request.getHeader("Accept-Language"));
        
        // Security information
        requestLog.setIsSecure(request.isSecure());
        requestLog.setAuthType(request.getAuthType());
        requestLog.setRemoteUser(request.getRemoteUser());
        requestLog.setReferer(request.getHeader("Referer"));
        requestLog.setOrigin(request.getHeader("Origin"));
        
        return requestLogRepo.save(requestLog);
    }

    /**
     * Update request log with response information
     */
    public void updateRequestLogWithResponse(RequestLog requestLog, HttpServletResponse response) {
        requestLog.setResponseTimestamp(LocalDateTime.now());
        requestLog.setResponseStatus(response.getStatus());
        requestLog.setResponseContentType(response.getContentType());
        requestLog.setResponseCharacterEncoding(response.getCharacterEncoding());
        
        // Calculate duration
        requestLog.calculateDuration();
        
        requestLogRepo.save(requestLog);
    }

    /**
     * Get request log by ID
     */
    public RequestLog getRequestLogById(UUID id) {
        return requestLogRepo.findById(id).orElse(null);
    }

    /**
     * Get request logs by date range
     */
    public List<RequestLog> getRequestLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return requestLogRepo.findByRequestTimestampBetween(startDate, endDate);
    }

    /**
     * Get request logs by date range with pagination
     */
    public Page<RequestLog> getRequestLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return requestLogRepo.findByRequestTimestampBetween(startDate, endDate, pageable);
    }

    /**
     * Get request logs by HTTP method
     */
    public List<RequestLog> getRequestLogsByMethod(String httpMethod) {
        return requestLogRepo.findByHttpMethod(httpMethod);
    }

    /**
     * Get request logs by response status
     */
    public List<RequestLog> getRequestLogsByResponseStatus(Integer responseStatus) {
        return requestLogRepo.findByResponseStatus(responseStatus);
    }

    /**
     * Get request logs by client IP
     */
    public List<RequestLog> getRequestLogsByClientIp(String clientIp) {
        return requestLogRepo.findByClientIp(clientIp);
    }

    /**
     * Get request logs by browser
     */
    public List<RequestLog> getRequestLogsByBrowser(String browser) {
        return requestLogRepo.findByBrowser(browser);
    }

    /**
     * Get request logs by device type
     */
    public List<RequestLog> getRequestLogsByDeviceType(String deviceType) {
        return requestLogRepo.findByDeviceType(deviceType);
    }

    /**
     * Get request logs by session ID
     */
    public List<RequestLog> getRequestLogsBySessionId(String sessionId) {
        return requestLogRepo.findBySessionId(sessionId);
    }

    /**
     * Get bot requests
     */
    public List<RequestLog> getBotRequests() {
        return requestLogRepo.findByIsBotTrue();
    }

    /**
     * Get non-bot requests
     */
    public List<RequestLog> getNonBotRequests() {
        return requestLogRepo.findByIsBotFalse();
    }

    /**
     * Get slow requests (above threshold)
     */
    public List<RequestLog> getSlowRequests(Long thresholdMs) {
        return requestLogRepo.findSlowRequests(thresholdMs);
    }

    /**
     * Get error requests (status >= 400)
     */
    public List<RequestLog> getErrorRequests() {
        return requestLogRepo.findErrorRequests();
    }

    /**
     * Get analytics data
     */
    public Map<String, Object> getAnalyticsData() {
        Map<String, Object> analytics = new java.util.HashMap<>();
        
        // Request count by method
        analytics.put("requestCountByMethod", requestLogRepo.getRequestCountByMethod());
        
        // Request count by browser
        analytics.put("requestCountByBrowser", requestLogRepo.getRequestCountByBrowser());
        
        // Request count by operating system
        analytics.put("requestCountByOperatingSystem", requestLogRepo.getRequestCountByOperatingSystem());
        
        // Request count by device type
        analytics.put("requestCountByDeviceType", requestLogRepo.getRequestCountByDeviceType());
        
        // Request count by response status
        analytics.put("requestCountByResponseStatus", requestLogRepo.getRequestCountByResponseStatus());
        
        // Bot vs human ratio
        analytics.put("botVsHumanRatio", requestLogRepo.getBotVsHumanRatio());
        
        // Device type ratio
        analytics.put("deviceTypeRatio", requestLogRepo.getDeviceTypeRatio());
        
        // Top requested URIs
        analytics.put("topRequestedUris", requestLogRepo.getTopRequestedUris());
        
        // Top client IPs
        analytics.put("topClientIps", requestLogRepo.getTopClientIps());
        
        return analytics;
    }

    /**
     * Get daily request statistics
     */
    public List<Object[]> getDailyRequestStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return requestLogRepo.getDailyRequestStatistics(startDate, endDate);
    }

    /**
     * Get hourly request statistics
     */
    public List<Object[]> getHourlyRequestStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return requestLogRepo.getHourlyRequestStatistics(startDate, endDate);
    }

    /**
     * Clean up old request logs
     */
    public void cleanupOldRequestLogs(LocalDateTime cutoffDate) {
        requestLogRepo.deleteByRequestTimestampBefore(cutoffDate);
    }

    /**
     * Get total request count
     */
    public long getTotalRequestCount() {
        return requestLogRepo.count();
    }

    /**
     * Get request count by date range
     */
    public long getRequestCountByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return requestLogRepo.countByRequestTimestampBetween(startDate, endDate);
    }

    /**
     * Get request count by HTTP method
     */
    public long getRequestCountByMethod(String httpMethod) {
        return requestLogRepo.countByHttpMethod(httpMethod);
    }

    /**
     * Get request count by response status
     */
    public long getRequestCountByResponseStatus(Integer responseStatus) {
        return requestLogRepo.countByResponseStatus(responseStatus);
    }

    /**
     * Get bot request count
     */
    public long getBotRequestCount() {
        return requestLogRepo.countByIsBotTrue();
    }

    /**
     * Get non-bot request count
     */
    public long getNonBotRequestCount() {
        return requestLogRepo.countByIsBotFalse();
    }

    /**
     * Extract client IP address considering proxy headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        String xForwarded = request.getHeader("X-Forwarded");
        if (xForwarded != null && !xForwarded.isEmpty() && !"unknown".equalsIgnoreCase(xForwarded)) {
            return xForwarded;
        }
        
        String forwardedFor = request.getHeader("Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(forwardedFor)) {
            return forwardedFor;
        }
        
        String forwarded = request.getHeader("Forwarded");
        if (forwarded != null && !forwarded.isEmpty() && !"unknown".equalsIgnoreCase(forwarded)) {
            return forwarded;
        }
        
        return request.getRemoteAddr();
    }
}



