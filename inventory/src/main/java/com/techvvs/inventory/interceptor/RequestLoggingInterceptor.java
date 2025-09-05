package com.techvvs.inventory.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvvs.inventory.model.RequestLog;
import com.techvvs.inventory.service.requestlog.RequestLogService;
import com.techvvs.inventory.util.DeviceDetectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor to log incoming HTTP request metadata including method, device info, headers, etc.
 */
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private RequestLogService requestLogService;
    
    // ThreadLocal to store RequestLog between preHandle and afterCompletion
    private static final ThreadLocal<RequestLog> requestLogThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // Create and save request log to database
            RequestLog requestLog = requestLogService.createRequestLog(request);
            requestLogThreadLocal.set(requestLog);
            
            // Also log to file for immediate debugging (optional)
            if (logger.isDebugEnabled()) {
                Map<String, Object> requestMetadata = new HashMap<>();
                requestMetadata.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                requestMetadata.put("method", request.getMethod());
                requestMetadata.put("uri", request.getRequestURI());
                requestMetadata.put("clientIp", requestLog.getClientIp());
                requestMetadata.put("userAgent", request.getHeader("User-Agent"));
                requestMetadata.put("deviceInfo", DeviceDetectionUtil.parseDeviceInfo(request.getHeader("User-Agent")));
                
                String jsonMetadata = objectMapper.writeValueAsString(requestMetadata);
                logger.debug("INCOMING_REQUEST: {}", jsonMetadata);
            }
            
        } catch (Exception e) {
            logger.error("Error creating request log", e);
        }
        
        return true; // Continue with the request
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            // Get the request log from ThreadLocal
            RequestLog requestLog = requestLogThreadLocal.get();
            if (requestLog != null) {
                // Update with response information
                requestLogService.updateRequestLogWithResponse(requestLog, response);
                
                // Clean up ThreadLocal
                requestLogThreadLocal.remove();
                
                // Also log to file for immediate debugging (optional)
                if (logger.isDebugEnabled()) {
                    Map<String, Object> responseMetadata = new HashMap<>();
                    responseMetadata.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    responseMetadata.put("status", response.getStatus());
                    responseMetadata.put("contentType", response.getContentType());
                    responseMetadata.put("characterEncoding", response.getCharacterEncoding());
                    responseMetadata.put("method", request.getMethod());
                    responseMetadata.put("uri", request.getRequestURI());
                    responseMetadata.put("durationMs", requestLog.getDurationMs());
                    
                    String jsonMetadata = objectMapper.writeValueAsString(responseMetadata);
                    logger.debug("RESPONSE_SENT: {}", jsonMetadata);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error updating request log with response", e);
            // Clean up ThreadLocal even if there's an error
            requestLogThreadLocal.remove();
        }
    }

}
