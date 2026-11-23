import java.io.*;
import java.nio.file.*;

/** Creates sample_passes.csv: 20,000 good lines + deliberately bad ones. */
public class MakeSampleFile {
    public static void main(String[] a) throws Exception {
        try (BufferedWriter w = Files.newBufferedWriter(Path.of("sample_passes.csv"))) {
            for (int i = 0; i < 20000; i++) {
                w.write("P" + (200000 + i) + ",19231" + (10000 + i) + ",Student" + i + "," + (i % 40 + 1) + ",Stop" + (i % 15) + ",2027-03-31," + (3000 + i % 2000) + ".0\n");
                if (i == 3000)  w.write("P300001,19231999,Short,5\n");                                   // wrong field count
                if (i == 6000)  w.write("P300002,19231998,BadAmt,5,StopX,2027-03-31,ABC\n");             // non-numeric amount
                if (i == 9000)  w.write("\n");                                                          // empty line
                if (i == 12000) w.write("P300003,19231997,NegAmt,5,StopX,2027-03-31,-50.0\n");           // negative amount
                if (i == 15000) w.write("P200010,19231996,Duplicate,5,StopX,2027-03-31,3000.0\n");       // duplicate pass id
                if (i == 17000) w.write("P300004,19231995,BadRoute,zz,StopX,2027-03-31,3000.0\n");       // non-numeric route
            }
        }
        System.out.println("sample_passes.csv written");
    }
}
