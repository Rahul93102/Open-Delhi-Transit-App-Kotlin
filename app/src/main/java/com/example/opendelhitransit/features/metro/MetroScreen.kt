package com.example.opendelhitransit.features.metro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.opendelhitransit.R
import com.example.opendelhitransit.data.model.Route
import com.example.opendelhitransit.data.model.Station
import com.example.opendelhitransit.viewmodel.MetroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetroScreen(viewModel: MetroViewModel = hiltViewModel()) {
    val searchResults by viewModel.searchResults.collectAsState()
    val route by viewModel.route.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    
    var searchQuery by remember { mutableStateOf("") }
    var sourceStation by remember { mutableStateOf("") }
    var destinationStation by remember { mutableStateOf("") }
    var selectionMode by remember { mutableStateOf<String?>(null) } // "source" or "destination"
    
    // Show error in snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Delhi Metro") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Selection mode indicator
            if (selectionMode != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Select ${if (selectionMode == "source") "source" else "destination"} station",
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { selectionMode = null }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Cancel selection"
                            )
                        }
                    }
                }
            }
            
            // Search Station
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it 
                    viewModel.searchStations(it)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (selectionMode != null) "Search station" else "Search for any station") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            searchQuery = ""
                            viewModel.searchStations("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                    }
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Route Planner (only show if not in selection mode)
            if (selectionMode == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Plan Your Journey",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Source Station
                        OutlinedTextField(
                            value = sourceStation,
                            onValueChange = { /* Only updated via selection */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectionMode = "source"
                                    searchQuery = ""
                                    viewModel.clearResults()
                                },
                            label = { Text("From") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Place, contentDescription = "From", tint = MaterialTheme.colorScheme.primary) },
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = false
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Destination Station
                        OutlinedTextField(
                            value = destinationStation,
                            onValueChange = { /* Only updated via selection */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectionMode = "destination"
                                    searchQuery = ""
                                    viewModel.clearResults()
                                },
                            label = { Text("To") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Place, contentDescription = "To", tint = MaterialTheme.colorScheme.primary) },
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = false
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Find Route Button
                        Button(
                            onClick = {
                                if (sourceStation.isNotEmpty() && destinationStation.isNotEmpty()) {
                                    viewModel.findRoute(sourceStation, destinationStation)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = sourceStation.isNotEmpty() && destinationStation.isNotEmpty()
                        ) {
                            Text("Find Route")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Display search results or the route
            if (searchQuery.isNotEmpty() && searchResults.isNotEmpty()) {
                // Show search results
                Text(
                    text = "Search Results",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn {
                    items(searchResults) { station ->
                        StationItem(
                            station = station,
                            onClick = {
                                when (selectionMode) {
                                    "source" -> {
                                        sourceStation = station.name
                                        selectionMode = null
                                    }
                                    "destination" -> {
                                        destinationStation = station.name
                                        selectionMode = null
                                    }
                                    else -> {
                                        // Just view station info
                                    }
                                }
                                searchQuery = ""
                                viewModel.clearResults()
                                focusManager.clearFocus()
                            }
                        )
                    }
                }
            } else if (route != null) {
                // Show route details in a scrollable column
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        RouteDetails(route = route!!)
                    }
                }
            }
        }
    }
}

@Composable
fun StationItem(
    station: Station,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Line indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(getLineColor(station.line), CircleShape)
                    .border(1.dp, Color.Black, CircleShape)
            )
            
            Spacer(modifier = Modifier.padding(8.dp))
            
            Column {
                Text(
                    text = station.name,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Line: ${getLineName(station.line)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RouteDetails(route: Route) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with metro icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_metro),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Journey Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            // Source and destination
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("From:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(route.source.name, style = MaterialTheme.typography.bodyLarge)
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text("To:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(route.destination.name, style = MaterialTheme.typography.bodyLarge)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Route stats
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_station),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${route.totalStations}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text("Stations")
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_interchange),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${route.interchangeCount}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text("Interchanges")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Info text about stations list
                    Text(
                        text = "Scroll down to see the complete list of all ${route.totalStations} stations in this route",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Complete station list - Simplified version
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_metro),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "COMPLETE STATION LIST",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                        )
                    }
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        route.path.forEachIndexed { index, station ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.White, CircleShape)
                                        .border(1.dp, Color.DarkGray, CircleShape)
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column {
                                    Text(
                                        text = station.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = getLineName(station.line),
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            
                            // Add a visual divider between stations
                            if (index < route.path.size - 1) {
                                if (route.path[index].line != route.path[index + 1].line) {
                                    // Interchange indicator
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_interchange),
                                            contentDescription = "Interchange",
                                            tint = Color.Yellow,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Change to ${getLineName(route.path[index + 1].line)}",
                                            color = Color.Yellow,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                } else {
                                    // Just a divider line
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 16.dp)
                                            .height(10.dp)
                                            .width(1.dp)
                                            .background(Color.White.copy(alpha = 0.5f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Original detailed implementation with line colors
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_metro),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Detailed Route Path",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    
                    Text(
                        text = "Complete journey with line information:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        route.path.forEachIndexed { index, station ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Station number
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(getLineColor(station.line), CircleShape)
                                        .border(1.dp, Color.Black, CircleShape)
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column {
                                    Text(
                                        text = station.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = getLineName(station.line),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            // Show interchange indicator
                            if (index < route.path.size - 1 && route.path[index].line != route.path[index + 1].line) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 36.dp, bottom = 8.dp, top = 4.dp, end = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_interchange),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Change from ${getLineName(route.path[index].line)} to ${getLineName(route.path[index + 1].line)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getLineColor(line: String): Color {
    return when (line.lowercase()) {
        "yellow" -> Color(0xFFFFD700)
        "blue" -> Color(0xFF0000FF)
        "red" -> Color(0xFFFF0000)
        "green" -> Color(0xFF008000)
        "violet" -> Color(0xFF8A2BE2)
        "orange" -> Color(0xFFFF8C00)
        "magenta" -> Color(0xFFFF00FF)
        "pink" -> Color(0xFFFFC0CB)
        "aqua" -> Color(0xFF00FFFF)
        "grey" -> Color(0xFF808080)
        "rapid" -> Color(0xFF4682B4)
        else -> Color.Gray
    }
}

fun getLineName(line: String): String {
    return when (line.lowercase()) {
        "yellow" -> "Yellow Line"
        "blue" -> "Blue Line"
        "red" -> "Red Line"
        "green" -> "Green Line"
        "violet" -> "Violet Line"
        "orange" -> "Orange Line"
        "magenta" -> "Magenta Line"
        "pink" -> "Pink Line"
        "aqua" -> "Aqua Line"
        "grey" -> "Grey Line"
        "rapid" -> "Rapid Metro"
        "greenbranch" -> "Green Line Branch"
        "bluebranch" -> "Blue Line Branch"
        "pinkbranch" -> "Pink Line Branch"
        else -> line
    }
} 