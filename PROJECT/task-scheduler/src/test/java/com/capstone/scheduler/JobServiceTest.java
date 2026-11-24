package com.capstone.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.capstone.scheduler.dto.JobRequest;
import com.capstone.scheduler.kafka.JobPublisher;
import com.capstone.scheduler.model.*;
import com.capstone.scheduler.service.JobService;
import com.capstone.scheduler.service.JobStateService;
import com.capstone.scheduler.service.RedisJobStore;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JobServiceTest {
    private JobStateService state;
    private JobPublisher publisher;
    private RedisJobStore store;
    private JobService service;
    private Job job;

    @BeforeEach
    void setUp() {
        state = mock(JobStateService.class);
        publisher = mock(JobPublisher.class);
        store = mock(RedisJobStore.class);
        service = new JobService(state, publisher, store);
        job = new Job();
        job.setId("job-1");
        job.setType(JobType.SEND_EMAIL);
        job.setPriority(Priority.HIGH);
        job.setStatus(JobStatus.PENDING);
        when(state.createJob(any(), any())).thenReturn(job);
    }

    private JobRequest req(Long delay, String cron) {
        return new JobRequest(JobType.SEND_EMAIL, null, Priority.HIGH, null, delay, cron, false);
    }

    @Test
    void submittedJobIsSavedAsPending() {
        Job saved = service.submit(req(null, null));
        assertEquals(JobStatus.PENDING, saved.getStatus());
        verify(state).createJob(any(), any());
    }

    @Test
    void immediateJobIsPublishedToKafkaAndNotDelayed() {
        service.submit(req(null, null));
        verify(publisher).publish(job, 1);
        verify(store, never()).scheduleDelayed(anyString(), anyLong());
    }

    @Test
    void delayedJobGoesToRedisSortedSetNotKafka() {
        long before = System.currentTimeMillis();
        service.submit(req(30L, null));
        verify(store).scheduleDelayed(eq("J:job-1:1"), longThat(score -> score >= before + 29_000));
        verify(publisher, never()).publish(any(), anyInt());
    }

    @Test
    void recurringJobIsRegisteredWithNextCronFireTime() {
        service.submit(req(null, "0/10 * * * * *"));
        verify(store).scheduleDelayed(eq("R:job-1"), longThat(score -> score > System.currentTimeMillis() - 1000));
        verify(publisher, never()).publish(any(), anyInt());
    }

    @Test
    void invalidCronIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.submit(req(null, "not a cron")));
        verify(state, never()).createJob(any(), any());
    }

    @Test
    void runAtInThePastRunsImmediately() {
        JobRequest r = new JobRequest(JobType.SEND_EMAIL, null, Priority.LOW, Instant.now().minusSeconds(60), null, null, false);
        service.submit(r);
        verify(publisher).publish(job, 1);
    }
}
