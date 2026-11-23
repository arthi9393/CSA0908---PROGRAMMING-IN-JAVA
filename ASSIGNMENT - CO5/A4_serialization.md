# Task A4 – What Should Be Serialized?  [K4]

## A4.1 Decide what to persist
```java
class Hostel   implements Serializable { List<Room> rooms; }
class Room     implements Serializable { int no; List<Resident> residents; }
class Resident implements Serializable {
    String name; String regNo;
    transient String password;       // never write secrets to a file
    transient Connection con;        // live resource, not Serializable
}
```
* `con` **must** be `transient`: `java.sql.Connection` is not `Serializable`, so without `transient` `writeObject` throws `NotSerializableException`. A connection is also meaningless after restart (socket/session are gone).
* `password` should be `transient` (security): serialization output is easily read/copied; store only a salted hash in the database, never the plain text in an object file.
* After deserialization transient fields hold default values (`null`), so the program must reopen the connection and ask for / reload credentials.
* **Object graph:** serializing `Hostel` writes the object *and everything reachable* from it (Hostel → Room list → each Room → Resident list → each Resident). Every class in the graph must be `Serializable`; shared references are written once (back-references), so cycles are safe and the graph structure is restored on read. A static field is never written.

## A4.2 Plan for change
A `phone` field is added to `Resident`, but old files were written by the old class.
* If `serialVersionUID` was **not declared**, the JVM computes it from the class structure; adding a field changes it → reading an old file throws **`InvalidClassException`** (local class incompatible: stream classdesc serialVersionUID ≠ local class serialVersionUID). All saved data becomes unreadable.
* If `private static final long serialVersionUID = 1L;` was declared in both versions, adding a field is a **compatible change**: the old file is read successfully, and the new field gets its default value (`null`) – the code must handle `phone == null`.
* Incompatible changes (renaming/removing fields, changing a field's type, changing the class hierarchy) still break reading even with the same UID; then use a custom `readObject`, version-bump the UID and write a migration.

## Alternative concept – CSV / JSON text
| | Java serialization | CSV / JSON |
|---|---|---|
| File size for n residents | Larger per object: class descriptors written once, plus per-object overhead (type codes, references); typically 1.5–3× the CSV for simple objects | Compact for flat data (CSV); JSON adds field names to every record (≈ 2× CSV) |
| Save/load time | O(n) but uses reflection, so a bigger constant; Java-only | O(n) with simple parsing; CSV fastest, JSON slower (parser) |
| New field added | Works only with a fixed `serialVersionUID`; old data gets default | CSV: add a column (old rows must be padded; position-based, fragile). JSON: old files simply lack the key → default; **most tolerant** |
| Other | Preserves the whole graph and shared references automatically; binary, not human readable, Java-only; security risks when reading untrusted data | Human-readable, language independent, no graph/reference support (relationships must be flattened or re-linked) |
(Measure with n = 1,000 / 10,000 / 100,000 residents using `ObjectOutputStream` vs `BufferedWriter` if you want your own numbers.)
