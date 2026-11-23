CREATE DATABASE IF NOT EXISTS buspass_test;
USE buspass_test;
DROP TABLE IF EXISTS bus_pass;
CREATE TABLE bus_pass (
  pass_id  VARCHAR(10) PRIMARY KEY,
  reg_no   VARCHAR(12) NOT NULL,
  name     VARCHAR(60) NOT NULL,
  route_no INT         NOT NULL,
  amount   DOUBLE      NOT NULL,
  INDEX idx_route (route_no)
) ENGINE=InnoDB;
