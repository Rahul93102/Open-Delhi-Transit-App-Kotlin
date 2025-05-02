package com.example.myapplication.data.api

import com.example.myapplication.data.model.MetroLine
import com.example.myapplication.data.model.Route
import com.example.myapplication.data.model.Station
import retrofit2.http.GET
import retrofit2.http.Path

interface MetroApiService {
    @GET("stations")
    suspend fun getAllStations(): List<Station>

    @GET("stations/{stationName}")
    suspend fun getStation(@Path("stationName") stationName: String): Station

    @GET("lines")
    suspend fun getAllLines(): List<MetroLine>

    @GET("lines/{lineName}")
    suspend fun getLine(@Path("lineName") lineName: String): MetroLine

    @GET("lines/{lineName}/stations")
    suspend fun getStationsByLine(@Path("lineName") lineName: String): List<Station>

    @GET("route/{sourceStation}/{destinationStation}")
    suspend fun getRoute(
        @Path("sourceStation") sourceStation: String,
        @Path("destinationStation") destinationStation: String
    ): Route
} 