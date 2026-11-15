
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

class E_Bike extends Vehicle {
    E_Bike(String regNo) {
        super(regNo);
    }

    @Override
    double calculateFee(int hours) {
        return hours * 5.0;
    }
}

class DeliveryVan extends Vehicle {
    DeliveryVan(String regNo) {
        super(regNo);
    }

    @Override
    double calculateFee(int hours) {
        return hours * 50.0;
    }
}

class ParkingFloor {
    private final int floorNumber;
    private final int capacity;
    private final List<Vehicle> vehicles = new ArrayList<>();

    ParkingFloor(int floorNumber, int capacity) {
        this.floorNumber = floorNumber;
        this.capacity = capacity;
    }

    public boolean park(Vehicle vehicle) {
        if (vehicles.size() >= capacity) {
            return false;
        }

        for (Vehicle v : vehicles) {
            if (v.getRegNo().equals(vehicle.getRegNo())) {
                return false;
            }
        }

        vehicles.add(vehicle);
        return true;
    }

    public void display() {
        System.out.println("Floor " + floorNumber + ": "
                + vehicles.size() + "/" + capacity + " occupied");
        for (Vehicle v : vehicles) {
            System.out.println("  " + v.getClass().getSimpleName()
                    + " - " + v.getRegNo());
        }
    }
}

public class Task12 {
    public static void main(String[] args) {
        ParkingFloor[] floors = new ParkingFloor[5];

        for (int i = 0; i < floors.length; i++) {
            floors[i] = new ParkingFloor(i + 1, 2);
        }

        floors[0].park(new Car("TN01AB1234"));
        floors[0].park(new E_Bike("TN02CD5678"));
        floors[1].park(new DeliveryVan("TN03EF9012"));

        System.out.println("=== Five-Floor Parking System ===");
        for (ParkingFloor floor : floors) {
            floor.display();
        }

        Vehicle van = new DeliveryVan("TN04GH1111");
        System.out.println("Van fee for 3 hours: Rs "
                + van.calculateFee(3));

        // One-day pass example: fixed daily fee
        double dailyPassFee = 200.0;
        System.out.println("One-day pass: Rs " + dailyPassFee);
    }
}
