package com.techvvs.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "api_request_log")
public class ApiRequestLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty
    private Long requestLogId;

    // Foreign key to JobLog
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_log_id", nullable = true)
    private JobLog jobLog;

    @JsonProperty
    @Column(nullable = false, length = 10)
    private String httpMethod; // GET, POST, PUT, DELETE, etc.

    @JsonProperty
    @Column(nullable = false, length = 2000)
    private String requestUrl;

    @JsonProperty
    @Lob
    @Column(columnDefinition = "TEXT")
    private String requestHeaders; // JSON string of headers

    @JsonProperty
    @Lob
    @Column(columnDefinition = "TEXT")
    private String requestBody;

    @JsonProperty
    @Column(length = 100)
    private String contentType;

    @JsonProperty
    @Column(length = 100)
    private String userAgent;

    @JsonProperty
    @Column(length = 50)
    private String correlationId; // for tracking requests across systems

    @JsonProperty
    @Column(length = 50)
    private String tenantId;

    @JsonProperty
    @Column(length = 200)
    private String apiName; // "QuickBooks", "eBay", "PayPal", etc.

    @JsonProperty
    @Column(length = 200)
    private String operationName; // "createCustomer", "syncInventory", etc.

    @JsonProperty
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestTimestamp;

    @JsonProperty
    private Integer timeoutSeconds;

    @JsonProperty
    private Integer retryAttempt = 0;

    @JsonProperty
    @Lob
    @Column(columnDefinition = "TEXT")
    private String additionalMetadata; // JSON string for flexible data

    // One-to-one relationship with response (optional)
    @OneToOne(mappedBy = "apiRequestLog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonProperty
    private ApiResponseLog apiResponseLog;

    // generic fields below
    @JsonProperty
    @Column(name = "update_timestamp")
    private LocalDateTime updateTimeStamp;

    @JsonProperty
    @Column(name = "create_timestamp")
    private LocalDateTime createTimeStamp;

    // Default constructor
    public ApiRequestLog() {
        this.createTimeStamp = LocalDateTime.now();
        this.updateTimeStamp = LocalDateTime.now();
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
    public Long getRequestLogId() {
        return requestLogId;
    }

    public void setRequestLogId(Long requestLogId) {
        this.requestLogId = requestLogId;
    }

    public JobLog getJobLog() {
        return jobLog;
    }

    public void setJobLog(JobLog jobLog) {
        this.jobLog = jobLog;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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

    public LocalDateTime getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(LocalDateTime requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Integer getRetryAttempt() {
        return retryAttempt;
    }

    public void setRetryAttempt(Integer retryAttempt) {
        this.retryAttempt = retryAttempt;
    }

    public String getAdditionalMetadata() {
        return additionalMetadata;
    }

    public void setAdditionalMetadata(String additionalMetadata) {
        this.additionalMetadata = additionalMetadata;
    }

    public ApiResponseLog getApiResponseLog() {
        return apiResponseLog;
    }

    public void setApiResponseLog(ApiResponseLog apiResponseLog) {
        this.apiResponseLog = apiResponseLog;
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
        return "ApiRequestLog{" +
                "requestLogId=" + requestLogId +
                ", httpMethod='" + httpMethod + '\'' +
                ", requestUrl='" + requestUrl + '\'' +
                ", apiName='" + apiName + '\'' +
                ", operationName='" + operationName + '\'' +
                ", requestTimestamp=" + requestTimestamp +
                ", retryAttempt=" + retryAttempt +
                ", createTimeStamp=" + createTimeStamp +
                ", updateTimeStamp=" + updateTimeStamp +
                '}';
    }
}
