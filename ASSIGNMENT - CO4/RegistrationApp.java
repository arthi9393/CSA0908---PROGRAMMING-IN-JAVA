import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Properties;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Task 6 - main window: menu bar, event filter, paged JTable. Every database call runs in a SwingWorker. */
public class RegistrationApp extends JFrame {
    private final RegistrationService service;
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Register No", "Name", "Email", "Event"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable table = new JTable(model);
    private final JComboBox<String> filter = new JComboBox<>();
    private final JButton prev = new JButton("Prev"), next = new JButton("Next");
    private final JLabel pageLabel = new JLabel(" "), status = new JLabel("Ready");
    private int pageIndex = 0, total = 0;
    private boolean loadingFilter = false;

    public RegistrationApp(RegistrationService service, String modeText) {
        super("Campus Event Registrations");
        this.service = service;
        setJMenuBar(buildMenu());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Event:")); top.add(filter);
        JPanel bottom = new JPanel(new BorderLayout());
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER)); nav.add(prev); nav.add(pageLabel); nav.add(next);
        bottom.add(nav, BorderLayout.CENTER); bottom.add(status, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH); add(new JScrollPane(table), BorderLayout.CENTER); add(bottom, BorderLayout.SOUTH);
        filter.addActionListener(e -> { if (!loadingFilter) { pageIndex = 0; load(); } });
        prev.addActionListener(e -> { if (pageIndex > 0) { pageIndex--; load(); } });
        next.addActionListener(e -> { if ((pageIndex + 1) * RegistrationService.PAGE_SIZE < total) { pageIndex++; load(); } });
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(720, 480);
        status.setText(modeText);
        loadEvents();
    }

    private JMenuBar buildMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File"), reg = new JMenu("Registrations"), help = new JMenu("Help");
        JMenuItem export = new JMenuItem("Export CSV..."), exit = new JMenuItem("Exit");
        JMenuItem add = new JMenuItem("New Registration..."), refresh = new JMenuItem("Refresh"), about = new JMenuItem("About");
        add.setAccelerator(KeyStroke.getKeyStroke("control N")); refresh.setAccelerator(KeyStroke.getKeyStroke("F5"));
        export.addActionListener(e -> exportCsv()); exit.addActionListener(e -> dispose());
        add.addActionListener(e -> openForm()); refresh.addActionListener(e -> load());
        about.addActionListener(e -> JOptionPane.showMessageDialog(this, "Campus Event Registration\nSwing + JDBC + MySQL", "About", JOptionPane.INFORMATION_MESSAGE));
        file.add(export); file.addSeparator(); file.add(exit); reg.add(add); reg.add(refresh); help.add(about);
        bar.add(file); bar.add(reg); bar.add(help);
        return bar;
    }

    void loadEvents() {
        new SwingWorker<List<String>, Void>() {
            @Override protected List<String> doInBackground() throws Exception { return service.events(); }
            @Override protected void done() {
                try {
                    loadingFilter = true; filter.removeAllItems(); filter.addItem("All events");
                    for (String s : get()) filter.addItem(s);
                    loadingFilter = false; load();
                } catch (Exception ex) { loadingFilter = false; status.setText("Error: " + rootMessage(ex)); }
            }
        }.execute();
    }

    private String selectedEvent() { String s = (String) filter.getSelectedItem(); return s == null || s.equals("All events") ? null : s; }

    /** Loads ONE page (SQL LIMIT/OFFSET) in a background thread, then fills the table on the event thread. */
    void load() {
        final String ev = selectedEvent(); final int p = pageIndex;
        status.setText("Loading..."); prev.setEnabled(false); next.setEnabled(false);
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() throws Exception { return new Object[]{service.count(ev), service.page(ev, p)}; }
            @Override @SuppressWarnings("unchecked") protected void done() {
                try {
                    Object[] r = get(); total = (Integer) r[0]; List<Registration> rows = (List<Registration>) r[1];
                    model.setRowCount(0);
                    for (Registration x : rows) model.addRow(new Object[]{x.regNo(), x.name(), x.email(), x.eventName()});
                    int pages = Math.max(1, (total + RegistrationService.PAGE_SIZE - 1) / RegistrationService.PAGE_SIZE);
                    pageLabel.setText("Page " + (pageIndex + 1) + " of " + pages + "  (" + total + " registrations)");
                    prev.setEnabled(pageIndex > 0); next.setEnabled(pageIndex + 1 < pages);
                    status.setText("Loaded " + rows.size() + " rows.");
                } catch (Exception ex) { status.setText("Error: " + rootMessage(ex)); }
            }
        }.execute();
    }

    private void openForm() {
        new SwingWorker<List<String>, Void>() {
            @Override protected List<String> doInBackground() throws Exception { return service.events(); }
            @Override protected void done() {
                try { new RegistrationDialog(RegistrationApp.this, service, get(), () -> load()).setVisible(true); }
                catch (Exception ex) { status.setText("Error: " + rootMessage(ex)); }
            }
        }.execute();
    }

    private void exportCsv() {
        JFileChooser fc = new JFileChooser(); fc.setSelectedFile(new File("registrations.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        final Path path = fc.getSelectedFile().toPath(); final String ev = selectedEvent();
        status.setText("Exporting...");
        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() throws Exception { return service.exportCsv(ev, path); }
            @Override protected void done() {
                try { status.setText("Exported " + get() + " rows to " + path.getFileName()); }
                catch (Exception ex) { status.setText("Export failed: " + rootMessage(ex)); }
            }
        }.execute();
    }

    private static String rootMessage(Exception ex) { Throwable t = ex.getCause() != null ? ex.getCause() : ex; return t.getMessage(); }

    /** Starts with MySQL if db.properties exists, otherwise in demo mode with in-memory data. */
    public static void main(String[] args) {
        RegistrationDao dao; String mode;
        File cfg = new File("db.properties");
        if (cfg.exists()) {
            try (FileInputStream in = new FileInputStream(cfg)) {
                Properties p = new Properties(); p.load(in);
                dao = new MySqlRegistrationDao(p.getProperty("url"), p.getProperty("user"), p.getProperty("password")); mode = "Connected to MySQL";
            } catch (IOException e) { throw new UncheckedIOException(e); }
        } else { dao = new InMemoryRegistrationDao(300); mode = "DEMO MODE: no db.properties found (in-memory data)"; }
        final RegistrationDao d = dao; final String m = mode;
        SwingUtilities.invokeLater(() -> new RegistrationApp(new RegistrationService(d), m).setVisible(true));
    }
}
