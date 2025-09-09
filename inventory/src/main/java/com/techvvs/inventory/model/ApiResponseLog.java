package com.techvvs.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "api_response_log")
public class ApiResponseLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty
    private Long responseLogId;

    // Foreign key to JobLog
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_log_id", nullable = true)
    private JobLog jobLog;

    // Optional one-to-one relationship with request
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_log_id", nullable = true)
    private ApiRequestLog apiRequestLog;

    @JsonProperty
    @Column(nullable = false)
    private Integer httpStatusCode;

    @JsonProperty
    @Column(length = 100)
    private String httpStatusText; // "OK", "Bad Request", "Internal Server Error", etc.

    @JsonProperty
    @Lob
    @Column(columnDefinition = "TEXT")
    private String responseHeaders; // JSON string of headers

    @JsonProperty
    @Lob
    @Column(columnDefinition = "TEXT")
    private String responseBody;

    @JsonProperty
    @Column(length = 100)
    private String contentType;

    @JsonProperty
    private Long responseSizeBytes;

    @JsonProperty
    private Long responseTimeMillis;

    @JsonProperty
    @Column(length = 50)
    private String correlationId;

    @JsonProperty
    @Column(length = 50)
    private String tenantId;

    @JsonProperty
    @Column(length = 200)
    private String apiName;

    @JsonProperty
    @Column(length = 200)
    private String operationName;

    @JsonProperty
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime responseTimestamp;

    @JsonProperty
    private Boolean isSuccess;

    @JsonProperty
    @Lob
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @JsonProperty
    @Lob
    @Column(columnDefinition = "TEXT")
    private String additionalMetadata; // JSON string for flexible data

    // generic fields below
    @JsonProperty
    @Column(name = "update_timestamp")
    private LocalDateTime updateTimeStamp;

    @JsonProperty
    @Column(name = "create_timestamp")
    private LocalDateTime createTimeStamp;

    // Default constructor
    public ApiResponseLog() {
        this.createTimeStamp = LocalDateTime.now();
        this.updateTimeStamp = LocalDateTime.now();
    }

    // Convenience methods
    public void markAsSuccess() {
        this.isSuccess = true;
        this.updateTimeStamp = LocalDateTime.now();
    }

    public void markAsError(String errorMessage) {
        this.isSuccess = false;
        this.errorMessage = errorMessage;
        this.updateTimeStamp = LocalDateTime.now();
    }

    public Boolean isHttpSuccess() {
        return httpStatusCode >= 200 && httpStatusCode < 300;
    }

    public Boolean isHttpClientError() {
        return httpStatusCode >= 400 && httpStatusCode < 500;
    }

    public Boolean isHttpServerError() {
        return httpStatusCode >= 500;
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
    public Long getResponseLogId() {
        return responseLogId;
    }

    public void setResponseLogId(Long responseLogId) {
        this.responseLogId = responseLogId;
    }

    public JobLog getJobLog() {
        return jobLog;
    }

    public void setJobLog(JobLog jobLog) {
        this.jobLog = jobLog;
    }

    public ApiRequestLog getApiRequestLog() {
        return apiRequestLog;
    }

    public void setApiRequestLog(ApiRequestLog apiRequestLog) {
        this.apiRequestLog = apiRequestLog;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getHttpStatusText() {
        return httpStatusText;
    }

    public void setHttpStatusText(String httpStatusText) {
        this.httpStatusText = httpStatusText;
    }

    public String getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(String responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getResponseSizeBytes() {
        return responseSizeBytes;
    }

    public void setResponseSizeBytes(Long responseSizeBytes) {
        this.responseSizeBytes = responseSizeBytes;
    }

    public Long getResponseTimeMillis() {
        return responseTimeMillis;
    }

    public void setResponseTimeMillis(Long responseTimeMillis) {
        this.responseTimeMillis = responseTimeMillis;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public LocalDateTime getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(LocalDateTime responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getAdditionalMetadata() {
        return additionalMetadata;
    }

    public void setAdditionalMetadata(String additionalMetadata) {
        this.additionalMetadata = additionalMetadata;
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
        return "ApiResponseLog{" +
                "responseLogId=" + responseLogId +
                ", httpStatusCode=" + httpStatusCode +
                ", httpStatusText='" + httpStatusText + '\'' +
                ", responseTimeMillis=" + responseTimeMillis +
                ", apiName='" + apiName + '\'' +
                ", operationName='" + operationName + '\'' +
                ", responseTimestamp=" + responseTimestamp +
                ", isSuccess=" + isSuccess +
                ", createTimeStamp=" + createTimeStamp +
                ", updateTimeStamp=" + updateTimeStamp +
                '}';
    }
}
