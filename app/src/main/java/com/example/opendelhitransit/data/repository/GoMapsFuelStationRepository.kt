package com.example.opendelhitransit.data.repository

import android.util.Log
import com.example.opendelhitransit.data.model.FuelStation
import com.example.opendelhitransit.data.model.FuelType
import com.example.opendelhitransit.data.model.GoMapsFuelStation
import com.example.opendelhitransit.data.network.GoMapsFuelStationApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoMapsFuelStationRepository @Inject constructor(
    private val apiService: GoMapsFuelStationApiService
) {
    private val TAG = "GoMapsFuelStationRepo"
    private val apiKey = GoMapsFuelStationApiService.getApiKey()
    
    suspend fun getNearbyFuelStations(
        latitude: Double,
        longitude: Double,
        radius: Int = 5000,  // in meters
        maxResults: Int = 10
    ): List<GoMapsFuelStation> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching fuel stations near ($latitude, $longitude)")
            
            val response = apiService.getNearbyFuelStations(
                location = "$latitude,$longitude",
                radius = radius,
                type = "gas_station",
                apiKey = apiKey
            )
            
            if (!response.isSuccessful) {
                Log.e(TAG, "API request failed with code: ${response.code()}")
                return@withContext emptyList()
            }
            
            val stationResponse = response.body()
            if (stationResponse == null) {
                Log.e(TAG, "Response body is null")
                return@withContext emptyList()
            }
            
            if (stationResponse.status != "OK") {
                Log.e(TAG, "API Error: ${stationResponse.status}")
                return@withContext emptyList()
            }
            
            Log.d(TAG, "Fetched ${stationResponse.results.size} stations")
            
            // Limit the number of results if needed
            val limitedResults = if (stationResponse.results.size > maxResults) {
                stationResponse.results.take(maxResults)
            } else {
                stationResponse.results
            }
            
            return@withContext limitedResults
            
        } catch (e: IOException) {
            Log.e(TAG, "Network error fetching stations", e)
            return@withContext emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching fuel stations", e)
            return@withContext emptyList()
        }
    }
    
    // Helper method to convert GoMapsFuelStation to FuelStation for compatibility with existing UI
    fun convertToFuelStation(goMapsStation: GoMapsFuelStation): FuelStation {
        return FuelStation(
            id = goMapsStation.placeId.hashCode().toLong(),
            stationName = goMapsStation.name,
            streetAddress = goMapsStation.vicinity,
            city = "",  // Not available in GoMaps API
            state = "",  // Not available in GoMaps API
            zip = "",    // Not available in GoMaps API
            latitude = goMapsStation.geometry.location.lat,
            longitude = goMapsStation.geometry.location.lng,
            distance = 0.0,  // Distance not provided directly by GoMaps
            fuelTypeCode = "GASOLINE",  // Default type
            statusCode = "E",  // Assuming all are available
            accessDaysTime = if (goMapsStation.openingHours?.openNow == true) "Open now" else "Unknown hours",
            evConnectorTypes = null,
            evLevel2Count = null,
            evDcFastCount = null,
            cngFillTypeCode = null,
            cngPsi = null,
            ngFillTypeCode = null,
            ngPsi = null,
            hyIsRetail = null,
            hyPressures = null,
            phone = null
        )
    }
} 