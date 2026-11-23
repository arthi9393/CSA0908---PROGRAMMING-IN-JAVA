import java.io.*;
import java.nio.file.*;

public class LineCounter {
    /** A2 original: one read() call (one OS call) per byte. */
    public static long countUnbuffered(Path p) throws IOException {
        long lines = 0;
        try (FileInputStream in = new FileInputStream(p.toFile())) {
            int b;
            while ((b = in.read()) != -1) if (b == '\n') lines++;
        }
        return lines;
    }
    /** A2 redesign: BufferedInputStream (8 KB buffer) + bulk read into a 64 KB array. */
    public static long countBuffered(Path p) throws IOException {
        long lines = 0;
        byte[] buf = new byte[64 * 1024];
        try (InputStream in = new BufferedInputStream(new FileInputStream(p.toFile()), 64 * 1024)) {
            int n;
            while ((n = in.read(buf)) != -1)
                for (int i = 0; i < n; i++) if (buf[i] == '\n') lines++;
        }
        return lines;
    }
    /** Alternative concept: read the whole file into memory. O(file size) memory. */
    public static long countReadAll(Path p) throws IOException {
        return Files.readAllLines(p).size();
    }
}
