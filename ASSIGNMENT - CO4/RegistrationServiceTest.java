import java.nio.file.*;
import java.util.List;

/** Task 6 test cases (normal, invalid, duplicate, paging, filter, export). Run: java RegistrationServiceTest */
public class RegistrationServiceTest {
    static int failed = 0;
    static void check(String n, boolean ok) { System.out.println((ok ? "PASS  " : "FAIL  ") + n); if (!ok) failed++; }
    static String attempt(RegistrationService s, String r, String n, String e, String ev) {
        try { s.register(r, n, e, ev); return "OK"; }
        catch (IllegalArgumentException x) { return "INVALID"; }
        catch (DuplicateRegistrationException x) { return "DUPLICATE"; }
        catch (DaoException x) { return "DAO"; }
    }
    public static void main(String[] a) throws Exception {
        RegistrationService s = new RegistrationService(new InMemoryRegistrationDao());
        String ev = "Hackathon 2026";
        check("TC1 normal registration accepted", attempt(s, "SIM20001", "Asha Kumar", "asha@example.edu", ev).equals("OK"));
        check("TC2 same student, different event accepted", attempt(s, "SIM20001", "Asha Kumar", "asha@example.edu", "Quiz Bowl").equals("OK"));
        check("TC3 duplicate (same student, same event) rejected", attempt(s, "SIM20001", "Asha Kumar", "asha@example.edu", ev).equals("DUPLICATE"));
        check("TC4 duplicate detected even with lower-case reg no", attempt(s, "sim20001", "Asha Kumar", "asha@example.edu", ev).equals("DUPLICATE"));
        check("TC5 empty register number rejected", attempt(s, "", "Ravi", "ravi@example.edu", ev).equals("INVALID"));
        check("TC6 short register number rejected", attempt(s, "AB1", "Ravi", "ravi@example.edu", ev).equals("INVALID"));
        check("TC7 digits-only name rejected", attempt(s, "SIM20002", "12345", "ravi@example.edu", ev).equals("INVALID"));
        check("TC8 bad email rejected", attempt(s, "SIM20002", "Ravi Singh", "not-an-email", ev).equals("INVALID"));
        check("TC9 no event selected rejected", attempt(s, "SIM20002", "Ravi Singh", "ravi@example.edu", null).equals("INVALID"));
        check("TC10 SQL-injection style input rejected", attempt(s, "x'; DROP TABLE registrations;--", "Ravi Singh", "ravi@example.edu", ev).equals("INVALID"));
        check("TC11 rejected inputs saved nothing (count still 1 for Hackathon)", s.count(ev) == 1);
        for (int i = 0; i < 45; i++) attempt(s, String.format("BULK%05d", i), "Student " + (char) ('A' + i % 26), "b" + i + "@example.edu", "Cultural Night");
        check("TC12 filter by event counts only that event", s.count("Cultural Night") == 45 && s.count(null) == 47);
        check("TC13 page 1 has 20 rows, page 3 has 5 rows", s.page("Cultural Night", 0).size() == 20 && s.page("Cultural Night", 2).size() == 5);
        Path f = Files.createTempFile("reg", ".csv");
        check("TC14 CSV export writes header + 45 rows", s.exportCsv("Cultural Night", f) == 45 && Files.readAllLines(f).size() == 46);
        System.out.println(failed == 0 ? "ALL TESTS PASSED" : failed + " TEST(S) FAILED");
        System.exit(failed == 0 ? 0 : 1);
    }
}
