package com.capstone.scheduler.worker;

import com.capstone.scheduler.model.Job;
import com.capstone.scheduler.model.JobType;

public interface JobHandler {
    JobType type();

    /** Runs the job; returns a short result text or throws to signal failure. */
    String execute(Job job) throws Exception;
}
