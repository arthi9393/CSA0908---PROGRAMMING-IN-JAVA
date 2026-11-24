package com.capstone.scheduler.dto;

/** Kafka message body. attempt is the 1-based execution number this message asks for. */
public record JobMessage(String jobId, int attempt, long notBeforeEpochMs) {
}
