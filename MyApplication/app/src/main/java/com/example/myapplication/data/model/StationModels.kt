package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

// Basic Station model used in the app
data class Station(
    val name: String,
    val line: String,
    val index: Int
)

// Line model
data class MetroLine(
    val name: String,
    val stations: List<Station>
)

// Route model for path finding
data class Route(
    val source: Station,
    val destination: Station,
    val path: List<Station>,
    val totalStations: Int,
    val interchangeCount: Int,
    val estimatedTime: Int = 0 // in minutes
)

// Models for JSON parsing

// Station Entity from station_entity.json
data class StationEntity(
    val value: String,
    val synonyms: List<String>
)

// Station from line JSON files (yellow.json, blue.json, etc.)
data class LineStation(
    val English: String?,
    val Hindi: String?,
    val Phase: String?,
    val Opening: String?,
    @SerializedName("Interchange\nConnection") val interchange: String?,
    @SerializedName("Station Layout") val stationLayout: String?,
    @SerializedName("Depot Connection") val depotConnection: String?,
    @SerializedName("Depot Layout") val depotLayout: String?
) {
    // Fallback name resolution
    fun getStationName(): String {
        // Try Hindi as primary name, fall back to English name
        // For the Hindi name, strip numeric prefixes that might be in the data
        val hindiBestName = Hindi?.replace(Regex("^\\d+\\s*"), "")?.trim()
        return when {
            !hindiBestName.isNullOrBlank() -> hindiBestName
            !English.isNullOrBlank() && !English.matches(Regex("^\\d+$")) -> English.trim()
            !Phase.isNullOrBlank() -> Phase.trim()
            else -> "Unknown Station"
        }
    }
    
    // Check if this is an interchange station
    fun isInterchange(): Boolean {
        return !interchange.isNullOrBlank() && 
               !interchange.equals("None", ignoreCase = true) &&
               !interchange.equals("No", ignoreCase = true)
    }
    
    // Convert to normalized Station with provided line name and index
    fun toStation(lineName: String, index: Int): Station {
        return Station(
            name = getStationName().trim(),
            line = lineName.trim(),
            index = index
        )
    }
}

// Station from JSON file format
data class JsonStation(
    val name: String?,
    val index: Int?,
    val line: String?
) {
    // Convert to normalized Station
    fun toStation(): Station? {
        return if (name != null && line != null && index != null) {
            Station(
                name = name.trim(),
                line = line.trim(),
                index = index
            )
        } else {
            null
        }
    }
}

// Graph node for Dijkstra's algorithm
data class GraphNode(
    val station: Station,
    var distance: Int = Int.MAX_VALUE,  // Using index difference as distance
    var previousNode: GraphNode? = null,
    var visited: Boolean = false
) {
    // For priority queue comparison
    fun compareTo(other: GraphNode): Int {
        return distance.compareTo(other.distance)
    }
} 