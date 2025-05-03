#!/bin/bash

# Create build directories
mkdir -p build/classes

# Compile the main class
javac -d build/classes -cp "src/main/kotlin" src/main/kotlin/com/opendelhitransit/metroapi/MetroApiApplication.kt

# Run the application
java -cp build/classes com.opendelhitransit.metroapi.MetroApiApplication 