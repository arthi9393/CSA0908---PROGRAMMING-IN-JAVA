package com.capstone.scheduler.worker;

import com.capstone.scheduler.config.SchedulerProperties;
import com.capstone.scheduler.dto.JobMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Fixed ExecutorService; each thread loops: take highest-priority message, process it. */
@Component
public class WorkerPool {
    private static final Logger log = LoggerFactory.getLogger(WorkerPool.class);

    private final SchedulerProperties props;
    private final PriorityJobBuffer buffer;
    private final JobProcessor processor;
    private final AtomicInteger active = new AtomicInteger();
    private ExecutorService executor;

    public WorkerPool(SchedulerProperties props, PriorityJobBuffer buffer, JobProcessor processor) {
        this.props = props;
        this.buffer = buffer;
        this.processor = processor;
    }

    @PostConstruct
    void start() {
        final AtomicInteger n = new AtomicInteger();
        ThreadFactory tf = r -> new Thread(r, "worker-" + n.incrementAndGet());
        int size = props.getWorkerPoolSize();
        executor = Executors.newFixedThreadPool(size, tf);
        for (int i = 0; i < size; i++) executor.submit(this::loop);
        log.info("Worker pool started with {} threads", size);
    }

    private void loop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                JobMessage msg = buffer.take();
                active.incrementAndGet();
                try {
                    processor.process(msg);
                } catch (Exception e) {
                    log.error("Unhandled error processing {}: {}", msg, e.toString());
                } finally {
                    active.decrementAndGet();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public int activeWorkers() {
        return active.get();
    }

    public int poolSize() {
        return props.getWorkerPoolSize();
    }

    @PreDestroy
    void stop() {
        if (executor != null) executor.shutdownNow();
    }
}
