import java.sql.*;

/** Connection settings. For MySQL use the defaults; override with environment variables. */
public class Db {
    static final String URL  = System.getenv().getOrDefault("DB_URL",  "jdbc:mysql://localhost:3306/buspass_test");
    static final String USER = System.getenv().getOrDefault("DB_USER", "csa09");
    static final String PASS = System.getenv().getOrDefault("DB_PASS", "csa09pass");
    static Connection open() throws SQLException { return DriverManager.getConnection(URL, USER, PASS); }
}
