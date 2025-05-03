package com.example.opendelhitransit.data.network

import android.util.Log
import com.example.opendelhitransit.data.model.Route
import com.example.opendelhitransit.data.model.Station
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Data models specific to API responses
data class ApiRouteResponse(
    @SerializedName("source") val source: String,
    @SerializedName("destination") val destination: String,
    @SerializedName("path") val path: List<String>,
    @SerializedName("lines") val lines: List<String>,
    @SerializedName("interchanges") val interchanges: Int,
    @SerializedName("totalStations") val totalStations: Int
)

interface MetroApiService {
    @GET("lines")
    suspend fun getLines(): Response<List<String>>
    
    @GET("stations")
    suspend fun getAllStations(): Response<List<String>>
    
    @GET("stationsByLine")
    suspend fun getStationsByLine(@Query("line") line: String): Response<List<String>>
    
    @GET("shortestPath")
    suspend fun getShortestPath(
        @Query("source") sourceStation: String,
        @Query("destination") destinationStation: String
    ): Response<ApiRouteResponse>

    companion object {
        // Switch to control which server to use
        private const val USE_LOCAL_SERVER = true
        
        // Remote server on render.com
        private const val REMOTE_URL = "https://iiitd-delhimetro-shortestroute.onrender.com/"
        
        // Local server running on your machine
        // Use 10.0.2.2 for Android Emulator (this maps to 127.0.0.1 on your host machine)
        // For physical devices, use your computer's actual IP address (e.g., 192.168.1.X)
        private const val LOCAL_URL = "http://10.0.2.2:8082/"
        
        // The URL that will be used based on the switch
        private val BASE_URL = if (USE_LOCAL_SERVER) LOCAL_URL else REMOTE_URL
        
        private const val TAG = "MetroApiService"
        
        fun create(): MetroApiService {
            val logger = HttpLoggingInterceptor { message ->
                Log.d(TAG, message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .connectTimeout(60, TimeUnit.SECONDS) // Longer timeout for render.com free tier
                .readTimeout(60, TimeUnit.SECONDS)
                .build()
            
            Log.i(TAG, "Using API server at: $BASE_URL")
            
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MetroApiService::class.java)
        }
    }
} 