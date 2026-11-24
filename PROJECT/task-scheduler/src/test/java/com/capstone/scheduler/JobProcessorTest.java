package com.capstone.scheduler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.capstone.scheduler.config.SchedulerProperties;
import com.capstone.scheduler.dto.JobMessage;
import com.capstone.scheduler.kafka.JobPublisher;
import com.capstone.scheduler.model.*;
import com.capstone.scheduler.service.JobStateService;
import com.capstone.scheduler.service.JobStateService.FailureOutcome;
import com.capstone.scheduler.service.RedisJobStore;
import com.capstone.scheduler.worker.JobHandler;
import com.capstone.scheduler.worker.JobProcessor;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JobProcessorTest {
    private JobStateService state;
    private RedisJobStore store;
    private JobPublisher publisher;
    private JobHandler handler;
    private JobProcessor processor;
    private Job job;

    @BeforeEach
    void setUp() throws Exception {
        state = mock(JobStateService.class);
        store = mock(RedisJobStore.class);
        publisher = mock(JobPublisher.class);
        handler = mock(JobHandler.class);
        when(handler.type()).thenReturn(JobType.SEND_EMAIL);
        processor = new JobProcessor(state, store, publisher, new SchedulerProperties(), List.of(handler));
        job = new Job();
        job.setId("j1");
        job.setType(JobType.SEND_EMAIL);
        job.setPriority(Priority.HIGH);
        when(store.tryLock(eq("j1"), anyString(), anyLong())).thenReturn(true);
        when(state.beginAttempt("j1", 1, Thread.currentThread().getName())).thenReturn(Optional.of(job));
    }

    @Test
    void successfulExecutionMarksSuccessAndReleasesLock() throws Exception {
        when(handler.execute(job)).thenReturn("done");
        processor.process(new JobMessage("j1", 1, 0));
        verify(state).completeSuccess("j1", 1, "done");
        verify(store).recordProcessed(true);
        verify(store).unlock(eq("j1"), anyString());
        verifyNoInteractions(publisher);
    }

    @Test
    void failureWithRetriesLeftPublishesToRetryTopicWithBackoff() throws Exception {
        when(handler.execute(job)).thenThrow(new RuntimeException("smtp down"));
        when(state.recordFailure("j1", 1, "smtp down")).thenReturn(new FailureOutcome(true, 2, 2, job, "smtp down"));
        long before = System.currentTimeMillis();
        processor.process(new JobMessage("j1", 1, 0));
        verify(publisher).publishRetry(eq(job), eq(2), longThat(t -> t >= before + 2000 && t < before + 4000));
        verify(publisher, never()).publishDlq(any(), anyString());
        verify(store).unlock(eq("j1"), anyString());
    }

    @Test
    void exhaustedRetriesPublishToDeadLetterQueue() throws Exception {
        when(handler.execute(job)).thenThrow(new RuntimeException("permanent"));
        when(state.recordFailure(eq("j1"), eq(1), anyString())).thenReturn(new FailureOutcome(false, 0, 0, job, "permanent"));
        processor.process(new JobMessage("j1", 1, 0));
        verify(publisher).publishDlq(job, "permanent");
        verify(publisher, never()).publishRetry(any(), anyInt(), anyLong());
    }

    @Test
    void jobLockedByAnotherWorkerIsNotExecuted() throws Exception {
        when(store.tryLock(eq("j1"), anyString(), anyLong())).thenReturn(false);
        processor.process(new JobMessage("j1", 1, 0));
        verifyNoInteractions(state);
        verify(handler, never()).execute(any());
        verify(store, never()).unlock(anyString(), anyString());
    }

    @Test
    void duplicateDeliveryOfFinishedJobIsSkipped() throws Exception {
        when(state.beginAttempt("j1", 1, Thread.currentThread().getName())).thenReturn(Optional.empty());
        processor.process(new JobMessage("j1", 1, 0));
        verify(handler, never()).execute(any());
        verify(store).unlock(eq("j1"), anyString());
    }
}
