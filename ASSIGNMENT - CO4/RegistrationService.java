import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

/** Business layer: validation rules, duplicate handling, paging and CSV export. No Swing code in this class. */
public class RegistrationService {
    public static final int PAGE_SIZE = 20;
    private static final Pattern REG = Pattern.compile("[A-Za-z0-9]{6,15}");
    private static final Pattern NAME = Pattern.compile("[A-Za-z][A-Za-z .'-]{1,99}");
    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private final RegistrationDao dao;
    public RegistrationService(RegistrationDao dao) { this.dao = dao; }

    /** Returns a list of readable problems (empty list = valid). Cheap, so it is safe to call on the event thread. */
    public List<String> validate(String regNo, String name, String email, String event) {
        List<String> errors = new ArrayList<>();
        if (regNo == null || !REG.matcher(regNo.trim()).matches()) errors.add("Register number must be 6-15 letters/digits.");
        if (name == null || !NAME.matcher(name.trim()).matches()) errors.add("Name must be 2-100 letters (spaces, . ' - allowed).");
        if (email == null || !EMAIL.matcher(email.trim()).matches()) errors.add("Email address is not valid.");
        if (event == null || event.isBlank()) errors.add("Select an event.");
        return errors;
    }

    /** Validates, then inserts. The database's UNIQUE key is the final guard against duplicates. */
    public void register(String regNo, String name, String email, String event) throws IllegalArgumentException, DuplicateRegistrationException, DaoException {
        List<String> errors = validate(regNo, name, email, event);
        if (!errors.isEmpty()) throw new IllegalArgumentException(String.join(" ", errors));
        dao.insert(new Registration(regNo.trim().toUpperCase(), name.trim(), email.trim().toLowerCase(), event));
    }

    public List<String> events() throws DaoException { return dao.findEventNames(); }
    public int count(String event) throws DaoException { return dao.count(event); }
    public List<Registration> page(String event, int pageIndex) throws DaoException { return dao.findPage(event, PAGE_SIZE, pageIndex * PAGE_SIZE); }

    /** Exports every registration of the chosen event (or all) to CSV, reading in pages of 1000 so memory stays small. */
    public int exportCsv(String event, Path file) throws DaoException, IOException {
        int n = 0;
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write("reg_no,name,email,event\n");
            for (int off = 0; ; off += 1000) {
                List<Registration> chunk = dao.findPage(event, 1000, off);
                if (chunk.isEmpty()) break;
                for (Registration r : chunk) { w.write(csv(r.regNo()) + "," + csv(r.name()) + "," + csv(r.email()) + "," + csv(r.eventName()) + "\n"); n++; }
            }
        }
        return n;
    }
    private static String csv(String s) { return '"' + s.replace("\"", "\"\"") + '"'; }
}
