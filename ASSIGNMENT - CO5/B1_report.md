# Task B1 – Implement and Instrument the File Store  [K4]
Environment: OpenJDK 21, Linux. Raw output: `test_output.txt`, `passstore_output.txt`, `benchmark_output.txt`.
Files: `PassStore.java` (Complete-the-Code), `BusPassFileStore.java` (full A1 design), `LineCounter.java`, `B1Tests.java`, `B1Benchmark.java`.
Run: `javac *.java && java PassStore && java B1Tests && java B1Benchmark`

## Complete the Code – TODO 1
Output (matches expected):
```
[P1003, 192311003, Divya, 7, 3800.0]
null
```
**Why the damaged line P1002 does not stop the search:** `find` reads line by line; `"P1002,DAMAGED LINE".split(",", -1)` has 2 fields, not 5, so the `continue` statement skips that iteration and the `while` loop moves on to the next line (P1003), which is then found. A damaged line is data to skip, not an error to throw. For `find("P1002")` the only line with that ID is damaged, so after the whole file is scanned the method returns `null`.

## B1.1 Validation tests (`test_output.txt`, 8 PASS / 0 FAIL)
| Test | Input | What it verifies |
|---|---|---|
| T1 normal | find P1002 (name `Meena, K`) | add + find round trip; comma in a name is escaped/unescaped correctly |
| T2 absent id | find P9999 | returns `null`, no exception |
| T3 list | route 12 | listByRoute returns exactly the two route-12 passes |
| T4 damaged | 4 bad lines then P1007 | scan continues past damage and finds the later good record |
| T5 report | same | each bad line is reported with line number + reason (wrong field count, non-numeric amount, empty line, non-numeric route) |
| T6 missing file | store on non-existent file | no crash – returns null |
| T7 list + damage | route 7 | damaged lines don't corrupt listing |
| T8 power failure | last line truncated (`P1008,...,Gui`) | truncated record rejected, not mis-read |

## B1.2 Comparison with the Part A design
Implemented as designed. Changes: (1) `split(",", -1)` – without the `-1` Java drops trailing empty fields and a damaged line like `a,b,c,,` would look like it has 3 fields instead of 5; (2) `Files.newBufferedReader` (UTF-8, try-with-resources) used instead of `FileReader` so the charset is explicit; (3) the Complete-the-Code `PassStore` has 5 fields as the template requires, while `BusPassFileStore` has the 7 fields of A1; (4) the damaged-line log is kept in a list rather than only skipped.

## B1.3 Effect of buffering (measured)
| File | Lines | Unbuffered read() | Buffered 64 KB | readAllLines | Speed-up |
|---|---|---|---|---|---|
| 1 MB | 24,545 | 226.8 ms | 5.4 ms | 39.7 ms | 42× |
| 10 MB | 245,436 | 2,228.1 ms | 6.3 ms | 76.2 ms | 356× |
| 50 MB | 1,227,175 | 10,767.3 ms | 18.5 ms | 413.7 ms | 581× |

Prediction (A2.1): 52.4 M calls, 5–50 s → **measured 10.8 s: within range.** Unbuffered time grows linearly with bytes (≈ 0.2 µs per call); buffered stays small because it makes only 800 OS calls for 50 MB. The speed-up grows with size because JIT warm-up/fixed costs matter less in the larger files. (Machine-dependent – re-run on your own machine and use your numbers.)

## B1.4 Growth (measured, find of the **last** record, mean of 20 runs)
| Records | File scan | HashMap build (once) | HashMap lookup |
|---|---|---|---|
| 1,000 | 7.17 ms | 3.29 ms | 0.09 µs |
| 5,000 | 7.28 ms | 11.13 ms | 0.03 µs |
| 10,000 | 7.02 ms | 11.66 ms | 0.05 µs |
| 25,000 | 16.37 ms | 54.14 ms | 0.007 µs |
| 50,000 | 16.73 ms | 45.62 ms | 0.05 µs |
| 100,000 | 41.84 ms | 100.68 ms | 0.007 µs |

**Trend:** scan time rises with n: ≈ 6× from 1,000 to 100,000 (100× more records). The small sizes (≤ 10,000) are flat because fixed costs (file open, JIT, OS cache) dominate; beyond ≈ 10,000 the growth is close to linear – O(n). The lookup in the HashMap is constant O(1) (sub-microsecond; differences are timer noise).

## Alternative – HashMap loaded once
```
ALGORITHM LoadIndex:  map ← empty HashMap; FOR EACH valid line: map.put(f[0], f)        // O(n) once
ALGORITHM Find(id):   RETURN map.get(id)                                                 // O(1) average
```
| | Scan per search | HashMap index |
|---|---|---|
| Time for q searches on n passes | **O(q × n)** | **O(n + q)** |
| Memory | O(1) / O(buffer) | O(n) – every record as String[] + map entries (100,000 records ≈ 20–40 MB) |
Example (measured, n = 100,000): 1,000 searches ≈ 1,000 × 41.8 ms = 41.8 s by scanning; ≈ 100.7 ms + 1,000 × ~0 = ~0.1 s with the map. Break-even is after about 2–3 searches. Use scanning for rare lookups/low memory; the map when many lookups are done in one run. Caveat: the map is not updated by other processes' changes to the file.
