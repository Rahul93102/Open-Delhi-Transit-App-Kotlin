package com.example.opendelhitransit.util

data class BusLocation(
    val vehicleId: String,
    val label: String,
    val routeId: String,
    val tripId: String,
    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val timestamp: Long
)
