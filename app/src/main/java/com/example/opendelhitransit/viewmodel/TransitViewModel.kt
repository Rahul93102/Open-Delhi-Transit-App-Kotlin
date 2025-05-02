package com.example.opendelhitransit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opendelhitransit.data.model.VehicleData
import com.example.opendelhitransit.data.repository.TransitRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransitViewModel @Inject constructor(
    private val repository: TransitRepository
) : ViewModel() {
    
    // Auto-refresh interval (in milliseconds)
    private val REFRESH_INTERVAL = 30000L
    
    private val _vehicles = MutableStateFlow<List<VehicleData>>(emptyList())
    val vehicles: StateFlow<List<VehicleData>> = _vehicles.asStateFlow()
    
    private val _nearbyVehicles = MutableStateFlow<List<VehicleData>>(emptyList())
    val nearbyVehicles: StateFlow<List<VehicleData>> = _nearbyVehicles.asStateFlow()
    
    private val _selectedVehicle = MutableStateFlow<VehicleData?>(null)
    val selectedVehicle: StateFlow<VehicleData?> = _selectedVehicle.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()
    
    private val _isAutoRefreshEnabled = MutableStateFlow(false)
    val isAutoRefreshEnabled: StateFlow<Boolean> = _isAutoRefreshEnabled.asStateFlow()
    
    private var autoRefreshJob: Job? = null
    
    fun loadVehicles() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val result = repository.getAllVehicles()
                _vehicles.value = result
                
                // If user location is set, also update nearby vehicles
                _userLocation.value?.let { updateNearbyVehicles(it) }
                
            } catch (e: Exception) {
                _error.value = "Error loading vehicles: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setUserLocation(location: LatLng) {
        _userLocation.value = location
        updateNearbyVehicles(location)
    }
    
    private fun updateNearbyVehicles(location: LatLng) {
        viewModelScope.launch {
            try {
                val nearby = repository.getVehiclesNear(location)
                _nearbyVehicles.value = nearby
            } catch (e: Exception) {
                _error.value = "Error finding nearby vehicles: ${e.message}"
            }
        }
    }
    
    fun selectVehicle(vehicleId: String) {
        _selectedVehicle.value = _vehicles.value.find { it.id == vehicleId }
    }
    
    fun clearSelectedVehicle() {
        _selectedVehicle.value = null
    }
    
    fun toggleAutoRefresh() {
        _isAutoRefreshEnabled.value = !_isAutoRefreshEnabled.value
        
        if (_isAutoRefreshEnabled.value) {
            startAutoRefresh()
        } else {
            stopAutoRefresh()
        }
    }
    
    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                loadVehicles()
                delay(REFRESH_INTERVAL)
            }
        }
    }
    
    private fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }
    
    fun clearError() {
        _error.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }
} 