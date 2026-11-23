import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;

/** Style (b v2): a named inner class (can read the frame's private fields directly, and can be reused). */
public class ConverterB2_Inner extends JFrame {
    private final JTextField amount = new JTextField(10);
    private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
    private final JLabel result = new JLabel(" ");
    private final JButton convertButton = new JButton("Convert");

    private class ConvertListener implements ActionListener {
        @Override public void actionPerformed(ActionEvent e) {
            try {
                double inr = Double.parseDouble(amount.getText().trim());
                double rate = target.getSelectedItem().equals("USD") ? 0.012 : 0.011;
                String out = String.format(java.util.Locale.US, "%.2f %s", inr * rate, target.getSelectedItem());
                result.setText(out);
                JOptionPane.showMessageDialog(ConverterB2_Inner.this, out, "Result", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) { result.setText("Invalid amount"); }
        }
    }
    public ConverterB2_Inner() {
        super("Currency Converter (b v2)");
        setLayout(new FlowLayout());
        add(new JLabel("INR")); add(amount); add(target); add(convertButton); add(result);
        ConvertListener l = new ConvertListener();
        convertButton.addActionListener(l);
        amount.addActionListener(l);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dispose(); System.exit(0); } });
        pack();
    }
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new ConverterB2_Inner().setVisible(true)); }
}
