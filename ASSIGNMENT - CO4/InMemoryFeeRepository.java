import java.util.*;

/** Test/demo repository. delayMillis simulates a slow bank/database so the UI behaviour can be observed. */
public class InMemoryFeeRepository implements FeeRepository {
    private final List<String> rows = new ArrayList<>();
    private final long delayMillis;
    public InMemoryFeeRepository() { this(0); }
    public InMemoryFeeRepository(long delayMillis) { this.delayMillis = delayMillis; }

    @Override public synchronized void save(String regNo, double amount) throws FeeException {
        try { if (delayMillis > 0) Thread.sleep(delayMillis); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new FeeException("Interrupted", e); }
        rows.add(regNo + "," + amount);
    }
    public synchronized int count() { return rows.size(); }
}
