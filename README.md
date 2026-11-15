# CSA09 – Programming in Java – Assignment 3 (CO3)

Starter implementation for Q1–Q7. Target: **Java 17** (most examples also work on Java 8+).

## Before submission
- Replace the sample name/register number with your own details.
- Run every program yourself in VS Code.
- Save your own terminal screenshots and actual timing/experiment data.
- Do not report sample timings or sample race-condition counts as measured results.
- Each student must run every question in their own GitHub repository.

## Folder map
- `Q1_ATM_Exception/ATM.java`
- `Q2_Custom_Exception/StorageDemo.java`, `PortalDemo.java`
- `Q3_Resource_Management/ResourceDemo.java` and `input/`
- `Q4_Thread_Lifecycle/ThreadDemo.java`
- `Q5_Seat_Booking/SeatBooking.java`
- `Q6_Deadlock_Communication/TokenCounter.java`, `WalletTransferDemo.java`
- `Q7_Concurrent_Processor/ResultProcessor.java` and `input/`

## Compile and run
Open the project folder in VS Code. Install a JDK 17 and the Java Extension Pack.

In the VS Code terminal, run each command from the project root:

```bash
javac Q1_ATM_Exception/ATM.java
java -cp Q1_ATM_Exception ATM

javac Q2_Custom_Exception/StorageDemo.java
java -cp Q2_Custom_Exception StorageDemo
javac Q2_Custom_Exception/PortalDemo.java
java -cp Q2_Custom_Exception PortalDemo

javac Q3_Resource_Management/ResourceDemo.java
java -cp Q3_Resource_Management ResourceDemo

javac Q4_Thread_Lifecycle/ThreadDemo.java
java -cp Q4_Thread_Lifecycle ThreadDemo

javac Q5_Seat_Booking/SeatBooking.java
java -cp Q5_Seat_Booking SeatBooking

javac Q6_Deadlock_Communication/TokenCounter.java
java -cp Q6_Deadlock_Communication TokenCounter
javac Q6_Deadlock_Communication/WalletTransferDemo.java
java -cp Q6_Deadlock_Communication WalletTransferDemo

javac Q7_Concurrent_Processor/ResultProcessor.java
java -cp Q7_Concurrent_Processor ResultProcessor Q7_Concurrent_Processor/input
```

On Windows PowerShell, the same commands work with `;` if you chain commands.

## Suggested GitHub repository
Create one repository per student:
`CSA09-<RegisterNumber>-Assignment3`

Each student must independently run all programs and push to their own repository. Add your faculty as collaborator if the repository is private.

## Git workflow in VS Code
1. Extract this ZIP.
2. Open the extracted folder: **File → Open Folder**.
3. Open **Source Control** (branch icon).
4. Select **Initialize Repository**.
5. Stage the files for Q1, enter commit message `Add Q1 ATM exception handling`, and click **Commit**.
6. Repeat with one commit per task (Q2 through Q7). You may stage only the relevant folder each time.
7. Click **Publish Branch** / **Publish to GitHub**, sign in, and create the repository using the exact required name.
8. Copy the repository URL into the report and README.
9. For later changes: stage → write a meaningful commit message → Commit → Sync Changes / Push.

Equivalent terminal commands:

```bash
git init
git add Q1_ATM_Exception
git commit -m "Add Q1 ATM exception handling"
git add Q2_Custom_Exception
git commit -m "Add Q2 custom exceptions and chaining"
git add Q3_Resource_Management
git commit -m "Add Q3 resource management examples"
git add Q4_Thread_Lifecycle
git commit -m "Add Q4 thread lifecycle examples"
git add Q5_Seat_Booking
git commit -m "Add Q5 synchronized seat booking"
git add Q6_Deadlock_Communication
git commit -m "Add Q6 deadlock prevention and producer consumer"
git add Q7_Concurrent_Processor
git commit -m "Add Q7 concurrent result processor"
git add README.md
git commit -m "Document setup and execution"
git branch -M main
git remote add origin https://github.com/<YOUR-USERNAME>/CSA09-<RegisterNumber>-Assignment3.git
git push -u origin main
```

If Git says `remote origin already exists`, use:
`git remote set-url origin <YOUR-REPOSITORY-URL>`

## Academic evidence to add
Create an `evidence/` folder in each task and add:
- Terminal screenshots showing the run.
- Test-case tables and observed results.
- Stack traces where requested.
- Raw timing measurements from your own machine.
- Debugger screenshots for Q3.
- Ten-run scheduling observations for Q4 and Q5.

The sample outputs in source comments are illustrative. Thread output order and measured timings can vary.
