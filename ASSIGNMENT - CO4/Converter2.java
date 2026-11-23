import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/** Style (d): lambda version - the assignment's "Complete the Code" program. */
public class Converter2 extends JFrame {
    private final JTextField amount = new JTextField(10);
    private final JComboBox<String> target =
        new JComboBox<>(new String[]{"USD", "EUR"});
    private final JLabel result = new JLabel(" ");

    private static double rate(String currency) {
        return currency.equals("USD") ? 0.012 : 0.011;
    }

    public Converter2() {
        super("Currency Converter (d v2)");
        setLayout(new FlowLayout());
        JButton convertButton = new JButton("Convert");
        add(new JLabel("INR")); add(amount); add(target);
        add(convertButton); add(result);
        // TODO 1 (done): ONE lambda listener shared by the Convert button and the Enter key in the amount field
        ActionListener convertListener = e -> convert();
        convertButton.addActionListener(convertListener);
        amount.addActionListener(convertListener);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
    }

    private void convert() {
        // TODO 2 (done): parse the amount, show the converted value with 2 decimals, or "Invalid amount"
        try {
            double inr = Double.parseDouble(amount.getText().trim());
            String out = String.format(java.util.Locale.US, "%.2f %s", inr * rate((String) target.getSelectedItem()), target.getSelectedItem());
            result.setText(out);
            javax.swing.JOptionPane.showMessageDialog(this, out, "Result", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            result.setText("Invalid amount");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Converter2().setVisible(true));
    }
}
