package com.capstone.scheduler.kafka;

import com.capstone.scheduler.config.SchedulerProperties;
import com.capstone.scheduler.dto.JobMessage;
import com.capstone.scheduler.service.JobService;
import com.capstone.scheduler.service.RedisJobStore;
import com.capstone.scheduler.worker.PriorityJobBuffer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * One poller thread subscribed to jobs.high/medium/low/retry. Because all priorities are fetched together
 * and handed to the PriorityJobBuffer, workers always pick HIGH first.
 * - jobs.retry records are NOT executed here: they are put into the Redis delayed_jobs sorted set for their
 *   notBefore time (2s/4s/8s), so no Kafka thread ever sleeps.
 * - Offsets are committed after the record is buffered/scheduled (at-least-once); duplicates are made harmless
 *   by the Redis lock plus the attempt-number check in JobStateService.beginAttempt.
 */
@Component
public class PriorityJobConsumer {
    private static final Logger log = LoggerFactory.getLogger(PriorityJobConsumer.class);

    private final SchedulerProperties props;
    private final PriorityJobBuffer buffer;
    private final RedisJobStore store;
    private final ObjectMapper mapper;
    private final String bootstrapServers;
    private volatile boolean running = true;
    private Thread thread;

    public PriorityJobConsumer(SchedulerProperties props, PriorityJobBuffer buffer, RedisJobStore store,
                               ObjectMapper mapper, @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        this.props = props;
        this.buffer = buffer;
        this.store = store;
        this.mapper = mapper;
        this.bootstrapServers = bootstrapServers;
    }

    @PostConstruct
    void start() {
        thread = new Thread(this::run, "kafka-poller");
        thread.setDaemon(true);
        thread.start();
    }

    @PreDestroy
    void stop() {
        running = false;
        if (thread != null) thread.interrupt();
    }

    private Properties consumerProps() {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        p.put(ConsumerConfig.GROUP_ID_CONFIG, props.getConsumerGroup());
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        p.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "200");
        return p;
    }

    private void run() {
        while (running) {
            try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps())) {
                consumer.subscribe(List.of(JobPublisher.TOPIC_HIGH, JobPublisher.TOPIC_MEDIUM,
                        JobPublisher.TOPIC_LOW, JobPublisher.TOPIC_RETRY));
                log.info("Kafka poller subscribed to priority and retry topics");
                boolean paused = false;
                int limit = props.getConsumerBufferLimit();
                while (running) {
                    int size = buffer.size();
                    if (!paused && size >= limit) {
                        consumer.pause(consumer.assignment());
                        paused = true;
                    } else if (paused && size < limit / 2) {
                        consumer.resume(consumer.paused());
                        paused = false;
                    }
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(200));
                    for (ConsumerRecord<String, String> record : records) handle(record);
                    if (!records.isEmpty()) consumer.commitSync();
                }
            } catch (Exception e) {
                if (!running) return;
                log.error("Kafka poller error ({}), reconnecting in 2s", e.toString());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    return;
                }
            }
        }
    }

    private void handle(ConsumerRecord<String, String> record) {
        try {
            JobMessage msg = mapper.readValue(record.value(), JobMessage.class);
            if (JobPublisher.TOPIC_RETRY.equals(record.topic())) {
                store.scheduleDelayed(JobService.delayedMember(msg.jobId(), msg.attempt()), msg.notBeforeEpochMs());
                log.info("Retry for job {} attempt {} scheduled in delayed_jobs", msg.jobId(), msg.attempt());
            } else {
                buffer.offer(record.topic(), msg);
            }
        } catch (Exception e) {
            log.error("Dropping unreadable record from {}: {}", record.topic(), e.getMessage());
        }
    }
}
