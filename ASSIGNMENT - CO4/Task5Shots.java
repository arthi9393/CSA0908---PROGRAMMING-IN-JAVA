import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Drives the real Converter window under a virtual display and captures screenshots (valid and invalid amount). */
public class Task5Shots {
    public static void main(String[] a) throws Exception {
        Robot robot = new Robot();
        Converter f = new Converter();
        f.setLocation(0, 0); f.setVisible(true); Thread.sleep(800);
        Component[] c = f.getContentPane().getComponents();   // INR label, amount, target, button, result
        JTextField amount = (JTextField) c[1]; JButton btn = (JButton) c[3]; JLabel result = (JLabel) c[4];
        SwingUtilities.invokeAndWait(() -> { amount.setText("5000"); btn.doClick(); }); Thread.sleep(400);
        System.out.println("valid 5000 INR -> '" + result.getText() + "'");
        ImageIO.write(robot.createScreenCapture(f.getBounds()), "png", new java.io.File("Task5_converter_valid.png"));
        SwingUtilities.invokeAndWait(() -> { amount.setText("abc"); amount.postActionEvent(); }); Thread.sleep(400);   // Enter key path
        System.out.println("invalid 'abc' + Enter -> '" + result.getText() + "'");
        ImageIO.write(robot.createScreenCapture(f.getBounds()), "png", new java.io.File("Task5_converter_invalid.png"));
        System.exit(0);
    }
}
