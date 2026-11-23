import java.util.Locale;

/** All validation and payment logic. No AWT/Swing here, so it can be unit-tested without a window. */
public class FeeService {
    public static final double MAX_AMOUNT = 1_000_000.0;
    private final FeeRepository repository;
    private String lastReceipt = null;

    public FeeService(FeeRepository repository) { this.repository = repository; }

    /** Validates the raw text from the window, saves the payment and returns a receipt text. */
    public String pay(String regNo, String amountText) throws FeeException {
        regNo = regNo == null ? "" : regNo.trim();
        amountText = amountText == null ? "" : amountText.trim();
        if (regNo.isEmpty()) throw new FeeException("Register number is required.");
        if (!regNo.matches("[A-Za-z0-9]{5,15}")) throw new FeeException("Register number must be 5-15 letters/digits.");
        if (amountText.isEmpty()) throw new FeeException("Amount is required.");
        double amount;
        try { amount = Double.parseDouble(amountText); }
        catch (NumberFormatException e) { throw new FeeException("Amount must be a number."); }
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) throw new FeeException("Amount must be greater than zero.");
        if (amount > MAX_AMOUNT) throw new FeeException("Amount exceeds the maximum of " + (long) MAX_AMOUNT + ".");
        repository.save(regNo, amount);
        lastReceipt = String.format(Locale.US, "RECEIPT: %s paid Rs. %.2f", regNo, amount);
        return lastReceipt;
    }

    /** Text for the Receipt button; throws a friendly error if nothing has been paid yet. */
    public String lastReceipt() throws FeeException {
        if (lastReceipt == null) throw new FeeException("No payment has been made yet.");
        return lastReceipt;
    }
}
