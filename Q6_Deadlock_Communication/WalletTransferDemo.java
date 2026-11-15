class Wallet {
    private final int id;
    private int balance;
    Wallet(int id, int balance) { this.id = id; this.balance = balance; }
    int getId() { return id; }
    synchronized int getBalance() { return balance; }
    void debit(int amount) { balance -= amount; }
    void credit(int amount) { balance += amount; }
}

public class WalletTransferDemo {
    // Global lock makes the two-account update atomic in this simple demonstration.
    private static final Object transferLock = new Object();

    static void transfer(Wallet from, Wallet to, int amount) {
        synchronized (transferLock) {
            if (from.getBalance() >= amount) {
                from.debit(amount);
                to.credit(amount);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Wallet a = new Wallet(1, 1000);
        Wallet b = new Wallet(2, 1000);
        Thread t1 = new Thread(() -> { for (int i = 0; i < 1000; i++) transfer(a, b, 1); });
        Thread t2 = new Thread(() -> { for (int i = 0; i < 1000; i++) transfer(b, a, 1); });
        t1.start(); t2.start(); t1.join(); t2.join();
        System.out.println("Final total: " + (a.getBalance() + b.getBalance()));
    }
}
