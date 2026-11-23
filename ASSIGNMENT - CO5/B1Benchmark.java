import java.io.*;
import java.nio.file.*;
import java.util.*;

/** B1.3 buffering measurements, B1.4 growth measurements, B1 alternative (HashMap). */
public class B1Benchmark {
    static Path makeLog(long mb) throws IOException {
        Path p = Files.createTempFile("log" + mb + "mb", ".txt");
        long target = mb * 1024 * 1024, written = 0;
        try (BufferedWriter w = Files.newBufferedWriter(p)) {
            int i = 0;
            while (written < target) {
                String s = "2026-11-23 09:" + (i % 60) + ",192311" + (i % 999) + ",PRESENT,Block-A";
                w.write(s); w.newLine(); written += s.length() + 1; i++;
            }
        }
        return p;
    }
    static double ms(long t) { return t / 1e6; }

    public static void main(String[] args) throws Exception {
        System.out.println("=== B1.3 Line counting: unbuffered vs buffered vs readAllLines ===");
        System.out.printf("%-8s %-12s %-14s %-12s %-14s %-10s%n", "Size", "lines", "unbuffered ms", "buffered ms", "readAllLines ms", "speedup");
        for (long mb : new long[]{1, 10, 50}) {
            Path p = makeLog(mb);
            LineCounter.countBuffered(p);                       // warm-up
            long t = System.nanoTime(); long a = LineCounter.countUnbuffered(p); double tu = ms(System.nanoTime() - t);
            t = System.nanoTime(); long b = LineCounter.countBuffered(p);   double tb = ms(System.nanoTime() - t);
            t = System.nanoTime(); long c = LineCounter.countReadAll(p);    double tr = ms(System.nanoTime() - t);
            if (a != b || b != c) throw new IllegalStateException("counts differ");
            System.out.printf("%-8s %-12d %-14.1f %-12.1f %-14.1f %-10.0fx%n", mb + " MB", a, tu, tb, tr, tu / tb);
            Files.delete(p);
        }

        System.out.println("\n=== B1.4 Find-pass growth (worst case: id absent / last record) ===");
        System.out.printf("%-10s %-14s %-16s %-18s%n", "records", "file scan ms", "HashMap build ms", "HashMap lookup us");
        for (int n : new int[]{1000, 5000, 10000, 25000, 50000, 100000}) {
            Path p = Files.createTempFile("passes", ".csv");
            BusPassFileStore s = new BusPassFileStore(p);
            try (BufferedWriter w = Files.newBufferedWriter(p)) {
                for (int i = 0; i < n; i++)
                    w.write("P" + (100000 + i) + ",19231" + i + ",Student" + i + "," + (i % 40) + ",Stop" + (i % 15) + ",2027-03-31," + (3000 + i % 2000) + ".0\n");
            }
            String target = "P" + (100000 + n - 1);
            s.find(target);                                  // warm-up
            int reps = 20; long t = System.nanoTime();
            for (int i = 0; i < reps; i++) s.find(target);
            double scan = ms(System.nanoTime() - t) / reps;
            t = System.nanoTime();
            Map<String, String[]> map = new HashMap<>();
            try (BufferedReader r = Files.newBufferedReader(p)) {
                String line; while ((line = r.readLine()) != null) { String[] f = line.split(",", -1); map.put(f[0], f); }
            }
            double build = ms(System.nanoTime() - t);
            t = System.nanoTime();
            for (int i = 0; i < 100000; i++) map.get(target);
            double look = (System.nanoTime() - t) / 1e3 / 100000;
            System.out.printf("%-10d %-14.2f %-16.2f %-18.3f%n", n, scan, build, look);
            Files.delete(p);
        }
    }
}
