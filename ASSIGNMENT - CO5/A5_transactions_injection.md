# Task A5 – Design for Correctness and Safety  [K4]

## A5.1 Transaction design – ₹500 transfer
```
ALGORITHM TransferMoney
INPUT  : con, hostelWalletId, canteenWalletId, amount = 500
OUTPUT : both updates applied, or neither
1. oldMode ← con.getAutoCommit()
2. con.setAutoCommit(false)                         // begin transaction
3. TRY
4.     rows1 ← EXECUTE "UPDATE wallet SET bal = bal − ? WHERE id = ? AND bal >= ?"  (amount, hostelWalletId, amount)
5.     IF rows1 ≠ 1 THEN THROW "insufficient funds / no such wallet"
6.     rows2 ← EXECUTE "UPDATE wallet SET bal = bal + ? WHERE id = ?"               (amount, canteenWalletId)
7.     IF rows2 ≠ 1 THEN THROW "no such canteen wallet"
8.     con.commit()                                  // both become permanent together
9. CATCH any error
10.    con.rollback()                                // undo update 1 if update 2 failed
11.    RE-THROW error
12. FINALLY
13.    con.setAutoCommit(oldMode)
14. END TRY
END
```
If the program crashes between steps 4 and 8, the database server rolls back the uncommitted transaction, so money is neither lost nor created (atomicity). Relative updates (`bal = bal − ?`) plus the `bal >= ?` guard also prevent lost updates and negative balances.

## A5.2 Injection analysis
```java
String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
```
Input is pasted into the SQL text, so it can contain SQL syntax.
* `id = admin' -- ` (note the space after `--`), `pwd = anything` gives
  `SELECT * FROM users WHERE id='admin' -- ' AND pwd='anything'` → everything after `--` is a comment, the password test disappears.
* `pwd = ' OR '1'='1` gives `... AND pwd='' OR '1'='1'` → `AND` binds tighter than `OR`, the condition is always true.
(Both demonstrated in `B2/output.txt`.)

Safe redesign:
```
ALGORITHM SafeLogin
INPUT  : con, id, pwd
OUTPUT : TRUE if the login is valid
1. ps ← con.prepareStatement("SELECT 1 FROM users WHERE id = ? AND pwd = ?")   // SQL fixed, parsed first
2. ps.setString(1, id);  ps.setString(2, pwd)      // values bound as DATA
3. rs ← ps.executeQuery()
4. RETURN rs.next()
END
```
(In a real system also store a salted hash of the password and compare hashes.)

## Alternative concept – two auto-commit updates
With auto-commit ON each `UPDATE` commits at once. If the program fails after update 1 and before update 2, the hostel wallet has lost ₹500 permanently and the canteen wallet never received it: **₹500 vanishes** and the database is inconsistent; nothing can roll it back.
* Time: a transaction does one commit (one log flush) for the pair; two auto-commits do two commits, so two log flushes – the transaction is usually *as fast or faster* (measured in B3.3: 100,000 batched rows took 12,566 ms with per-batch/auto commits vs 1,420 ms in one transaction). The extra cost of a transaction is holding row locks slightly longer (and the memory for the undo log).
* Risk: auto-commit = possible lost/created money after any failure; transaction = all-or-nothing (ACID atomicity).
