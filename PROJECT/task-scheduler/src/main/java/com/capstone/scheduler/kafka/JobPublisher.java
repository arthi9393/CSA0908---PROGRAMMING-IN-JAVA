package com.capstone.scheduler.kafka;

import com.capstone.scheduler.dto.JobMessage;
import com.capstone.scheduler.model.Job;
import com.capstone.scheduler.model.Priority;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class JobPublisher {
    public static final String TOPIC_HIGH = "jobs.high";
    public static final String TOPIC_MEDIUM = "jobs.medium";
    public static final String TOPIC_LOW = "jobs.low";
    public static final String TOPIC_RETRY = "jobs.retry";
    public static final String TOPIC_DLQ = "jobs.dlq";

    private static final Logger log = LoggerFactory.getLogger(JobPublisher.class);

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    public JobPublisher(KafkaTemplate<String, String> kafka, ObjectMapper mapper) {
        this.kafka = kafka;
        this.mapper = mapper;
    }

    public static String topicFor(Priority priority) {
        if (priority == null) return TOPIC_MEDIUM;
        switch (priority) {
            case HIGH: return TOPIC_HIGH;
            case LOW: return TOPIC_LOW;
            default: return TOPIC_MEDIUM;
        }
    }

    /** Publish a job to the topic matching its priority. */
    public void publish(Job job, int attempt) {
        String topic = topicFor(job.getPriority());
        send(topic, job.getId(), toJson(new JobMessage(job.getId(), attempt, 0L)));
        log.info("Published job {} (attempt {}) to {}", job.getId(), attempt, topic);
    }

    /** Publish a failed job to jobs.retry; the consumer schedules it for notBefore without blocking. */
    public void publishRetry(Job job, int nextAttempt, long notBeforeEpochMs) {
        send(TOPIC_RETRY, job.getId(), toJson(new JobMessage(job.getId(), nextAttempt, notBeforeEpochMs)));
        log.info("Published job {} to {} (next attempt {} not before {})", job.getId(), TOPIC_RETRY, nextAttempt, notBeforeEpochMs);
    }

    public void publishDlq(Job job, String reason) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("jobId", job.getId());
        body.put("type", job.getType());
        body.put("attempts", job.getAttempts());
        body.put("reason", reason);
        send(TOPIC_DLQ, job.getId(), toJson(body));
        log.warn("Published job {} to {} after {} attempts: {}", job.getId(), TOPIC_DLQ, job.getAttempts(), reason);
    }

    private String toJson(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialise Kafka message", e);
        }
    }

    private void send(String topic, String key, String value) {
        try {
            kafka.send(topic, key, value).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PublishFailedException("Interrupted while publishing to " + topic, e);
        } catch (Exception e) {
            throw new PublishFailedException("Kafka publish to " + topic + " failed: " + e.getMessage(), e);
        }
    }
}
