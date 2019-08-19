#!/bin/bash

echo "# Kill"
kill $(cat app.pid)

echo "# Pull"
cd tattoo
git pull

echo "# Build"
mvn clean install -DskipTests

echo "# Start"
cd ..
java -jar tattoo/target/tattoo-1.0-SNAPSHOT.jar > /dev/null &
