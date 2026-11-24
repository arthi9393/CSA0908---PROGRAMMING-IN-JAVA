package com.capstone.scheduler.service;

import com.capstone.scheduler.config.SchedulerProperties;
import com.capstone.scheduler.dto.JobRequest;
import com.capstone.scheduler.model.*;
import com.capstone.scheduler.repo.DeadLetterJobRepository;
import com.capstone.scheduler.repo.JobExecutionRepository;
import com.capstone.scheduler.repo.JobRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** All database state transitions. Each public method is one short transaction. */
@Service
public class JobStateService {

    public record FailureOutcome(boolean retry, int nextAttempt, long delaySeconds, Job job, String reason) { }

    private final JobRepository jobs;
    private final JobExecutionRepository executions;
    private final DeadLetterJobRepository deadLetters;
    private final BackoffPolicy backoff;
    private final SchedulerProperties props;

    public JobStateService(JobRepository jobs, JobExecutionRepository executions,
                           DeadLetterJobRepository deadLetters, BackoffPolicy backoff, SchedulerProperties props) {
        this.jobs = jobs;
        this.executions = executions;
        this.deadLetters = deadLetters;
        this.backoff = backoff;
        this.props = props;
    }

    @Transactional
    public Job createJob(JobRequest req, Instant runAt) {
        Job job = new Job();
        job.setId(UUID.randomUUID().toString());
        job.setType(req.type());
        job.setPayload(req.payload() == null ? "{}" : req.payload().toString());
        job.setPriority(req.priority() == null ? Priority.MEDIUM : req.priority());
        job.setStatus(JobStatus.PENDING);
        job.setRunAt(runAt);
        job.setCron(req.cron() == null || req.cron().isBlank() ? null : req.cron().trim());
        job.setPermanentFailure(Boolean.TRUE.equals(req.permanentFailure()));
        return jobs.save(job);
    }

    @Transactional
    public Job createChild(Job template) {
        Job child = new Job();
        child.setId(UUID.randomUUID().toString());
        child.setType(template.getType());
        child.setPayload(template.getPayload());
        child.setPriority(template.getPriority());
        child.setStatus(JobStatus.PENDING);
        child.setParentJobId(template.getId());
        child.setPermanentFailure(template.isPermanentFailure());
        return jobs.save(child);
    }

    @Transactional(readOnly = true)
    public Optional<Job> find(String id) {
        return jobs.findById(id);
    }

    /**
     * Idempotent start of an attempt. Returns empty when the job is already finished or the message is
     * stale/duplicate (attempt != attempts + 1).
     */
    @Transactional
    public Optional<Job> beginAttempt(String jobId, int attempt, String worker) {
        Job job = jobs.findById(jobId).orElse(null);
        if (job == null) return Optional.empty();
        if (job.getStatus() == JobStatus.SUCCESS || job.getStatus() == JobStatus.FAILED) return Optional.empty();
        if (attempt != job.getAttempts() + 1) return Optional.empty();
        job.setStatus(JobStatus.RUNNING);
        job.setAttempts(attempt);
        jobs.save(job);
        JobExecution exec = new JobExecution();
        exec.setJob(job);
        exec.setAttemptNo(attempt);
        exec.setStartedAt(Instant.now());
        exec.setResult("RUNNING");
        exec.setWorkerName(worker);
        executions.saveAndFlush(exec);
        return Optional.of(job);
    }

    @Transactional
    public void completeSuccess(String jobId, int attempt, String result) {
        Job job = jobs.findById(jobId).orElseThrow();
        JobExecution exec = executions.findByJobIdAndAttemptNo(jobId, attempt).orElseThrow();
        exec.setFinishedAt(Instant.now());
        exec.setResult("SUCCESS");
        exec.setError(result);
        executions.save(exec);
        job.setStatus(JobStatus.SUCCESS);
        job.setLastError(null);
        jobs.save(job);
    }

    /** Records the failed attempt, then decides: RETRYING with backoff, or FAILED + dead_letter_jobs row. */
    @Transactional
    public FailureOutcome recordFailure(String jobId, int attempt, String error) {
        Job job = jobs.findById(jobId).orElseThrow();
        JobExecution exec = executions.findByJobIdAndAttemptNo(jobId, attempt).orElseThrow();
        exec.setFinishedAt(Instant.now());
        exec.setResult("FAILURE");
        exec.setError(error);
        executions.save(exec);
        job.setLastError(error);

        if (job.getRetryCount() < props.getMaxRetries()) {
            job.setRetryCount(job.getRetryCount() + 1);
            job.setStatus(JobStatus.RETRYING);
            jobs.save(job);
            long delay = backoff.delaySeconds(job.getRetryCount());
            return new FailureOutcome(true, attempt + 1, delay, job, error);
        }

        job.setStatus(JobStatus.FAILED);
        jobs.save(job);
        DeadLetterJob dead = new DeadLetterJob();
        dead.setJob(job);
        dead.setReason(error);
        dead.setAttempts(job.getAttempts());
        dead.setFailedAt(Instant.now());
        deadLetters.save(dead);
        return new FailureOutcome(false, 0, 0, job, error);
    }

    /** Manual retry of a dead-lettered job: reset the retry cycle, keep execution history. */
    @Transactional
    public Job resetForManualRetry(String jobId) {
        Job job = jobs.findById(jobId).orElseThrow(() -> new java.util.NoSuchElementException("Job not found: " + jobId));
        if (job.getStatus() != JobStatus.FAILED) {
            throw new IllegalStateException("Only FAILED (dead-lettered) jobs can be retried; job is " + job.getStatus());
        }
        job.setStatus(JobStatus.PENDING);
        job.setRetryCount(0);
        job.setPermanentFailure(false);
        jobs.save(job);
        deadLetters.deleteByJobId(jobId);
        return job;
    }
}
