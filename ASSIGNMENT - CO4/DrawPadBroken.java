import java.awt.*;
import java.awt.event.*;

/** The ORIGINAL faulty program from the assignment (Task 4) - compiles, but never handles a click. */
public class DrawPadBroken extends Frame {
    int count = 0; Label status = new Label("Clicks: 0");
    public DrawPadBroken() {
        add(status, BorderLayout.SOUTH);
        addMouseListener(new MouseAdapter() {
            public void mouseclicked(MouseEvent e) {          // BUG: lower-case 'c' - not an override
                count++;
                Graphics g = getGraphics(); g.fillOval(e.getX(), e.getY(), 6, 6);   // BUG 2: draws outside paint()
            }
        });
        setSize(400, 300); setVisible(true);
    }
    public static void main(String[] args) { new DrawPadBroken(); }
}
