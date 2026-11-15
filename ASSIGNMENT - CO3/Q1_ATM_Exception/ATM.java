import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class InsufficientFundsException extends Exception {
    InsufficientFundsException(String message) { super(message); }
}

public class ATM {
    private static final double BALANCE = 5000.0;

    static void withdraw(String accountText, String amountText)
            throws InsufficientFundsException, FileNotFoundException {
        // Simulate a required card-reader configuration file.
        try (Scanner fileScanner = new Scanner(new File("Q1_ATM_Exception/card-reader.txt"))) {
            String cardStatus = fileScanner.nextLine();
            if (!"READY".equals(cardStatus)) {
                throw new IllegalStateException("Card reader is not ready");
            }
        }

        int accountNumber = Integer.parseInt(accountText);
        double amount = Double.parseDouble(amountText);

        if (accountNumber <= 0) {
            throw new IllegalArgumentException("Account number must be positive");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (amount > BALANCE) {
            throw new InsufficientFundsException("Insufficient account balance");
        }
        System.out.printf("Withdrawal successful: %.2f%n", amount);
    }

    public static void main(String[] args) {
        // Create Q1_ATM_Exception/card-reader.txt containing READY before running.
        try (Scanner input = new Scanner(System.in)) {
            System.out.print("Account number: ");
            String account = input.nextLine();
            System.out.print("Withdrawal amount: ");
            String amount = input.nextLine();

            try {
                withdraw(account, amount);
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric input: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid account or amount: " + e.getMessage());
            } catch (InsufficientFundsException e) {
                System.out.println("Transaction rejected: " + e.getMessage());
            } catch (FileNotFoundException | SecurityException e) {
                System.out.println("Card-reader file error: " + e.getMessage());
            } catch (IllegalStateException e) {
                System.out.println("ATM unavailable: " + e.getMessage());
            } finally {
                System.out.println("ATM session finished.");
            }
        }
    }
}
