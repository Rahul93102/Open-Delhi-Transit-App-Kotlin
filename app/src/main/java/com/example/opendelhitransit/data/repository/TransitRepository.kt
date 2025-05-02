package com.example.opendelhitransit.data.repository

import android.util.Log
import com.example.opendelhitransit.data.model.VehicleData
import com.example.opendelhitransit.data.network.TransitApiService
import com.example.opendelhitransit.util.GtfsRealtime
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

@Singleton
class TransitRepository @Inject constructor(
    private val apiService: TransitApiService
) {
    private val apiKey = "7w7PJE7dxvuqYy1pOJL7FhfaKYVs70Pe"
    private val TAG = "TransitRepository"

    suspend fun getAllVehicles(limit: Int = 100): List<VehicleData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Making API request to get all vehicles (limit: $limit)")
            val response = apiService.getVehiclePositions(apiKey)
            
            if (!response.isSuccessful) {
                Log.e(TAG, "API request failed with code: ${response.code()}")
                return@withContext createSampleVehicleList(limit)
            }
            
            val responseBody = response.body()
            if (responseBody == null) {
                Log.e(TAG, "Response body is null")
                return@withContext createSampleVehicleList(limit)
            }
            
            try {
                Log.d(TAG, "Received response, parsing protobuf data")
                val bytes = responseBody.bytes()
                Log.d(TAG, "Response size: ${bytes.size} bytes")
                
                if (bytes.isEmpty()) {
                    Log.e(TAG, "Empty response received")
                    return@withContext createSampleVehicleList(limit)
                }
                
                val feedMessage = GtfsRealtime.FeedMessage.parseFrom(bytes)
                Log.d(TAG, "Total entities in feed: ${feedMessage.entityCount}")
                
                if (feedMessage.entityCount == 0) {
                    Log.e(TAG, "No entities found in feed")
                    return@withContext createSampleVehicleList(limit)
                }
                
                val vehicleList = feedMessage.entityList
                    .take(limit)
                    .mapNotNull { entity ->
                        if (entity.hasVehicle()) {
                            val vehicle = entity.vehicle
                            val trip = if (vehicle.hasTrip()) vehicle.trip else null
                            val position = if (vehicle.hasPosition()) vehicle.position else null
                            
                            VehicleData(
                                id = entity.id,
                                routeId = trip?.routeId,
                                tripId = trip?.tripId,
                                startTime = trip?.startTime,
                                startDate = trip?.startDate,
                                latitude = position?.latitude,
                                longitude = position?.longitude,
                                speed = position?.speed,
                                timestamp = vehicle.timestamp
                            )
                        } else null
                    }
                
                Log.d(TAG, "Processed ${vehicleList.size} vehicles")
                
                if (vehicleList.isEmpty()) {
                    Log.e(TAG, "No vehicles found in feed")
                    return@withContext createSampleVehicleList(limit)
                }
                
                return@withContext vehicleList
                
            } catch (e: IOException) {
                Log.e(TAG, "Error parsing protobuf data", e)
                e.printStackTrace()
                return@withContext createSampleVehicleList(limit)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching vehicle data", e)
            e.printStackTrace()
            return@withContext createSampleVehicleList(limit)
        }
    }

    // Helper function to generate sample vehicle data for testing
    private fun createSampleVehicleList(count: Int): List<VehicleData> {
        val delhiCenter = LatLng(28.6139, 77.2090)
        val vehicles = mutableListOf<VehicleData>()
        
        // Generate a few random vehicles around Delhi
        for (i in 1..count) {
            // Generate a random position within ~5km of Delhi center
            val latOffset = (Math.random() - 0.5) * 0.1
            val lngOffset = (Math.random() - 0.5) * 0.1
            
            vehicles.add(
                VehicleData(
                    id = "SAMPLE_$i",
                    routeId = "Route${i % 10}",
                    tripId = "Trip$i",
                    latitude = (delhiCenter.latitude + latOffset).toFloat(),
                    longitude = (delhiCenter.longitude + lngOffset).toFloat(),
                    speed = (10 + (Math.random() * 40)).toFloat(),
                    timestamp = System.currentTimeMillis() / 1000
                )
            )
        }
        
        return vehicles
    }
    
    // Function to find buses near the given location
    suspend fun getVehiclesNear(
        location: LatLng,
        radiusKm: Double = 2.0,
        limit: Int = 10
    ): List<VehicleData> = withContext(Dispatchers.IO) {
        val allVehicles = getAllVehicles(100)
        
        val nearbyVehicles = allVehicles
            .filter { vehicle -> 
                vehicle.getLatLng()?.let { vehicleLocation ->
                    calculateDistance(location, vehicleLocation) <= radiusKm
                } ?: false
            }
            .sortedBy { vehicle ->
                vehicle.getLatLng()?.let { vehicleLocation ->
                    calculateDistance(location, vehicleLocation)
                } ?: Double.MAX_VALUE
            }
            .take(limit)
            
        return@withContext nearbyVehicles
    }
    
    // Calculate distance between two LatLng points using Haversine formula
    private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val earthRadius = 6371.0 // Radius of Earth in kilometers
        
        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val lon1Rad = Math.toRadians(point1.longitude)
        val lon2Rad = Math.toRadians(point2.longitude)
        
        val x = (lon2Rad - lon1Rad) * cos((lat1Rad + lat2Rad) / 2)
        val y = lat2Rad - lat1Rad
        return earthRadius * Math.sqrt(x * x + y * y)
    }
} 