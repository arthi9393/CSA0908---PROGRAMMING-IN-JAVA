# Task 12: Design Challenge — Extended Parking System

## Requirements
The system demonstrates support for e-bikes, delivery vans, one-day passes, and five parking floors.

## Design
- **Inheritance:** `Car`, `E_Bike`, and `DeliveryVan` extend the abstract `Vehicle` class.
- **Polymorphism:** Each vehicle type implements its own hourly parking fee.
- **Floor management:** Five `ParkingFloor` objects are created, each with a capacity of two vehicles for this demonstration.
- **Capacity validation:** A floor rejects vehicles when it is full.
- **Duplicate validation:** A floor rejects duplicate registration numbers already parked on that floor.
- **One-day pass:** The demonstration displays a fixed daily-pass fee of Rs 200.

## Sample Fee Calculation
For a delivery van parked for three hours:

3 × Rs 50 = Rs 150.

## Complexity
Parking searches for duplicate registration numbers on a floor, so it takes O(n) time for n vehicles on that floor. Displaying all vehicles across the floors takes O(N) time, where N is the total number of parked vehicles.

## Limitations and Future Improvements
The daily pass is displayed as an example fee and is not yet connected to individual vehicle billing. Duplicate registration checks currently operate within a floor; a complete implementation should validate registration numbers across all floors. The next version could include vehicle-specific slot sizes, pass expiry, vehicle removal, and persistent storage.

## Conclusion
The design extends the parking system with multiple vehicle types and a five-floor layout while applying inheritance, polymorphism, and basic capacity validation.