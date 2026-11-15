# Task 11: Complete System and Testing

## System Overview
The parking system uses an abstract `Vehicle` class, with `Car` and `TwoWheeler` subclasses implementing their own parking-fee calculations. The `ParkingSystem` class manages parked vehicles using an `ArrayList`.

## Features
- Park cars and two-wheelers.
- Prevent duplicate registration numbers.
- Display all currently parked vehicles.
- Calculate parking fees when vehicles exit.
- Handle attempts to remove unknown vehicles.

## Testing

| Test | Expected result |
|---|---|
| Park a car | Accepted |
| Park a two-wheeler | Accepted |
| Park a duplicate registration | Rejected |
| Remove a parked vehicle | Fee calculated and vehicle removed |
| Remove an unknown vehicle | Returns false |
| Display parked vehicles | Shows current vehicles |

## Complexity
Parking a vehicle and removing a vehicle take O(n) time in the worst case because the list is searched linearly. Displaying all parked vehicles takes O(n) time. The list requires O(n) space for n parked vehicles.

## Limitations
This implementation stores data in memory only. Information is lost when the program terminates. A production system could add persistent storage, slot limits, input validation, and a user interface.

## Conclusion
The program integrates inheritance, abstraction, polymorphism, collections, and validation into a basic parking-management system.