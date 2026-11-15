# Task 9: Abstraction and Interfaces

## 1. Abstract Class
`ParkingVehicle` is an abstract class. It contains shared registration information and a concrete `display()` method. Its abstract `calculateParkingFee()` method requires subclasses to provide their own implementation.

## 2. Interface
`Chargeable` defines the contract `chargingFee(int kWh)`. The `ElectricCar` class implements this interface to calculate its charging cost.

## 3. Abstract Class vs Interface
- An abstract class can store instance state and provide constructors and implemented methods.
- An interface defines a contract that implementing classes must fulfil.
- A class can extend one class but can implement multiple interfaces.

## 4. Benefits
Abstraction hides implementation details and exposes essential operations. Interfaces allow unrelated classes to share a common capability without requiring them to inherit from the same class.

## Conclusion
The example combines an abstract vehicle hierarchy with a charging interface to separate parking-fee behaviour from charging capability.