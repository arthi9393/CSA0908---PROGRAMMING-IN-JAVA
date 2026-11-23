import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;

/** Style (c v2): an anonymous inner class written where it is registered. */
public class ConverterC2_Anonymous extends JFrame {
    private final JTextField amount = new JTextField(10);
    private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
    private final JLabel result = new JLabel(" ");
    private final JButton convertButton = new JButton("Convert");

    public ConverterC2_Anonymous() {
        super("Currency Converter (c v2)");
        setLayout(new FlowLayout());
        add(new JLabel("INR")); add(amount); add(target); add(convertButton); add(result);
        ActionListener l = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                try {
                    double inr = Double.parseDouble(amount.getText().trim());
                    double rate = target.getSelectedItem().equals("USD") ? 0.012 : 0.011;
                    String out = String.format(java.util.Locale.US, "%.2f %s", inr * rate, target.getSelectedItem());
                    result.setText(out);
                    JOptionPane.showMessageDialog(ConverterC2_Anonymous.this, out, "Result", JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException ex) { result.setText("Invalid amount"); }
            }
        };
        convertButton.addActionListener(l);
        amount.addActionListener(l);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dispose(); System.exit(0); } });
        pack();
    }
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new ConverterC2_Anonymous().setVisible(true)); }
}
