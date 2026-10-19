
interface Chargeable {
    double chargingFee(int kWh);
}

abstract class ParkingVehicle {
    protected String regNo;

    ParkingVehicle(String regNo) {
        this.regNo = regNo;
    }

    abstract double calculateParkingFee(int hours);

    public void display() {
        System.out.println("Registration: " + regNo);
    }
}

class PetrolCar extends ParkingVehicle {
    PetrolCar(String regNo) {
        super(regNo);
    }

    @Override
    double calculateParkingFee(int hours) {
        return hours * 30.0;
    }
}

class ElectricCar extends ParkingVehicle implements Chargeable {
    ElectricCar(String regNo) {
        super(regNo);
    }

    @Override
    double calculateParkingFee(int hours) {
        return hours * 30.0;
    }

    @Override
    public double chargingFee(int kWh) {
        return kWh * 12.0;
    }
}

public class Task9 {
    public static void main(String[] args) {
        ParkingVehicle car = new PetrolCar("TN01AB1234");
        ParkingVehicle electric = new ElectricCar("TN02CD5678");

        car.display();
        System.out.println("Parking fee: Rs "
                + car.calculateParkingFee(2));

        electric.display();
        System.out.println("Parking fee: Rs "
                + electric.calculateParkingFee(2));

        if (electric instanceof Chargeable c) {
            System.out.println("Charging fee: Rs "
                    + c.chargingFee(10));
        }
    }
}
