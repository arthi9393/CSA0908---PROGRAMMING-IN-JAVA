import java.sql.*;

public class LoginDemo {
    /** UNSAFE: the original check from Task A5.2 */
    static boolean unsafeLogin(Connection con, String id, String pwd) throws SQLException {
        String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
        System.out.println("   SQL built : " + sql);
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) { return rs.next(); }
    }
    /** SAFE: PreparedStatement - input is always data, never SQL */
    static boolean safeLogin(Connection con, String id, String pwd) throws SQLException {
        String sql = "SELECT * FROM users WHERE id=? AND pwd=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id); ps.setString(2, pwd);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
    public static void main(String[] a) throws Exception {
        String[][] cases = {
            {"admin", "S3cret!",        "valid login"},
            {"admin", "wrong",          "wrong password"},
            {"admin", "' OR '1'='1",    "tautology in password"},
            {"admin' -- ", "anything",  "comment out the password test"},
            {"nobody' OR 1=1 #", "x",   "tautology + MySQL # comment in id"},
        };
        try (Connection con = Db.open()) {
            for (String[] c : cases) {
                System.out.println("\nCase: " + c[2] + "   id=[" + c[0] + "] pwd=[" + c[1] + "]");
                boolean u; try { u = unsafeLogin(con, c[0], c[1]); } catch (SQLException e) { u = false; System.out.println("   SQL error: " + e.getMessage()); }
                boolean s = safeLogin(con, c[0], c[1]);
                System.out.println("   UNSAFE accepted? " + u + "    SAFE accepted? " + s);
            }
        }
    }
}
