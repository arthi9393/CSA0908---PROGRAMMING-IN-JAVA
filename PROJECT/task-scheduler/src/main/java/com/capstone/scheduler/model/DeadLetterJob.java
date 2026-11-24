package com.capstone.scheduler.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dead_letter_jobs", indexes = @Index(name = "idx_dlq_job_id", columnList = "job_id"))
public class DeadLetterJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;
    @Column(nullable = false, columnDefinition = "text")
    private String reason;
    @Column(nullable = false)
    private int attempts;
    @Column(name = "failed_at", nullable = false)
    private Instant failedAt;

    public Long getId() { return id; }
    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public Instant getFailedAt() { return failedAt; }
    public void setFailedAt(Instant failedAt) { this.failedAt = failedAt; }
}
