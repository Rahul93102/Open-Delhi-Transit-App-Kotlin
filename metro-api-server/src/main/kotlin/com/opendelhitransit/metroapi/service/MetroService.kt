package com.opendelhitransit.metroapi.service

import com.opendelhitransit.metroapi.model.MetroLine
import com.opendelhitransit.metroapi.model.Route
import com.opendelhitransit.metroapi.model.Station
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.util.PriorityQueue
import jakarta.annotation.PostConstruct

@Service
class MetroService {
    private val stations = mutableListOf<Station>()
    private val lines = mutableMapOf<String, MutableList<Station>>()
    private val graph = mutableMapOf<Station, MutableSet<Station>>()
    private val stationMap = mutableMapOf<String, Station>() // For quick lookup by name
    private val gson = Gson()
    
    private val lineNames = listOf(
        "Yellow", "Blue", "Red", "Green", "Violet"
    )

    @PostConstruct
    fun init() {
        // Initialize with hardcoded station data (replace with actual JSON loading in a real implementation)
        initializeStationData()
        buildGraph()
    }

    private fun initializeStationData() {
        // In a real implementation, we'd load from JSON files
        // For this demo, let's create some basic station data for two lines
        
        // Yellow Line
        val yellowStations = listOf(
            Station("Samaypur Badli", "Yellow", 0),
            Station("Rohini Sector 18, 19", "Yellow", 1),
            Station("Haiderpur Badli Mor", "Yellow", 2),
            Station("Jahangirpuri", "Yellow", 3),
            Station("Adarsh Nagar", "Yellow", 4),
            Station("Azadpur", "Yellow", 5),
            Station("Model Town", "Yellow", 6),
            Station("Guru Tegh Bahadur Nagar", "Yellow", 7),
            Station("Vishwavidyalaya", "Yellow", 8),
            Station("Vidhan Sabha", "Yellow", 9),
            Station("Civil Lines", "Yellow", 10),
            Station("Kashmere Gate", "Yellow", 11),
            Station("Chandni Chowk", "Yellow", 12),
            Station("Chawri Bazar", "Yellow", 13),
            Station("New Delhi", "Yellow", 14),
            Station("Rajiv Chowk", "Yellow", 15),
            Station("Patel Chowk", "Yellow", 16),
            Station("Central Secretariat", "Yellow", 17),
            Station("Udyog Bhawan", "Yellow", 18),
            Station("Lok Kalyan Marg", "Yellow", 19),
            Station("Jor Bagh", "Yellow", 20),
            Station("Dilli Haat - INA", "Yellow", 21),
            Station("AIIMS", "Yellow", 22),
            Station("Green Park", "Yellow", 23),
            Station("Hauz Khas", "Yellow", 24),
            Station("Malviya Nagar", "Yellow", 25),
            Station("Saket", "Yellow", 26),
            Station("Qutab Minar", "Yellow", 27),
            Station("Chhatarpur", "Yellow", 28),
            Station("Sultanpur", "Yellow", 29),
            Station("Ghitorni", "Yellow", 30),
            Station("Arjan Garh", "Yellow", 31),
            Station("Guru Dronacharya", "Yellow", 32),
            Station("Sikandarpur", "Yellow", 33),
            Station("MG Road", "Yellow", 34),
            Station("IFFCO Chowk", "Yellow", 35),
            Station("Huda City Centre", "Yellow", 36)
        )
        
        // Blue Line
        val blueStations = listOf(
            Station("Dwarka Sector 21", "Blue", 0),
            Station("Dwarka Sector 8", "Blue", 1),
            Station("Dwarka Sector 9", "Blue", 2),
            Station("Dwarka Sector 10", "Blue", 3),
            Station("Dwarka Sector 11", "Blue", 4),
            Station("Dwarka Sector 12", "Blue", 5),
            Station("Dwarka Sector 13", "Blue", 6),
            Station("Dwarka Sector 14", "Blue", 7),
            Station("Dwarka", "Blue", 8),
            Station("Dwarka Mor", "Blue", 9),
            Station("Nawada", "Blue", 10),
            Station("Uttam Nagar West", "Blue", 11),
            Station("Uttam Nagar East", "Blue", 12),
            Station("Janakpuri West", "Blue", 13),
            Station("Janakpuri East", "Blue", 14),
            Station("Tilak Nagar", "Blue", 15),
            Station("Subhash Nagar", "Blue", 16),
            Station("Tagore Garden", "Blue", 17),
            Station("Rajouri Garden", "Blue", 18),
            Station("Ramesh Nagar", "Blue", 19),
            Station("Moti Nagar", "Blue", 20),
            Station("Kirti Nagar", "Blue", 21),
            Station("Shadipur", "Blue", 22),
            Station("Patel Nagar", "Blue", 23),
            Station("Rajendra Place", "Blue", 24),
            Station("Karol Bagh", "Blue", 25),
            Station("Jhandewalan", "Blue", 26),
            Station("Ramakrishna Ashram Marg", "Blue", 27),
            Station("Rajiv Chowk", "Blue", 28),
            Station("Barakhamba Road", "Blue", 29),
            Station("Mandi House", "Blue", 30),
            Station("Supreme Court", "Blue", 31),
            Station("Indraprastha", "Blue", 32),
            Station("Yamuna Bank", "Blue", 33),
            Station("Akshardham", "Blue", 34),
            Station("Mayur Vihar Phase 1", "Blue", 35),
            Station("Mayur Vihar Extension", "Blue", 36),
            Station("New Ashok Nagar", "Blue", 37),
            Station("Noida Sector 15", "Blue", 38),
            Station("Noida Sector 16", "Blue", 39),
            Station("Noida Sector 18", "Blue", 40),
            Station("Botanical Garden", "Blue", 41),
            Station("Golf Course", "Blue", 42),
            Station("Noida City Centre", "Blue", 43),
            Station("Noida Sector 34", "Blue", 44),
            Station("Noida Sector 52", "Blue", 45),
            Station("Noida Sector 61", "Blue", 46),
            Station("Noida Sector 59", "Blue", 47),
            Station("Noida Sector 62", "Blue", 48),
            Station("Noida Electronic City", "Blue", 49)
        )
        
        // Add all stations to the main list
        stations.addAll(yellowStations)
        stations.addAll(blueStations)
        
        // Organize stations by line
        lines["Yellow"] = yellowStations.toMutableList()
        lines["Blue"] = blueStations.toMutableList()
        
        // Create a map for quick lookup
        stations.forEach { station ->
            stationMap[station.name.lowercase()] = station
        }
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
    }

