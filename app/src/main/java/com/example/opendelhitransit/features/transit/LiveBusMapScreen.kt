package com.example.opendelhitransit.features.transit

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.opendelhitransit.data.repository.TransitRepository
import com.example.opendelhitransit.util.BusLocation
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
fun LiveBusMapScreen(
    transitRepository: TransitRepository,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var busLocations by remember { mutableStateOf<List<BusLocation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    // Request permission on first launch
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Fetch bus locations
    fun refreshBusLocations() {
        coroutineScope.launch {
            isLoading = true
            try {
                val vehicles = transitRepository.getAllVehicles(100)
                busLocations = vehicles.map {
                    BusLocation(
                        vehicleId = it.id ?: "",
                        label = "",
                        routeId = it.routeId ?: "",
                        tripId = it.tripId ?: "",
                        latitude = it.latitude?.toDouble() ?: 0.0,
                        longitude = it.longitude?.toDouble() ?: 0.0,
                        speed = it.speed ?: 0f,
                        timestamp = it.timestamp ?: 0L
                    )
                }.filter { it.latitude != 0.0 && it.longitude != 0.0 }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Failed to load bus locations")
            }
            isLoading = false
        }
    }

    // Initial load and periodic refresh
    LaunchedEffect(Unit) {
        refreshBusLocations()
        while (true) {
            kotlinx.coroutines.delay(30000)
            refreshBusLocations()
        }
    }

    val delhiLatLng = LatLng(28.6139, 77.2090)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(delhiLatLng, 12f)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { refreshBusLocations() },
                modifier = Modifier.padding(bottom = 16.dp, end = 16.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    maxZoomPreference = 20f,
                    minZoomPreference = 5f
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    compassEnabled = true,
                    myLocationButtonEnabled = hasLocationPermission
                )
            ) {
                busLocations.forEach { bus ->
                    Marker(
                        state = com.google.maps.android.compose.MarkerState(
                            position = LatLng(bus.latitude, bus.longitude)
                        ),
                        title = "Bus ${bus.vehicleId}",
                        snippet = "Route: ${bus.routeId}"
                    )
                }
            }
            if (isLoading) {
                // Simple loading indicator
                Box(
                    Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                ) {
                    androidx.compose.material3.CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
