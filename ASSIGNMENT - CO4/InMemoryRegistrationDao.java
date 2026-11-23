import java.util.*;

/** Demo / test data-access layer: same contract and same duplicate rule as the database. latencyMillis simulates a slow DB. */
public class InMemoryRegistrationDao implements RegistrationDao {
    private final List<String> events = List.of("Hackathon 2026", "Cultural Night", "Robotics Workshop", "Quiz Bowl");
    private final List<Registration> rows = new ArrayList<>();
    private final Set<String> keys = new HashSet<>();
    private final long latencyMillis;
    public InMemoryRegistrationDao() { this(0); }
    public InMemoryRegistrationDao(long latencyMillis) { this.latencyMillis = latencyMillis; }
    private void lag() { try { if (latencyMillis > 0) Thread.sleep(latencyMillis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }

    @Override public List<String> findEventNames() { return events; }
    @Override public synchronized void insert(Registration r) throws DuplicateRegistrationException, DaoException {
        lag();
        if (!events.contains(r.eventName())) throw new DaoException("Unknown event: " + r.eventName(), null);
        if (!keys.add(r.regNo() + "|" + r.eventName())) throw new DuplicateRegistrationException("This student is already registered for " + r.eventName() + ".");
        rows.add(r);
    }
    private List<Registration> filtered(String ev) { List<Registration> o = new ArrayList<>(); for (Registration r : rows) if (ev == null || r.eventName().equals(ev)) o.add(r); return o; }
    @Override public synchronized int count(String ev) { lag(); return filtered(ev).size(); }
    @Override public synchronized List<Registration> findPage(String ev, int limit, int offset) {
        lag(); List<Registration> all = filtered(ev);
        return new ArrayList<>(all.subList(Math.min(offset, all.size()), Math.min(offset + limit, all.size())));
    }
}
