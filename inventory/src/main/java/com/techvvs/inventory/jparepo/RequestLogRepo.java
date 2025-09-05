package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.RequestLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for RequestLog entity
 */
@Repository
public interface RequestLogRepo extends JpaRepository<RequestLog, UUID> {

    // Find by date range
    List<RequestLog> findByRequestTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by HTTP method
    List<RequestLog> findByHttpMethod(String httpMethod);
    
    // Find by request URI pattern
    List<RequestLog> findByRequestUriContaining(String uriPattern);
    
    // Find by client IP
    List<RequestLog> findByClientIp(String clientIp);
    
    // Find by browser
    List<RequestLog> findByBrowser(String browser);
    
    // Find by operating system
    List<RequestLog> findByOperatingSystem(String operatingSystem);
    
    // Find by device type
    List<RequestLog> findByDeviceType(String deviceType);
    
    // Find by response status
    List<RequestLog> findByResponseStatus(Integer responseStatus);
    
    // Find by session ID
    List<RequestLog> findBySessionId(String sessionId);
    
    // Find bot requests
    List<RequestLog> findByIsBotTrue();
    
    // Find non-bot requests
    List<RequestLog> findByIsBotFalse();
    
    // Find by date range with pagination
    Page<RequestLog> findByRequestTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Find by HTTP method with pagination
    Page<RequestLog> findByHttpMethod(String httpMethod, Pageable pageable);
    
    // Find by response status with pagination
    Page<RequestLog> findByResponseStatus(Integer responseStatus, Pageable pageable);
    
    // Find by client IP with pagination
    Page<RequestLog> findByClientIp(String clientIp, Pageable pageable);
    
    // Find by browser with pagination
    Page<RequestLog> findByBrowser(String browser, Pageable pageable);
    
    // Find by device type with pagination
    Page<RequestLog> findByDeviceType(String deviceType, Pageable pageable);
    
    // Find by session ID with pagination
    Page<RequestLog> findBySessionId(String sessionId, Pageable pageable);
    
    // Custom queries for analytics
    
    // Get request count by HTTP method
    @Query("SELECT r.httpMethod, COUNT(r) FROM RequestLog r GROUP BY r.httpMethod ORDER BY COUNT(r) DESC")
    List<Object[]> getRequestCountByMethod();
    
    // Get request count by browser
    @Query("SELECT r.browser, COUNT(r) FROM RequestLog r WHERE r.browser IS NOT NULL GROUP BY r.browser ORDER BY COUNT(r) DESC")
    List<Object[]> getRequestCountByBrowser();
    
    // Get request count by operating system
    @Query("SELECT r.operatingSystem, COUNT(r) FROM RequestLog r WHERE r.operatingSystem IS NOT NULL GROUP BY r.operatingSystem ORDER BY COUNT(r) DESC")
    List<Object[]> getRequestCountByOperatingSystem();
    
    // Get request count by device type
    @Query("SELECT r.deviceType, COUNT(r) FROM RequestLog r WHERE r.deviceType IS NOT NULL GROUP BY r.deviceType ORDER BY COUNT(r) DESC")
    List<Object[]> getRequestCountByDeviceType();
    
    // Get request count by response status
    @Query("SELECT r.responseStatus, COUNT(r) FROM RequestLog r WHERE r.responseStatus IS NOT NULL GROUP BY r.responseStatus ORDER BY COUNT(r) DESC")
    List<Object[]> getRequestCountByResponseStatus();
    
    // Get request count by hour of day
    @Query("SELECT EXTRACT(HOUR FROM r.requestTimestamp), COUNT(r) FROM RequestLog r GROUP BY EXTRACT(HOUR FROM r.requestTimestamp) ORDER BY EXTRACT(HOUR FROM r.requestTimestamp)")
    List<Object[]> getRequestCountByHour();
    
    // Get request count by day of week
    @Query("SELECT EXTRACT(DOW FROM r.requestTimestamp), COUNT(r) FROM RequestLog r GROUP BY EXTRACT(DOW FROM r.requestTimestamp) ORDER BY EXTRACT(DOW FROM r.requestTimestamp)")
    List<Object[]> getRequestCountByDayOfWeek();
    
    // Get top requested URIs
    @Query("SELECT r.requestUri, COUNT(r) FROM RequestLog r GROUP BY r.requestUri ORDER BY COUNT(r) DESC")
    List<Object[]> getTopRequestedUris();
    
    // Get top client IPs
    @Query("SELECT r.clientIp, COUNT(r) FROM RequestLog r WHERE r.clientIp IS NOT NULL GROUP BY r.clientIp ORDER BY COUNT(r) DESC")
    List<Object[]> getTopClientIps();
    
    // Get average response time by HTTP method
    @Query("SELECT r.httpMethod, AVG(r.durationMs) FROM RequestLog r WHERE r.durationMs IS NOT NULL GROUP BY r.httpMethod ORDER BY AVG(r.durationMs) DESC")
    List<Object[]> getAverageResponseTimeByMethod();
    
    // Get average response time by URI
    @Query("SELECT r.requestUri, AVG(r.durationMs) FROM RequestLog r WHERE r.durationMs IS NOT NULL GROUP BY r.requestUri ORDER BY AVG(r.durationMs) DESC")
    List<Object[]> getAverageResponseTimeByUri();
    
    // Get bot vs human request ratio
    @Query("SELECT r.isBot, COUNT(r) FROM RequestLog r WHERE r.isBot IS NOT NULL GROUP BY r.isBot")
    List<Object[]> getBotVsHumanRatio();
    
