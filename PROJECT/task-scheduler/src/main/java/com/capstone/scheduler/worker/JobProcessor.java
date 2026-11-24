package com.capstone.scheduler.worker;

import com.capstone.scheduler.config.SchedulerProperties;
import com.capstone.scheduler.dto.JobMessage;
import com.capstone.scheduler.kafka.JobPublisher;
import com.capstone.scheduler.model.Job;
import com.capstone.scheduler.model.JobType;
import com.capstone.scheduler.service.JobStateService;
import com.capstone.scheduler.service.JobStateService.FailureOutcome;
import com.capstone.scheduler.service.RedisJobStore;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/** Executes one job message: lock, idempotency check, run, record result, retry or dead-letter. */
@Component
public class JobProcessor {
    private static final Logger log = LoggerFactory.getLogger(JobProcessor.class);

    private final JobStateService state;
    private final RedisJobStore store;
    private final JobPublisher publisher;
    private final SchedulerProperties props;
    private final Map<JobType, JobHandler> handlers = new EnumMap<>(JobType.class);

    public JobProcessor(JobStateService state, RedisJobStore store, JobPublisher publisher,
                        SchedulerProperties props, List<JobHandler> handlerList) {
        this.state = state;
        this.store = store;
        this.publisher = publisher;
        this.props = props;
        for (JobHandler h : handlerList) handlers.put(h.type(), h);
    }

    public void process(JobMessage msg) {
        String jobId = msg.jobId();
        String token = UUID.randomUUID().toString();
        if (!store.tryLock(jobId, token, props.getLockTtlMs())) {
            log.warn("Job {} is locked by another worker - skipping duplicate delivery", jobId);
            return;
        }
        try {
            String worker = Thread.currentThread().getName();
            Optional<Job> started;
            try {
                started = state.beginAttempt(jobId, msg.attempt(), worker);
            } catch (DataIntegrityViolationException dup) {
                log.warn("Job {} attempt {} already recorded - duplicate ignored", jobId, msg.attempt());
                return;
            }
            if (started.isEmpty()) {
                log.info("Job {} attempt {} skipped (already finished, unknown or stale)", jobId, msg.attempt());
                return;
            }
            Job job = started.get();
            log.info("[{}] START job {} {} {} attempt {}", worker, jobId, job.getType(), job.getPriority(), msg.attempt());

            String result = null;
            String error = null;
            try {
                JobHandler handler = handlers.get(job.getType());
                if (handler == null) throw new IllegalStateException("No handler for " + job.getType());
                result = handler.execute(job);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                error = "Worker interrupted";
            } catch (Exception ex) {
                error = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            }

            if (error == null) {
                state.completeSuccess(jobId, msg.attempt(), result);
                store.recordProcessed(true);
                log.info("[{}] SUCCESS job {} attempt {}", worker, jobId, msg.attempt());
                return;
            }

            FailureOutcome outcome = state.recordFailure(jobId, msg.attempt(), error);
            store.recordProcessed(false);
            if (outcome.retry()) {
                long notBefore = System.currentTimeMillis() + outcome.delaySeconds() * 1000L;
                publisher.publishRetry(outcome.job(), outcome.nextAttempt(), notBefore);
                log.warn("[{}] RETRYING job {} after failure '{}' - next attempt {} in {}s", worker, jobId, error,
                        outcome.nextAttempt(), outcome.delaySeconds());
            } else {
                publisher.publishDlq(outcome.job(), error);
                log.error("[{}] FAILED job {} permanently after {} attempts: {}", worker, jobId, outcome.job().getAttempts(), error);
            }
        } finally {
            store.unlock(jobId, token);
        }
    }
}
