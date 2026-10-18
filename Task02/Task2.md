# Task 2 – Choose the Data Representation

## 1. Data Types

| Field | Data type | Reason |
|---|---|---|
| Slot number | `int` | Slot numbers are whole numbers. |
| Vehicle registration number | `String` | Registration numbers contain letters and digits. |
| Entry time | `LocalDateTime` | Stores the date and time of vehicle entry. |
| Duration | `long` | Stores parking duration in minutes as a whole number. |
| Parking fee | `double` | Supports fractional monetary calculations. |
| Slot availability | `String` or `null` | A registration number means occupied; `null` means free. |

## 2. Array Representation

I would use a jagged two-dimensional array, `String[][] slots`, to represent floors and slots. Each row represents a floor, and each element stores a vehicle registration number or `null` if the slot is free.

A jagged array is suitable because different floors may have different numbers of parking slots. For example, one floor may have 20 slots and another may have 50.

## 3. Type Conversion and Precision

Parking duration is measured in minutes. To charge for every started hour, I would calculate the billable hours using integer arithmetic:

`hours = (minutes + 59) / 60`

For example, 61 minutes becomes 2 billable hours.

Incorrect integer division can produce a wrong fee. For example, `61 / 60` gives `1`, not `1.0167`, because both operands are integers. If the fee is ₹30 per hour, this would incorrectly charge ₹30 instead of ₹60.

The ceiling formula avoids this problem when every started hour is charged in full. For other billing rules, floating-point division or an appropriate decimal type may be needed.

## 4. Input Method

I would use Java's `Scanner` class to read vehicle registration numbers, vehicle types, floor and slot numbers, and parking duration from the user. Input should be validated before it is used.

## 5. Alternative Representation and Complexity

A single one-dimensional array can represent all slots if every floor has the same number of slots. The index can be calculated using:

`index = floor * slotsPerFloor + slot`

This is less suitable when floors have different numbers of slots because the conversion requires offsets for the varying floor sizes.

An `ArrayList<String[]>` can store one array per floor and supports floors with different slot counts, similar to a jagged array.

Accessing a known slot by its index takes O(1) time in an array or list. Searching all slots for a particular vehicle takes O(n) time, where n is the total number of slots. All representations require O(n) space for n slots.