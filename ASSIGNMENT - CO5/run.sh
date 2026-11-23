#!/bin/sh
# Usage: ./run.sh   (needs MySQL running, create_db.sql executed, connector jar in ./lib or CLASSPATH)
CP=".:lib/*:$CLASSPATH"
javac -cp "$CP" *.java && java -cp "$CP" LoginDemo && java -cp "$CP" DamagedFileDemo
