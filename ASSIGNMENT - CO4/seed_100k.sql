-- Optional: 100,000 sample registrations for the Task 6 "alternative concept" measurement (MySQL 8).
USE campus_events;
SET SESSION cte_max_recursion_depth = 200000;
INSERT INTO registrations (student_reg_no, student_name, email, event_id)
WITH RECURSIVE n(i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM n WHERE i < 100000)
SELECT CONCAT('S', LPAD(i, 7, '0')), CONCAT('Student ', i), CONCAT('s', i, '@example.edu'), 1 + (i MOD 4) FROM n;
