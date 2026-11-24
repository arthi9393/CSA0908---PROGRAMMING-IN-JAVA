package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.JobRequest;
import com.capstone.scheduler.model.Job;
import com.capstone.scheduler.model.JobType;
import com.capstone.scheduler.model.Priority;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

/** Helpers for the live demo: a mixed batch and a job that can never succeed. */
@Service
public class DemoService {
    private final JobService jobService;

    public DemoService(JobService jobService) {
        this.jobService = jobService;
    }

    public int submitMixedBatch(int count) {
        int n = Math.max(1, Math.min(count, 1000));
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < n; i++) {
            int p = r.nextInt(10);
            Priority priority = p < 2 ? Priority.HIGH : (p < 6 ? Priority.MEDIUM : Priority.LOW);
            JobType type = r.nextInt(10) < 6 ? JobType.SEND_EMAIL : JobType.GENERATE_INVOICE;
            jobService.submit(new JobRequest(type, null, priority, null, null, null, false));
        }
        return n;
    }

    public Job submitPermanentFailure() {
        return jobService.submit(new JobRequest(JobType.SEND_EMAIL, null, Priority.HIGH, null, null, null, true));
    }
}
