/** Checked exception for validation and payment failures; the message is safe to show to the user. */
public class FeeException extends Exception {
    public FeeException(String message) { super(message); }
    public FeeException(String message, Throwable cause) { super(message, cause); }
}
