package com.capstone.scheduler;

import static org.junit.jupiter.api.Assertions.*;

import com.capstone.scheduler.dto.JobMessage;
import com.capstone.scheduler.kafka.JobPublisher;
import com.capstone.scheduler.model.Priority;
import com.capstone.scheduler.worker.PriorityJobBuffer;
import org.junit.jupiter.api.Test;

class PriorityBufferTest {
    @Test
    void topicIsSelectedByPriority() {
        assertEquals("jobs.high", JobPublisher.topicFor(Priority.HIGH));
        assertEquals("jobs.medium", JobPublisher.topicFor(Priority.MEDIUM));
        assertEquals("jobs.low", JobPublisher.topicFor(Priority.LOW));
    }

    @Test
    void highPriorityIsTakenBeforeMediumAndLowRegardlessOfArrivalOrder() throws Exception {
        PriorityJobBuffer buffer = new PriorityJobBuffer();
        buffer.offer(JobPublisher.TOPIC_LOW, new JobMessage("low-1", 1, 0));
        buffer.offer(JobPublisher.TOPIC_MEDIUM, new JobMessage("med-1", 1, 0));
        buffer.offer(JobPublisher.TOPIC_LOW, new JobMessage("low-2", 1, 0));
        buffer.offer(JobPublisher.TOPIC_HIGH, new JobMessage("high-1", 1, 0));
        buffer.offer(JobPublisher.TOPIC_HIGH, new JobMessage("high-2", 1, 0));

        assertEquals("high-1", buffer.take().jobId());
        assertEquals("high-2", buffer.take().jobId());
        assertEquals("med-1", buffer.take().jobId());
        assertEquals("low-1", buffer.take().jobId());
        assertEquals("low-2", buffer.take().jobId());
    }
}
