import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;

/** Style (a): the frame itself implements ActionListener. */
public class ConverterA_Frame extends JFrame implements ActionListener {
    private final JTextField amount = new JTextField(10);
    private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
    private final JLabel result = new JLabel(" ");
    private final JButton convertButton = new JButton("Convert");

    public ConverterA_Frame() {
        super("Currency Converter (a)");
        setLayout(new FlowLayout());
        add(new JLabel("INR")); add(amount); add(target); add(convertButton); add(result);
        convertButton.addActionListener(this);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dispose(); System.exit(0); } });
        pack();
    }
    @Override public void actionPerformed(ActionEvent e) {
        try {
            double inr = Double.parseDouble(amount.getText().trim());
            double rate = target.getSelectedItem().equals("USD") ? 0.012 : 0.011;
            result.setText(String.format(java.util.Locale.US, "%.2f %s", inr * rate, target.getSelectedItem()));
        } catch (NumberFormatException ex) { result.setText("Invalid amount"); }
    }
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new ConverterA_Frame().setVisible(true)); }
}
