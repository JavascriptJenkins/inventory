package com.techvvs.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "job_log")
public class JobLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty
    private Long jobLogId;

    @JsonProperty
    @Column(nullable = false, length = 100)
    private String jobName;

    @JsonProperty
    @Column(length = 500)
    private String jobDescription;

    @JsonProperty
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.STARTED;

    @JsonProperty
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime startTime;

    @JsonProperty
    @UpdateTimestamp
    private LocalDateTime endTime;

    @JsonProperty
    @Column(nullable = true)
    private Long durationMillis;

    @JsonProperty
    @Column(length = 100)
    private String executedBy; // system user, scheduled task name, etc.

    @JsonProperty
    @Column(length = 50)
    private String tenantId; // for multi-tenant systems

    @JsonProperty
    private Integer recordsProcessed = 0;

    @JsonProperty
    private Integer recordsSucceeded = 0;

    @JsonProperty
    private Integer recordsFailed = 0;

    @JsonProperty
    @Lob
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @JsonProperty
    @Lob
    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    @JsonProperty
    @Lob
    @Column(columnDefinition = "TEXT")
    private String jobParameters; // JSON string of input parameters

    @JsonProperty
    @Lob
    @Column(columnDefinition = "TEXT")
    private String additionalMetadata; // JSON string for flexible data

    // One-to-many relationship with API request logs
    @OneToMany(mappedBy = "jobLog", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonProperty
    private List<ApiRequestLog> apiRequestLogs = new ArrayList<>();

    // One-to-many relationship with API response logs
    @OneToMany(mappedBy = "jobLog", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonProperty
    private List<ApiResponseLog> apiResponseLogs = new ArrayList<>();

    // generic fields below
    @JsonProperty
    @Column(name = "update_timestamp")
    private LocalDateTime updateTimeStamp;

    @JsonProperty
    @Column(name = "create_timestamp")
    private LocalDateTime createTimeStamp;

    // Default constructor
    public JobLog() {
        this.createTimeStamp = LocalDateTime.now();
        this.updateTimeStamp = LocalDateTime.now();
    }

    // Convenience methods
    public void markAsCompleted() {
        this.status = JobStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.updateTimeStamp = LocalDateTime.now();
        calculateDuration();
    }

    public void markAsFailed(String errorMessage, String stackTrace) {
        this.status = JobStatus.FAILED;
        this.endTime = LocalDateTime.now();
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace;
        this.updateTimeStamp = LocalDateTime.now();
        calculateDuration();
    }

    public void markAsFailed(String errorMessage) {
        markAsFailed(errorMessage, null);
    }

    public void markAsPartiallyCompleted(String errorMessage) {
        this.status = JobStatus.PARTIALLY_COMPLETED;
        this.endTime = LocalDateTime.now();
        this.errorMessage = errorMessage;
        this.updateTimeStamp = LocalDateTime.now();
        calculateDuration();
    }

    public void markAsPartiallyCompleted() {
        markAsPartiallyCompleted(null);
    }

    private void calculateDuration() {
        if (startTime != null && endTime != null) {
            this.durationMillis = Duration.between(startTime, endTime).toMillis();
        }
    }

    public void addApiRequest(ApiRequestLog requestLog) {
        requestLog.setJobLog(this);
        this.apiRequestLogs.add(requestLog);
    }

    public void addApiResponse(ApiResponseLog responseLog) {
        responseLog.setJobLog(this);
        this.apiResponseLogs.add(responseLog);
    }

    public String getDurationFormatted() {
        if (durationMillis == null) return "N/A";
        long seconds = durationMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
    }

    // Pre-persist and pre-update hooks
    @PrePersist
    protected void onCreate() {
        createTimeStamp = LocalDateTime.now();
        updateTimeStamp = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTimeStamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getJobLogId() {
        return jobLogId;
    }

    public void setJobLogId(Long jobLogId) {
        this.jobLogId = jobLogId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(Long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public String getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(String executedBy) {
        this.executedBy = executedBy;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getRecordsProcessed() {
        return recordsProcessed;
    }

    public void setRecordsProcessed(Integer recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }

    public Integer getRecordsSucceeded() {
        return recordsSucceeded;
    }

    public void setRecordsSucceeded(Integer recordsSucceeded) {
        this.recordsSucceeded = recordsSucceeded;
    }

    public Integer getRecordsFailed() {
        return recordsFailed;
    }

    public void setRecordsFailed(Integer recordsFailed) {
        this.recordsFailed = recordsFailed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getJobParameters() {
        return jobParameters;
    }

    public void setJobParameters(String jobParameters) {
        this.jobParameters = jobParameters;
    }

    public String getAdditionalMetadata() {
        return additionalMetadata;
    }

    public void setAdditionalMetadata(String additionalMetadata) {
        this.additionalMetadata = additionalMetadata;
    }

    public List<ApiRequestLog> getApiRequestLogs() {
        return apiRequestLogs;
    }

    public void setApiRequestLogs(List<ApiRequestLog> apiRequestLogs) {
        this.apiRequestLogs = apiRequestLogs;
    }

    public List<ApiResponseLog> getApiResponseLogs() {
        return apiResponseLogs;
    }

    public void setApiResponseLogs(List<ApiResponseLog> apiResponseLogs) {
        this.apiResponseLogs = apiResponseLogs;
    }

    public LocalDateTime getUpdateTimeStamp() {
        return updateTimeStamp;
    }

    public void setUpdateTimeStamp(LocalDateTime updateTimeStamp) {
        this.updateTimeStamp = updateTimeStamp;
    }

    public LocalDateTime getCreateTimeStamp() {
        return createTimeStamp;
    }

    public void setCreateTimeStamp(LocalDateTime createTimeStamp) {
        this.createTimeStamp = createTimeStamp;
    }

    @Override
    public String toString() {
        return "JobLog{" +
                "jobLogId=" + jobLogId +
                ", jobName='" + jobName + '\'' +
                ", status=" + status +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", durationMillis=" + durationMillis +
                ", executedBy='" + executedBy + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", recordsProcessed=" + recordsProcessed +
                ", recordsSucceeded=" + recordsSucceeded +
                ", recordsFailed=" + recordsFailed +
                ", createTimeStamp=" + createTimeStamp +
                ", updateTimeStamp=" + updateTimeStamp +
                '}';
    }
}
