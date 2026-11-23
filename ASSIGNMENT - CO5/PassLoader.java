import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PassLoader {
    /** Loads all passes in one transaction, in batches of 1,000 rows.
     * Returns the number of rows loaded. */
    public static int loadAll(Connection con, List<String[]> passes)
            throws SQLException {
        String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) "
                + "VALUES (?, ?, ?, ?, ?)";
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int n = 0;
            for (String[] p : passes) {
                // TODO 1 (completed)
                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setString(3, p[2]);
                ps.setInt(4, Integer.parseInt(p[3]));        // NumberFormatException -> rollback
                ps.setDouble(5, Double.parseDouble(p[4]));   // invalid amount -> rollback
                ps.addBatch();
                n++;
                if (n % 1000 == 0) ps.executeBatch();
            }
            ps.executeBatch();
            // TODO 2 (completed)
            con.commit();
            return n;
        } catch (SQLException | RuntimeException e) {
            // TODO 3 (completed)
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }
}
