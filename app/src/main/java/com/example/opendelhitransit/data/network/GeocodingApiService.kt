package com.example.opendelhitransit.data.network

import com.example.opendelhitransit.data.model.GeocodingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {
    @GET("geocode")
    suspend fun geocodeAddress(
        @Query("address") address: String,
        @Query("key") apiKey: String,
        @Query("region") region: String = "in"
    ): Response<GeocodingResponse>
    
    companion object {
        const val API_KEY = "AIzaSyC7oBshXVa_S5hMfv3dQGHd-K5KnMJ4w9c"  // Replace with your actual API key
    }
} 