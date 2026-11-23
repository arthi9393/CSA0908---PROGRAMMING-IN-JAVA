# Task B2 – Engineer an Adversarial Input  [K4]
Local test database `buspass_test` created by `create_db.sql` (table `users`). Code: `LoginDemo.java`, `DamagedFileDemo.java`, `Db.java`. Output: `output.txt`.

## 1. Unsafe vs PreparedStatement login (real output)
| Input id / pwd | SQL built (unsafe) | Unsafe | PreparedStatement |
|---|---|---|---|
| admin / S3cret! | `... id='admin' AND pwd='S3cret!'` | accepted | accepted |
| admin / wrong | `... pwd='wrong'` | rejected | rejected |
| `admin` / `' OR '1'='1` | `id='admin' AND pwd='' OR '1'='1'` | **ACCEPTED** | rejected |
| `admin' -- ` / anything | `id='admin' -- ' AND pwd='anything'` | **ACCEPTED** | rejected |
| `nobody' OR 1=1 #` / x | `id='nobody' OR 1=1 #' AND pwd='x'` | **ACCEPTED** (no such user!) | rejected |

The PreparedStatement sends the SQL template first and the values separately, so `'`, `--`, `OR` are only characters inside a string compared against the column; the query still looks for a user whose id is literally `admin' -- `, finds none, and rejects.

## 2. Damaged bus-pass file (real output)
File had: a good line, a line with 5 fields, a non-numeric amount (`forty-five`), an empty line, a good line. `find P2004` → found; `find P2002` → null; report lists line 2 (expected 7 fields but found 5), line 3 (amount not numeric), line 4 (empty line). The store never crashed and never returned wrong data.

## 3. What each input reveals about the original design
* **Quote/comment/tautology inputs** – the original design mixes *code and data* in one string; the database cannot tell the programmer's SQL from the user's text. The root cause is string concatenation, not the particular input.
* **Wrong number of fields** – the file format has no checksum or schema; the only integrity check is the field count, which is why validation must be explicit.
* **Non-numeric amount / empty line** – parsing without validation would throw `NumberFormatException` and stop the whole program, or silently load garbage; validate each line and isolate failures.

## Alternative – escaping quotes or regex validation instead of PreparedStatement
```java
String safe = id.replace("'", "''");      // or: if (!id.matches("[A-Za-z0-9_]{1,30}")) reject
String sql  = "SELECT * FROM users WHERE id='" + safe + "' AND pwd='" + ...
```
Weaker because: the escape rules depend on the database and character set (MySQL also treats `\` as an escape: input `\' OR 1=1 #` can defeat naive quote doubling), a missed field or one forgotten call re-opens the hole, numeric contexts need no quotes at all, and a whitelist regex also rejects legitimate input (a surname such as O'Brien, passwords with symbols). It is a defence that must be remembered *every time*; PreparedStatement is safe by construction.
Cost: regex/escape validation is O(L) in the input length L per query (microseconds); PreparedStatement binding is also O(L) and, with a server-side prepared plan, can even save parsing time when reused. So safety does **not** cost performance – the PreparedStatement is both safer and equal or faster.
