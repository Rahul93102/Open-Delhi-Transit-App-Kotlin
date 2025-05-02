package com.example.opendelhitransit.util

import android.content.Context
import android.util.Log
import com.example.opendelhitransit.data.model.Station
import com.opencsv.CSVReader
import java.io.InputStreamReader

object CsvLoader {
    // Line colors mapped to line identifiers
    private val lineColors = mapOf(
        "Yellow" to "yellow",
        "Blue" to "blue",
        "Red" to "red",
        "Green" to "green",
        "Violet" to "violet",
        "Orange" to "orange",
        "Magenta" to "magenta",
        "Pink" to "pink",
        "Aqua" to "aqua",
        "Grey" to "grey",
        "Rapid Metro" to "rapid"
    )
    
    // Load stations from the Delhi Metro CSV file
    fun loadStationsFromCsv(context: Context, csvFileName: String = "DELHI_METRO_DATA.csv"): List<Station> {
        val stations = mutableListOf<Station>()
        
        try {
            context.assets.open(csvFileName).use { inputStream ->
                CSVReader(InputStreamReader(inputStream)).use { reader ->
                    // Skip header row
                    reader.skip(1)
                    
                    // Read rows
                    var line: Array<String>?
                    var stationIndex = 0
                    var currentLine = ""
                    
                    while (reader.readNext().also { line = it } != null) {
                        line?.let { csvLine ->
                            if (csvLine.size >= 3) {
                                val stationName = csvLine[0].trim()
                                val lineColor = csvLine[1].trim()
                                val lineCode = lineColors[lineColor] ?: lineColor.lowercase()
                                
                                // If line changed, reset station index
                                if (lineCode != currentLine) {
                                    stationIndex = 0
                                    currentLine = lineCode
                                }
                                
                                // Create station and add to list
                                val station = Station(
                                    name = stationName,
                                    line = lineCode,
                                    index = stationIndex++
                                )
                                stations.add(station)
                            }
                        }
                    }
                }
            }
            
            Log.d("CsvLoader", "Successfully loaded ${stations.size} stations from CSV")
        } catch (e: Exception) {
            Log.e("CsvLoader", "Error loading CSV: ${e.message}")
            e.printStackTrace()
        }
        
        return stations
    }
    
    // Find stations by name in the CSV data
    fun findStationsByName(stations: List<Station>, query: String): List<Station> {
        val normalizedQuery = query.lowercase().trim()
        return stations.filter { 
            it.name.lowercase().contains(normalizedQuery)
        }
    }
    
    // Group stations by line
    fun groupStationsByLine(stations: List<Station>): Map<String, List<Station>> {
        return stations.groupBy { it.line }
    }
} 