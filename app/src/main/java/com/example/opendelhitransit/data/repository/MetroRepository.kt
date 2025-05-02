package com.example.opendelhitransit.data.repository

import android.content.Context
import android.util.Log
import com.example.opendelhitransit.data.model.MetroLine
import com.example.opendelhitransit.data.model.Route
import com.example.opendelhitransit.data.model.Station
import com.example.opendelhitransit.util.JsonLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.PriorityQueue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetroRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val stations = mutableListOf<Station>()
    private val lines = mutableMapOf<String, MutableList<Station>>()
    private val graph = mutableMapOf<Station, MutableSet<Station>>()
    private val stationMap = mutableMapOf<String, Station>() // For quick lookup by name

    init {
        loadStationsFromJson()
        buildGraph()
    }

    private fun loadStationsFromJson() {
        stations.clear()
        lines.clear()
        stationMap.clear()
        
        // Load all stations from JSON files
        val loadedStations = JsonLoader.loadAllStations(context)
        stations.addAll(loadedStations)
        
        // Organize stations by line
        loadedStations.forEach { station ->
            lines.getOrPut(station.line) { mutableListOf() }.add(station)
            stationMap[station.name.lowercase()] = station
        }
        
        // Sort stations by index within each line
        lines.forEach { (_, stationList) ->
            stationList.sortBy { it.index }
        }
        
        Log.d("MetroRepository", "Loaded ${stations.size} stations across ${lines.size} lines")
    }

    private fun buildGraph() {
        graph.clear()
        
        // Connect stations on the same line based on index sequence
        lines.forEach { (_, lineStations) ->
            if (lineStations.size > 1) {
                // Sort by index to ensure correct sequence
                val sortedStations = lineStations.sortedBy { it.index }
                
                // Connect adjacent stations
                for (i in 0 until sortedStations.size - 1) {
                    addEdge(sortedStations[i], sortedStations[i + 1])
                }
            }
        }
        
        // Connect interchange stations (stations with same name but different lines)
        val stationsByName = stations.groupBy { it.name.lowercase() }
        stationsByName.values.filter { it.size > 1 }.forEach { sameNameStations ->
            // Connect all stations with the same name (interchanges)
            for (i in 0 until sameNameStations.size) {
                for (j in i + 1 until sameNameStations.size) {
                    addEdge(sameNameStations[i], sameNameStations[j])
                }
            }
        }
        
        Log.d("MetroRepository", "Built graph with ${graph.size} nodes")
    }

    private fun addEdge(station1: Station, station2: Station) {
        graph.getOrPut(station1) { mutableSetOf() }.add(station2)
        graph.getOrPut(station2) { mutableSetOf() }.add(station1)
    }

    suspend fun getAllStations(): List<Station> = stations

    suspend fun getStation(stationName: String): Station? {
        val normalizedName = stationName.lowercase().trim()
        return stationMap[normalizedName] ?: stations.find { 
            it.name.lowercase().trim() == normalizedName
        }
    }

    suspend fun getAllLines(): List<MetroLine> = 
        withContext(Dispatchers.IO) {
            lines.map { (name, stations) -> MetroLine(name, stations) }
        }

    suspend fun getLine(lineName: String): MetroLine? = 
        withContext(Dispatchers.IO) {
            val normalizedName = lineName.lowercase().trim()
            lines.entries.find { it.key.lowercase().contains(normalizedName) }?.let { (name, stations) ->
                MetroLine(name, stations)
            }
        }

    suspend fun getStationsByLine(lineName: String): List<Station> =
        withContext(Dispatchers.IO) {
            val normalizedName = lineName.lowercase().trim()
            lines.entries.find { it.key.lowercase().contains(normalizedName) }?.value ?: emptyList()
        }

    suspend fun getRoute(sourceStation: String, destinationStation: String): Route {
        return withContext(Dispatchers.Default) {
            val source = getStation(sourceStation)
            val destination = getStation(destinationStation)
            
            if (source == null || destination == null) {
                throw IllegalArgumentException("Invalid station names: $sourceStation or $destinationStation")
            }

            val path = findShortestPath(source, destination)
            
            Route(
                source = source,
                destination = destination,
                path = path,
                totalStations = path.size - 1, // Number of stations excluding source
                interchangeCount = calculateInterchangeCount(path)
            )
        }
    }

    private fun findShortestPath(source: Station, destination: Station): List<Station> {
        // If source and destination are the same
        if (source.name.equals(destination.name, ignoreCase = true)) {
            return listOf(source)
        }
        
        // Initialize Dijkstra algorithm with priority queue
        val visited = mutableSetOf<Station>()
        val distances = mutableMapOf<Station, Int>()
        val previousStation = mutableMapOf<Station, Station>()
        
        // Custom comparator for priority queue based on distance
        val comparator = Comparator<Station> { s1, s2 ->
            (distances[s1] ?: Int.MAX_VALUE).compareTo(distances[s2] ?: Int.MAX_VALUE)
        }
        
        val queue = PriorityQueue(comparator)
        
        // Initialize all distances as infinite
        stations.forEach { station ->
            distances[station] = Int.MAX_VALUE
        }
        
        // Distance to source is 0
        distances[source] = 0
        queue.add(source)
        
        // Main Dijkstra algorithm loop
        while (queue.isNotEmpty()) {
            val current = queue.poll() ?: continue // Safe null check
            
            // If we reached the destination, we can stop
            if (current == destination) {
                break
            }
            
            // Skip if already visited
            if (current in visited) {
                continue
            }
            
            visited.add(current)
            
            // Process all neighbors
            val neighbors = graph[current] ?: continue // Safe null check
            for (neighbor in neighbors) {
                if (neighbor !in visited) {
                    // Calculate new distance
                    // 1 unit for same line stations, 3 units penalty for interchange
                    val edgeWeight = if (current.line == neighbor.line) 1 else 3
                    val currentDistance = distances[current] ?: Int.MAX_VALUE
                    val newDistance = currentDistance + edgeWeight
                    
                    // If we found a better path
                    val neighborDistance = distances[neighbor] ?: Int.MAX_VALUE
                    if (newDistance < neighborDistance) {
                        distances[neighbor] = newDistance
                        previousStation[neighbor] = current
                        queue.add(neighbor)
                    }
                }
            }
        }
        
        // Reconstruct path
        val path = mutableListOf<Station>()
        
        // Check if destination is reachable
        if (previousStation.containsKey(destination)) {
            var current: Station? = destination
            while (current != null) {
                path.add(0, current)
                if (current == source) {
                    break
                }
                current = previousStation[current]
            }
        } else {
            // No path found, return just source and destination
            path.add(source)
            if (source != destination) {
                path.add(destination)
            }
        }
        
        return path
    }

    private fun calculateInterchangeCount(path: List<Station>): Int {
        var count = 0
        for (i in 1 until path.size) {
            if (path[i].line != path[i - 1].line) {
                count++
            }
        }
        return count
    }
    
    // For searching stations
    suspend fun searchStation(query: String): List<Station> {
        return withContext(Dispatchers.IO) {
            val normalizedQuery = query.lowercase().trim()
            if (normalizedQuery.isEmpty()) {
                return@withContext emptyList()
            }
            
            // First try exact matches
            val exactMatches = stations.filter { 
                it.name.lowercase() == normalizedQuery
            }
            
            if (exactMatches.isNotEmpty()) {
                return@withContext exactMatches
            }
            
            // Then try contains matches
            val containsMatches = stations.filter { 
                it.name.lowercase().contains(normalizedQuery)
            }
            
            // If no direct contains matches, try splitting the query into words
            // and search for partial matches
            if (containsMatches.isEmpty() && normalizedQuery.contains(" ")) {
                val queryWords = normalizedQuery.split(" ").filter { it.length > 1 }
                return@withContext stations.filter { station ->
                    val stationName = station.name.lowercase()
                    queryWords.any { word -> stationName.contains(word) }
                }
            }
            
            containsMatches
        }
    }
} 