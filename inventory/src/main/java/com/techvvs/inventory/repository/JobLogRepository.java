package com.techvvs.inventory.repository;

import com.techvvs.inventory.model.JobLog;
import com.techvvs.inventory.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobLogRepository extends JpaRepository<JobLog, Long> {

    List<JobLog> findByJobNameAndStatus(String jobName, JobStatus status);
    
    List<JobLog> findByStatusIn(List<JobStatus> statuses);
    
    List<JobLog> findByTenantIdAndStartTimeBetween(String tenantId, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT j FROM JobLog j WHERE j.status = :status AND j.startTime < :cutoffTime")
    List<JobLog> findStuckJobs(@Param("status") JobStatus status, @Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT j FROM JobLog j WHERE j.durationMillis > :thresholdMillis")
    List<JobLog> findSlowJobs(@Param("thresholdMillis") Long thresholdMillis);
    
    List<JobLog> findTop10ByOrderByStartTimeDesc();
    
    List<JobLog> findByJobNameOrderByStartTimeDesc(String jobName);
    
    List<JobLog> findByExecutedByOrderByStartTimeDesc(String executedBy);
    
    @Query("SELECT j FROM JobLog j WHERE j.status IN :statuses AND j.startTime >= :startTime")
    List<JobLog> findByStatusInAndStartTimeAfter(@Param("statuses") List<JobStatus> statuses, @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT COUNT(j) FROM JobLog j WHERE j.jobName = :jobName AND j.status = :status")
    Long countByJobNameAndStatus(@Param("jobName") String jobName, @Param("status") JobStatus status);
}

