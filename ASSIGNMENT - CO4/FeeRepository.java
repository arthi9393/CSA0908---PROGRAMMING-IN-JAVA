/** Persistence boundary: the service depends on this interface, not on JDBC. */
public interface FeeRepository {
    void save(String regNo, double amount) throws FeeException;
}
