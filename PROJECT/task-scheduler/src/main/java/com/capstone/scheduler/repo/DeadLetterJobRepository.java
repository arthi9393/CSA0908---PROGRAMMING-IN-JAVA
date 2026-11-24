package com.capstone.scheduler.repo;

import com.capstone.scheduler.model.DeadLetterJob;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeadLetterJobRepository extends JpaRepository<DeadLetterJob, Long> {
    @Query("select d from DeadLetterJob d join fetch d.job order by d.failedAt desc")
    List<DeadLetterJob> findAllWithJob();

    void deleteByJobId(String jobId);
}
