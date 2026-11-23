import java.nio.file.*;
import java.util.*;

/** B1.1 validation tests + B2 damaged-file test. */
public class B1Tests {
    static int pass = 0, fail = 0;
    static void check(String name, boolean ok, String detail) {
        System.out.printf("[%s] %s  %s%n", ok ? "PASS" : "FAIL", name, detail);
        if (ok) pass++; else fail++;
    }
    public static void main(String[] a) throws Exception {
        Path f = Files.createTempFile("buspass", ".csv");
        BusPassFileStore s = new BusPassFileStore(f);
        s.add("P1001", "192311001", "Arun", 12, "Tambaram", "2027-03-31", 4500);
        s.add("P1002", "192311002", "Meena, K", 7, "Guindy", "2027-03-31", 3800);   // comma in name
        s.add("P1003", "192311003", "Divya", 12, "Adyar", "2027-03-31", 4100);

        // T1 normal
        String[] r = s.find("P1002");
        check("T1 find existing (comma in name)", r != null && r[2].equals("Meena, K"), Arrays.toString(r));
        check("T2 find non-existent id", s.find("P9999") == null, "returned null");
        check("T3 list route 12", s.listByRoute(12).size() == 2, "2 passes on route 12");

        // damaged lines
        Files.writeString(f, "P1004,192311004,Kiran\n"                          // wrong field count
                + "P1005,192311005,Latha,7,Adyar,2027-03-31,abc\n"              // non-numeric amount
                + "\n"                                                         // empty line
                + "P1006,192311006,Ravi,xx,Adyar,2027-03-31,100.0\n",           // non-numeric route
                StandardOpenOption.APPEND);
        s.add("P1007", "192311007", "Sana", 7, "Velachery", "2027-03-31", 3900);
        check("T4 search continues past damaged lines", s.find("P1007") != null, "P1007 found after 4 bad lines");
        check("T5 damaged lines reported", s.damagedReport().size() == 4, s.damagedReport().size() + " reported");
        s.damagedReport().forEach(x -> System.out.println("      " + x));
        check("T6 missing file", new BusPassFileStore(Path.of("/tmp/no_such_file.csv")).find("P1") == null, "null, no exception");
        s.damagedReport().clear();
        check("T7 list route 7 skips bad lines", s.listByRoute(7).size() == 2, "P1002, P1007");

        // incomplete write (power failure) : last line without newline / truncated
        Files.writeString(f, "P1008,192311008,Dev,7,Gui", StandardOpenOption.APPEND);
        s.damagedReport().clear();
        check("T8 truncated last line rejected", s.find("P1008") == null && s.damagedReport().size() >= 1,
                s.damagedReport().get(s.damagedReport().size() - 1));
        System.out.println("\nPassed " + pass + ", failed " + fail);
        Files.delete(f);
    }
}
