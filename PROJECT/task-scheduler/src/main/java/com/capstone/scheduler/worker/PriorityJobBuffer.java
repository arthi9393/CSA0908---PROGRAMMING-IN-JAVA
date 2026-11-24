package com.capstone.scheduler.worker;

import com.capstone.scheduler.dto.JobMessage;
import com.capstone.scheduler.kafka.JobPublisher;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * In-memory hand-off between the Kafka poller and the worker threads. Workers always take the
 * highest-priority message first (HIGH, then MEDIUM, then LOW; FIFO inside a priority). This is what
 * makes consumption priority-aware; separate topics alone would not.
 */
@Component
public class PriorityJobBuffer {

    public static final class Ranked implements Comparable<Ranked> {
        final int rank;
        final long seq;
        final JobMessage message;

        Ranked(int rank, long seq, JobMessage message) {
            this.rank = rank;
            this.seq = seq;
            this.message = message;
        }

        @Override
        public int compareTo(Ranked o) {
            if (rank != o.rank) return Integer.compare(rank, o.rank);
            return Long.compare(seq, o.seq);
        }
    }

    private final PriorityBlockingQueue<Ranked> queue = new PriorityBlockingQueue<>();
    private final AtomicLong sequence = new AtomicLong();

    public static int rankOfTopic(String topic) {
        if (JobPublisher.TOPIC_HIGH.equals(topic)) return 0;
        if (JobPublisher.TOPIC_MEDIUM.equals(topic)) return 1;
        return 2;
    }

    public void offer(String topic, JobMessage message) {
        queue.put(new Ranked(rankOfTopic(topic), sequence.incrementAndGet(), message));
    }

    public JobMessage take() throws InterruptedException {
        return queue.take().message;
    }

    public int size() {
        return queue.size();
    }
}
