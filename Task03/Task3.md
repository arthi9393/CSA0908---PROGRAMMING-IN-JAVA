# Task 3 – Design the Control Logic

## 1. Slot Allocation Pseudocode
START
FOR each floor
    FOR each slot on the floor
        IF slot is empty
            Store vehicle number in slot
            RETURN floor and slot
        END IF
    END FOR
END FOR
RETURN no slot available
END

## 2. Slot Release Pseudocode
START
IF floor is invalid OR slot is invalid
    RETURN false
END IF
IF slot is empty
    RETURN false
END IF
Clear the slot
RETURN true
END

## 3. Parking Fee Logic
- Calculate parking duration in minutes.
- Convert minutes to hours using ceiling division.
- Apply the first-hour fee.
- Apply the additional-hour fee for every extra hour.
- Limit the total fee to the daily maximum.

## 4. Control Statements
- `for` loops search floors and slots.
- `return` stops searching after finding a free slot.
- `if` checks slot availability and validates positions.
- `break` can stop a loop when a match is found.
- `continue` can skip occupied slots.

## 5. Complexity Analysis
Let N be the total number of parking slots.
- Allocation: O(N) worst-case time.
- Release by floor and slot: O(1) time.
- Space: O(N) for storing slot occupancy.

## 6. Result
The program allocates the first available slot, reports when the facility is full, and rejects invalid or already-empty slot releases.