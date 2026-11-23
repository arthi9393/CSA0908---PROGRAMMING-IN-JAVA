# Task A2 – Don't Read What You Don't Need  [K4]

## A2.1 Predict the cost
`FileInputStream.read()` returns **one byte per call**. 50 MB = 50 × 1,048,576 = **52,428,800 bytes → about 52.4 million `read()` calls**.
Each call is a Java→JNI→kernel `read` system call (user/kernel mode switch, copy of 1 byte), costing roughly 0.1–1 µs at best, so the program is predicted to need **≈ 5–50 s** while the useful work (testing a byte for `\n`) is nanoseconds. The program is slow because it is dominated by system-call overhead, not by computation. Reading time is O(N) in the number of bytes but with a very large constant.

**Prediction vs measurement (B1.3):** predicted 5–50 s; measured **10.8 s** for 50 MB (see `B1/benchmark_output.txt`) – inside the predicted range, ≈ 0.2 µs per call.

## A2.2 Redesign with buffering
```
ALGORITHM CountLinesBuffered
INPUT  : file
OUTPUT : number of lines
1. buf ← new byte array of 64 KB
2. lines ← 0
3. OPEN file using BufferedInputStream(FileInputStream, 64 KB)
4. WHILE (n ← READ(buf)) ≠ END_OF_FILE DO
5.     FOR i ← 0 TO n − 1 DO IF buf[i] = '\n' THEN lines ← lines + 1
6. ENDWHILE
7. CLOSE file; RETURN lines
END
```
Number of OS calls: **52,428,800 / 65,536 = 800** instead of 52.4 million (65,536 × fewer). The 52 million byte comparisons still happen, but in memory (fast, cache friendly). Measured: 18.5 ms for 50 MB, **581× faster**.

## A2.3 When buffering does not help
* When the caller already reads in large blocks (e.g. `read(byte[1 MB])` or `Files.copy`/`transferTo`) – the buffer is bypassed or only adds one more copy.
* When doing random access (`RandomAccessFile.seek` to scattered positions): a buffer is refilled but its contents are never reused.
* Very small files (a few hundred bytes) – one system call either way.

## Alternative concept – `Files.readAllLines()`
```
lines ← Files.readAllLines(path).size()
```
| | Buffered stream | readAllLines |
|---|---|---|
| Time | O(N) – N = file bytes | O(N) – same order, plus creating one `String` per line (≈ 1.2 M objects for 50 MB) and UTF-8 decoding |
| Memory | **O(B)**, B = buffer size (64 KB) | **O(N)**: all lines in memory – for 50 MB of text ≈ 100–150 MB of heap (String objects + overhead) |
| Measured (50 MB) | 18.5 ms | 413.7 ms |

Use the buffered stream for large or unknown-size files and one-pass processing (counting, filtering, searching). Use `readAllLines` for small files (config files, a few MB) when you need random access to lines or simpler code, and when heap is plentiful.
