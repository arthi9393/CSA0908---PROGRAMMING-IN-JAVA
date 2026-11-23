import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/** Task 3.3 - Campus Library Search screen.
 *  Layout: frame BorderLayout -> NORTH header (title + search row), CENTER results (grows), SOUTH (buttons + status). */
public class LibrarySearch extends JFrame {
    private static final String[][] CATALOGUE = {
        {"Java: The Complete Reference", "Programming"}, {"Operating System Concepts", "Computing"},
        {"Database System Concepts", "Computing"}, {"Clean Code", "Programming"},
        {"Computer Networks", "Computing"}, {"Engineering Mathematics", "Mathematics"},
        {"Linear Algebra and Its Applications", "Mathematics"}, {"Effective Java", "Programming"},
        {"Introduction to Algorithms", "Computing"}, {"Discrete Mathematics", "Mathematics"}};
    private static final int PAGE = 4;

    private final JTextField query = new JTextField(18);
    private final JComboBox<String> category = new JComboBox<>(new String[]{"All", "Programming", "Computing", "Mathematics"});
    private final JButton search = new JButton("Search"), prev = new JButton("Prev"), next = new JButton("Next"), reserve = new JButton("Reserve");
    private final JTextArea results = new JTextArea();
    private final JLabel status = new JLabel("Ready", SwingConstants.LEFT);
    private List<String> hits = new ArrayList<>();
    private int page = 0;

    public LibrarySearch() {
        super("Campus Library Search");
        setLayout(new BorderLayout(5, 5));                      // frame: BorderLayout (centre takes all spare space)

        JLabel title = new JLabel("Campus Library", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));  // keeps natural size of its components
        searchRow.add(new JLabel("Search:")); searchRow.add(query); searchRow.add(category); searchRow.add(search);
        JPanel header = new JPanel(new BorderLayout());          // title above search row
        header.add(title, BorderLayout.NORTH); header.add(searchRow, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        results.setEditable(false);
        add(new JScrollPane(results), BorderLayout.CENTER);      // the only region that grows/shrinks

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        buttonRow.add(prev); buttonRow.add(next); buttonRow.add(reserve);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(buttonRow, BorderLayout.CENTER); bottom.add(status, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        search.addActionListener(e -> doSearch());
        query.addActionListener(e -> doSearch());
        prev.addActionListener(e -> { if (page > 0) { page--; showPage(); } });
        next.addActionListener(e -> { if ((page + 1) * PAGE < hits.size()) { page++; showPage(); } });
        reserve.addActionListener(e -> status.setText(hits.isEmpty() ? "Nothing to reserve." : "Reserved first book on this page."));

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(560, 380);
        setMinimumSize(new Dimension(520, 260));
        doSearch();
    }

    private void doSearch() {
        String q = query.getText().trim().toLowerCase(), cat = (String) category.getSelectedItem();
        hits = new ArrayList<>();
        for (String[] b : CATALOGUE)
            if (b[0].toLowerCase().contains(q) && (cat.equals("All") || cat.equals(b[1]))) hits.add(b[0] + "  [" + b[1] + "]");
        page = 0; showPage();
    }

    private void showPage() {
        StringBuilder sb = new StringBuilder();
        int from = page * PAGE, to = Math.min(from + PAGE, hits.size());
        for (int i = from; i < to; i++) sb.append(i + 1).append(". ").append(hits.get(i)).append('\n');
        results.setText(sb.toString());
        status.setText(hits.isEmpty() ? "No results." : "Showing " + (from + 1) + "-" + to + " of " + hits.size());
        prev.setEnabled(page > 0); next.setEnabled(to < hits.size());
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new LibrarySearch().setVisible(true)); }
}
