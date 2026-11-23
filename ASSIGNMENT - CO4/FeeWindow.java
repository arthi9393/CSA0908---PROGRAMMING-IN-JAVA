import java.awt.*;
import java.awt.event.*;
import javax.swing.SwingWorker;

/** Task 1.2 - the window only collects input and shows results; FeeService does the work. */
public class FeeWindow extends Frame {
    private final TextField regNo = new TextField(15), amount = new TextField(10);
    private final Button pay = new Button("Pay"), clear = new Button("Clear"), receipt = new Button("Receipt");
    private final Label message = new Label(" ", Label.CENTER);
    private final FeeService service;

    public FeeWindow(FeeService service) {
        super("Fee Payment");
        this.service = service;
        setLayout(new GridLayout(4, 2, 5, 5));
        add(new Label("Register No:")); add(regNo);
        add(new Label("Amount:"));      add(amount);
        add(pay); add(clear);
        add(receipt); add(message);

        // One small listener per button (lambda) - no string comparison, no if-else chain
        pay.addActionListener(e -> onPay());
        clear.addActionListener(e -> { regNo.setText(""); amount.setText(""); show("Cleared."); });
        receipt.addActionListener(e -> {
            try { show(service.lastReceipt()); } catch (FeeException ex) { show(ex.getMessage()); }
        });
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { dispose(); }
        });
        pack();
    }

    private void onPay() {
        final String r = regNo.getText(), a = amount.getText();
        pay.setEnabled(false);
        show("Processing payment...");
        // The slow work runs off the event-dispatch thread, so the window stays responsive
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception { return service.pay(r, a); }
            @Override protected void done() {
                try { show(get()); }
                catch (Exception ex) {
                    Throwable t = ex.getCause() != null ? ex.getCause() : ex;
                    show(t instanceof FeeException ? t.getMessage() : "Unexpected error.");
                } finally { pay.setEnabled(true); }
            }
        }.execute();
    }

    private void show(String text) { message.setText(text); }

    public static void main(String[] args) {
        // Demo: a 1.5 s delay in the repository shows that the window does not freeze.
        EventQueue.invokeLater(() -> new FeeWindow(new FeeService(new InMemoryFeeRepository(1500))).setVisible(true));
    }
}
