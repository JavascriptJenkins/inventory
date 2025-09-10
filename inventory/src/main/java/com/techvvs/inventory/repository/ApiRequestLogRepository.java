package com.techvvs.inventory.repository;

import com.techvvs.inventory.model.ApiRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApiRequestLogRepository extends JpaRepository<ApiRequestLog, Long> {

    List<ApiRequestLog> findByApiNameAndOperationName(String apiName, String operationName);
    
    List<ApiRequestLog> findByCorrelationId(String correlationId);
    
    List<ApiRequestLog> findByTenantIdAndRequestTimestampBetween(String tenantId, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(r) FROM ApiRequestLog r WHERE r.apiName = :apiName AND r.requestTimestamp >= :since")
    Long countRequestsByApiSince(@Param("apiName") String apiName, @Param("since") LocalDateTime since);
    
    List<ApiRequestLog> findByJobLogJobLogId(Long jobLogId);
    
    List<ApiRequestLog> findByApiNameOrderByRequestTimestampDesc(String apiName);
    
    List<ApiRequestLog> findByHttpMethodAndApiName(String httpMethod, String apiName);
    
    @Query("SELECT r FROM ApiRequestLog r WHERE r.requestTimestamp >= :since AND r.apiName = :apiName")
    List<ApiRequestLog> findByApiNameSince(@Param("apiName") String apiName, @Param("since") LocalDateTime since);
    
    @Query("SELECT DISTINCT r.apiName FROM ApiRequestLog r")
    List<String> findDistinctApiNames();
    
    @Query("SELECT DISTINCT r.operationName FROM ApiRequestLog r WHERE r.apiName = :apiName")
    List<String> findDistinctOperationsByApiName(@Param("apiName") String apiName);
}

