package com.capstone.scheduler.config;

import com.capstone.scheduler.kafka.JobPublisher;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {
    @Bean public NewTopic jobsHigh() { return topic(JobPublisher.TOPIC_HIGH); }
    @Bean public NewTopic jobsMedium() { return topic(JobPublisher.TOPIC_MEDIUM); }
    @Bean public NewTopic jobsLow() { return topic(JobPublisher.TOPIC_LOW); }
    @Bean public NewTopic jobsRetry() { return topic(JobPublisher.TOPIC_RETRY); }
    @Bean public NewTopic jobsDlq() { return topic(JobPublisher.TOPIC_DLQ); }

    private static NewTopic topic(String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }
}