    // Get mobile vs desktop ratio
    @Query("SELECT r.deviceType, COUNT(r) FROM RequestLog r WHERE r.deviceType IS NOT NULL GROUP BY r.deviceType")
    List<Object[]> getDeviceTypeRatio();
    
    // Find requests with high response times
    @Query("SELECT r FROM RequestLog r WHERE r.durationMs > :threshold ORDER BY r.durationMs DESC")
    List<RequestLog> findSlowRequests(@Param("threshold") Long thresholdMs);
    
    // Find requests with error status codes
    @Query("SELECT r FROM RequestLog r WHERE r.responseStatus >= 400 ORDER BY r.responseStatus DESC")
    List<RequestLog> findErrorRequests();
    
    // Find requests from specific time period with filters
    @Query("SELECT r FROM RequestLog r WHERE r.requestTimestamp BETWEEN :startDate AND :endDate " +
           "AND (:httpMethod IS NULL OR r.httpMethod = :httpMethod) " +
           "AND (:responseStatus IS NULL OR r.responseStatus = :responseStatus) " +
           "AND (:clientIp IS NULL OR r.clientIp = :clientIp) " +
           "AND (:browser IS NULL OR r.browser = :browser) " +
           "ORDER BY r.requestTimestamp DESC")
    List<RequestLog> findRequestsWithFilters(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("httpMethod") String httpMethod,
        @Param("responseStatus") Integer responseStatus,
        @Param("clientIp") String clientIp,
        @Param("browser") String browser
    );
    
    // Get daily request statistics
    @Query(value = "SELECT DATE(request_timestamp) as request_date, COUNT(*) as request_count, AVG(duration_ms) as avg_duration " +
                   "FROM request_logs " +
                   "WHERE request_timestamp BETWEEN :startDate AND :endDate " +
                   "GROUP BY DATE(request_timestamp) ORDER BY DATE(request_timestamp)", nativeQuery = true)
    List<Object[]> getDailyRequestStatistics(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Get hourly request statistics
    @Query(value = "SELECT EXTRACT(HOUR FROM request_timestamp) as request_hour, COUNT(*) as request_count, AVG(duration_ms) as avg_duration " +
                   "FROM request_logs " +
                   "WHERE request_timestamp BETWEEN :startDate AND :endDate " +
                   "GROUP BY EXTRACT(HOUR FROM request_timestamp) ORDER BY EXTRACT(HOUR FROM request_timestamp)", nativeQuery = true)
    List<Object[]> getHourlyRequestStatistics(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Delete old request logs (for cleanup)
    void deleteByRequestTimestampBefore(LocalDateTime cutoffDate);
    
    // Count requests by date range
    long countByRequestTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Get device type distribution
    @Query(value = "SELECT device_type, COUNT(*) as device_count " +
                   "FROM request_logs " +
                   "WHERE request_timestamp BETWEEN :startDate AND :endDate " +
                   "GROUP BY device_type ORDER BY device_count DESC", nativeQuery = true)
    List<Object[]> getDeviceTypeDistribution(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Get browser distribution
    @Query(value = "SELECT browser, COUNT(*) as browser_count " +
                   "FROM request_logs " +
                   "WHERE request_timestamp BETWEEN :startDate AND :endDate " +
                   "GROUP BY browser ORDER BY browser_count DESC", nativeQuery = true)
    List<Object[]> getBrowserDistribution(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Get response status distribution
    @Query(value = "SELECT response_status, COUNT(*) as status_count " +
                   "FROM request_logs " +
                   "WHERE request_timestamp BETWEEN :startDate AND :endDate " +
                   "GROUP BY response_status ORDER BY response_status", nativeQuery = true)
    List<Object[]> getResponseStatusDistribution(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Get average response time by endpoint
    @Query(value = "SELECT request_uri, COUNT(*) as request_count, AVG(duration_ms) as avg_duration " +
                   "FROM request_logs " +
                   "WHERE request_timestamp BETWEEN :startDate AND :endDate " +
                   "GROUP BY request_uri ORDER BY avg_duration DESC LIMIT 20", nativeQuery = true)
    List<Object[]> getAverageResponseTimeByEndpoint(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Get distinct values for filter dropdowns
    @Query("SELECT DISTINCT r.deviceType FROM RequestLog r WHERE r.deviceType IS NOT NULL ORDER BY r.deviceType")
    List<String> findDistinctDeviceTypes();
    
    @Query("SELECT DISTINCT r.browser FROM RequestLog r WHERE r.browser IS NOT NULL ORDER BY r.browser")
    List<String> findDistinctBrowsers();
    
    @Query("SELECT DISTINCT r.operatingSystem FROM RequestLog r WHERE r.operatingSystem IS NOT NULL ORDER BY r.operatingSystem")
    List<String> findDistinctOperatingSystems();
    
    @Query("SELECT DISTINCT r.responseStatus FROM RequestLog r WHERE r.responseStatus IS NOT NULL ORDER BY r.responseStatus")
    List<Integer> findDistinctResponseStatuses();
    
    @Query("SELECT DISTINCT r.locale FROM RequestLog r WHERE r.locale IS NOT NULL ORDER BY r.locale")
    List<String> findDistinctLocales();
    
    @Query("SELECT DISTINCT r.origin FROM RequestLog r WHERE r.origin IS NOT NULL ORDER BY r.origin")
    List<String> findDistinctOrigins();
    
    // Count requests by HTTP method
    long countByHttpMethod(String httpMethod);
    
    // Count requests by response status
    long countByResponseStatus(Integer responseStatus);
    
    // Count bot requests
    long countByIsBotTrue();
    
    // Count non-bot requests
    long countByIsBotFalse();
}
