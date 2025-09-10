package com.techvvs.inventory.repository;

import com.techvvs.inventory.model.ApiResponseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApiResponseLogRepository extends JpaRepository<ApiResponseLog, Long> {

    List<ApiResponseLog> findByHttpStatusCodeBetween(Integer startCode, Integer endCode);
    
    List<ApiResponseLog> findByIsSuccessFalse();
    
    List<ApiResponseLog> findByApiNameAndIsSuccessFalse(String apiName);
    
    @Query("SELECT AVG(r.responseTimeMillis) FROM ApiResponseLog r WHERE r.apiName = :apiName AND r.responseTimestamp >= :since")
    Double getAverageResponseTime(@Param("apiName") String apiName, @Param("since") LocalDateTime since);
    
    @Query("SELECT r FROM ApiResponseLog r WHERE r.responseTimeMillis > :thresholdMillis")
    List<ApiResponseLog> findSlowResponses(@Param("thresholdMillis") Long thresholdMillis);
    
    List<ApiResponseLog> findByJobLogJobLogId(Long jobLogId);
    
    List<ApiResponseLog> findByApiNameOrderByResponseTimestampDesc(String apiName);
    
    List<ApiResponseLog> findByHttpStatusCode(Integer httpStatusCode);
    
    @Query("SELECT COUNT(r) FROM ApiResponseLog r WHERE r.apiName = :apiName AND r.isSuccess = true AND r.responseTimestamp >= :since")
    Long countSuccessfulRequestsByApiSince(@Param("apiName") String apiName, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(r) FROM ApiResponseLog r WHERE r.apiName = :apiName AND r.isSuccess = false AND r.responseTimestamp >= :since")
    Long countFailedRequestsByApiSince(@Param("apiName") String apiName, @Param("since") LocalDateTime since);
    
    @Query("SELECT r FROM ApiResponseLog r WHERE r.responseTimestamp >= :since AND r.apiName = :apiName")
    List<ApiResponseLog> findByApiNameSince(@Param("apiName") String apiName, @Param("since") LocalDateTime since);
    
    @Query("SELECT DISTINCT r.apiName FROM ApiResponseLog r")
    List<String> findDistinctApiNames();
}

