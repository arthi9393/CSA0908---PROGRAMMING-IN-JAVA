import java.awt.*;
import java.util.List;
import javax.swing.*;

/** Registration form. Validation runs on the event thread (cheap); database work runs in a SwingWorker. */
public class RegistrationDialog extends JDialog {
    final JTextField regNo = new JTextField(14), name = new JTextField(20), email = new JTextField(20);
    final JComboBox<String> event;
    private final JLabel message = new JLabel(" ");
    private final JButton register = new JButton("Register"), cancel = new JButton("Cancel");
    private final RegistrationService service;
    private final Runnable onSuccess;

    public RegistrationDialog(Frame owner, RegistrationService service, List<String> events, Runnable onSuccess) {
        super(owner, "New Registration", true);
        this.service = service; this.onSuccess = onSuccess;
        event = new JComboBox<>(events.toArray(new String[0]));
        JPanel form = new JPanel(new GridLayout(4, 2, 6, 6));
        form.add(new JLabel("Register No:")); form.add(regNo);
        form.add(new JLabel("Name:"));        form.add(name);
        form.add(new JLabel("Email:"));       form.add(email);
        form.add(new JLabel("Event:"));       form.add(event);
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        message.setForeground(new Color(0xB00020)); message.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT)); buttons.add(register); buttons.add(cancel);
        setLayout(new BorderLayout()); add(form, BorderLayout.NORTH); add(message, BorderLayout.CENTER); add(buttons, BorderLayout.SOUTH);
        register.addActionListener(e -> onRegister());
        cancel.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(register);
        pack(); setLocationRelativeTo(owner);
    }

    void showMessage(String text, boolean error) {
        message.setForeground(error ? new Color(0xB00020) : new Color(0x1B5E20));
        message.setText("<html>" + text + "</html>");
        pack();
    }

    /** Called by the Register button. */
    void onRegister() {
        final String r = regNo.getText(), n = name.getText(), m = email.getText(), ev = (String) event.getSelectedItem();
        List<String> errors = service.validate(r, n, m, ev);                 // cheap: no database needed
        if (!errors.isEmpty()) { showMessage(String.join("<br>", errors), true); return; }
        register.setEnabled(false); showMessage("Saving...", false);
        new SwingWorker<Void, Void>() {                                       // off the event-dispatch thread
            @Override protected Void doInBackground() throws Exception { service.register(r, n, m, ev); return null; }
            @Override protected void done() {
                try { get(); showMessage("Registered successfully.", false); onSuccess.run(); regNo.setText(""); }
                catch (Exception ex) {
                    Throwable t = ex.getCause() != null ? ex.getCause() : ex;
                    showMessage(t instanceof DuplicateRegistrationException || t instanceof IllegalArgumentException || t instanceof DaoException
                                ? t.getMessage() : "Unexpected error.", true);
                } finally { register.setEnabled(true); }
            }
        }.execute();
    }
}
