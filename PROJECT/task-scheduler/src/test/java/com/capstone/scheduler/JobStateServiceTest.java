package com.capstone.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.capstone.scheduler.config.SchedulerProperties;
import com.capstone.scheduler.model.*;
import com.capstone.scheduler.repo.*;
import com.capstone.scheduler.service.BackoffPolicy;
import com.capstone.scheduler.service.JobStateService;
import com.capstone.scheduler.service.JobStateService.FailureOutcome;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JobStateServiceTest {
    private JobRepository jobs;
    private JobExecutionRepository executions;
    private DeadLetterJobRepository deadLetters;
    private JobStateService service;
    private Job job;
    private JobExecution exec;

    @BeforeEach
    void setUp() {
        jobs = mock(JobRepository.class);
        executions = mock(JobExecutionRepository.class);
        deadLetters = mock(DeadLetterJobRepository.class);
        SchedulerProperties props = new SchedulerProperties();
        service = new JobStateService(jobs, executions, deadLetters, new BackoffPolicy(props), props);
        job = new Job();
        job.setId("j1");
        job.setType(JobType.SEND_EMAIL);
        job.setPriority(Priority.MEDIUM);
        job.setStatus(JobStatus.PENDING);
        exec = new JobExecution();
        exec.setJob(job);
        when(jobs.findById("j1")).thenReturn(Optional.of(job));
        when(executions.findByJobIdAndAttemptNo(eq("j1"), anyInt())).thenReturn(Optional.of(exec));
    }

    @Test
    void beginAttemptMarksRunningAndRecordsExecution() {
        Optional<Job> started = service.beginAttempt("j1", 1, "worker-1");
        assertTrue(started.isPresent());
        assertEquals(JobStatus.RUNNING, job.getStatus());
        assertEquals(1, job.getAttempts());
        verify(executions).saveAndFlush(any(JobExecution.class));
    }

    @Test
    void duplicateOrStaleMessageIsIgnored() {
        job.setAttempts(2);
        assertTrue(service.beginAttempt("j1", 1, "w").isEmpty());
        job.setStatus(JobStatus.SUCCESS);
        assertTrue(service.beginAttempt("j1", 3, "w").isEmpty());
        verify(executions, never()).saveAndFlush(any(JobExecution.class));
    }

    @Test
    void successUpdatesStatusAndExecution() {
        service.completeSuccess("j1", 1, "ok");
        assertEquals(JobStatus.SUCCESS, job.getStatus());
        assertEquals("SUCCESS", exec.getResult());
        assertNotNull(exec.getFinishedAt());
    }

    @Test
    void failuresRetryWithTwoFourEightSecondDelays() {
        long[] expected = {2, 4, 8};
        for (int retry = 0; retry < 3; retry++) {
            FailureOutcome o = service.recordFailure("j1", retry + 1, "boom");
            assertTrue(o.retry());
            assertEquals(expected[retry], o.delaySeconds());
            assertEquals(retry + 2, o.nextAttempt());
            assertEquals(JobStatus.RETRYING, job.getStatus());
            assertEquals(retry + 1, job.getRetryCount());
        }
        verify(deadLetters, never()).save(any());
    }

    @Test
    void fourthFailureDeadLettersTheJob() {
        job.setRetryCount(3);
        FailureOutcome o = service.recordFailure("j1", 4, "still failing");
        assertFalse(o.retry());
        assertEquals(JobStatus.FAILED, job.getStatus());
        verify(deadLetters).save(any(DeadLetterJob.class));
        assertEquals("FAILURE", exec.getResult());
    }

    @Test
    void manualRetryOnlyAllowedForFailedJobs() {
        assertThrows(IllegalStateException.class, () -> service.resetForManualRetry("j1"));
        job.setStatus(JobStatus.FAILED);
        job.setRetryCount(3);
        Job reset = service.resetForManualRetry("j1");
        assertEquals(JobStatus.PENDING, reset.getStatus());
        assertEquals(0, reset.getRetryCount());
        verify(deadLetters).deleteByJobId("j1");
    }
}
