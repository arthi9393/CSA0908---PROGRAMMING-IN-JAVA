# Distributed Task Scheduler with Retry, Priority Queues and Monitoring Dashboard

Java 17 · Spring Boot 3.2 · Kafka · Redis · PostgreSQL · Thymeleaf · Docker Compose · JUnit 5 · JMeter

## Run it (4 lines)
```bash
cp .env.example .env        # optional; defaults work without it
docker compose up --build   # starts app + Kafka + Redis + PostgreSQL
# wait until the log shows "Started SchedulerApplication", then open:
#   http://localhost:8080
```
Logs: `docker compose logs -f app` and `./logs/scheduler.log` (mounted from the container).
Stop: `docker compose down` (add `-v` to wipe the database).

## Flow (same 6 steps as the slides)
1. `POST /api/jobs` → 2. saved in PostgreSQL as **PENDING** → 3. published to `jobs.high|medium|low`
→ 4. worker consumes and executes (Redis lock) → 5. failure → `jobs.retry` → delayed (2s/4s/8s) → re-run; after 3 failed retries → `jobs.dlq`
→ 6. status updated in PostgreSQL (`RUNNING`, `SUCCESS`, `RETRYING`, `FAILED`).

## Design decisions
| Topic | Implementation |
|---|---|
| Priority-aware consumption | One Kafka poller reads `jobs.high/medium/low` together into an in-memory `PriorityBlockingQueue`; worker threads always take HIGH, then MEDIUM, then LOW (FIFO inside a level). Priority applies among jobs waiting in that buffer; a job already running is never pre-empted, and sustained HIGH load can starve LOW (no aging). |
| Worker pool | `ExecutorService` fixed pool, size `WORKER_POOL_SIZE` (default 4). |
| No duplicate runs | Redis `SET locks:job:{id} token NX PX 30000`, released with a compare-and-delete Lua script. Also: unique `(job_id, attempt_no)` constraint and an "attempt must equal attempts+1" check make Kafka redelivery harmless. |
| Delayed jobs | Redis sorted set `delayed_jobs` (score = run time in epoch ms). `@Scheduled` poller (every `SCHEDULER_POLL_MS`, default 500 ms) claims due entries with `ZREM` and publishes them to Kafka. |
| Recurring jobs | Submit with a Spring **6-field** cron, e.g. `0/10 * * * * *`. The saved job is a template (hidden from the job table and counts); each firing creates a child job. |
| Retry | Failure → status `RETRYING` → message to `jobs.retry` → consumer puts it into `delayed_jobs` at now+2s / 4s / 8s → scheduler republishes. No thread ever sleeps. Max 3 retries (4 executions total). |
| DLQ | After the 3rd retry fails: row in `dead_letter_jobs`, message to `jobs.dlq`, status `FAILED`. `POST /api/jobs/{id}/retry` re-runs it (clears the demo "permanent failure" flag, keeps history). |
| Rate counters | Redis keys `stats:processed|success|failure:{epochMinute}` (2 h TTL) feed the per-minute charts. |
| Delivery semantics | Offsets are committed after a message is buffered/scheduled (at-least-once into the buffer). Jobs sitting in the in-memory buffer when the app is killed are **not** automatically re-queued; a crash mid-run leaves the job `RUNNING`. |

## REST API
| Method & path | Purpose | Success |
|---|---|---|
| `POST /api/jobs` | Submit a job | 201 |
| `GET /api/jobs?status=&priority=&q=&size=` | List jobs (filters optional; `recurring=true` lists cron templates) | 200 |
| `GET /api/jobs/{id}` | Job + execution history | 200 / 404 |
| `POST /api/jobs/{id}/retry` | Re-run a dead-lettered job | 202 / 404 / 409 |
| `GET /api/stats` | Counts (PostgreSQL) + per-minute counters (Redis) | 200 |
| `GET /api/dlq` | Dead-lettered jobs with reason | 200 |
| `POST /api/demo/batch?count=100` | Mixed-priority demo batch | 202 |
| `POST /api/demo/permanent-failure` | Job that always fails | 201 |

Errors return `{timestamp,status,error,message}` with 400 (validation), 404, 409, 503 (Kafka unavailable) or 500.

