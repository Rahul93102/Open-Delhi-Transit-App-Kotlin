package com.opendelhitransit.metroapi.model

// Basic Station model used in the API
data class Station(
    val name: String,
    val line: String,
    val index: Int
)

// Line model
data class MetroLine(
    val name: String,
    val stations: List<Station>
)

// Route model for path finding
data class Route(
    val source: Station,
    val destination: Station,
    val path: List<Station>,
    val totalStations: Int,
    val interchangeCount: Int
)

// API response model for the shortestPath endpoint
data class ApiRouteResponse(
    val source: String,
    val destination: String,
    val path: List<String>,
    val lines: List<String>,
    val interchanges: Int,
    val totalStations: Int
) 