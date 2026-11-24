package com.capstone.scheduler;

import static org.junit.jupiter.api.Assertions.*;

import com.capstone.scheduler.config.SchedulerProperties;
import com.capstone.scheduler.service.BackoffPolicy;
import org.junit.jupiter.api.Test;

class BackoffPolicyTest {
    private final BackoffPolicy policy = new BackoffPolicy(new SchedulerProperties());

    @Test
    void exponentialBackoffIsTwoFourEightSeconds() {
        assertEquals(2, policy.delaySeconds(1));
        assertEquals(4, policy.delaySeconds(2));
        assertEquals(8, policy.delaySeconds(3));
    }

    @Test
    void retryNumberMustStartAtOne() {
        assertThrows(IllegalArgumentException.class, () -> policy.delaySeconds(0));
    }
}
