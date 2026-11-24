package com.capstone.scheduler.service;

import com.capstone.scheduler.model.JobStatus;
import com.capstone.scheduler.model.Priority;
import com.capstone.scheduler.repo.DeadLetterJobRepository;
import com.capstone.scheduler.repo.JobExecutionRepository;
import com.capstone.scheduler.repo.JobRepository;
import com.capstone.scheduler.worker.PriorityJobBuffer;
import com.capstone.scheduler.worker.WorkerPool;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Dashboard statistics: counts come from PostgreSQL, per-minute rates from Redis counters. */
@Service
public class StatsService {
    private final JobRepository jobs;
    private final JobExecutionRepository executions;
    private final DeadLetterJobRepository deadLetters;
    private final RedisJobStore store;
    private final WorkerPool workers;
    private final PriorityJobBuffer buffer;

    public StatsService(JobRepository jobs, JobExecutionRepository executions, DeadLetterJobRepository deadLetters,
                        RedisJobStore store, WorkerPool workers, PriorityJobBuffer buffer) {
        this.jobs = jobs;
        this.executions = executions;
        this.deadLetters = deadLetters;
        this.store = store;
        this.workers = workers;
        this.buffer = buffer;
    }

    public Map<String, Object> compute() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", jobs.countByCronIsNull());
        out.put("pending", jobs.countByStatusAndCronIsNull(JobStatus.PENDING));
        out.put("running", jobs.countByStatusAndCronIsNull(JobStatus.RUNNING));
        out.put("success", jobs.countByStatusAndCronIsNull(JobStatus.SUCCESS));
        out.put("retrying", jobs.countByStatusAndCronIsNull(JobStatus.RETRYING));
        out.put("failed", jobs.countByStatusAndCronIsNull(JobStatus.FAILED));
        out.put("deadLettered", deadLetters.count());
        out.put("recurringTemplates", jobs.countByCronIsNotNull());

        Map<String, Long> byPriority = new LinkedHashMap<>();
        for (Priority p : Priority.values()) byPriority.put(p.name(), jobs.countByPriorityAndCronIsNull(p));
        out.put("byPriority", byPriority);

        long everFailed = executions.countJobsThatEverFailed();
        long recovered = executions.countJobsThatFailedWithFinalStatus(JobStatus.SUCCESS);
        out.put("jobsThatFailedAtLeastOnce", everFailed);
        out.put("jobsRecoveredByRetry", recovered);
        out.put("retrySuccessRatePercent", everFailed == 0 ? null : Math.round(recovered * 1000.0 / everFailed) / 10.0);

        List<Map<String, Object>> series = store.series(15);
        out.put("perMinute", series);
        out.put("currentMinuteProcessed", series.get(series.size() - 1).get("processed"));
        out.put("lastFullMinuteProcessed", series.get(series.size() - 2).get("processed"));

        out.put("activeWorkers", workers.activeWorkers());
        out.put("workerPoolSize", workers.poolSize());
        out.put("bufferedMessages", buffer.size());
        out.put("delayedQueueSize", store.delayedCount());
        return out;
    }
}
