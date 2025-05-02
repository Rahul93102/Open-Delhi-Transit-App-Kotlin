package com.example.opendelhitransit.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opendelhitransit.data.model.FuelStation
import com.example.opendelhitransit.data.model.FuelType
import com.example.opendelhitransit.data.repository.GoMapsFuelStationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FuelStationViewModel @Inject constructor(
    private val repository: GoMapsFuelStationRepository
) : ViewModel() {
    
    private val _selectedFuelType = MutableStateFlow(FuelType.ELECTRIC)
    val selectedFuelType: StateFlow<FuelType> = _selectedFuelType.asStateFlow()
    
    private val _stations = MutableStateFlow<List<FuelStation>>(emptyList())
    val stations: StateFlow<List<FuelStation>> = _stations.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()
    
    private val _allFuelTypeStations = MutableStateFlow<Map<FuelType, List<FuelStation>>>(emptyMap())
    val allFuelTypeStations: StateFlow<Map<FuelType, List<FuelStation>>> = _allFuelTypeStations.asStateFlow()
    
    fun setFuelType(fuelType: FuelType) {
        if (_selectedFuelType.value != fuelType) {
            _selectedFuelType.value = fuelType
            refreshStations()
        }
    }
    
    fun setUserLocation(location: Location) {
        _userLocation.value = location
        refreshStations()
    }
    
    fun refreshStations() {
        val location = _userLocation.value ?: return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Use the GoMaps API to get nearby fuel stations
                val goMapsStations = repository.getNearbyFuelStations(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radius = 5000,  // 5 km radius
                    maxResults = 10
                )
                
                // Convert the GoMaps stations to our FuelStation model for UI compatibility
                val convertedStations = goMapsStations.map { 
                    repository.convertToFuelStation(it)
                }
                
                _stations.value = convertedStations
                
                if (convertedStations.isEmpty()) {
                    _error.value = "No fuel stations found nearby"
                }
            } catch (e: Exception) {
                _error.value = "Error loading stations: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
} 