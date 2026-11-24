package com.capstone.scheduler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.capstone.scheduler.kafka.JobPublisher;
import com.capstone.scheduler.model.*;
import com.capstone.scheduler.service.DelayedJobScheduler;
import com.capstone.scheduler.service.JobStateService;
import com.capstone.scheduler.service.RedisJobStore;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DelayedJobSchedulerTest {
    private RedisJobStore store;
    private JobStateService state;
    private JobPublisher publisher;
    private DelayedJobScheduler scheduler;

    @BeforeEach
    void setUp() {
        store = mock(RedisJobStore.class);
        state = mock(JobStateService.class);
        publisher = mock(JobPublisher.class);
        scheduler = new DelayedJobScheduler(store, state, publisher);
    }

    @Test
    void dueDelayedJobIsPublishedToKafka() {
        Job job = new Job();
        job.setId("abc");
        job.setPriority(Priority.LOW);
        when(store.claimDue(anyLong(), anyInt())).thenReturn(List.of("J:abc:3"));
        when(state.find("abc")).thenReturn(Optional.of(job));
        scheduler.processDue(System.currentTimeMillis());
        verify(publisher).publish(job, 3);
    }

    @Test
    void recurringTemplateSpawnsChildAndRearmsNextRun() {
        Job template = new Job();
        template.setId("tpl");
        template.setCron("0/10 * * * * *");
        Job child = new Job();
        child.setId("child");
        when(store.claimDue(anyLong(), anyInt())).thenReturn(List.of("R:tpl"));
        when(state.find("tpl")).thenReturn(Optional.of(template));
        when(state.createChild(template)).thenReturn(child);
        scheduler.processDue(System.currentTimeMillis());
        verify(store).scheduleDelayed(eq("R:tpl"), longThat(t -> t > System.currentTimeMillis() - 1000));
        verify(publisher).publish(child, 1);
    }

    @Test
    void nothingDueMeansNothingPublished() {
        when(store.claimDue(anyLong(), anyInt())).thenReturn(List.of());
        scheduler.processDue(System.currentTimeMillis());
        verifyNoInteractions(publisher);
    }
}
