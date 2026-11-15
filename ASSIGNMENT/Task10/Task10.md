# Task 10: Packages and Encapsulation

## 1. Encapsulation
The `ParkingAccount` class encapsulates its data by declaring `ownerName` and `balance` as private fields. Other classes cannot directly modify these fields.

## 2. Getters and Controlled Updates
Public getter methods allow read-only access to account information. The `addFunds()` and `payFee()` methods validate requests before changing the balance.

## 3. Data Validation
The constructor prevents a negative starting balance. Adding funds requires a positive amount, and fee payment succeeds only when the balance is sufficient.

## 4. Packages
Packages organise related Java classes, avoid naming conflicts, and support access control. In a larger project, `ParkingAccount` could be placed in a named package and imported by the application class.

## 5. Complexity
Getting the balance, adding funds, and paying a fee each take O(1) time and O(1) additional space.

## Conclusion
Encapsulation protects account data and ensures changes happen through validated methods. Packages provide a clear structure for organising the parking system.