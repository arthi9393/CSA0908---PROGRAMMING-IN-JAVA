import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Evidence generator (run under a virtual display): writes PNG screenshots for Tasks 3 and 4.
 *  Screenshots are rendered by painting the real windows to images; replace/add your own real screenshots as well. */
public class Shots {
    static BufferedImage paintOf(Component c, int w, int h) {
        Window win = (Window) SwingUtilities.getAncestorOfClass(Window.class, c); if (c instanceof Window) win = (Window) c;
        if (win != null && !win.isDisplayable()) { win.setVisible(true); try { Thread.sleep(400); } catch (Exception e) { } }
        if (c instanceof Window) { c.setSize(w, h); } else { win.setSize(w + win.getInsets().left + win.getInsets().right, h + win.getInsets().top + win.getInsets().bottom); }
        try { Thread.sleep(300); } catch (Exception e) { }
        c.doLayout(); c.validate();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics(); g.setColor(Color.WHITE); g.fillRect(0, 0, w, h); g.setColor(Color.BLACK);
        c.printAll(g); g.dispose(); return img;
    }
    static void save(BufferedImage i, String name) throws Exception { ImageIO.write(i, "png", new File(name)); System.out.println("wrote " + name); }
    static boolean same(BufferedImage a, BufferedImage b) {
        for (int y = 0; y < a.getHeight(); y++) for (int x = 0; x < a.getWidth(); x++) if (a.getRGB(x, y) != b.getRGB(x, y)) return false;
        return true;
    }
    static void click(Component c, int x, int y) {
        c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, x, y, 1, false));
    }
    public static void main(String[] a) throws Exception {
        // ---- Task 3.4 : three window sizes
        LibrarySearch lib = new LibrarySearch();
        Container cp = lib.getContentPane();
        int[][] sizes = {{560, 380}, {800, 600}, {520, 260}};
        String[] names = {"Task3_resize_small_560x380.png", "Task3_resize_large_800x600.png", "Task3_resize_min_520x260.png"};
        for (int i = 0; i < 3; i++) save(paintOf(cp, sizes[i][0], sizes[i][1]), names[i]);

        // ---- Task 4 : DrawPad - real window on the virtual screen, captured with Robot; real mouse events via Robot
        Robot robot = new Robot();
        DrawPad pad = new DrawPad();
        pad.setLocation(0, 0); pad.setSize(400, 300); pad.setVisible(true); Thread.sleep(800);
        Rectangle r = pad.getBounds();
        BufferedImage before = robot.createScreenCapture(r);
        save(before, "Task4_DrawPad_before_clicks.png");
        Point o = pad.getLocationOnScreen();
        int[][] pts = {{50, 80}, {120, 150}, {200, 100}, {300, 220}};
        for (int[] p : pts) { robot.mouseMove(o.x + p[0], o.y + p[1]); robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); Thread.sleep(250); }
        Thread.sleep(500);
        BufferedImage after = robot.createScreenCapture(r);
        save(after, "Task4_DrawPad_after_4_clicks.png");
        pad.setVisible(false); Thread.sleep(500); pad.setVisible(true); Thread.sleep(800);   // hide + show forces a full repaint, like restore after minimise
        BufferedImage restored = robot.createScreenCapture(pad.getBounds());
        save(restored, "Task4_DrawPad_after_repaint_restore.png");
        System.out.println("Image changed after 4 clicks: " + !same(before, after));
        System.out.println("Dots still present after hide/show repaint (images identical): " + same(after, restored));
        System.exit(0);
    }
}
