package com.example.opendelhitransit.data.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import java.util.concurrent.TimeUnit

interface TransitApiService {
    @Streaming
    @GET("realtime/VehiclePositions.pb")
    suspend fun getVehiclePositions(@Query("key") apiKey: String): Response<ResponseBody>

    companion object {
        private const val BASE_URL = "https://otd.delhi.gov.in/api/"
        private const val TAG = "TransitApiService"
        
        fun create(): TransitApiService {
            val logger = HttpLoggingInterceptor { message ->
                Log.d(TAG, message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            
            Log.d(TAG, "Creating API service with base URL: $BASE_URL")
                
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .build()
                .create(TransitApiService::class.java)
        }
    }
} 