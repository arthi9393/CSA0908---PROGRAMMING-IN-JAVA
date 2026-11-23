import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** B4.1 + B4.2 : migrate the pass file to MySQL in batches inside one transaction, report failures. */
public class Migrate {
    public static void main(String[] args) throws Exception {
        Path in = Path.of(args.length > 0 ? args[0] : "sample_passes.csv");
        List<String[]> good = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        long t0 = System.nanoTime();
        try (BufferedReader r = Files.newBufferedReader(in)) {
            String line; int ln = 0;
            while ((line = r.readLine()) != null) {
                ln++;
                String why = BusPassFileStore.validate(line);
                String[] f = why == null ? line.split(",", -1) : null;
                if (why == null && Double.parseDouble(f[6]) < 0) why = "negative amount";
                if (why == null && !seen.add(f[0])) why = "duplicate pass id " + f[0];
                if (why != null) { failed.add("line " + ln + ": " + why + "  -> [" + line + "]"); continue; }
                good.add(new String[]{f[0], f[1], f[2], f[3], f[6]});    // file fields -> table columns
            }
        }
        System.out.println("Read " + (good.size() + failed.size()) + " lines: " + good.size() + " valid, " + failed.size() + " rejected");
        Files.write(Path.of("failed_records.txt"), failed);
        failed.forEach(x -> System.out.println("  REJECTED " + x));

        try (Connection con = Db.open()) {
            try (Statement s = con.createStatement()) { s.execute("TRUNCATE TABLE bus_pass"); }
            int n = PassLoader.loadAll(con, good);               // batches of 1000, one transaction
            System.out.printf("Loaded %d rows in %.0f ms (single transaction, batches of 1000)%n", n, (System.nanoTime() - t0) / 1e6);
            try (Statement s = con.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM bus_pass")) { rs.next(); System.out.println("SELECT COUNT(*) = " + rs.getInt(1)); }

            // ---- B4.2 DatabaseMetaData ----
            DatabaseMetaData md = con.getMetaData();
            System.out.println("\n=== DatabaseMetaData ===");
            System.out.println("Product        : " + md.getDatabaseProductName() + " " + md.getDatabaseProductVersion());
            System.out.println("Driver         : " + md.getDriverName() + " " + md.getDriverVersion());
            System.out.println("JDBC version   : " + md.getJDBCMajorVersion() + "." + md.getJDBCMinorVersion());
            System.out.println("URL / User     : " + md.getURL() + " / " + md.getUserName());
            System.out.println("Supports batch : " + md.supportsBatchUpdates() + "   Transactions: " + md.supportsTransactions());
            System.out.println("Tables in database:");
            try (ResultSet rs = md.getTables(con.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (rs.next()) System.out.println("  - " + rs.getString("TABLE_NAME"));
            }
            System.out.println("Columns of bus_pass:");
            try (ResultSet rs = md.getColumns(con.getCatalog(), null, "bus_pass", "%")) {
                while (rs.next()) System.out.printf("  %-9s %-8s size=%d%n", rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"), rs.getInt("COLUMN_SIZE"));
            }
        }
    }
}
