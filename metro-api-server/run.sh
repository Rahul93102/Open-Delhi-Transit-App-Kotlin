#!/bin/bash

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install Maven to run this application."
    exit 1
fi

# Run the Spring Boot application with Maven
mvn spring-boot:run 