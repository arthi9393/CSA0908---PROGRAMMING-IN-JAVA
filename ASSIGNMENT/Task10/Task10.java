
class ParkingAccount {
    private String ownerName;
    private double balance;

    public ParkingAccount(String ownerName, double balance) {
        this.ownerName = ownerName;
        this.balance = Math.max(0, balance);
    }

    public String getOwnerName() {
        return ownerName;
    }

    public double getBalance() {
        return balance;
    }

    public void addFunds(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("Funds added: Rs " + amount);
        } else {
            System.out.println("Invalid amount");
        }
    }

    public boolean payFee(double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            System.out.println("Fee paid: Rs " + amount);
            return true;
        }

        System.out.println("Payment failed");
        return false;
    }
}

public class Task10 {
    public static void main(String[] args) {
        ParkingAccount account =
                new ParkingAccount("Student", 200.0);

        System.out.println("Owner: " + account.getOwnerName());
        System.out.println("Balance: Rs " + account.getBalance());

        account.payFee(50.0);
        account.addFunds(100.0);

        System.out.println("Final balance: Rs "
                + account.getBalance());
    }
}
