import java.sql.*;
import java.util.*;

public class B3Benchmark {
    static List<String[]> gen(int n, int offset) {
        List<String[]> l = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int k = offset + i;
            l.add(new String[]{"P" + (100000 + k), "19231" + (10000 + k), "Student" + k, String.valueOf(k % 40 + 1), String.valueOf(3000 + k % 2000)});
        }
        return l;
    }
    static void clear(Connection con) throws SQLException { try (Statement s = con.createStatement()) { s.execute("TRUNCATE TABLE bus_pass"); } }
    static long count(Connection con) throws SQLException { try (Statement s = con.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT(*) FROM bus_pass")) { r.next(); return r.getLong(1); } }
    static double ms(long t0) { return (System.nanoTime() - t0) / 1e6; }

    // (a) Statement: SQL text built per row, auto-commit ON
    static void viaStatement(Connection con, List<String[]> ps) throws SQLException {
        con.setAutoCommit(true);
        try (Statement st = con.createStatement()) {
            for (String[] p : ps)
                st.executeUpdate("INSERT INTO bus_pass VALUES ('" + p[0] + "','" + p[1] + "','" + p[2] + "'," + p[3] + "," + p[4] + ")");
        }
    }
    // (b) PreparedStatement, one executeUpdate per row, auto-commit ON
    static void viaPrepared(Connection con, List<String[]> ps) throws SQLException {
        con.setAutoCommit(true);
        try (PreparedStatement s = con.prepareStatement("INSERT INTO bus_pass VALUES (?,?,?,?,?)")) {
            for (String[] p : ps) { bind(s, p); s.executeUpdate(); }
        }
    }
    // (c) PreparedStatement + addBatch/executeBatch (1000 per batch), auto-commit ON (each batch commits itself)
    static int viaBatchAutoCommit(Connection con, List<String[]> ps) throws SQLException {
        con.setAutoCommit(true);
        int n = 0;
        try (PreparedStatement s = con.prepareStatement("INSERT INTO bus_pass VALUES (?,?,?,?,?)")) {
            for (String[] p : ps) { bind(s, p); s.addBatch(); if (++n % 1000 == 0) s.executeBatch(); }
            s.executeBatch();
        }
        return n;
    }
    // (d) alternative concept: multi-row INSERT, b rows per statement, one transaction
    static void viaMultiRow(Connection con, List<String[]> ps, int b) throws SQLException {
        con.setAutoCommit(false);
        try (Statement st = con.createStatement()) {
            StringBuilder sb = new StringBuilder();
            int k = 0;
            for (String[] p : ps) {
                sb.append(k == 0 ? "INSERT INTO bus_pass VALUES " : ",")
                  .append("('").append(p[0]).append("','").append(p[1]).append("','").append(p[2]).append("',").append(p[3]).append(',').append(p[4]).append(')');
                if (++k == b) { st.executeUpdate(sb.toString()); sb.setLength(0); k = 0; }
            }
            if (k > 0) st.executeUpdate(sb.toString());
            con.commit();
        } finally { con.setAutoCommit(true); }
    }
    static void bind(PreparedStatement s, String[] p) throws SQLException {
        s.setString(1, p[0]); s.setString(2, p[1]); s.setString(3, p[2]); s.setInt(4, Integer.parseInt(p[3])); s.setDouble(5, Double.parseDouble(p[4]));
    }

    public static void main(String[] args) throws Exception {
        try (Connection con = Db.open()) {
            // ---------- B3.1 correctness on 10 rows ----------
            System.out.println("=== B3.1 Correctness: 10 rows with each approach ===");
            String[] names = {"Statement", "PreparedStatement", "Batch"};
            for (int a = 0; a < 3; a++) {
                clear(con);
                List<String[]> d = gen(10, 0);
                if (a == 0) viaStatement(con, d); else if (a == 1) viaPrepared(con, d); else viaBatchAutoCommit(con, d);
                System.out.println("\n" + names[a] + " -> rows in table: " + count(con));
            }
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM bus_pass ORDER BY pass_id LIMIT 3")) {
                ResultSetMetaData md = rs.getMetaData();
                System.out.println("\nResultSetMetaData of SELECT * FROM bus_pass:");
                for (int i = 1; i <= md.getColumnCount(); i++)
                    System.out.printf("  col %d: %-9s type=%-8s (%d) nullable=%s%n", i, md.getColumnName(i), md.getColumnTypeName(i), md.getColumnType(i), md.isNullable(i) == 0 ? "NO" : "YES");
                while (rs.next()) {
                    for (int i = 1; i <= md.getColumnCount(); i++) System.out.print(rs.getString(i) + (i < md.getColumnCount() ? " | " : "\n"));
                }
            }

            // ---------- B3.2 growth ----------
            System.out.println("\n=== B3.2 Insert time (ms), auto-commit ON ===");
            System.out.printf("%-9s %-12s %-20s %-18s %-22s%n", "rows", "Statement", "PreparedStatement", "Batch(1000)", "Multi-row INSERT(1000)");
            for (int n : new int[]{1000, 10000, 100000}) {
                List<String[]> d = gen(n, 0);
                clear(con); long t = System.nanoTime(); viaStatement(con, d);       double a = ms(t); assert count(con) == n;
                clear(con); t = System.nanoTime(); viaPrepared(con, d);             double b = ms(t);
                clear(con); t = System.nanoTime(); viaBatchAutoCommit(con, d);      double c = ms(t);
                clear(con); t = System.nanoTime(); viaMultiRow(con, d, 1000);       double e = ms(t);
                System.out.printf("%-9d %-12.0f %-20.0f %-18.0f %-22.0f%n", n, a, b, c, e);
            }

            // ---------- B3.3 auto-commit vs transaction ----------
            System.out.println("\n=== B3.3 Batch insert: auto-commit vs single transaction (100,000 rows) ===");
            List<String[]> big = gen(100000, 0);
            clear(con); long t = System.nanoTime(); viaBatchAutoCommit(con, big); double ta = ms(t);
            clear(con); t = System.nanoTime(); int loaded = PassLoader.loadAll(con, big); double tt = ms(t);
            System.out.printf("Auto-commit batches : %.0f ms%nSingle transaction   : %.0f ms (rows=%d)%n", ta, tt, loaded);

            System.out.println("\n--- Forced failure: row 5,500 of 10,000 has amount 'ABC' ---");
            List<String[]> bad = gen(10000, 0);
            bad.get(5499)[4] = "ABC";
            clear(con);
            try { viaBatchAutoCommit(con, bad); } catch (RuntimeException ex) { System.out.println("Auto-commit  -> failed with " + ex); }
            System.out.println("Rows left in table after AUTO-COMMIT failure : " + count(con) + "   (partial, inconsistent)");
            clear(con);
            try { PassLoader.loadAll(con, bad); } catch (RuntimeException ex) { System.out.println("Transaction -> failed with " + ex); }
            System.out.println("Rows left in table after TRANSACTION failure : " + count(con) + "   (all-or-nothing)");
            clear(con);
        }
    }
}
