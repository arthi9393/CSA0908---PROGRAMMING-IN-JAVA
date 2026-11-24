package com.capstone.scheduler;

import static org.junit.jupiter.api.Assertions.*;

import com.capstone.scheduler.config.SchedulerProperties;
import com.capstone.scheduler.model.Job;
import com.capstone.scheduler.worker.GenerateInvoiceHandler;
import com.capstone.scheduler.worker.SendEmailHandler;
import com.capstone.scheduler.worker.SimulatedFailureException;
import org.junit.jupiter.api.Test;

class HandlerTest {
    private SchedulerProperties props(double failureRate) {
        SchedulerProperties p = new SchedulerProperties();
        p.setEmailWorkMs(0);
        p.setInvoiceWorkMs(0);
        p.setEmailFailureRate(failureRate);
        return p;
    }

    private Job job(boolean permanent) {
        Job j = new Job();
        j.setId("12345678-aaaa");
        j.setPermanentFailure(permanent);
        return j;
    }

    @Test
    void emailSucceedsWhenFailureRateIsZero() throws Exception {
        assertNotNull(new SendEmailHandler(props(0.0)).execute(job(false)));
    }

    @Test
    void emailAlwaysFailsWhenFailureRateIsOne() {
        assertThrows(SimulatedFailureException.class, () -> new SendEmailHandler(props(1.0)).execute(job(false)));
    }

    @Test
    void permanentFailureFlagAlwaysFails() {
        assertThrows(SimulatedFailureException.class, () -> new SendEmailHandler(props(0.0)).execute(job(true)));
        assertThrows(SimulatedFailureException.class, () -> new GenerateInvoiceHandler(props(0.0)).execute(job(true)));
    }

    @Test
    void invoiceSucceeds() throws Exception {
        assertTrue(new GenerateInvoiceHandler(props(0.0)).execute(job(false)).contains("INV-12345678"));
    }
}
