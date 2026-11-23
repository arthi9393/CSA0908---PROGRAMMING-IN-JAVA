# CSA09 – Programming in Java – Assignment 5 (CO5): Data Persistence Engineering

| | |
|---|---|
| Name | ARTHI A |
| Register number | 192525152 |
| Java version | OpenJDK 21 (any JDK 11+ works) |
| Database | MySQL 8 (results in this repo were produced on MariaDB 10.11, which is MySQL-compatible) with Connector/J |

## Folder layout
| Folder | Task | Contents |
|---|---|---|
| A1 | File format design | record layouts, pseudocode, damaged data, RandomAccessFile alternative |
| A2 | Buffered I/O | prediction, buffered redesign, readAllLines alternative |
| A3 | Data-access pattern | Dao<T,K>, object–table mapping |
| A4 | Serialization | transient, serialVersionUID, CSV/JSON alternative |
| A5 | Transactions & injection | transfer pseudocode, injection analysis |
| B1 | File store | `PassStore` (complete the code), `BusPassFileStore`, tests, benchmarks, outputs |
| B2 | Adversarial inputs | `LoginDemo`, `DamagedFileDemo`, `create_db.sql`, outputs |
| B3 | Statement vs PreparedStatement vs Batch | `PassLoader` (complete the code), `B3Benchmark`, SQL, outputs |
| B4 | Final decision | migration, DatabaseMetaData, concurrency test, decision |

## How to run
**B1 (no database):** `cd B1 && javac *.java && java PassStore && java B1Tests && java B1Benchmark`

**B2–B4 (database):**
1. Install MySQL and download MySQL Connector/J; put the jar in a `lib/` folder inside the task folder.
2. `mysql -u root -p < create_db.sql` (creates `buspass_test` and the tables) and create a user:
   `CREATE USER 'csa09'@'localhost' IDENTIFIED BY 'csa09pass'; GRANT ALL ON buspass_test.* TO 'csa09'@'localhost';`
3. `cd B2 && ./run.sh`, `cd B3 && ./run.sh`, `cd B4 && ./run.sh`
   (Windows: `javac -cp ".;lib/*" *.java` and `java -cp ".;lib/*" LoginDemo`.)
4. Different settings: environment variables `DB_URL`, `DB_USER`, `DB_PASS`. For batching speed with Connector/J use `jdbc:mysql://localhost:3306/buspass_test?rewriteBatchedStatements=true`.

## Results (short)
* **A1–A5:** design documents in each folder.
* **B1:** 8/8 tests pass. 50 MB line count: unbuffered 10.8 s vs buffered 18.5 ms (581×). Scan of 100,000 records 41.8 ms vs HashMap lookup < 1 µs.
* **B2:** 3 injection inputs accepted by the unsafe check, all rejected by PreparedStatement; damaged file handled with a report.
* **B3:** per-row commit dominated (100,000 rows ≈ 13.6 s in all auto-commit modes); one transaction with batches 1.4 s; failure test: auto-commit left 5,000 partial rows, transaction left 0.
* **B4:** 20,000 rows migrated, 6 rejected and reported; DatabaseMetaData printed; 6 concurrent counters inserted 12,000 rows, duplicate pass id rejected 5 of 6 times. Decision: database.

> **Numbers depend on the machine.** Re-run every program on your own computer, take your own screenshots into each task folder and replace/confirm the figures in the reports with your own measurements.

## Commit progressively (required)
Use `commit_each_task.sh` (or run the commands by hand) – one commit per task, e.g. `git add A1 && git commit -m "Task A1: file format design"`.
