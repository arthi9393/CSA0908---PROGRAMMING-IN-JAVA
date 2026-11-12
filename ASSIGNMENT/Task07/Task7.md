# Task 7: Method Overriding

## 1. Overriding
The `TwoWheeler`, `Car`, and `ElectricCar` classes override `calculateFee(int hours)` to provide their own parking fee calculations. The `@Override` annotation helps the compiler verify that the method correctly overrides a superclass method.

## 2. Overriding Rules
- The method name and parameter list must match.
- The return type must be compatible.
- The overriding method cannot reduce access visibility.
- `final` methods cannot be overridden.

## 3. Use of `final`
The registration number is declared `final` because it should not change after a vehicle is created.

## 4. Object Methods
- `toString()` returns a readable vehicle description.
- `equals()` compares vehicles by registration number.
- `hashCode()` uses the registration number so equal vehicles have matching hash codes.

In this example, a `Car` and an `ElectricCar` with the same registration number compare as equal. Whether that is suitable depends on the system's identity rules; a real system may also need to consider vehicle category.

## 5. Alternative: `switch`
A single fee method could use a vehicle-type field and a `switch` statement. This can be simple for a small fixed number of types, but adding a category requires changing the central method. Overriding keeps each type's calculation in its own class.

## 6. Complexity Analysis
- Fee calculation: O(1) for each vehicle.
- A `switch` over `k` vehicle types is typically O(k) in the worst case when implemented as sequential checks; a Java `switch` may be optimized by the compiler.
- Overriding allows a new vehicle category to add its own class without modifying the existing fee methods.

## Conclusion
Method overriding supports specialized behaviour and runtime method selection, while `final` fields and consistent `equals()`/`hashCode()` implementations improve object reliability.