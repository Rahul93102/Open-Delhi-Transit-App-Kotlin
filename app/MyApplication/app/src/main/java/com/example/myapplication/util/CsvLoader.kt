package com.example.myapplication.util

import android.content.Context
import android.util.Log
import com.example.myapplication.data.model.Station
import java.io.BufferedReader
import java.io.InputStreamReader

object CsvLoader {
    
    fun loadStationsFromCsv(context: Context): List<Station> {
        val stations = mutableListOf<Station>()
        
        try {
            val inputStream = context.assets.open("DELHI_METRO_DATA.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val allLines = reader.readLines()
            Log.d("CsvLoader", "Total lines in CSV: ${allLines.size}")
            
            // Skip header (first line)
            val dataLines = allLines.drop(1)
            Log.d("CsvLoader", "Data lines after skipping header: ${dataLines.size}")
            
            // Process each line
            dataLines.forEachIndexed { index, line ->
                processLine(line, stations, index)
            }
            
            reader.close()
            Log.d("CsvLoader", "Successfully loaded ${stations.size} stations from CSV")
            
        } catch (e: Exception) {
            Log.e("CsvLoader", "Error loading CSV: ${e.message}", e)
            // If there's an error, load hardcoded stations
            if (stations.isEmpty()) {
                Log.d("CsvLoader", "Loading hardcoded stations as fallback")
                loadAllHardcodedStations(stations)
            }
        }
        
        // If still no stations (should never happen), load hardcoded data
        if (stations.isEmpty()) {
            Log.d("CsvLoader", "CSV loading failed completely, using hardcoded stations")
            loadAllHardcodedStations(stations)
        }
        
        Log.d("CsvLoader", "Final station count: ${stations.size}")
        return stations
    }
    
    private fun processLine(line: String, stations: MutableList<Station>, index: Int) {
        if (line.isBlank()) return
        
        try {
            val parts = line.split(",")
            if (parts.size >= 2) { // Only need name and line
                val stationName = parts[0].trim()
                val lineName = parts[1].trim()
                
                if (stationName.isNotEmpty() && lineName.isNotEmpty()) {
                    stations.add(Station(stationName, lineName, index))
                    Log.v("CsvLoader", "Added station: $stationName on $lineName with index $index")
                }
            }
        } catch (e: Exception) {
            Log.e("CsvLoader", "Error processing line: $line, Error: ${e.message}")
        }
    }
    
    private fun loadAllHardcodedStations(stations: MutableList<Station>) {
        // We'll only use this as a fallback if CSV loading fails
        // Just load a few essential stations to ensure the app works
        addHardcodedStations(stations)
    }
    
    private fun addHardcodedStations(stations: MutableList<Station>) {
        // Just load some key stations as fallback
        stations.add(Station("Rajiv Chowk", "Yellow Line", 1))
        stations.add(Station("Central Secretariat", "Yellow Line", 2))
        stations.add(Station("Patel Chowk", "Yellow Line", 3))
        stations.add(Station("Kashmere Gate", "Red Line", 1))
        stations.add(Station("Chandni Chowk", "Yellow Line", 4))
        stations.add(Station("New Delhi", "Yellow Line", 5))
        stations.add(Station("Mandi House", "Blue Line", 1))
    }
} 