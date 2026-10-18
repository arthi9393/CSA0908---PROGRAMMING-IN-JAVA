# Task 4: Classes, Objects and Constructors

## 1. Class and Objects
`ParkingTicket` is a class representing a parking ticket. Objects `t1`, `t2`, `t3`, and `t4` are created using different constructors.

## 2. Constructors
- **Default constructor:** Initializes a ticket with default values.
- **Parameterized constructor:** Accepts ticket number and vehicle number.
- **Overloaded constructor:** Accepts ticket number, vehicle number, floor, and slot.
- **Copy constructor:** Creates a new ticket using the values of an existing ticket.

## 3. Use of `this`
The `this` keyword refers to the current object. Constructor chaining using `this()` reuses another constructor and avoids repeated initialization code.

## 4. Memory Management
Object references such as `t1` and `t2` are local variables stored in the stack frame, while the ticket objects are allocated on the heap. Unreachable objects can later be reclaimed by Java's garbage collector.

## 5. Constructor Chaining and Copy Constructor
Constructor chaining reduces duplicated code. A copy constructor is useful when a separate object with the same initial values is required.

## 6. Alternative Approaches
- **Setters:** Create an object first, then assign values. This may allow partially initialized objects.
- **Factory methods:** Named methods can make object creation clearer and can return different object types.
- **Constructors:** Best when an object should be initialized immediately at creation.

## 7. Complexity
Each constructor initializes a fixed number of fields, so its time complexity is **O(1)**. Creating `n` tickets takes **O(n)** total time and **O(n)** heap space.

## Conclusion
Constructor overloading, `this()`, and copy constructors support clear and reusable object initialization in the parking system.