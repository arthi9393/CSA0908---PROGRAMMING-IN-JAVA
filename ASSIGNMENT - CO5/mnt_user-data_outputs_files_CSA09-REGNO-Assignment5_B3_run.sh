#!/bin/sh
# mysql -u root -p < create_db.sql   (once)   then:
CP=".:lib/*:$CLASSPATH"
javac -cp "$CP" *.java && java -cp "$CP" B3Benchmark
