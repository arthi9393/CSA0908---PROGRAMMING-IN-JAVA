
abstract class Vehicle {
    protected String regNo;

    Vehicle(String regNo) {
        this.regNo = regNo;
    }

    public String getRegNo() {
        return regNo;
    }

    protected abstract double ratePerHour();

    public double calculateFee(int minutes) {
        int hours = (minutes + 59) / 60;
        return hours * ratePerHour();
    }

    // Overloaded method: compile-time polymorphism
    public double calculateFee(int hours, boolean exactHours) {
        return hours * ratePerHour();
    }
}

class TwoWheeler extends Vehicle {
    TwoWheeler(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return 10.0;
    }
}

class Car extends Vehicle {
    Car(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return 30.0;
    }
}

class ElectricCar extends Car {
    ElectricCar(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return 60.0;
    }
}

public class Task8 {
    public static void main(String[] args) {
        Vehicle[] vehicles = {
            new TwoWheeler("TN01A1"),
            new Car("TN02B2"),
            new ElectricCar("TN03C3")
        };

        for (Vehicle v : vehicles) {
            double fee = v.calculateFee(130);
            System.out.println(
                v.getClass().getSimpleName() + " "
                + v.getRegNo() + " -> Rs " + fee
            );
        }

        System.out.println("Overloaded fee: Rs "
                + vehicles[1].calculateFee(2, true));
    }
}
