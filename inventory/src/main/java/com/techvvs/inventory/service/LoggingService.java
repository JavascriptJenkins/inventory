package com.techvvs.inventory.service;

import com.techvvs.inventory.model.*;
import com.techvvs.inventory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing job logging and API request/response logging.
 * Provides comprehensive logging capabilities for background jobs and API interactions.
 */
@Service
@Transactional
public class LoggingService {

    @Autowired
    JobLogRepository jobLogRepository;

    @Autowired
    ApiRequestLogRepository apiRequestLogRepository;

    @Autowired
    ApiResponseLogRepository apiResponseLogRepository;

    /**
     * Start a new job log entry.
     *
     * @param jobName Name of the job
     * @param jobDescription Description of what the job does
     * @param tenantId Tenant ID (can be null for system jobs)
     * @param executedBy Who/what executed the job
     * @return JobLog object for tracking
     */
    public JobLog startJob(String jobName, String jobDescription, String tenantId, String executedBy) {
        JobLog jobLog = new JobLog();
        jobLog.setJobName(jobName);
        jobLog.setJobDescription(jobDescription);
        jobLog.setTenantId(tenantId);
        jobLog.setExecutedBy(executedBy);
        jobLog.setStatus(JobStatus.STARTED);
        jobLog.setStartTime(LocalDateTime.now());
        
        return jobLogRepository.save(jobLog);
    }

    /**
     * Update job progress with current statistics.
     *
     * @param jobLog JobLog object to update
     * @param processed Total number of items processed
     * @param succeeded Number of items that succeeded
     * @param failed Number of items that failed
     */
    public void updateJobProgress(JobLog jobLog, Integer processed, Integer succeeded, Integer failed) {
        jobLog.setRecordsProcessed(processed);
        jobLog.setRecordsSucceeded(succeeded);
        jobLog.setRecordsFailed(failed);
        jobLog.setUpdateTimeStamp(LocalDateTime.now());
        
        jobLogRepository.save(jobLog);
    }

    /**
     * Mark a job as completed successfully.
     *
     * @param jobLog JobLog object to complete
     */
    public void completeJob(JobLog jobLog) {
        jobLog.markAsCompleted();
        jobLogRepository.save(jobLog);
    }

    /**
     * Mark a job as failed with error details.
     *
     * @param jobLog JobLog object to mark as failed
     * @param errorMessage Error message describing the failure
     * @param stackTrace Stack trace of the error (can be null)
     */
    public void failJob(JobLog jobLog, String errorMessage, String stackTrace) {
        jobLog.markAsFailed(errorMessage, stackTrace);
        jobLogRepository.save(jobLog);
    }

    /**
     * Log an API request.
     *
     * @param httpMethod HTTP method (GET, POST, etc.)
     * @param requestUrl Full URL of the request
     * @param requestHeaders JSON string of request headers
     * @param requestBody Request body content
     * @param apiName Name of the API being called
     * @param operationName Name of the operation being performed
     * @param tenantId Tenant ID (can be null)
     * @param jobLog Associated job log (can be null)
     * @param correlationId Correlation ID for tracking related requests
     * @return ApiRequestLog object for tracking
     */
    public ApiRequestLog logApiRequest(String httpMethod, String requestUrl, String requestHeaders, 
                                     String requestBody, String apiName, String operationName, 
                                     String tenantId, JobLog jobLog, String correlationId) {
        ApiRequestLog requestLog = new ApiRequestLog();
        requestLog.setHttpMethod(httpMethod);
        requestLog.setRequestUrl(requestUrl);
        requestLog.setRequestHeaders(requestHeaders);
        requestLog.setRequestBody(requestBody);
        requestLog.setApiName(apiName);
        requestLog.setOperationName(operationName);
        requestLog.setTenantId(tenantId);
        requestLog.setJobLog(jobLog);
        requestLog.setCorrelationId(correlationId);
        requestLog.setRequestTimestamp(LocalDateTime.now());
        
        return apiRequestLogRepository.save(requestLog);
    }

