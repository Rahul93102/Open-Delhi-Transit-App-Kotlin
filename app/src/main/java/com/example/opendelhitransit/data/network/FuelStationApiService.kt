package com.example.opendelhitransit.data.network

import com.example.opendelhitransit.data.model.FuelStationResponse
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

interface FuelStationApiService {
    
    @GET("alt-fuel-stations/v1/nearest.json")
    suspend fun getNearestStations(
        @Query("api_key") apiKey: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("fuel_type") fuelType: String,
        @Query("radius") radius: Int = 10,
        @Query("limit") limit: Int = 5,
        @Query("status") status: String = "E",  // E = Available
        @Query("ev_connector_type") evConnectorType: String? = null,
        @Query("ev_charging_level") evChargingLevel: String? = null,
        @Query("cng_fill_type") cngFillType: String? = null,
        @Query("hy_is_retail") hyIsRetail: String? = null
    ): Response<FuelStationResponse>
    
    companion object {
        private const val BASE_URL = "https://developer.nrel.gov/api/"
        private const val API_KEY = "htowLoRULYl8PPx3YewO0gUhKVWjHCcDFF2EqLuv"
        
        fun create(): FuelStationApiService {
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
                .create(FuelStationApiService::class.java)
        }
        
        fun getApiKey(): String {
            return API_KEY
        }
    }
} 