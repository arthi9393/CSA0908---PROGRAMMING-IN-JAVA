
class Vehicle {
    private final String regNo;

    Vehicle(String regNo) {
        this.regNo = regNo;
    }

    public String getRegNo() {
        return regNo;
    }

    public double calculateFee(int hours) {
        return 0.0;
    }

    @Override
    public String toString() {
        return "Vehicle: " + regNo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vehicle)) return false;
        Vehicle other = (Vehicle) obj;
        return regNo.equals(other.regNo);
    }

    @Override
    public int hashCode() {
        return regNo.hashCode();
    }
}

class TwoWheeler extends Vehicle {
    TwoWheeler(String regNo) {
        super(regNo);
    }

    @Override
    public double calculateFee(int hours) {
        return hours * 10.0;
    }
}

class Car extends Vehicle {
    Car(String regNo) {
        super(regNo);
    }

    @Override
    public double calculateFee(int hours) {
        return hours * 30.0;
    }
}

class ElectricCar extends Car {
    ElectricCar(String regNo) {
        super(regNo);
    }

    @Override
    public double calculateFee(int hours) {
        return hours * 30.0 + 50.0;
    }
}

public class Task7 {
    public static void main(String[] args) {
        Vehicle[] vehicles = {
            new TwoWheeler("TN01AB1234"),
            new Car("TN02CD5678"),
            new ElectricCar("TN03EF9012")
        };

        for (Vehicle v : vehicles) {
            System.out.println(v + " - Fee: Rs "
                    + v.calculateFee(2));
        }

        Vehicle a = new Car("TN04GH1111");
        Vehicle b = new ElectricCar("TN04GH1111");
        System.out.println("Same registration: " + a.equals(b));
    }
}
