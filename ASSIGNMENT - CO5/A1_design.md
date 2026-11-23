# Task A1 – Design a File Format (Bus-Pass Records)  [K4]

> Pseudocode follows the SIMATS Engineering – Standard for Algorithmic Pseudocode
> (ALGORITHM / INPUT / OUTPUT header, numbered steps, `←` assignment, upper-case keywords).
> Re-check keyword style against the official standard sheet before submitting.

## A1.1 Record layout

Fields (in order): passId, regNo, name, routeNo, boardingStop, validityDate, amountPaid

### (a) Delimited text file  (`passes.csv`, UTF-8, one record per line)
```
passId,regNo,name,routeNo,boardingStop,validityDate(yyyy-MM-dd),amountPaid
P1001,192311001,Arun,12,Tambaram,2027-03-31,4500.0
```
* Delimiter: comma `,`; record terminator: newline `\n`.
* Escape rule applied to every *text* field before writing: `\` → `\\`, `,` → `\c`, newline → `\n`.
  A name such as `Meena, K` is stored as `Meena\c K` and unescaped on read, so the number of
  delimiters in a line is always exactly 6 and the field count is always 7.
* Numeric/date fields cannot contain the delimiter, so they are not escaped.

### (b) Binary file with DataOutputStream  (`passes.dat`)
| Order | Field | Write method | Bytes |
|---|---|---|---|
| 1 | passId | `writeUTF` | 2 + length (≤ 10) |
| 2 | regNo | `writeUTF` | 2 + 9..12 |
| 3 | name | `writeUTF` | 2 + length (≤ 60 chars) |
| 4 | routeNo | `writeInt` | 4 |
| 5 | boardingStop | `writeUTF` | 2 + length |
| 6 | validityDate | `writeInt` (yyyyMMdd) | 4 |
| 7 | amountPaid | `writeDouble` | 8 |

`writeUTF` is length-prefixed, so a name containing a comma (or any character) needs **no escaping**.
Records are variable length, so the file must be read sequentially with `DataInputStream`;
a `EOFException` at a field boundary means normal end of file, in the middle of a record means damage.

## A1.2 Operations (text-file design)

Streams used and why:
* **Add** – `FileOutputStream(file, append=true)` wrapped in `OutputStreamWriter(UTF-8)` and `BufferedWriter`.
  Append mode never rewrites old data; the buffer joins `write` + `newLine` into one OS write; `getFD().sync()` forces it to disk.
* **Find / List** – `FileReader`-style character stream wrapped in `BufferedReader` (`Files.newBufferedReader`).
  We need *lines of text*, which only `readLine()` gives; the buffer means one OS read per 8 KB, not per character.
  `FileInputStream` alone is not used because it gives raw bytes (no line or charset handling) and is slow unbuffered (see A2).

```
ALGORITHM AddPass
INPUT  : passId, regNo, name, routeNo, stop, validity, amount
OUTPUT : record appended to file
1. line ← esc(passId) + "," + esc(regNo) + "," + esc(name) + "," + routeNo + ","
          + esc(stop) + "," + validity + "," + amount
2. OPEN file for APPEND using BufferedWriter
3. WRITE line, WRITE newline
4. FLUSH writer and SYNC to disk
5. CLOSE file                                   // try-with-resources, always closed
END

ALGORITHM FindPass
INPUT  : file, passId
OUTPUT : fields of the pass, or NOT_FOUND
1. IF file does not exist THEN RETURN NOT_FOUND
2. OPEN file using BufferedReader
3. lineNo ← 0
4. WHILE (line ← READLINE) is not END_OF_FILE DO
5.     lineNo ← lineNo + 1
6.     IF Validate(line) fails THEN LOG(lineNo, reason); CONTINUE
7.     f ← SPLIT(line, ",")
8.     IF unesc(f[0]) = passId THEN CLOSE file; RETURN unescaped f
9. ENDWHILE
10. CLOSE file; RETURN NOT_FOUND
END

ALGORITHM ListByRoute
INPUT  : file, route
OUTPUT : list of passes on that route
1. result ← empty list
2. OPEN file using BufferedReader
3. FOR EACH line IN file DO
4.     IF Validate(line) fails THEN LOG; CONTINUE
5.     f ← SPLIT(line, ",")
6.     IF INTEGER(f[3]) = route THEN APPEND unescaped f TO result
7. ENDFOR
8. CLOSE file; RETURN result
END

ALGORITHM Validate
INPUT  : line
OUTPUT : OK or reason
1. IF line is blank THEN RETURN "empty line"
2. f ← SPLIT(line, ",", keep empty fields)
3. IF LENGTH(f) ≠ 7 THEN RETURN "wrong field count"
4. IF f[0] is empty THEN RETURN "empty id"
5. IF f[3] is not an integer OR f[6] is not a number THEN RETURN "non-numeric"
6. RETURN OK
END
```

## A1.3 Design for damaged data
| Problem | Detection | Handling |
|---|---|---|
| Damaged line | `Validate` – field count ≠ 7, blank line, non-numeric route/amount | Line is **skipped and logged** (line number + reason); scan continues so one bad line never blocks the file |
| Missing file | `Files.exists` / `NoSuchFileException` | `find`/`list` return NOT_FOUND / empty list; `add` creates the file (`CREATE`). A missing file on a *known-existing* store is reported to the user |
| Power failure in the middle of a write | The last line is truncated → fewer than 7 fields and/or no `\n` | Truncated line fails validation and is ignored; every `add` calls `flush()` + `FileDescriptor.sync()` so an acknowledged add is durable; bulk rewrites are done to `passes.tmp` and then `Files.move(..., ATOMIC_MOVE)` so the real file is never half-written |

## Alternative concept – RandomAccessFile with fixed-length records
Fixed record size: passId 10 + regNo 12 + name 60 + route 4 + stop 30 + date 4 + amount 8 = **128 bytes** (UTF-8 padded with spaces / zero bytes).
```
ALGORITHM FindByIndex(i):  raf.seek(i × 128); raf.readFully(buf[128]); RETURN decode(buf)
```
* If pass IDs are sequential (P1001 = record 0, P1002 = record 1 …) the index is computed from the ID → **O(1)** find (one `seek` + one 128-byte read). Otherwise an extra index (HashMap id→position) is needed, or a binary search on a sorted file (O(log n)).
* Text file: **O(n)** scan (n ≈ 8,000/year → every find reads up to 8,000 lines).
* Listing by route is O(n) in both designs (no index on route).
* Space: 8,000 × 128 B = **1.0 MB**. Average real record ≈ 55 bytes of text, so ≈ 57 % of the space is padding, but the file is predictable in size. Names longer than 60 chars must be truncated or rejected – a design limit.
* Updates: fixed-length records can be overwritten in place; text records cannot (length changes).
