package com.example.opendelhitransit.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opendelhitransit.data.model.MetroLine
import com.example.opendelhitransit.data.model.Route
import com.example.opendelhitransit.data.model.Station
import com.example.opendelhitransit.data.network.ApiRouteResponse
import com.example.opendelhitransit.data.network.MetroApiService
import com.example.opendelhitransit.data.repository.MetroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MetroViewModel @Inject constructor(
    private val repository: MetroRepository,
    private val apiService: MetroApiService
) : ViewModel() {

    private val TAG = "MetroViewModel"
    
    private val _stations = MutableStateFlow<List<Station>>(emptyList())
    val stations: StateFlow<List<Station>> = _stations.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Station>>(emptyList())
    val searchResults: StateFlow<List<Station>> = _searchResults.asStateFlow()

    private val _lines = MutableStateFlow<List<MetroLine>>(emptyList())
    val lines: StateFlow<List<MetroLine>> = _lines.asStateFlow()

    private val _route = MutableStateFlow<Route?>(null)
    val route: StateFlow<Route?> = _route.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadAllStations()
        loadAllLines()
    }

    private fun loadAllStations() {
        viewModelScope.launch {
            try {
                _stations.value = repository.getAllStations()
            } catch (e: Exception) {
                _error.value = "Failed to load stations: ${e.message}"
            }
        }
    }

    private fun loadAllLines() {
        viewModelScope.launch {
            try {
                _lines.value = repository.getAllLines()
            } catch (e: Exception) {
                _error.value = "Failed to load lines: ${e.message}"
            }
        }
    }

    fun findRoute(sourceStation: String, destinationStation: String) {
        viewModelScope.launch {
            try {
                // Try to get route from remote API first
                try {
                    Log.i(TAG, "Fetching route from remote API: $sourceStation to $destinationStation")
                    val response = apiService.getShortestPath(sourceStation, destinationStation)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val apiRouteResponse = response.body()!!
                        Log.i(TAG, "Remote API successful: ${apiRouteResponse.path.size} stations")
                        
                        // Convert API response to our Route model
                        val pathStations = apiRouteResponse.path.mapIndexed { index, stationName ->
                            // Create Station objects with appropriate line information
                            Station(
                                name = stationName,
                                line = if (index < apiRouteResponse.lines.size) apiRouteResponse.lines[index] else "Unknown",
                                index = index
                            )
                        }
                        
                        if (pathStations.isNotEmpty()) {
                            val routeResult = Route(
                                source = pathStations.first(),
                                destination = pathStations.last(),
                                path = pathStations,
                                totalStations = apiRouteResponse.totalStations,
                                interchangeCount = apiRouteResponse.interchanges
                            )
                            
                            _route.value = routeResult
                            _error.value = null
                            return@launch
                        }
                    } else {
                        Log.w(TAG, "Remote API failed: ${response.code()} - ${response.message()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error calling remote API", e)
                    // If remote API fails, fall back to local calculation
                }
                
                // Fall back to local route calculation
                Log.i(TAG, "Falling back to local route calculation")
                val routeResult = repository.getRoute(sourceStation, destinationStation)
                _route.value = routeResult
                
                // Log route information for debugging
                Log.i(TAG, "Found route from $sourceStation to $destinationStation")
                Log.i(TAG, "Total stations: ${routeResult.totalStations}")
                Log.i(TAG, "Interchanges: ${routeResult.interchangeCount}")
                Log.i(TAG, "Path size: ${routeResult.path.size}")
                
                // Log all stations in the path
                routeResult.path.forEachIndexed { index, station ->
                    Log.i(TAG, "Station $index: ${station.name} (${station.line})")
                }
                
                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error finding route", e)
                _error.value = "Failed to find route: ${e.message}"
                _route.value = null
            }
        }
    }

    fun searchStations(query: String) {
        viewModelScope.launch {
            try {
                _searchResults.value = if (query.isBlank()) {
                    emptyList()
                } else {
                    repository.searchStation(query)
                }
            } catch (e: Exception) {
                _error.value = "Failed to search stations: ${e.message}"
                _searchResults.value = emptyList()
            }
        }
    }

    fun clearResults() {
        _searchResults.value = emptyList()
        _route.value = null
    }

    fun clearError() {
        _error.value = null
    }
} 