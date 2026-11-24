package com.capstone.scheduler.service;

import com.capstone.scheduler.config.SchedulerProperties;
import org.springframework.stereotype.Component;

/** Exponential backoff: retry 1 = 2s, retry 2 = 4s, retry 3 = 8s (base * 2^(n-1)). */
@Component
public class BackoffPolicy {
    private final SchedulerProperties props;

    public BackoffPolicy(SchedulerProperties props) {
        this.props = props;
    }

    public long delaySeconds(int retryNumber) {
        if (retryNumber < 1) throw new IllegalArgumentException("retryNumber starts at 1");
        return props.getRetryBaseDelaySeconds() * (1L << (retryNumber - 1));
    }
}
