#!/bin/sh
CP=".:lib/*:$CLASSPATH"
javac -cp "$CP" *.java && java MakeSampleFile && java -cp "$CP" Migrate && java -cp "$CP" Concurrency
