class ParkingTicket {
    private final int ticketNumber;
    private final String vehicleNo;
    private final int floor;
    private final int slot;

    ParkingTicket() {
        this(1, "UNKNOWN", 0, 0);
    }

    ParkingTicket(int ticketNumber, String vehicleNo) {
        this(ticketNumber, vehicleNo, 0, 0);
    }

    ParkingTicket(int ticketNumber, String vehicleNo, int floor, int slot) {
        this.ticketNumber = ticketNumber;
        this.vehicleNo = vehicleNo;
        this.floor = floor;
        this.slot = slot;
    }

    ParkingTicket(ParkingTicket other) {
        this(other.ticketNumber, other.vehicleNo, other.floor, other.slot);
    }

    void display() {
        System.out.println("Ticket: " + ticketNumber
                + ", Vehicle: " + vehicleNo
                + ", Floor: " + floor + ", Slot: " + slot);
    }
}

public class Task4 {
    public static void main(String[] args) {
        ParkingTicket t1 = new ParkingTicket();
        ParkingTicket t2 = new ParkingTicket(102, "TN01AB1234");
        ParkingTicket t3 = new ParkingTicket(103, "TN02CD5678", 1, 2);
        ParkingTicket t4 = new ParkingTicket(t3);

        t1.display();
        t2.display();
        t3.display();
        t4.display();
    }
}