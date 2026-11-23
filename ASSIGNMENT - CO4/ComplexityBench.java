import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.*;

/** Measurements for the "Alternative Concept and Complexity Analysis" parts. Run: java ComplexityBench (headless is fine). */
public class ComplexityBench {
    static long sink = 0;
    static long used() { System.gc(); System.gc(); Runtime r = Runtime.getRuntime(); return r.totalMemory() - r.freeMemory(); }

    public static void main(String[] a) throws Exception {
        System.out.println("=== Task 1 alt: dispatch with if-else chain (equals) vs HashMap lookup, worst case (last command) ===");
        for (int k : new int[]{3, 10, 100, 1000}) {
            String[] cmds = new String[k]; for (int i = 0; i < k; i++) cmds[i] = "cmd" + i;
            Map<String, Runnable> map = new HashMap<>(); for (String c : cmds) map.put(c, () -> sink++);
            String target = new String(cmds[k - 1]);          // not interned: equals() must compare chars
            int N = 2_000_000;
            for (int w = 0; w < 2; w++) {                                // warm-up round then measured round
                long t0 = System.nanoTime();
                for (int n = 0; n < N; n++) { for (int i = 0; i < k; i++) if (cmds[i].equals(target)) { sink++; break; } }
                long chain = System.nanoTime() - t0;
                t0 = System.nanoTime();
                for (int n = 0; n < N; n++) map.get(target).run();
                long hm = System.nanoTime() - t0;
                if (w == 1) System.out.printf("k=%-5d if-else chain: %8.1f ns/event   HashMap/own listener: %6.1f ns/event%n", k, chain / (double) N, hm / (double) N);
            }
        }

        System.out.println("\n=== Task 2 alt: Map<String,List<Doctor>> lookup (in memory) ===");
        Map<String, List<String>> docs = new HashMap<>();
        for (int d = 0; d < 20; d++) { List<String> l = new ArrayList<>(); for (int m = 0; m < 15; m++) l.add("Dr " + d + "-" + m); docs.put("Dept" + d, l); }
        long t0 = System.nanoTime(); int N2 = 5_000_000; for (int i = 0; i < N2; i++) sink += docs.get("Dept" + (i % 20)).size();
        System.out.printf("map lookup: %.1f ns per department change (a database round trip is typically 0.5-50 ms)%n", (System.nanoTime() - t0) / (double) N2);

        System.out.println("\n=== Task 3 alt: layout pass time and container count (8 components) ===");
        System.out.printf("nested Border+Flow : %.1f us per layout pass, %d panels (+1 scroll pane)%n", layoutTime(nested()), 3);
        System.out.printf("GridBagLayout      : %.1f us per layout pass, %d container%n", layoutTime(gridBag()), 1);
        System.out.printf("null + setBounds   : %.1f us per layout pass, %d container (no resizing logic at all)%n", layoutTime(absolute()), 1);

        System.out.println("\n=== Task 4 alt: repaint cost, redraw n stored points vs copy one BufferedImage (400x300) ===");
        for (int n : new int[]{100, 10_000, 200_000}) {
            Point[] pts = new Point[n]; Random r = new Random(1); for (int i = 0; i < n; i++) pts[i] = new Point(r.nextInt(400), r.nextInt(300));
            BufferedImage screen = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB), cache = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
            Graphics2D cg = cache.createGraphics(); for (Point p : pts) cg.fillOval(p.x - 3, p.y - 3, 6, 6); cg.dispose();
            Graphics2D g = screen.createGraphics(); int reps = n >= 200_000 ? 5 : 50;
            for (int w = 0; w < 3; w++) { for (Point p : pts) g.fillOval(p.x - 3, p.y - 3, 6, 6); g.drawImage(cache, 0, 0, null); }
            t0 = System.nanoTime(); for (int i = 0; i < reps; i++) for (Point p : pts) g.fillOval(p.x - 3, p.y - 3, 6, 6);
            double redraw = (System.nanoTime() - t0) / 1e6 / reps;
            t0 = System.nanoTime(); for (int i = 0; i < reps; i++) g.drawImage(cache, 0, 0, null);
            double blit = (System.nanoTime() - t0) / 1e6 / reps; g.dispose();
            System.out.printf("n=%-7d redraw all points: %9.3f ms/repaint   copy image: %7.3f ms/repaint%n", n, redraw, blit);
        }
        System.out.printf("memory: 1 Point object ~ 16-24 bytes -> n=200,000 points ~ %.1f MB;  400x300 int image = %.2f MB (fixed)%n", 200_000 * 24 / 1e6, 400 * 300 * 4 / 1e6);

