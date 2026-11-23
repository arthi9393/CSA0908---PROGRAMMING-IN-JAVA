# Task B4 – Final Engineering Decision  [K5 – Evaluate]
Code: `MakeSampleFile.java`, `Migrate.java`, `Concurrency.java`, `PassLoader.java`, `BusPassFileStore.java`, `create_db.sql`. Output: `output.txt`, `failed_records.txt`.

## B4.1 Migration (real output)
20,006 lines read from the pass file → **20,000 valid loaded** in one transaction (batches of 1000; ≈ 1.3 s; the very first cold run took 6.6 s because of JVM/DB warm-up) and **6 rejected**, each reported with line number and reason: wrong field count, non-numeric amount, empty line, negative amount, duplicate pass id, non-numeric route. `SELECT COUNT(*) = 20000` confirms it.

## B4.2 DatabaseMetaData (real output)
Product MariaDB 10.11.14; driver MariaDB Connector/J 2.7.6; JDBC 4.2; batch and transactions supported; tables `bus_pass`, `users`; column names/types/sizes of `bus_pass`.
**Why it is useful when moving to another server:** the program can check at start-up that the target database product/version, driver and JDBC level are what it was tested with, that required tables/columns exist (fail fast with a clear message instead of a run-time SQL error), and that features such as batch updates and transactions are supported – and adapt SQL dialects instead of assuming.

## B4.3 Production decision
**Recommendation: use a relational database (MySQL) – not text files and not serialization.**

| Requirement | Text file | Serialized file | Database |
|---|---|---|---|
| Scale: 12 campuses × 100 × 8,000 ≈ **800,000 passes/year** | Scan measured 41.8 ms at 100,000 → ≈ **0.35 s per find at 800,000** (O(n)), whole-file memory if indexed in a HashMap | Entire graph must be loaded into memory to read one pass; reading/writing the whole file for every change | Primary-key index → O(log n) find, `idx_route` for route lists; 100,000 rows load in 1.4 s |
| Concurrent counters | None – two writers can interleave/overwrite lines; needs external locking | Last writer overwrites everything | **Measured:** 6 counters inserted 12,000 passes concurrently in 0.57 s; all rows present; 5 of 6 attempts to issue the same pass id were rejected by the primary key |
| Safety / correctness | Damaged lines possible (power failure); validation by hand | Version problems (`serialVersionUID`), one corrupt byte can lose the whole file | ACID transactions (B3.3: failure → 0 partial rows), constraints, backups, recovery after crash |
| Searching | Linear scan for every query | Needs in-memory structures | SQL + indexes (by id, route, register number) |
| Cost | Simplest | Simple, Java-only | Needs a DB server, admin and a driver |

Text files/serialization remain fine for a single-user tool with a few thousand records, config or export/backup files (the migration reads the old text file). For the new scale, concurrency and safety requirements the database is the only option that meets all of them.

## Alternative – one serialized file per campus
* **Search all passes:** every query must open and deserialize 12 files: O(N) total (N = all passes) and O(N) memory if loaded together, versus O(log N) with an indexed table in one database. Cross-campus queries (a student using another campus) need a custom merge.
* **Space:** similar raw size (or bigger – serialization overhead), plus duplicated data when a student appears in several campuses and no constraints to prevent duplicates.
* **Concurrent counters:** within a campus several counters writing the same file corrupt or overwrite each other unless a single "file server" process or `FileLock` serialises them (reads block writes, whole file rewritten); across campuses, no global uniqueness of pass IDs. In the shared database, the DBMS provides row-level locking, transactions and key constraints, so counters work simultaneously without code for it.
