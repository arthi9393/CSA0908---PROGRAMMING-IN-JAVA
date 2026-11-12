import java.util.Scanner;

public class Task2 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        int[] slotsPerFloor = {3, 4, 2};
        String[][] slots = new String[slotsPerFloor.length][];

        for (int f = 0; f < slotsPerFloor.length; f++) {
            slots[f] = new String[slotsPerFloor[f]];
        }

        System.out.println("CAMPUS SMART-PARKING SYSTEM");
        System.out.println("Number of floors: " + slots.length);

        System.out.print("Enter floor number (1-3): ");
        int floor = input.nextInt();

        System.out.print("Enter slot number: ");
        int slot = input.nextInt();

        if (floor >= 1 && floor <= slots.length
                && slot >= 1 && slot <= slots[floor - 1].length) {

            if (slots[floor - 1][slot - 1] == null) {
                System.out.print("Enter vehicle registration number: ");
                String regNo = input.next();

                slots[floor - 1][slot - 1] = regNo;
                System.out.println("Vehicle parked successfully.");
                System.out.println("Floor: " + floor + ", Slot: " + slot);
                System.out.println("Vehicle: " + regNo);
            } else {
                System.out.println("Slot is already occupied.");
            }

        } else {
            System.out.println("Invalid floor or slot number.");
        }

        int minutes = 61;
        int billableHours = (minutes + 59) / 60;
        double ratePerHour = 30.0;
        double fee = billableHours * ratePerHour;

        System.out.println();
        System.out.println("Billing example:");
        System.out.println("Duration: " + minutes + " minutes");
        System.out.println("Billable hours: " + billableHours);
        System.out.println("Parking fee: Rs. " + fee);

        input.close();
    }
}