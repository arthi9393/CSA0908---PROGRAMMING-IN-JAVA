# CSA09 - Programming in Java - Assignment 4 (CO4): Event-Driven GUI Design
**Name:** ARTHI A  **Register Number:** 192525152  **Java version:** [output of `java -version`]

## Compile and run
```
javac *.java                       # compiles everything in this package (Java 17+; tested with Java 21)
java FeeServiceTest                # Task 1 tests (no window)
java FeeWindow                     # Task 1.2 window (payment takes 1.5 s: window stays responsive)
java LibrarySearch                 # Task 3
java DrawPad                       # Task 4 (completed TODO 1 and 2);  java DrawPadBroken = original faulty program
java ConverterA_Frame | ConverterB_Inner | ConverterC_Anonymous | Converter     # Task 5 (a)-(d)
java ConverterA2_Frame | ConverterB2_Inner | ConverterC2_Anonymous | Converter2 # Task 5.2 modified versions
java RegistrationServiceTest       # Task 6 test cases
java RegistrationApp               # Task 6 demo mode (no database needed)
java ComplexityBench               # measurements used in the "Alternative Concept" sections
```
Task 6 with MySQL: `mysql -u root -p < schema.sql`, copy `db.properties.example` to `db.properties` (edit the password), download MySQL Connector/J, then
`java -cp .:mysql-connector-j-8.4.0.jar RegistrationApp` (use `;` instead of `:` on Windows). Optional: `seed_100k.sql` adds 100,000 rows for the load-all vs paged comparison.

## Files by task
| Task | Files |
|---|---|
| Report | Assignment4_Report.docx (all diagnoses, tables, diagrams, pseudocode, comparisons) |
| 1 | FeeException, FeeRepository, JdbcFeeRepository, InMemoryFeeRepository, FeeService, FeeWindow, FeeServiceTest (+ Task1_FeeServiceTest_output.txt) |
| 2 | Design only: tables, hierarchy, pseudocode and trace are in the report |
| 3 | LibrarySearch.java, Task3_resize_*.png |
| 4 | DrawPadBroken.java (original), DrawPad.java (completed), DrawPadOverrideError.java.txt, Task4_override_compiler_message.txt, Task4_DrawPad_*.png |
| 5 | ConverterA_Frame, ConverterB_Inner, ConverterC_Anonymous, Converter (lambda, completed TODOs), the four modified versions (A2, B2, C2, Converter2), Task5_change_effort.txt, Task5_converter_*.png |
| 6 | RegistrationApp, RegistrationDialog, RegistrationService, RegistrationDao, MySqlRegistrationDao, InMemoryRegistrationDao, Registration, DaoException, DuplicateRegistrationException, schema.sql, seed_100k.sql, db.properties.example, RegistrationServiceTest, Task6_*.png, Task6_test_output.txt, diagrams |
| Measurements | ComplexityBench.java, Complexity_measurements.txt |
| Screenshot generators | Shots.java, Task5Shots.java, Task6Shots.java (drive the real windows on a virtual display) |

## Results (fill in after running on your machine)
- Task 1: FeeServiceTest - all 11 checks pass
- Task 3: results area grows, search row and buttons keep their size at 3 window sizes
- Task 4: clicks counted, dots survive minimise/restore; @Override gives "method does not override or implement a method from a supertype"
- Task 5: all four styles convert 5000 INR to 60.00 USD; invalid amount shows "Invalid amount"
- Task 6: 14 of 14 test cases pass; duplicate registration rejected; [add MySQL load times]
