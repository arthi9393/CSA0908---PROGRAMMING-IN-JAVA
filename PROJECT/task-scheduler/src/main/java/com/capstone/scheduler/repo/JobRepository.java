package com.capstone.scheduler.repo;

import com.capstone.scheduler.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JobRepository extends JpaRepository<Job, String>, JpaSpecificationExecutor<Job> {
    long countByCronIsNull();
    long countByCronIsNotNull();
    long countByStatusAndCronIsNull(JobStatus status);
    long countByPriorityAndCronIsNull(Priority priority);
}