    /**
     * Log an API response.
     *
     * @param requestLog Associated request log
     * @param statusCode HTTP status code
     * @param statusMessage HTTP status message
     * @param responseHeaders JSON string of response headers
     * @param responseBody Response body content
     * @param responseTimeMillis Response time in milliseconds
     * @param errorMessage Error message if the request failed (can be null)
     */
    public void logApiResponse(ApiRequestLog requestLog, Integer statusCode, String statusMessage, 
                             String responseHeaders, String responseBody, Long responseTimeMillis, 
                             String errorMessage) {
        ApiResponseLog responseLog = new ApiResponseLog();
        responseLog.setApiRequestLog(requestLog);
        responseLog.setJobLog(requestLog.getJobLog());
        responseLog.setHttpStatusCode(statusCode);
        responseLog.setHttpStatusText(statusMessage);
        responseLog.setResponseHeaders(responseHeaders);
        responseLog.setResponseBody(responseBody);
        responseLog.setResponseTimeMillis(responseTimeMillis);
        responseLog.setErrorMessage(errorMessage);
        responseLog.setResponseTimestamp(LocalDateTime.now());
        
        // Set additional fields from request log
        responseLog.setCorrelationId(requestLog.getCorrelationId());
        responseLog.setTenantId(requestLog.getTenantId());
        responseLog.setApiName(requestLog.getApiName());
        responseLog.setOperationName(requestLog.getOperationName());
        
        // Determine if the response was successful
        responseLog.setIsSuccess(statusCode >= 200 && statusCode < 300);
        
        apiResponseLogRepository.save(responseLog);
    }

    /**
     * Get job log by ID.
     *
     * @param jobLogId Job log ID
     * @return Optional containing JobLog if found
     */
    public Optional<JobLog> getJobLogById(Long jobLogId) {
        return jobLogRepository.findById(jobLogId);
    }

    /**
     * Get job logs by name and status.
     *
     * @param jobName Job name
     * @param status Job status
     * @return List of matching job logs
     */
    public List<JobLog> getJobLogsByNameAndStatus(String jobName, JobStatus status) {
        return jobLogRepository.findByJobNameAndStatus(jobName, status);
    }

    /**
     * Get job logs by status.
     *
     * @param statuses List of job statuses
     * @return List of matching job logs
     */
    public List<JobLog> getJobLogsByStatus(List<JobStatus> statuses) {
        return jobLogRepository.findByStatusIn(statuses);
    }

    /**
     * Get recent job logs.
     *
     * @return List of recent job logs
     */
    public List<JobLog> getRecentJobLogs() {
        return jobLogRepository.findTop10ByOrderByStartTimeDesc();
    }

    /**
     * Get API request logs by correlation ID.
     *
     * @param correlationId Correlation ID
     * @return List of matching API request logs
     */
    public List<ApiRequestLog> getApiRequestLogsByCorrelationId(String correlationId) {
        return apiRequestLogRepository.findByCorrelationId(correlationId);
    }

    /**
     * Get API request logs by API name and operation.
     *
     * @param apiName API name
     * @param operationName Operation name
     * @return List of matching API request logs
     */
    public List<ApiRequestLog> getApiRequestLogsByApiAndOperation(String apiName, String operationName) {
        return apiRequestLogRepository.findByApiNameAndOperationName(apiName, operationName);
    }

    /**
     * Get API response logs by job log ID.
     *
     * @param jobLogId Job log ID
     * @return List of matching API response logs
     */
    public List<ApiResponseLog> getApiResponseLogsByJobLogId(Long jobLogId) {
        return apiResponseLogRepository.findByJobLogJobLogId(jobLogId);
    }

    /**
     * Clean up old job logs (older than specified date).
     *
     * @param cutoffDate Cutoff date for cleanup
     * @return Number of job logs deleted
     */
    public long cleanupOldJobLogs(LocalDateTime cutoffDate) {
        // Find all job logs older than cutoff date
        List<JobLog> allJobLogs = jobLogRepository.findAll();
        List<JobLog> oldJobLogs = allJobLogs.stream()
            .filter(jobLog -> jobLog.getStartTime() != null && jobLog.getStartTime().isBefore(cutoffDate))
            .collect(java.util.stream.Collectors.toList());
        
        long count = oldJobLogs.size();
        jobLogRepository.deleteAll(oldJobLogs);
        return count;
    }

    /**
     * Get job statistics.
     *
     * @return Map containing job statistics
     */
    public java.util.Map<String, Object> getJobStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // Total job count
        stats.put("totalJobs", jobLogRepository.count());
        
        // Job count by status (using available repository methods)
        List<JobLog> allJobs = jobLogRepository.findAll();
        for (JobStatus status : JobStatus.values()) {
            long count = allJobs.stream()
                .filter(job -> job.getStatus() == status)
                .count();
            stats.put("jobs_" + status.name().toLowerCase(), count);
        }
        
        // Recent job activity
        stats.put("recentJobs", getRecentJobLogs());
        
        return stats;
    }
}
