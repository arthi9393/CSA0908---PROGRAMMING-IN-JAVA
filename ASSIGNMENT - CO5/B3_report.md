# Task B3 – Statement vs PreparedStatement vs Batch  [K4]
Setup: MariaDB 10.11 (MySQL-compatible) + MariaDB Connector/J 2.7.6, local machine, InnoDB. `create_db.sql`, `PassLoader.java`, `B3Benchmark.java`, `output.txt`.
(For Oracle MySQL use Connector/J and add `?rewriteBatchedStatements=true` to the URL for real batching; the default URL is in `Db.java`.)

## Complete the Code – PassLoader
TODO 1: bind the 5 parameters (`Integer.parseInt`, `Double.parseDouble`), `addBatch()`, `executeBatch()` every 1,000 rows. TODO 2: `con.commit(); return n;`. TODO 3: `con.rollback(); throw e;`.
Failure test: row 5,500 of 10,000 had amount `ABC` → `NumberFormatException` → rollback → **0 rows remain**.

## B3.1 Correctness (10 rows each)
Each approach left 10 rows. ResultSetMetaData: pass_id VARCHAR, reg_no VARCHAR, name VARCHAR, route_no INTEGER, amount DOUBLE, all NOT NULL – matches the table definition.

## B3.2 Time (ms), auto-commit ON
| Rows | Statement | PreparedStatement | Batch (1000) | Multi-row INSERT (1000, in a transaction) |
|---|---|---|---|---|
| 1,000 | 242 | 245 | 318 | 16 |
| 10,000 | 1,680 | 1,298 | 1,311 | 60 |
| 100,000 | 13,742 | 13,598 | 13,972 | 383 |

## B3.3 Auto-commit vs single transaction (100,000 rows, batches of 1000)
| Mode | Time |
|---|---|
| Auto-commit batches | 12,566 ms |
| Single transaction (`PassLoader.loadAll`) | **1,420 ms** (≈ 9× faster) |

Forced failure (bad amount at row 5,500 of 10,000): **auto-commit → 5,000 rows remain** (the five complete batches were already committed); **transaction → 0 rows**.

## B3.4 Interpretation and trade-off
* With auto-commit **ON** the three approaches are about the same (≈ 0.13 ms/row) – the cost is one commit (log flush to disk) **per row**, and that dominates parsing and network cost; with this driver even `executeBatch()` commits row by row in auto-commit mode. So batching alone did not help; *batching inside one transaction* did (1,420 ms). Time is O(n) in every case but with a constant ≈ 9× smaller.
* Statement vs PreparedStatement: similar speed here (the server parse cost is small); the PreparedStatement is the one that is safe from injection and handles types/quotes.
* The multi-row INSERT (383 ms) was also run in one transaction, so it is not a pure comparison with the auto-commit columns; compared with the PreparedStatement batch in a transaction (1,420 ms) it is ≈ 3.7× faster because it sends 100 statements instead of 100,000 row inserts.
* Transactions also give all-or-nothing behaviour (B3.3 failure test).
* **(a) Nightly import of 100,000 passes:** PreparedStatement batches (1000 rows) in **one transaction** (or LOAD DATA if the file is on/accessible to the server), with rollback on failure.
* **(b) Single pass at the counter:** a plain **PreparedStatement** with one insert (auto-commit is acceptable – one row is atomic by itself); batching adds nothing for one row and Statement is unsafe.

## Alternative – multi-row INSERT / LOAD DATA
* Multi-row INSERT: n/b statements, each with b value tuples; time ≈ O(n) but with far fewer round trips (measured 383 ms vs 1,420 ms). Memory: the SQL string for b rows is O(b) (b = 1000 → ≈ 70 KB); a PreparedStatement batch keeps b rows of parameters, also O(b), but each row is a separate protocol entry. `max_allowed_packet` limits b.
* `LOAD DATA INFILE`: the server reads the file directly, bypassing SQL parsing per row – fastest bulk method, O(n) time, O(1) client memory (streaming); drawbacks: needs file privileges/access, less per-row error reporting, row-level validation must be done beforehand.
