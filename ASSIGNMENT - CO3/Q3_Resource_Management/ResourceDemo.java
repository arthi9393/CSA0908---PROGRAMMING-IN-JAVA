import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ResourceDemo {
    static void manualCopy(List<Path> inputs, Path output) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = Files.newBufferedWriter(output);
            for (Path input : inputs) {
                // A reader opened in try-with-resources even in this manual-writer example.
                try (BufferedReader reader = Files.newBufferedReader(input)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }
        } finally {
            if (writer != null) writer.close();
        }
    }

    static void safeCopy(List<Path> inputs, Path output) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            for (Path input : inputs) {
                try (BufferedReader reader = Files.newBufferedReader(input)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Path dir = Paths.get("Q3_Resource_Management", "input");
        Files.createDirectories(dir);
        List<Path> inputs = Arrays.asList(dir.resolve("store1.txt"), dir.resolve("store2.txt"));
        Files.write(inputs.get(0), Arrays.asList("Store 1 sales: 1200", "Store 1 returns: 50"));
        Files.write(inputs.get(1), Arrays.asList("Store 2 sales: 900", "Store 2 returns: 20"));

        manualCopy(inputs, dir.resolve("manual-report.txt"));
        safeCopy(inputs, dir.resolve("safe-report.txt"));
        System.out.println("Manual and try-with-resources reports generated.");
        boolean same = Files.mismatch(
                dir.resolve("manual-report.txt"), dir.resolve("safe-report.txt")) == -1;
        System.out.println("Reports equal: " + same);
    }
}
