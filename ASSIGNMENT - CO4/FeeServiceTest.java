/** Plain-Java tests for FeeService (no framework needed): run with  java FeeServiceTest */
public class FeeServiceTest {
    static int failed = 0;
    static void check(String name, boolean ok) { System.out.println((ok ? "PASS  " : "FAIL  ") + name); if (!ok) failed++; }
    static String err(FeeService s, String r, String a) { try { s.pay(r, a); return null; } catch (FeeException e) { return e.getMessage(); } }

    public static void main(String[] args) throws Exception {
        InMemoryFeeRepository repo = new InMemoryFeeRepository();
        FeeService s = new FeeService(repo);
        check("empty register number rejected", err(s, "", "100") != null);
        check("empty amount rejected", err(s, "REG12345", "") != null);
        check("non-numeric amount rejected", err(s, "REG12345", "abc") != null);
        check("negative amount rejected", err(s, "REG12345", "-5") != null);
        check("zero amount rejected", err(s, "REG12345", "0") != null);
        check("SQL-injection style register number rejected", err(s, "x'); DROP TABLE fees;--", "10") != null);
        check("nothing saved after invalid inputs", repo.count() == 0);
        check("receipt before payment gives friendly error", threw(s));
        String r = s.pay("REG12345", "2500.50");
        check("valid payment returns receipt", r.contains("REG12345") && r.contains("2500.50"));
        check("valid payment saved once", repo.count() == 1);
        check("receipt after payment works", s.lastReceipt().equals(r));
        System.out.println(failed == 0 ? "ALL TESTS PASSED" : failed + " TEST(S) FAILED");
        System.exit(failed == 0 ? 0 : 1);
    }
    static boolean threw(FeeService s) { try { s.lastReceipt(); return false; } catch (FeeException e) { return true; } }
}