Request body: `type` (SEND_EMAIL | GENERATE_INVOICE, required), `priority` (HIGH|MEDIUM|LOW, default MEDIUM), `payload` (any JSON), `delaySeconds` or `runAt` (ISO-8601), `cron`, `permanentFailure`.
```json
{"type":"SEND_EMAIL","priority":"HIGH","payload":{"to":"user@example.com","subject":"Hello"}}
{"type":"GENERATE_INVOICE","priority":"MEDIUM","delaySeconds":20,"payload":{"invoiceNo":1001}}
{"type":"SEND_EMAIL","priority":"LOW","cron":"0/10 * * * * *","payload":{"to":"report@example.com"}}
{"type":"SEND_EMAIL","priority":"HIGH","permanentFailure":true,"payload":{"to":"nobody@example.com"}}
```
Postman collection: `postman/task-scheduler.postman_collection.json`.

## Demo script
1. `docker compose up --build`, open http://localhost:8080.
2. Click **Submit 100 mixed jobs** (HIGH 20 %, MEDIUM 40 %, LOW 40 %). Watch Running (up to 4), Pending and the Priority chart; open several jobs and compare start times: after the first few, HIGH jobs start before MEDIUM and LOW.
3. SEND_EMAIL fails ~30 % (`EMAIL_FAILURE_RATE`): jobs show **RETRYING**, then usually **SUCCESS**. Open **Details**: the "Gap since previous attempt" column shows the 2 s, 4 s, 8 s backoff (actual gaps are slightly longer because of the 500 ms scheduler poll and job run time).
4. Click **Permanent-failure job**. It is retried 3 times (2 s, 4 s, 8 s) and then appears in **Dead Letter Queue** with reason and attempts = 4, status FAILED. Optionally press **Retry**.
5. Dashboard cards show success / retrying / failed; "Retry success rate" = jobs that failed at least once and later succeeded ÷ jobs that failed at least once.
6. Kafka check: `docker compose exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list` (expect the five `jobs.*` topics) and
   `docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic jobs.dlq --from-beginning`.
7. Redis check: `docker compose exec redis redis-cli zrange delayed_jobs 0 -1 withscores`.
8. Database check: `docker compose exec postgres psql -U scheduler -d scheduler -c "select status,count(*) from jobs group by 1"`.

## Tests
```bash
mvn test            # JUnit 5 + Mockito, no infrastructure needed
```
Covers: submission (PENDING), priority → topic and priority ordering, immediate / delayed / recurring scheduling, success, retry, 2-4-8 s backoff, DLQ, status transitions, duplicate-delivery and lock handling, handlers.
There are no Testcontainers integration tests; end-to-end behaviour is exercised by the demo above.

## JMeter load test (100 concurrent submissions)
```bash
jmeter -n -t jmeter/job-load-test.jmx -l jmeter/results.jtl -e -o jmeter/report -Jthreads=100 -Jramp=5
```
The HTML report gives total requests, average response time, error % and throughput (requests/s; multiply by 60 for jobs/min).
That measures **submission** throughput. For **processing** throughput (jobs completed per minute), read "Processed this minute / Last full minute" on the dashboard or `GET /api/stats` after the run. Report only numbers you measured; with 4 workers and ~1 s per job, processing throughput is bounded by roughly 4 jobs/s, far below the slide's "10K+" target, so raise `WORKER_POOL_SIZE` / lower the simulated work (`EMAIL_WORK_MS`, `INVOICE_WORK_MS`) if you want to explore limits.

## Configuration (environment variables)
`DB_URL, DB_USER, DB_PASSWORD, REDIS_HOST, REDIS_PORT, KAFKA_BOOTSTRAP_SERVERS, WORKER_POOL_SIZE, EMAIL_FAILURE_RATE, EMAIL_WORK_MS, INVOICE_WORK_MS, SCHEDULER_POLL_MS, LOG_FILE`. Postgres credentials come from `.env` / compose variables.
Run locally without Docker: start Kafka/Redis/Postgres yourself, set `DB_PASSWORD`, then `mvn spring-boot:run`.
