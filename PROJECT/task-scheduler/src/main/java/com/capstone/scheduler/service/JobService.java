package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.JobRequest;
import com.capstone.scheduler.kafka.JobPublisher;
import com.capstone.scheduler.model.Job;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

@Service
public class JobService {
    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    private final JobStateService state;
    private final JobPublisher publisher;
    private final RedisJobStore store;

    public JobService(JobStateService state, JobPublisher publisher, RedisJobStore store) {
        this.state = state;
        this.publisher = publisher;
        this.store = store;
    }

    public static String delayedMember(String jobId, int attempt) {
        return "J:" + jobId + ":" + attempt;
    }

    public static String recurringMember(String templateId) {
        return "R:" + templateId;
    }

    /** Next fire time of a Spring 6-field cron expression, as epoch millis. */
    public static long nextCronMillis(String cron, ZonedDateTime from) {
        ZonedDateTime next = CronExpression.parse(cron).next(from);
        if (next == null) throw new IllegalArgumentException("Cron expression has no future fire time: " + cron);
        return next.toInstant().toEpochMilli();
    }

    public Job submit(JobRequest req) {
        if (req.type() == null) throw new IllegalArgumentException("type is required");
        String cron = req.cron() == null || req.cron().isBlank() ? null : req.cron().trim();
        if (cron != null && !CronExpression.isValidExpression(cron)) {
            throw new IllegalArgumentException("Invalid cron expression (Spring 6-field format, e.g. '0/10 * * * * *'): " + cron);
        }
        Instant runAt = req.runAt();
        if (runAt == null && req.delaySeconds() != null && req.delaySeconds() > 0) {
            runAt = Instant.now().plusSeconds(req.delaySeconds());
        }

        Job job = state.createJob(req, runAt);

        if (cron != null) {
            long next = nextCronMillis(cron, ZonedDateTime.now(ZoneId.systemDefault()));
            store.scheduleDelayed(recurringMember(job.getId()), next);
            log.info("Recurring job template {} saved, cron '{}', first run at {}", job.getId(), cron, Instant.ofEpochMilli(next));
        } else if (runAt != null && runAt.isAfter(Instant.now())) {
            store.scheduleDelayed(delayedMember(job.getId(), 1), runAt.toEpochMilli());
            log.info("Delayed job {} stored in delayed_jobs for {}", job.getId(), runAt);
        } else {
            publisher.publish(job, 1);
        }
        return job;
    }

    public Job retryDeadLetter(String jobId) {
        Job job = state.resetForManualRetry(jobId);
        publisher.publish(job, job.getAttempts() + 1);
        log.info("Manual retry of dead-lettered job {}", jobId);
        return job;
    }
}
