package com.capstone.scheduler.worker;

import com.capstone.scheduler.config.SchedulerProperties;
import com.capstone.scheduler.model.Job;
import com.capstone.scheduler.model.JobType;
import org.springframework.stereotype.Component;

@Component
public class GenerateInvoiceHandler implements JobHandler {
    private final SchedulerProperties props;

    public GenerateInvoiceHandler(SchedulerProperties props) {
        this.props = props;
    }

    @Override
    public JobType type() {
        return JobType.GENERATE_INVOICE;
    }

    @Override
    public String execute(Job job) throws Exception {
        Thread.sleep(props.getInvoiceWorkMs());
        if (job.isPermanentFailure()) {
            throw new SimulatedFailureException("Permanent failure: invoice template missing (demo flag)");
        }
        return "Invoice INV-" + job.getId().substring(0, 8).toUpperCase() + " generated (simulated)";
    }
}
