import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/** Supporting evidence for B4.3: several counters issuing passes at the same time. */
public class Concurrency {
    public static void main(String[] a) throws Exception {
        try (Connection c = Db.open(); Statement s = c.createStatement()) { s.execute("TRUNCATE TABLE bus_pass"); }
        int counters = 6, each = 2000;
        ExecutorService ex = Executors.newFixedThreadPool(counters);
        AtomicInteger dupRejected = new AtomicInteger(), dupAccepted = new AtomicInteger();
        long t0 = System.nanoTime();
        List<Future<?>> fs = new ArrayList<>();
        for (int k = 0; k < counters; k++) {
            final int id = k;
            fs.add(ex.submit(() -> {
                try (Connection con = Db.open()) {
                    con.setAutoCommit(false);
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO bus_pass VALUES (?,?,?,?,?)")) {
                        for (int i = 0; i < each; i++) {
                            ps.setString(1, "C" + id + "-" + i); ps.setString(2, "R" + i); ps.setString(3, "Student"); ps.setInt(4, i % 40); ps.setDouble(5, 3500);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                    // every counter also tries to issue the SAME pass id "SAME-1": only one may succeed
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO bus_pass VALUES ('SAME-1','R','S',1,100)")) {
                        ps.executeUpdate(); dupAccepted.incrementAndGet();
                    } catch (SQLException e) { dupRejected.incrementAndGet(); }
                    con.commit();
                } catch (Exception e) { e.printStackTrace(); }
                return null;
            }));
        }
        for (Future<?> f : fs) f.get();
        ex.shutdown();
        try (Connection c = Db.open(); Statement s = c.createStatement()) {
            ResultSet r = s.executeQuery("SELECT COUNT(*) FROM bus_pass"); r.next();
            System.out.printf("%d counters x %d passes in %.0f ms -> rows = %d (expected %d + 1)%n", counters, each, (System.nanoTime() - t0) / 1e6, r.getInt(1), counters * each);
            System.out.println("Same pass id issued by every counter: accepted=" + dupAccepted + ", rejected(duplicate key)=" + dupRejected);
        }
    }
}
