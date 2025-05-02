package com.example.opendelhitransit.data.model

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    @SerializedName("results") val results: List<GeocodingResult>,
    @SerializedName("status") val status: String
)

data class GeocodingResult(
    @SerializedName("address_components") val addressComponents: List<AddressComponent>,
    @SerializedName("formatted_address") val formattedAddress: String,
    @SerializedName("geometry") val geometry: Geometry,
    @SerializedName("place_id") val placeId: String,
    @SerializedName("types") val types: List<String>
)

data class AddressComponent(
    @SerializedName("long_name") val longName: String,
    @SerializedName("short_name") val shortName: String,
    @SerializedName("types") val types: List<String>
)

data class Geometry(
    @SerializedName("location") val location: Location,
    @SerializedName("location_type") val locationType: String,
    @SerializedName("viewport") val viewport: Viewport
)

data class Location(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)

data class Viewport(
    @SerializedName("northeast") val northeast: Location,
    @SerializedName("southwest") val southwest: Location
) 