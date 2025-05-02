package com.example.opendelhitransit.data.network

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {
    @GET("geocoders/geocode")
    suspend fun geocodeAddress(
        @Query("address") address: String,
        @Query("cityCode") cityCode: String? = null,
        @Query("location") location: String? = null,
        @Query("max_num_results") maxResults: Int = 5,
        @Query("pretty_print") prettyPrint: Boolean = true
    ): Response<com.example.opendelhitransit.data.model.GeocodingResponse>
    
    @GET("geocoders/reverse")
    fun reverseGeocode(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("location") location: String? = null,
        @Query("max_num_results") maxResults: Int = 1,
        @Query("pretty_print") prettyPrint: Boolean = true
    ): Call<com.example.opendelhitransit.data.model.GeocodingResponse>
    
    companion object {
        const val BASE_URL = "https://maps.gomaps.pro/"
    }
} 