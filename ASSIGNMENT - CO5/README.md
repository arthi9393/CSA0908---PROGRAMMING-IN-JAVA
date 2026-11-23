# ASSIGNMENT 5 – CO5
## DATA PERSISTENCE ENGINEERING – FILES AND DATABASES

**Course:** CSA09 – Programming in Java  
**Course Outcome:** CO5

### STUDENT DETAILS
- **Name:** ARTHI A
- **Register Number:** 192525152

### PROJECT DESCRIPTION
This assignment focuses on Java Data Persistence Engineering using files and databases.

The assignment covers:
- Java I/O – Byte, Character and Buffered Streams
- File Class and File Handling
- Object Serialization
- JDBC Architecture and Drivers
- Statement and PreparedStatement
- CallableStatement
- Database Transactions
- Batch Processing
- Database Metadata
- SQL Injection Prevention

### FILE STRUCTURE

```text
Assignment5/
│
├── Assignment5_CO5_Report.docx
├── PassStore.java
├── PassLoader.java
├── create_bus_pass.sql
└── README.md
```

### SOFTWARE REQUIREMENTS
- Java Development Kit (JDK)
- MySQL Server
- MySQL JDBC Connector
- Visual Studio Code / IntelliJ IDEA / Eclipse
- Git and GitHub

### EXECUTION PROCEDURE

**1. Check Java installation**
```bash
java -version
javac -version
```

**2. Compile PassStore**
```bash
javac PassStore.java
```

**3. Run PassStore**
```bash
java PassStore
```

**4. Create the MySQL database**

Execute the `create_bus_pass.sql` file in MySQL.

**5. Configure JDBC**

Add the MySQL JDBC Connector to the project classpath and configure the database connection.

**6. Execute PassLoader**

Compile and run the JDBC program after configuring the database connection.

### TASKS COMPLETED

- Task A1 – Design a File Format
- Task A2 – Buffered I/O Design
- Task A3 – Data-Access Design Pattern
- Task A4 – Serialization Design
- Task A5 – Transactions and SQL Injection
- Task B1 – Implement and Instrument the File Store
- Task B2 – Engineer an Adversarial Input
- Task B3 – Statement vs PreparedStatement vs Batch
- Task B4 – Final Engineering Decision

### RESULTS
The assignment demonstrates file handling, buffered reading, serialization, JDBC connectivity, database transactions, batch processing and secure database operations.

Actual execution outputs and performance measurements should be recorded after running the programs.

### CONCLUSION
This assignment provides practical knowledge of Java persistence techniques and demonstrates how files and relational databases can be used to store, retrieve and manage data efficiently and securely.



**Course Outcome CO5 – Completed**
