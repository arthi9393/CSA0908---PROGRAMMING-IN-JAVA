package com.capstone.scheduler.dto;

import com.capstone.scheduler.model.JobType;
import com.capstone.scheduler.model.Priority;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record JobRequest(
        @NotNull(message = "type is required (SEND_EMAIL or GENERATE_INVOICE)") JobType type,
        JsonNode payload,
        Priority priority,
        Instant runAt,
        @Min(0) Long delaySeconds,
        String cron,
        Boolean permanentFailure) {
}
