package com.example.opendelhitransit.data.repository

import android.util.Log
import com.example.opendelhitransit.data.model.FuelStation
import com.example.opendelhitransit.data.model.FuelType
import com.example.opendelhitransit.data.network.FuelStationApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FuelStationRepository @Inject constructor(
    private val apiService: FuelStationApiService
) {
    private val TAG = "FuelStationRepository"
    private val apiKey = FuelStationApiService.getApiKey()
    
    suspend fun getNearestStations(
        latitude: Double,
        longitude: Double,
        fuelType: FuelType,
        radius: Int = 10,
        limit: Int = 5
    ): List<FuelStation> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching ${fuelType.displayName} stations near ($latitude, $longitude)")
            
            // Create special parameter maps based on fuel type
            val evConnectorType: String? = if (fuelType == FuelType.ELECTRIC) "J1772,TESLA,CHADEMO,J1772COMBO" else null
            val evChargingLevel: String? = if (fuelType == FuelType.ELECTRIC) "dc_fast,2" else null
            val cngFillType: String? = if (fuelType == FuelType.CNG) "B,Q" else null  // Both fast-fill and time-fill
            val hyIsRetail: String? = if (fuelType == FuelType.HYDROGEN) "true" else null  // Only retail hydrogen stations
            
            val response = apiService.getNearestStations(
                apiKey = apiKey,
                latitude = latitude,
                longitude = longitude,
                fuelType = fuelType.code,
                radius = radius,
                limit = limit,
                evConnectorType = evConnectorType,
                evChargingLevel = evChargingLevel,
                cngFillType = cngFillType,
                hyIsRetail = hyIsRetail
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
            
            Log.d(TAG, "Fetched ${stationResponse.fuelStations.size} stations")
            return@withContext stationResponse.fuelStations
            
        } catch (e: IOException) {
            Log.e(TAG, "Network error fetching stations", e)
            return@withContext emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching stations", e)
            return@withContext emptyList()
        }
    }
    
    suspend fun getAllFuelTypeStations(
        latitude: Double,
        longitude: Double,
        radius: Int = 10,
        limit: Int = 3
    ): Map<FuelType, List<FuelStation>> = withContext(Dispatchers.IO) {
        val result = mutableMapOf<FuelType, List<FuelStation>>()
        
        FuelType.getAllFuelTypes().forEach { fuelType ->
            val stations = getNearestStations(
                latitude = latitude,
                longitude = longitude,
                fuelType = fuelType,
                radius = radius,
                limit = limit
            )
            result[fuelType] = stations
        }
        
        return@withContext result
    }
} 