package com.example.opendelhitransit.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GoMapsFuelStationResponse(
    @Json(name = "status") val status: String,
    @Json(name = "results") val results: List<GoMapsFuelStation>
)

@JsonClass(generateAdapter = true)
data class GoMapsFuelStation(
    @Json(name = "place_id") val placeId: String,
    @Json(name = "name") val name: String,
    @Json(name = "vicinity") val vicinity: String,
    @Json(name = "geometry") val geometry: GoMapsGeometry,
    @Json(name = "rating") val rating: Double? = null,
    @Json(name = "opening_hours") val openingHours: OpeningHours? = null
)

@JsonClass(generateAdapter = true)
data class GoMapsGeometry(
    @Json(name = "location") val location: GoMapsLocation
)

@JsonClass(generateAdapter = true)
data class GoMapsLocation(
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double
)

@JsonClass(generateAdapter = true)
data class OpeningHours(
    @Json(name = "open_now") val openNow: Boolean? = null
) 