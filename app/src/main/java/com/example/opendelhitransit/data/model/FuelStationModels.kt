package com.example.opendelhitransit.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FuelStationResponse(
    @Json(name = "fuel_stations") val fuelStations: List<FuelStation>
)

@JsonClass(generateAdapter = true)
data class FuelStation(
    @Json(name = "id") val id: Long,
    @Json(name = "station_name") val stationName: String,
    @Json(name = "street_address") val streetAddress: String,
    @Json(name = "city") val city: String,
    @Json(name = "state") val state: String,
    @Json(name = "zip") val zip: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "distance") val distance: Double,
    @Json(name = "fuel_type_code") val fuelTypeCode: String,
    @Json(name = "status_code") val statusCode: String,
    @Json(name = "access_days_time") val accessDaysTime: String?,
    @Json(name = "ev_connector_types") val evConnectorTypes: List<String>?,
    @Json(name = "ev_level2_evse_num") val evLevel2Count: Int?,
    @Json(name = "ev_dc_fast_num") val evDcFastCount: Int?,
    @Json(name = "cng_fill_type_code") val cngFillTypeCode: String?,
    @Json(name = "cng_psi") val cngPsi: String?,
    @Json(name = "ng_fill_type_code") val ngFillTypeCode: String?,
    @Json(name = "ng_psi") val ngPsi: String?,
    @Json(name = "hy_is_retail") val hyIsRetail: Boolean?,
    @Json(name = "hy_pressures") val hyPressures: List<String>?,
    @Json(name = "phone") val phone: String?
)

enum class FuelType(val code: String, val displayName: String, val description: String) {
    ELECTRIC("ELEC", "Electric", "Electric Charging Stations"),
    ETHANOL("E85", "Ethanol", "Ethanol (E85) Fuel Stations"),
    CNG("CNG", "CNG", "Compressed Natural Gas Stations"),
    LNG("LNG", "LNG", "Liquefied Natural Gas Stations"),
    PROPANE("LPG", "Propane", "Propane (LPG) Stations"),
    BIODIESEL("BD", "Biodiesel", "Biodiesel (B20+) Stations"),
    HYDROGEN("HY", "Hydrogen", "Hydrogen Fueling Stations"),
    RENEWABLE_DIESEL("RD", "Renewable Diesel", "Renewable Diesel (R20+) Stations");

    companion object {
        fun getAllFuelTypes(): List<FuelType> = values().toList()
        
        fun getFuelTypeByCode(code: String): FuelType? = values().find { it.code == code }
    }
} 