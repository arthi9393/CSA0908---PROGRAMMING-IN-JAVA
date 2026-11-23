import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Drives the real Task 6 windows under a virtual display (demo data) and captures the demonstration screenshots. */
public class Task6Shots {
    static Robot robot;
    static void shot(Window w, String name) throws Exception {
        Thread.sleep(500);
        ImageIO.write(robot.createScreenCapture(w.getBounds()), "png", new java.io.File(name)); System.out.println("wrote " + name);
    }
    public static void main(String[] a) throws Exception {
        robot = new Robot();
        RegistrationService svc = new RegistrationService(new InMemoryRegistrationDao());
        svc.register("SIM20001", "Asha Kumar", "asha@example.edu", "Hackathon 2026");
        for (int i = 0; i < 45; i++) svc.register(String.format("BULK%05d", i), "Student " + (char) ('A' + i % 26), "b" + i + "@example.edu", i % 3 == 0 ? "Cultural Night" : "Robotics Workshop");
        final RegistrationApp[] app = new RegistrationApp[1];
        SwingUtilities.invokeAndWait(() -> { app[0] = new RegistrationApp(svc, "DEMO MODE (in-memory data)"); app[0].setLocation(0, 0); app[0].setVisible(true); });
        Thread.sleep(1200);
        shot(app[0], "Task6_1_main_window_all_events.png");

        final RegistrationDialog[] d = new RegistrationDialog[1];
        SwingUtilities.invokeAndWait(() -> { d[0] = new RegistrationDialog(app[0], svc, svc_events(svc), () -> app[0].load()); d[0].setModal(false); d[0].setLocation(740, 0); d[0].setVisible(true); });
        // invalid input
        SwingUtilities.invokeAndWait(() -> { d[0].regNo.setText("A1"); d[0].name.setText("123"); d[0].email.setText("bad"); d[0].onRegister(); });
        shot(d[0], "Task6_2_invalid_input.png");
        // duplicate (SIM20001 already registered for Hackathon 2026)
        SwingUtilities.invokeAndWait(() -> { d[0].regNo.setText("SIM20001"); d[0].name.setText("Asha Kumar"); d[0].email.setText("asha@example.edu"); d[0].event.setSelectedItem("Hackathon 2026"); d[0].onRegister(); });
        Thread.sleep(600); shot(d[0], "Task6_3_duplicate_rejected.png");
        // normal registration
        SwingUtilities.invokeAndWait(() -> { d[0].regNo.setText("SIM20002"); d[0].name.setText("Ravi Singh"); d[0].email.setText("ravi@example.edu"); d[0].onRegister(); });
        Thread.sleep(800); shot(d[0], "Task6_4_registration_success.png");
        shot(app[0], "Task6_5_table_refreshed_after_success.png");
        System.exit(0);
    }
    static java.util.List<String> svc_events(RegistrationService s) { try { return s.events(); } catch (Exception e) { throw new RuntimeException(e); } }
}
