# Task 8: Apply Polymorphism

## 1. Compile-Time Polymorphism
The `calculateFee()` method is overloaded with two parameter lists:
- `calculateFee(int minutes)`
- `calculateFee(int hours, boolean exactHours)`

The compiler selects the method based on the arguments supplied.

## 2. Runtime Polymorphism
A single `Vehicle[]` array stores `TwoWheeler`, `Car`, and `ElectricCar` objects. This is upcasting. When `v.ratePerHour()` is called inside `calculateFee()`, Java selects the overridden method according to the actual object type at runtime.

## 3. Dynamic Method Dispatch Trace
For `Vehicle v = new Car("TN02B2")`:
- Declared reference type: `Vehicle`
- Actual object type: `Car`
- Executed method: `Car.ratePerHour()`
- Fee for 130 minutes: 3 billable hours × Rs 30 = Rs 90

## 4. Avoiding Conditional Chains
Polymorphism avoids a long `if-else` chain that checks each vehicle type. Each subclass provides its own rate, so adding another vehicle category can be done with a new subclass.

## 5. Complexity Analysis
Fee calculation and dynamic method dispatch take O(1) time for each vehicle. Processing `n` vehicles takes O(n) time and O(n) space for the array. Using sequential `instanceof` checks for `k` types can require up to O(k) checks per vehicle.

## Conclusion
Method overloading demonstrates compile-time polymorphism, while overridden methods and superclass references demonstrate runtime polymorphism.