# Metro API Server

A local API server that provides Delhi Metro route information.

## Overview

This server implements the same API endpoints as the remote server at `https://iiitd-delhimetro-shortestroute.onrender.com/` but runs locally on your machine. It uses the same path-finding algorithm as the Android app's local implementation.

## Features

- Get all metro lines
- Get all stations
- Get stations by line
- Find shortest path between stations
- Search for stations by name

## API Endpoints

### Get all lines

```
GET /lines
```

### Get all stations

```
GET /stations
```

### Get stations by line

```
GET /stationsByLine?line={lineName}
```

### Find shortest path between stations

```
GET /shortestPath?source={sourceStation}&destination={destinationStation}
```

### Search stations

```
GET /searchStations?query={searchQuery}
```

## How to Run

### Using Gradle

```bash
cd metro-api-server
./gradlew bootRun
```

### Using Java

```bash
cd metro-api-server
./gradlew build
java -jar build/libs/metro-api-server-0.0.1-SNAPSHOT.jar
```

## Configuration

The server runs on port 8082 by default. You can change this in the `application.properties` file.

## Integration with Android App

To use this local server with your Android app, update the BASE_URL in MetroApiService.kt:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8082/"  // For emulator
// or
private const val BASE_URL = "http://192.168.1.X:8082/"  // For real device (use your computer's IP)
```
