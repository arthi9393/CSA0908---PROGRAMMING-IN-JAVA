package com.capstone.scheduler.web;

import com.capstone.scheduler.dto.JobRequest;
import com.capstone.scheduler.dto.Views.*;
import com.capstone.scheduler.model.Job;
import com.capstone.scheduler.model.JobStatus;
import com.capstone.scheduler.model.Priority;
import com.capstone.scheduler.repo.DeadLetterJobRepository;
import com.capstone.scheduler.repo.JobExecutionRepository;
import com.capstone.scheduler.repo.JobRepository;
import com.capstone.scheduler.service.DemoService;
import com.capstone.scheduler.service.JobService;
import com.capstone.scheduler.service.StatsService;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;

@RestController
@RequestMapping("/api")
public class JobController {
    private final JobService jobService;
    private final JobRepository jobs;
    private final JobExecutionRepository executions;
    private final DeadLetterJobRepository deadLetters;
    private final StatsService stats;
    private final DemoService demo;

    public JobController(JobService jobService, JobRepository jobs, JobExecutionRepository executions,
                         DeadLetterJobRepository deadLetters, StatsService stats, DemoService demo) {
        this.jobService = jobService;
        this.jobs = jobs;
        this.executions = executions;
        this.deadLetters = deadLetters;
        this.stats = stats;
        this.demo = demo;
    }

    @PostMapping("/jobs")
    public ResponseEntity<JobView> submit(@Valid @RequestBody JobRequest request) {
        Job job = jobService.submit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(JobView.of(job));
    }

    @GetMapping("/jobs")
    public List<JobView> list(@RequestParam(required = false) JobStatus status,
                              @RequestParam(required = false) Priority priority,
                              @RequestParam(required = false) String q,
                              @RequestParam(defaultValue = "false") boolean recurring,
                              @RequestParam(defaultValue = "100") int size) {
        Specification<Job> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(recurring ? cb.isNotNull(root.get("cron")) : cb.isNull(root.get("cron")));
            if (status != null) ps.add(cb.equal(root.get("status"), status));
            if (priority != null) ps.add(cb.equal(root.get("priority"), priority));
            if (q != null && !q.isBlank()) ps.add(cb.like(cb.lower(root.<String>get("id")), "%" + q.trim().toLowerCase() + "%"));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        int limit = Math.max(1, Math.min(size, 500));
        return jobs.findAll(spec, PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent().stream().map(JobView::of).toList();
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobDetailView> get(@PathVariable String id) {
        return jobs.findById(id).map(job -> {
            List<ExecutionView> history = executions.findByJobIdOrderByAttemptNoAsc(id).stream().map(ExecutionView::of).toList();
            return ResponseEntity.ok(new JobDetailView(JobView.of(job), history));
        }).orElseThrow(() -> new java.util.NoSuchElementException("Job not found: " + id));
    }

    @PostMapping("/jobs/{id}/retry")
    public ResponseEntity<JobView> retry(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(JobView.of(jobService.retryDeadLetter(id)));
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return stats.compute();
    }

    @GetMapping("/dlq")
    public List<DeadLetterView> deadLetters() {
        return deadLetters.findAllWithJob().stream().map(DeadLetterView::of).toList();
    }

    @PostMapping("/demo/batch")
    public ResponseEntity<Map<String, Object>> batch(@RequestParam(defaultValue = "100") int count) {
        int n = demo.submitMixedBatch(count);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("submitted", n));
    }

    @PostMapping("/demo/permanent-failure")
    public ResponseEntity<JobView> permanentFailure() {
        return ResponseEntity.status(HttpStatus.CREATED).body(JobView.of(demo.submitPermanentFailure()));
    }
}
