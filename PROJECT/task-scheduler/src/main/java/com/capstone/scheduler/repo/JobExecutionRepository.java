package com.capstone.scheduler.repo;

import com.capstone.scheduler.model.JobExecution;
import com.capstone.scheduler.model.JobStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {
    List<JobExecution> findByJobIdOrderByAttemptNoAsc(String jobId);

    Optional<JobExecution> findByJobIdAndAttemptNo(String jobId, int attemptNo);

    @Query("select count(distinct e.job.id) from JobExecution e where e.result = 'FAILURE'")
    long countJobsThatEverFailed();

    @Query("select count(distinct e.job.id) from JobExecution e where e.result = 'FAILURE' and e.job.status = :status")
    long countJobsThatFailedWithFinalStatus(@Param("status") JobStatus status);
}
