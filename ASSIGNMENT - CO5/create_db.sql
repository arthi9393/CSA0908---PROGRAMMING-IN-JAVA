-- Local TEST database created only for this assignment (Task B2)
CREATE DATABASE IF NOT EXISTS buspass_test;
USE buspass_test;
DROP TABLE IF EXISTS users;
CREATE TABLE users (
  id  VARCHAR(30) PRIMARY KEY,
  pwd VARCHAR(60) NOT NULL
);
INSERT INTO users VALUES ('admin','S3cret!'), ('arun','arun@123');
