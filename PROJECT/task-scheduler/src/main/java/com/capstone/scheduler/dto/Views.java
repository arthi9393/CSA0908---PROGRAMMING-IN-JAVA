package com.capstone.scheduler.dto;

import com.capstone.scheduler.model.*;
import java.time.Instant;
import java.util.List;

public final class Views {
    private Views() { }

    public record JobView(String id, JobType type, String payload, Priority priority, JobStatus status,
                          int attempts, int retryCount, Instant runAt, String cron, boolean permanentFailure,
                          String lastError, String parentJobId, Instant createdAt, Instant updatedAt) {
        public static JobView of(Job j) {
            return new JobView(j.getId(), j.getType(), j.getPayload(), j.getPriority(), j.getStatus(),
                    j.getAttempts(), j.getRetryCount(), j.getRunAt(), j.getCron(), j.isPermanentFailure(),
                    j.getLastError(), j.getParentJobId(), j.getCreatedAt(), j.getUpdatedAt());
        }
    }

    public record ExecutionView(Long id, int attemptNo, Instant startedAt, Instant finishedAt,
                                String result, String error, String worker) {
        public static ExecutionView of(JobExecution e) {
            return new ExecutionView(e.getId(), e.getAttemptNo(), e.getStartedAt(), e.getFinishedAt(),
                    e.getResult(), e.getError(), e.getWorkerName());
        }
    }

    public record JobDetailView(JobView job, List<ExecutionView> executions) { }

    public record DeadLetterView(Long id, String jobId, JobType type, Priority priority, String reason,
                                 int attempts, Instant failedAt) {
        public static DeadLetterView of(DeadLetterJob d) {
            Job j = d.getJob();
            return new DeadLetterView(d.getId(), j.getId(), j.getType(), j.getPriority(), d.getReason(),
                    d.getAttempts(), d.getFailedAt());
        }
    }
}
