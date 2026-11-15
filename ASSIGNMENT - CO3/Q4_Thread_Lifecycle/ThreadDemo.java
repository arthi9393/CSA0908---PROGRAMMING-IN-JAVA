class ScannerStation extends Thread {
    private final int stationId;
    private final int scripts;

    ScannerStation(int stationId, int scripts) {
        super("Station-" + stationId);
        this.stationId = stationId;
        this.scripts = scripts;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < scripts; i++) Thread.sleep(30);
            System.out.println("Thread station " + stationId + " scanned " + scripts);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(getName() + " interrupted");
        }
    }
}

class RunnableStation implements Runnable {
    private final int stationId;
    private final int scripts;

    RunnableStation(int stationId, int scripts) {
        this.stationId = stationId;
        this.scripts = scripts;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(100);
            System.out.println("Runnable station " + stationId + " scanned " + scripts);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class ThreadDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread[] stations = new Thread[4];
        for (int i = 0; i < stations.length; i++) {
            stations[i] = new ScannerStation(i + 1, 25);
            stations[i].start();
        }

        System.out.println("Station 1 alive? " + stations[0].isAlive());
        for (Thread station : stations) station.join();
        System.out.println("All Thread-based stations completed.");

        Thread[] runnableThreads = new Thread[4];
        for (int i = 0; i < runnableThreads.length; i++) {
            runnableThreads[i] = new Thread(new RunnableStation(i + 1, 25));
            runnableThreads[i].start();
        }
        for (Thread t : runnableThreads) t.join();
        System.out.println("All Runnable-based stations completed.");
    }
}
