import java.nio.file.*;

/** B2 second bullet: deliberately damaged bus-pass file and the file store's response. */
public class DamagedFileDemo {
    public static void main(String[] a) throws Exception {
        Path f = Files.createTempFile("damaged", ".csv");
        Files.writeString(f,
            "P2001,192311001,Arun,12,Tambaram,2027-03-31,4500.0\n" +   // good
            "P2002,192311002,Meena,7,Guindy\n" +                       // wrong number of fields
            "P2003,192311003,Divya,7,Adyar,2027-03-31,forty-five\n" +  // non-numeric amount
            "\n" +                                                     // empty line
            "P2004,192311004,Ravi,5,Adyar,2027-03-31,3800.0\n");       // good
        BusPassFileStore s = new BusPassFileStore(f);
        System.out.println("find P2004 -> " + java.util.Arrays.toString(s.find("P2004")));
        s.damagedReport().clear();
        System.out.println("find P2002 -> " + java.util.Arrays.toString(s.find("P2002")));
        System.out.println("Damaged-line report (full scan, P2002 does not exist as a valid record):");
        s.damagedReport().forEach(x -> System.out.println("  " + x));
        Files.delete(f);
    }
}
