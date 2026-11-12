
import java.util.ArrayList;
import java.util.List;

abstract class Vehicle {
    private final String regNo;

    Vehicle(String regNo) {
        this.regNo = regNo;
    }

    public String getRegNo() {
        return regNo;
    }

    abstract double calculateFee(int hours);
}

class Car extends Vehicle {
    Car(String regNo) {
        super(regNo);
    }

    @Override
    double calculateFee(int hours) {
        return hours * 30.0;
    }
}

class TwoWheeler extends Vehicle {
    TwoWheeler(String regNo) {
        super(regNo);
    }

    @Override
    double calculateFee(int hours) {
        return hours * 10.0;
    }
}

class ParkingSystem {
    private final List<Vehicle> parkedVehicles = new ArrayList<>();

    public boolean park(Vehicle vehicle) {
        for (Vehicle v : parkedVehicles) {
            if (v.getRegNo().equals(vehicle.getRegNo())) {
                return false;
            }
        }

        parkedVehicles.add(vehicle);
        return true;
    }

    public boolean remove(String regNo, int hours) {
        for (int i = 0; i < parkedVehicles.size(); i++) {
            Vehicle v = parkedVehicles.get(i);

            if (v.getRegNo().equals(regNo)) {
                System.out.println("Fee for " + regNo
                        + ": Rs " + v.calculateFee(hours));
                parkedVehicles.remove(i);
                return true;
            }
        }

        return false;
    }

    public void displayParkedVehicles() {
        System.out.println("Currently parked vehicles:");

        for (Vehicle v : parkedVehicles) {
            System.out.println(v.getClass().getSimpleName()
                    + " - " + v.getRegNo());
        }
    }
}

public class Task11 {
    public static void main(String[] args) {
        ParkingSystem system = new ParkingSystem();

        System.out.println("Park car: "
                + system.park(new Car("TN01AB1234")));

        System.out.println("Park bike: "
                + system.park(new TwoWheeler("TN02CD5678")));

        System.out.println("Duplicate registration: "
                + system.park(new Car("TN01AB1234")));

        system.displayParkedVehicles();

        System.out.println("Exit car: "
                + system.remove("TN01AB1234", 2));

        System.out.println("Exit unknown vehicle: "
                + system.remove("TN99ZZ9999", 1));

        system.displayParkedVehicles();
    }
}
