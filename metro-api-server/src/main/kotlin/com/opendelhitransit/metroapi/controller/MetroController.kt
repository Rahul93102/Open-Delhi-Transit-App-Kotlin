package com.opendelhitransit.metroapi.controller

import com.opendelhitransit.metroapi.model.ApiRouteResponse
import com.opendelhitransit.metroapi.model.Station
import com.opendelhitransit.metroapi.service.MetroService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = ["*"]) // Enable CORS for all origins
class MetroController(private val metroService: MetroService) {
    
    @GetMapping("/lines")
    fun getLines(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(metroService.getAllLines())
    }
    
    @GetMapping("/stations")
    fun getAllStations(): ResponseEntity<List<String>> {
        val stations = metroService.getAllStations()
        return ResponseEntity.ok(stations.map { it.name })
    }
    
    @GetMapping("/stationsByLine")
    fun getStationsByLine(@RequestParam line: String): ResponseEntity<List<String>> {
        val stations = metroService.getStationsByLine(line)
        return ResponseEntity.ok(stations.map { it.name })
    }
    
    @GetMapping("/shortestPath")
    fun getShortestPath(
        @RequestParam source: String,
        @RequestParam destination: String
    ): ResponseEntity<ApiRouteResponse> {
        return try {
            val route = metroService.getRoute(source, destination)
            
            // Convert to API response format
            val apiRouteResponse = ApiRouteResponse(
                source = route.source.name,
                destination = route.destination.name,
                path = route.path.map { it.name },
                lines = route.path.map { it.line },
                interchanges = route.interchangeCount,
                totalStations = route.totalStations
            )
            
            ResponseEntity.ok(apiRouteResponse)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
    
    @GetMapping("/searchStations")
    fun searchStations(@RequestParam query: String): ResponseEntity<List<Station>> {
        return ResponseEntity.ok(metroService.searchStation(query))
    }
} 