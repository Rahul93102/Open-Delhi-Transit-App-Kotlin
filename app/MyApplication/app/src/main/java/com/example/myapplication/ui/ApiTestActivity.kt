package com.example.myapplication.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.MetroLine
import com.example.myapplication.data.model.Route
import com.example.myapplication.data.model.Station
import com.example.myapplication.data.repository.MetroRepository
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ApiTestActivity : ComponentActivity() {
    @Inject
    lateinit var repository: MetroRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Debug CSV loading
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val stations = repository.getAllStations()
            Log.d("MetroAPI", "Total stations loaded: ${stations.size}")
            val lines = repository.getAllLines()
            Log.d("MetroAPI", "Total lines loaded: ${lines.size}")
            lines.forEach { line ->
                Log.d("MetroAPI", "Line ${line.name}: ${line.stations.size} stations")
            }
        }
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ApiTestScreen(repository)
                }
            }
        }
    }
}

@Composable
fun ApiTestScreen(repository: MetroRepository) {
    var selectedEndpoint by remember { mutableStateOf("") }
    var testResult by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val endpoints = listOf(
        "Get All Stations",
        "Get Station by Name",
        "Get All Lines",
        "Get Line by Name",
        "Get Stations by Line",
        "Find Route"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Delhi Metro API Test",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Select an endpoint to test:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxWidth()
        ) {
            items(endpoints) { endpoint ->
                Button(
                    onClick = { selectedEndpoint = endpoint },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(endpoint)
                }
            }
        }

        if (selectedEndpoint.isNotEmpty()) {
            var param1 by remember { mutableStateOf("") }
            var param2 by remember { mutableStateOf("") }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Testing: $selectedEndpoint",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Parameters based on selected endpoint
            when (selectedEndpoint) {
                "Get Station by Name" -> {
                    OutlinedTextField(
                        value = param1,
                        onValueChange = { param1 = it },
                        label = { Text("Station Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
                "Get Line by Name" -> {
                    OutlinedTextField(
                        value = param1,
                        onValueChange = { param1 = it },
                        label = { Text("Line Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
                "Get Stations by Line" -> {
                    OutlinedTextField(
                        value = param1,
                        onValueChange = { param1 = it },
                        label = { Text("Line Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
                "Find Route" -> {
                    OutlinedTextField(
                        value = param1,
                        onValueChange = { param1 = it },
                        label = { Text("Source Station") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = param2,
                        onValueChange = { param2 = it },
                        label = { Text("Destination Station") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
            }

            Button(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        testResult = try {
                            when (selectedEndpoint) {
                                "Get All Stations" -> {
                                    val stations = repository.getAllStations()
                                    "Found ${stations.size} stations:\n" +
                                    stations.take(5).joinToString("\n") { it.name }
                                }
                                "Get Station by Name" -> {
                                    val station = repository.getStation(param1)
                                    if (station != null) {
                                        "Station: ${station.name}\nLine: ${station.line}\nIndex: ${station.index}"
                                    } else {
                                        "Station not found"
                                    }
                                }
                                "Get All Lines" -> {
                                    val lines = repository.getAllLines()
                                    "Found ${lines.size} lines:\n" +
                                    lines.joinToString("\n") { it.name }
                                }
                                "Get Line by Name" -> {
                                    val line = repository.getLine(param1)
                                    if (line != null) {
                                        "Line: ${line.name}\nStations: ${line.stations.size}\nFirst few stations: " +
                                        line.stations.take(3).joinToString(", ") { it.name }
                                    } else {
                                        "Line not found"
                                    }
                                }
                                "Get Stations by Line" -> {
                                    val stations = repository.getStationsByLine(param1)
                                    if (stations.isNotEmpty()) {
                                        "Found ${stations.size} stations on $param1:\n" +
                                        stations.take(5).joinToString("\n") { it.name }
                                    } else {
                                        "No stations found on this line"
                                    }
                                }
                                "Find Route" -> {
                                    try {
                                        val route = repository.getRoute(param1, param2)
                                        "Route from ${route.source.name} to ${route.destination.name}:\n" +
                                        "Stations: ${route.totalStations}\n" +
                                        "Interchanges: ${route.interchangeCount}\n" +
                                        "Path: ${route.path.joinToString(" → ") { it.name }}"
                                    } catch (e: Exception) {
                                        "Error finding route: ${e.message}"
                                    }
                                }
                                else -> "Unknown endpoint"
                            }
                        } catch (e: Exception) {
                            "Error: ${e.message}"
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Test Endpoint")
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Box(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Text(
                    text = testResult,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
} 