    private fun addEdge(station1: Station, station2: Station) {
        graph.getOrPut(station1) { mutableSetOf() }.add(station2)
        graph.getOrPut(station2) { mutableSetOf() }.add(station1)
    }

    fun getAllStations(): List<Station> = stations

    fun getStation(stationName: String): Station? {
        val normalizedName = stationName.lowercase().trim()
        return stationMap[normalizedName] ?: stations.find { 
            it.name.lowercase().trim() == normalizedName
        }
    }

    fun getAllLines(): List<String> = lines.keys.toList()

    fun getStationsByLine(lineName: String): List<Station> {
        val normalizedName = lineName.lowercase().trim()
        return lines.entries.find { it.key.lowercase().contains(normalizedName) }?.value ?: emptyList()
    }

    fun getRoute(sourceStation: String, destinationStation: String): Route {
        val source = getStation(sourceStation)
            ?: throw IllegalArgumentException("Source station not found: $sourceStation")
            
        val destination = getStation(destinationStation)
            ?: throw IllegalArgumentException("Destination station not found: $destinationStation")

        val path = findShortestPath(source, destination)
        
        return Route(
            source = source,
            destination = destination,
            path = path,
            totalStations = path.size,
            interchangeCount = calculateInterchangeCount(path)
        )
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
    
    fun searchStation(query: String): List<Station> {
        val normalizedQuery = query.lowercase().trim()
        if (normalizedQuery.isEmpty()) {
            return emptyList()
        }
        
        // First try exact matches
        val exactMatches = stations.filter { 
            it.name.lowercase() == normalizedQuery
        }
        
        if (exactMatches.isNotEmpty()) {
            return exactMatches
        }
        
        // Then try contains matches
        val containsMatches = stations.filter { 
            it.name.lowercase().contains(normalizedQuery)
        }
        
        // If no direct contains matches, try splitting the query into words
        // and search for partial matches
        if (containsMatches.isEmpty() && normalizedQuery.contains(" ")) {
            val queryWords = normalizedQuery.split(" ").filter { it.length > 1 }
            return stations.filter { station ->
                val stationName = station.name.lowercase()
                queryWords.any { word -> stationName.contains(word) }
            }
        }
        
        return containsMatches
    }
} 