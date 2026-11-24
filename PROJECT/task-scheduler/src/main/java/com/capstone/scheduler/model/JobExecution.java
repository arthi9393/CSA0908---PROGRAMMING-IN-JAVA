package com.capstone.scheduler.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "job_executions",
        uniqueConstraints = @UniqueConstraint(name = "uq_exec_job_attempt", columnNames = {"job_id", "attempt_no"}),
        indexes = @Index(name = "idx_exec_job_id", columnList = "job_id"))
public class JobExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;
    @Column(name = "attempt_no", nullable = false)
    private int attemptNo;
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;
    @Column(name = "finished_at")
    private Instant finishedAt;
    @Column(nullable = false, length = 16)
    private String result;
    @Column(columnDefinition = "text")
    private String error;
    @Column(name = "worker_name", length = 64)
    private String workerName;

    public Long getId() { return id; }
    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
    public int getAttemptNo() { return attemptNo; }
    public void setAttemptNo(int attemptNo) { this.attemptNo = attemptNo; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
}
