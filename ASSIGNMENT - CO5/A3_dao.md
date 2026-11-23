# Task A3 – Discover the Data-Access Design Pattern  [K4]

## A3.1 Common pattern
**Repeated in StudentDB, CourseDB and MarksDB**
1. Obtain / open a `Connection` (URL, user, password).
2. Build an SQL string.
3. Create a `Statement`/`PreparedStatement`, set parameters, execute.
4. Loop over the `ResultSet` and copy each row into an object.
5. Close `ResultSet`, statement and connection; handle `SQLException`.

**Differs:** the table and column names, the SQL text, the parameter types/count and the row→object mapping (`Student`, `Course`, `Marks`).

## A3.2 Generic Data-Access Object (DAO) interface
```java
public interface Dao<T, K> {
    void insert(T entity) throws SQLException;
    T findById(K id) throws SQLException;          // null if absent
    List<T> findAll() throws SQLException;
    void update(T entity) throws SQLException;
    void delete(K id) throws SQLException;
}

public class StudentDao implements Dao<Student, String> {
    private final DataSource/ConnectionProvider cp;           // step 1 shared
    public void insert(Student s) throws SQLException {
        try (Connection c = cp.get();
             PreparedStatement ps = c.prepareStatement(
               "INSERT INTO student(reg_no,name,dept,cgpa) VALUES (?,?,?,?)")) {
            ps.setString(1, s.getRegNo()); ps.setString(2, s.getName());
            ps.setString(3, s.getDept());  ps.setDouble(4, s.getCgpa());
            ps.executeUpdate();
        }
    }
    public Student findById(String regNo) throws SQLException { /* SELECT ... WHERE reg_no=? ; map(rs) */ }
    /* findAll, update, delete follow the same pattern; map(ResultSet) builds a Student */
}
```
Steps 1, 3 and 5 move into a shared base class/helper (`Db.query(sql, params, mapper)`); each DAO supplies only the SQL and the `RowMapper<T>`. CourseDao (`Dao<Course,String>`) and MarksDao (`Dao<Marks,MarksKey>`) are the same shape.

## A3.3 Object–table mapping
| Java field (`Student`) | Column (`student`) | SQL type | Note |
|---|---|---|---|
| `String regNo` | `reg_no` | VARCHAR(12) | **PRIMARY KEY** |
| `String name` | `name` | VARCHAR(60) NOT NULL | |
| `String dept` | `dept` | VARCHAR(30) | |
| `double cgpa` | `cgpa` | DOUBLE | |

`rs.getString("reg_no") → setRegNo`, … ; one `Student` object = one row; the primary key is the DAO's `K` type.

## Alternative concept – SQL written inside service/GUI classes
* **Places to change when a column is renamed** (e.g. `reg_no` → `register_no`): with the DAO, **1** place per table (StudentDao). With SQL scattered in services/GUI, every class that mentions the column: if 6 screens/services use `student`, that is **6+** edits, and a missed one fails only at run time.
* **Time/space per DB operation:** essentially **identical**: both build the same SQL, send it to the server and read the same ResultSet – cost is dominated by the network round trip and the query plan. The DAO adds one extra method call and one mapping step, i.e. O(1) extra time and no extra asymptotic space (O(rows) objects in both). The benefit of the DAO is maintainability, testability and a single place to add pooling/caching – not speed.
