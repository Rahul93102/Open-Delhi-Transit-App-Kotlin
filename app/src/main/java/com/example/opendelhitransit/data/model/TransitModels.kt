package com.example.opendelhitransit.data.model

import com.google.android.gms.maps.model.LatLng

data class VehicleData(
    val id: String,
    val routeId: String? = null,
    val tripId: String? = null,
    val startTime: String? = null,
    val startDate: String? = null,
    val latitude: Float? = null,
    val longitude: Float? = null,
    val speed: Float? = null,
    val timestamp: Long = 0
) {
    fun getLatLng(): LatLng? {
        return if (latitude != null && longitude != null) {
            LatLng(latitude.toDouble(), longitude.toDouble())
        } else null
    }
    
    fun isValid(): Boolean {
        return latitude != null && longitude != null && id.isNotBlank()
    }
}

data class BusRoute(
    val id: String,
    val shortName: String,
    val longName: String,
    val color: String,
    val textColor: String,
    val type: Int,
    val vehicles: List<VehicleData> = emptyList()
)

data class BusStop(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val wheelchairBoarding: Boolean = false,
    val routes: List<String> = emptyList()
) 