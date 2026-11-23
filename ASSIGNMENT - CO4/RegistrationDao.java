import java.util.List;

/** Data-access layer contract (JDBC or in-memory). eventName == null means "all events". */
public interface RegistrationDao {
    List<String> findEventNames() throws DaoException;
    void insert(Registration r) throws DuplicateRegistrationException, DaoException;
    int count(String eventName) throws DaoException;
    List<Registration> findPage(String eventName, int limit, int offset) throws DaoException;
}
