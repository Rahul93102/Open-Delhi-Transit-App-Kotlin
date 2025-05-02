package com.example.opendelhitransit.util

import android.content.Context
import android.util.Log
import com.example.opendelhitransit.data.model.JsonStation
import com.example.opendelhitransit.data.model.LineStation
import com.example.opendelhitransit.data.model.Station
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

object JsonLoader {
    private val gson = GsonBuilder().setLenient().create()
    private val lineFiles = listOf(
        "yellow", "blue", "red", "green", "violet", "orange", 
        "magenta", "pink", "aqua", "grey", "rapid", 
        "greenbranch", "bluebranch", "pinkbranch"
    )
    
    // Load all stations from all line files
    fun loadAllStations(context: Context): List<Station> {
        val allStations = mutableListOf<Station>()
        
        // Load stations from each line file
        lineFiles.forEach { fileName ->
            try {
                Log.d("JsonLoader", "Attempting to load $fileName.json")
                val jsonContent = readJsonFromAssets(context, "lines/$fileName.json")
                Log.d("JsonLoader", "Read ${jsonContent.length} characters from $fileName.json")
                
                val stations = loadStationsFromJsonFile(context, fileName)
                allStations.addAll(stations)
                Log.d("JsonLoader", "Successfully loaded ${stations.size} stations from $fileName.json")
            } catch (e: Exception) {
                Log.e("JsonLoader", "Error loading $fileName.json: ${e.message}")
                e.printStackTrace() // Print stack trace for debugging
            }
        }
        
        Log.d("JsonLoader", "Total stations loaded: ${allStations.size}")
        return allStations
    }
    
    // Load stations for a specific line
    private fun loadStationsFromJsonFile(context: Context, fileName: String): List<Station> {
        val jsonString = readJsonFromAssets(context, "lines/$fileName.json")
        
        try {
            // Special handling for magenta.json which has a different structure
            if (fileName == "magenta") {
                Log.d("JsonLoader", "Using special handler for magenta.json")
                return loadMagentaLineStations(jsonString, fileName)
            }
            
            val listType = object : TypeToken<List<LineStation>>() {}.type
            val lineStations: List<LineStation> = gson.fromJson(jsonString, listType)
            Log.d("JsonLoader", "Parsed ${lineStations.size} LineStation objects from $fileName.json")
            
            // Convert LineStation objects to Station objects, adding index
            val stations = lineStations.mapIndexedNotNull { index, lineStation ->
                try {
                    val station = lineStation.toStation(fileName, index)
                    Log.d("JsonLoader", "Created station: ${station.name} (${station.line}) at index ${station.index}")
                    station
                } catch (e: Exception) {
                    Log.e("JsonLoader", "Error converting station at index $index: ${e.message}")
                    null
                }
            }
            
            Log.d("JsonLoader", "Successfully converted ${stations.size} stations for $fileName")
            return stations
        } catch (e: Exception) {
            Log.e("JsonLoader", "JSON parsing error for $fileName: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    // Special handler for magenta.json which has "25" as a key instead of "English"
    private fun loadMagentaLineStations(jsonString: String, fileName: String): List<Station> {
        val listType = object : TypeToken<List<JsonObject>>() {}.type
        val stationObjects: List<JsonObject> = gson.fromJson(jsonString, listType)
        
        val stations = stationObjects.mapIndexedNotNull { index, jsonObject ->
            try {
                // Get the station name from the "25" field
                val stationName = jsonObject.get("25")?.asString
                if (stationName != null) {
                    val station = Station(
                        name = stationName.trim(),
                        line = fileName,
                        index = index
                    )
                    Log.d("JsonLoader", "Created magenta station: ${station.name} at index ${station.index}")
                    station
                } else {
                    Log.e("JsonLoader", "Missing station name for magenta line index $index")
                    null
                }
            } catch (e: Exception) {
                Log.e("JsonLoader", "Error converting magenta station at index $index: ${e.message}")
                null
            }
        }
        
        Log.d("JsonLoader", "Successfully converted ${stations.size} stations for magenta line")
        return stations
    }
    
    // Load stations by line name
    fun loadStationsByLine(context: Context, lineName: String): List<Station> {
        val normalizedLineName = lineName.lowercase().trim()
        
        // Find corresponding file name
        val fileName = lineFiles.find { it.contains(normalizedLineName) }
            ?: return emptyList()
        
        return try {
            loadStationsFromJsonFile(context, fileName)
        } catch (e: Exception) {
            Log.e("JsonLoader", "Error loading stations for line $lineName: ${e.message}")
            emptyList()
        }
    }
    
    // Find stations by name (case insensitive partial match)
    fun findStationsByName(stations: List<Station>, name: String): List<Station> {
        val normalizedName = name.lowercase().trim()
        return stations.filter { 
            it.name.lowercase().contains(normalizedName) 
        }
    }
    
    // Helper method to read JSON file from assets
    private fun readJsonFromAssets(context: Context, filePath: String): String {
        val inputStream = context.assets.open(filePath)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        bufferedReader.close()
        return stringBuilder.toString()
    }
} 