-- Task 6: Campus Event Registration - MySQL 8 schema.   Run:  mysql -u root -p < schema.sql
CREATE DATABASE IF NOT EXISTS campus_events CHARACTER SET utf8mb4;
USE campus_events;

CREATE TABLE IF NOT EXISTS events (
    event_id    INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    event_date  DATE         NOT NULL,
    capacity    INT          NOT NULL DEFAULT 200
);

CREATE TABLE IF NOT EXISTS registrations (
    reg_id         INT AUTO_INCREMENT PRIMARY KEY,
    student_reg_no VARCHAR(15)  NOT NULL,
    student_name   VARCHAR(100) NOT NULL,
    email          VARCHAR(120) NOT NULL,
    event_id       INT          NOT NULL,
    registered_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- the database itself guarantees "no double registration", even if two desks click at the same time
    CONSTRAINT uq_student_event UNIQUE (student_reg_no, event_id),
    CONSTRAINT fk_reg_event FOREIGN KEY (event_id) REFERENCES events(event_id),
    INDEX idx_reg_event (event_id)
);

INSERT IGNORE INTO events (name, event_date, capacity) VALUES
    ('Hackathon 2026',   '2026-12-05', 150),
    ('Cultural Night',   '2026-12-12', 500),
    ('Robotics Workshop','2026-12-19',  60),
    ('Quiz Bowl',        '2027-01-09', 100);
