import java.sql.*;

/** JDBC implementation: parameterised SQL (no injection), connection closed by try-with-resources. */
public class JdbcFeeRepository implements FeeRepository {
    private final String url, user, pwd;
    public JdbcFeeRepository(String url, String user, String pwd) { this.url = url; this.user = user; this.pwd = pwd; }

    @Override public void save(String regNo, double amount) throws FeeException {
        String sql = "INSERT INTO fees(reg_no, amount) VALUES (?, ?)";
        try (Connection c = DriverManager.getConnection(url, user, pwd);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, regNo);
            ps.setDouble(2, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new FeeException("Payment could not be saved. Please try again later.", e);
        }
    }
}
