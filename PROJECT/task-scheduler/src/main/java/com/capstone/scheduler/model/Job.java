package com.capstone.scheduler.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "jobs", indexes = {
        @Index(name = "idx_jobs_status", columnList = "status"),
        @Index(name = "idx_jobs_priority", columnList = "priority"),
        @Index(name = "idx_jobs_created_at", columnList = "created_at"),
        @Index(name = "idx_jobs_status_priority", columnList = "status,priority")
})
public class Job {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private JobType type;
    @Column(columnDefinition = "text")
    private String payload;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Priority priority;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private JobStatus status;
    @Column(nullable = false)
    private int attempts;
    @Column(name = "retry_count", nullable = false)
    private int retryCount;
    @Column(name = "run_at")
    private Instant runAt;
    @Column(length = 64)
    private String cron;
    @Column(name = "parent_job_id", length = 36)
    private String parentJobId;
    @Column(name = "permanent_failure", nullable = false)
    private boolean permanentFailure;
    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public JobType getType() { return type; }
    public void setType(JobType type) { this.type = type; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public Instant getRunAt() { return runAt; }
    public void setRunAt(Instant runAt) { this.runAt = runAt; }
    public String getCron() { return cron; }
    public void setCron(String cron) { this.cron = cron; }
    public String getParentJobId() { return parentJobId; }
    public void setParentJobId(String parentJobId) { this.parentJobId = parentJobId; }
    public boolean isPermanentFailure() { return permanentFailure; }
    public void setPermanentFailure(boolean permanentFailure) { this.permanentFailure = permanentFailure; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