        System.out.println("\n=== Task 6 alt: heap needed to hold all 100,000 table rows vs one page of 20 ===");
        long before = used(); List<Object[]> all = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) all.add(new Object[]{"S" + String.format("%07d", i), "Student " + i, "s" + i + "@example.edu", "Hackathon 2026"});
        long after = used(); System.out.printf("100,000 rows : about %.1f MB%n", (after - before) / 1e6); sink += all.size(); all = null;
        before = used(); List<Object[]> pg = new ArrayList<>();
        for (int i = 0; i < 20; i++) pg.add(new Object[]{"S" + String.format("%07d", i), "Student " + i, "s" + i + "@example.edu", "Hackathon 2026"});
        after = used(); System.out.printf("one page (20): about %.4f MB%n", Math.max(0, after - before) / 1e6); sink += pg.size();
        System.out.println("(the database load time itself needs your MySQL: see report for how to time it with seed_100k.sql)");
        System.out.println("ignore: " + (sink == 42));
    }

    static List<Component> parts() {
        return List.of(new JLabel("Search:"), new JTextField(15), new JComboBox<>(new String[]{"All"}), new JButton("Search"),
                       new JTextArea(), new JButton("Prev"), new JButton("Next"), new JButton("Reserve"));
    }
    static JPanel nested() {
        List<Component> c = parts(); JPanel root = new JPanel(new BorderLayout());
        JPanel row = new JPanel(new FlowLayout()); for (int i = 0; i < 4; i++) row.add(c.get(i));
        JPanel btn = new JPanel(new FlowLayout()); for (int i = 5; i < 8; i++) btn.add(c.get(i));
        root.add(row, BorderLayout.NORTH); root.add(new JScrollPane(c.get(4)), BorderLayout.CENTER); root.add(btn, BorderLayout.SOUTH);
        return root;
    }
    static JPanel gridBag() {
        List<Component> c = parts(); JPanel root = new JPanel(new GridBagLayout()); GridBagConstraints g = new GridBagConstraints();
        for (int i = 0; i < 4; i++) { g.gridx = i; g.gridy = 0; root.add(c.get(i), g); }
        g.gridx = 0; g.gridy = 1; g.gridwidth = 4; g.weightx = 1; g.weighty = 1; g.fill = GridBagConstraints.BOTH; root.add(new JScrollPane(c.get(4)), g);
        g.gridwidth = 1; g.weightx = 0; g.weighty = 0; g.fill = GridBagConstraints.NONE;
        for (int i = 5; i < 8; i++) { g.gridx = i - 5; g.gridy = 2; root.add(c.get(i), g); }
        return root;
    }
    static JPanel absolute() {
        List<Component> c = parts(); JPanel root = new JPanel(null); int x = 5;
        for (int i = 0; i < 4; i++) { Component k = c.get(i); k.setBounds(x, 5, 90, 25); x += 95; root.add(k); }
        Component area = c.get(4); area.setBounds(5, 40, 380, 300); root.add(area);
        for (int i = 5; i < 8; i++) { Component k = c.get(i); k.setBounds(5 + (i - 5) * 95, 350, 90, 25); root.add(k); }
        return root;
    }
    static double layoutTime(JPanel p) {
        p.setSize(560, 380); for (int i = 0; i < 2000; i++) { p.invalidate(); p.doLayout(); validateTree(p); }
        int N = 20000; long t0 = System.nanoTime();
        for (int i = 0; i < N; i++) { p.invalidate(); validateTree(p); }
        return (System.nanoTime() - t0) / 1e3 / N;
    }
    static void validateTree(Container c) { c.doLayout(); for (Component k : c.getComponents()) if (k instanceof Container ct) validateTree(ct); }
}
