package com.capstone.scheduler.worker;

import com.capstone.scheduler.config.SchedulerProperties;
import com.capstone.scheduler.model.Job;
import com.capstone.scheduler.model.JobType;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class SendEmailHandler implements JobHandler {
    private final SchedulerProperties props;

    public SendEmailHandler(SchedulerProperties props) {
        this.props = props;
    }

    @Override
    public JobType type() {
        return JobType.SEND_EMAIL;
    }

    @Override
    public String execute(Job job) throws Exception {
        Thread.sleep(props.getEmailWorkMs());
        if (job.isPermanentFailure()) {
            throw new SimulatedFailureException("Permanent failure: SMTP server rejected the message (demo flag)");
        }
        if (ThreadLocalRandom.current().nextDouble() < props.getEmailFailureRate()) {
            throw new SimulatedFailureException("Simulated transient SMTP failure");
        }
        return "Email sent (simulated)";
    }
}
