import java.sql.*;
import java.util.*;

/** JDBC implementation. Needs the MySQL Connector/J jar on the class path. All SQL is parameterised. */
public class MySqlRegistrationDao implements RegistrationDao {
    private final String url, user, password;
    public MySqlRegistrationDao(String url, String user, String password) { this.url = url; this.user = user; this.password = password; }

    private Connection open() throws SQLException { return DriverManager.getConnection(url, user, password); }

    @Override public List<String> findEventNames() throws DaoException {
        try (Connection c = open(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT name FROM events ORDER BY event_date")) {
            List<String> out = new ArrayList<>(); while (rs.next()) out.add(rs.getString(1)); return out;
        } catch (SQLException e) { throw new DaoException("Could not load events.", e); }
    }

    @Override public void insert(Registration r) throws DuplicateRegistrationException, DaoException {
        String sql = "INSERT INTO registrations (student_reg_no, student_name, email, event_id) SELECT ?, ?, ?, event_id FROM events WHERE name = ?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.regNo()); ps.setString(2, r.name()); ps.setString(3, r.email()); ps.setString(4, r.eventName());
            if (ps.executeUpdate() == 0) throw new DaoException("Unknown event: " + r.eventName(), null);
        } catch (SQLException e) {
            // MySQL error 1062 = duplicate entry for the UNIQUE (student_reg_no, event_id) key
            if (e.getErrorCode() == 1062 || (e.getSQLState() != null && e.getSQLState().startsWith("23")))
                throw new DuplicateRegistrationException("This student is already registered for " + r.eventName() + ".");
            throw new DaoException("Registration could not be saved.", e);
        }
    }

    @Override public int count(String eventName) throws DaoException {
        String sql = "SELECT COUNT(*) FROM registrations r JOIN events e ON e.event_id = r.event_id" + (eventName == null ? "" : " WHERE e.name = ?");
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (eventName != null) ps.setString(1, eventName);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        } catch (SQLException e) { throw new DaoException("Could not count registrations.", e); }
    }

    @Override public List<Registration> findPage(String eventName, int limit, int offset) throws DaoException {
        String sql = "SELECT r.student_reg_no, r.student_name, r.email, e.name FROM registrations r JOIN events e ON e.event_id = r.event_id"
                   + (eventName == null ? "" : " WHERE e.name = ?") + " ORDER BY r.reg_id LIMIT ? OFFSET ?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1; if (eventName != null) ps.setString(i++, eventName);
            ps.setInt(i++, limit); ps.setInt(i, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Registration> out = new ArrayList<>();
                while (rs.next()) out.add(new Registration(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
                return out;
            }
        } catch (SQLException e) { throw new DaoException("Could not load registrations.", e); }
    }
}
