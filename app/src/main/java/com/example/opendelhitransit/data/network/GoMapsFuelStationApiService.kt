package com.example.opendelhitransit.data.network

import com.example.opendelhitransit.data.model.GoMapsFuelStationResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GoMapsFuelStationApiService {
    
    @GET("maps/api/place/nearbysearch/json")
    suspend fun getNearbyFuelStations(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") apiKey: String,
        @Query("rankby") rankBy: String = "prominence"
    ): Response<GoMapsFuelStationResponse>
    
    companion object {
        private const val BASE_URL = "https://maps.gomaps.pro/"
        private const val API_KEY = "AlzaSyCTuTnPjNntqTTs2I_zMHCbXfWDcoTqGVJ"
        
        fun create(): GoMapsFuelStationApiService {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
                
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
                
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(GoMapsFuelStationApiService::class.java)
        }
        
        fun getApiKey(): String {
            return API_KEY
        }
    }
} 