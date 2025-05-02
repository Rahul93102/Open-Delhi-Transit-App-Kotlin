package com.example.myapplication.ui

import android.os.Bundle
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
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewmodel.MetroViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MetroApp()
                }
            }
        }
    }
}

@Composable
fun MetroApp(viewModel: MetroViewModel = hiltViewModel()) {
    var sourceStation by remember { mutableStateOf("") }
    var destinationStation by remember { mutableStateOf("") }
    val stations by viewModel.stations.collectAsState()
    val lines by viewModel.lines.collectAsState()
    val route by viewModel.route.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Delhi Metro Route Finder",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = sourceStation,
            onValueChange = { sourceStation = it },
            label = { Text("Source Station") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = destinationStation,
            onValueChange = { destinationStation = it },
            label = { Text("Destination Station") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Button(
            onClick = { viewModel.findRoute(sourceStation, destinationStation) },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Find Route")
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        route?.let { currentRoute ->
            Text(
                text = "Route Found!",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text("Total Stations: ${currentRoute.totalStations}")
            Text("Interchanges: ${currentRoute.interchangeCount}")
            Text("Path:")
            LazyColumn {
                items(currentRoute.path) { station ->
                    Text("${station.name} (${station.line})")
                }
            }
        }
    }
} 