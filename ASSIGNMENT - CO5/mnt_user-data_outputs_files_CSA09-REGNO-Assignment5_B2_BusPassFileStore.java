import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Full text-file store designed in Task A1 (7 fields, escaped delimiter, validation,
 * damaged-line reporting, atomic rewrite).
 * Line format:  passId,regNo,name,route,stop,validity(yyyy-MM-dd),amount
 * Escaping inside a field:  "\\" -> "\\\\"   "," -> "\\c"   newline -> "\\n"
 */
public class BusPassFileStore {
    public static final int FIELDS = 7;
    private final Path file;
    private final List<String> damaged = new ArrayList<>();   // report of rejected lines

    public BusPassFileStore(Path file) { this.file = file; }
    public List<String> damagedReport() { return damaged; }

    static String esc(String s) {
        return s.replace("\\", "\\\\").replace(",", "\\c").replace("\n", "\\n").replace("\r", "");
    }
    static String unesc(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                b.append(n == 'c' ? ',' : n == 'n' ? '\n' : n);
            } else b.append(c);
        }
        return b.toString();
    }

    /** Returns null if valid, otherwise the reason the line is damaged. */
    public static String validate(String line) {
        if (line.isBlank()) return "empty line";
        String[] f = line.split(",", -1);
        if (f.length != FIELDS) return "expected 7 fields but found " + f.length;
        if (f[0].isEmpty()) return "empty pass id";
        try { Integer.parseInt(f[3]); } catch (NumberFormatException e) { return "route not numeric: '" + f[3] + "'"; }
        try { Double.parseDouble(f[6]); } catch (NumberFormatException e) { return "amount not numeric: '" + f[6] + "'"; }
        return null;
    }

    /** (i) add: append one line, force it to disk. */
    public void add(String passId, String regNo, String name, int route, String stop,
                    String validity, double amount) throws IOException {
        String line = String.join(",", esc(passId), esc(regNo), esc(name),
                String.valueOf(route), esc(stop), validity, String.valueOf(amount));
        try (FileOutputStream fos = new FileOutputStream(file.toFile(), true);
             BufferedWriter w = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8))) {
            w.write(line);
            w.newLine();
            w.flush();
            fos.getFD().sync();         // survive power failure once add() returns
        }
    }

    /** (ii) find by pass id. Returns the unescaped fields, or null. */
    public String[] find(String passId) throws IOException {
        if (!Files.exists(file)) return null;               // missing file = no records
        try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line; int ln = 0;
            while ((line = r.readLine()) != null) {
                ln++;
                String why = validate(line);
                if (why != null) { damaged.add("line " + ln + ": " + why + "  -> [" + line + "]"); continue; }
                String[] f = line.split(",", -1);
                if (unesc(f[0]).equals(passId)) {
                    for (int i = 0; i < f.length; i++) f[i] = unesc(f[i]);
                    return f;
                }
            }
        }
        return null;
    }

    /** (iii) list all passes on a route. */
    public List<String[]> listByRoute(int route) throws IOException {
        List<String[]> out = new ArrayList<>();
        if (!Files.exists(file)) return out;
        try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line; int ln = 0;
            while ((line = r.readLine()) != null) {
                ln++;
                String why = validate(line);
                if (why != null) { damaged.add("line " + ln + ": " + why + "  -> [" + line + "]"); continue; }
                String[] f = line.split(",", -1);
                if (Integer.parseInt(f[3]) == route) {
                    for (int i = 0; i < f.length; i++) f[i] = unesc(f[i]);
                    out.add(f);
                }
            }
        }
        return out;
    }
}
