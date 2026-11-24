package com.capstone.scheduler.service;

import com.capstone.scheduler.kafka.JobPublisher;
import com.capstone.scheduler.model.Job;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Polls the Redis sorted set delayed_jobs and publishes due entries to Kafka.
 * Members: "J:{jobId}:{attempt}" (delayed job or retry) and "R:{templateId}" (recurring cron template).
 */
@Component
public class DelayedJobScheduler {
    private static final Logger log = LoggerFactory.getLogger(DelayedJobScheduler.class);

    private final RedisJobStore store;
    private final JobStateService state;
    private final JobPublisher publisher;

    public DelayedJobScheduler(RedisJobStore store, JobStateService state, JobPublisher publisher) {
        this.store = store;
        this.state = state;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelayString = "${scheduler.poll-ms:500}")
    public void tick() {
        try {
            processDue(System.currentTimeMillis());
        } catch (Exception e) {
            log.error("Scheduler tick failed: {}", e.getMessage());
        }
    }

    public void processDue(long nowMs) {
        for (String member : store.claimDue(nowMs, 200)) {
            try {
                if (member.startsWith("R:")) fireRecurring(member.substring(2));
                else fireDelayed(member);
            } catch (Exception e) {
                log.error("Failed to dispatch delayed entry {}: {}", member, e.getMessage());
            }
        }
    }

    private void fireDelayed(String member) {
        String[] parts = member.split(":");
        if (parts.length != 3) {
            log.warn("Ignoring malformed delayed entry {}", member);
            return;
        }
        Optional<Job> job = state.find(parts[1]);
        if (job.isEmpty()) {
            log.warn("Delayed entry {} refers to an unknown job", member);
            return;
        }
        publisher.publish(job.get(), Integer.parseInt(parts[2]));
    }

    private void fireRecurring(String templateId) {
        Optional<Job> template = state.find(templateId);
        if (template.isEmpty() || template.get().getCron() == null) return;
        // Re-arm the next occurrence first so a failure below can never stop the recurrence.
        long next = JobService.nextCronMillis(template.get().getCron(), ZonedDateTime.now(ZoneId.systemDefault()));
        store.scheduleDelayed(JobService.recurringMember(templateId), next);
        Job child = state.createChild(template.get());
        publisher.publish(child, 1);
        log.info("Recurring template {} fired job {}", templateId, child.getId());
    }
}
