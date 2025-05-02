package com.example.opendelhitransit.features.transitapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.opendelhitransit.viewmodel.MetroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransitScreen(
    navController: NavHostController,
    viewModel: MetroViewModel = hiltViewModel()
) {
    var source by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var showRouteOptions by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delhi Transit Planner") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Search Box
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
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = source,
                        onValueChange = { source = it },
                        label = { Text("Starting Point") },
                        placeholder = { Text("Enter your starting location") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, "Starting Point") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = { Text("Destination") },
                        placeholder = { Text("Enter your destination") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, "Destination") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showRouteOptions = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = source.isNotEmpty() && destination.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Find Routes")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Show route options if search has been performed
            if (showRouteOptions) {
                RouteOptionsSection(onViewDetails = { navController.navigate("metro") })
            }
        }
    }
}

@Composable
fun RouteOptionsSection(onViewDetails: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Available Routes",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Recommended Route
        RouteOptionCard(
            routeType = "Recommended",
            duration = "45",
            startTime = "Now",
            arrivalTime = "10:45 AM",
            fare = "₹30",
            primaryColor = MaterialTheme.colorScheme.primary,
            isRecommended = true,
            onViewDetails = onViewDetails
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Cheapest Route
        RouteOptionCard(
            routeType = "Cheapest",
            duration = "55",
            startTime = "Now",
            arrivalTime = "10:55 AM",
            fare = "₹20",
            primaryColor = Color(0xFF00796B),
            onViewDetails = onViewDetails
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Fastest Route
        RouteOptionCard(
            routeType = "Fastest",
            duration = "40",
            startTime = "Now",
            arrivalTime = "10:40 AM",
            fare = "₹45",
            primaryColor = Color(0xFFF57C00),
            onViewDetails = onViewDetails
        )
    }
}

@Composable
fun RouteOptionCard(
    routeType: String,
    duration: String,
    startTime: String,
    arrivalTime: String,
    fare: String,
    primaryColor: Color,
    isRecommended: Boolean = false,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (isRecommended) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    color = primaryColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "RECOMMENDED",
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                        fontSize = 12.sp
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = routeType,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                
                Text(
                    text = "$duration min",
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Times
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = startTime,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Duration",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = arrivalTime,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Journey details
            TransitLeg(
                icon = Icons.Default.LocationOn,
                description = "Walk to metro station",
                duration = "5 min",
                iconTint = Color(0xFF2196F3)
            )
            
            TransitConnector()
            
            TransitLeg(
                icon = Icons.Default.Place,
                description = "Yellow Line to Rajiv Chowk",
                duration = "15 min",
                iconTint = Color(0xFFFFD700)
            )
            
            TransitConnector()
            
            TransitLeg(
                icon = Icons.Default.Place,
                description = "Blue Line to Dwarka",
                duration = "20 min",
                iconTint = Color(0xFF0000FF)
            )
            
            TransitConnector()
            
            TransitLeg(
                icon = Icons.Default.LocationOn,
                description = "Walk to destination",
                duration = "5 min",
                iconTint = Color(0xFF2196F3)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Fare information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Fare",
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = fare,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onViewDetails() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Details")
            }
        }
    }
}

@Composable
fun TransitLeg(
    icon: ImageVector,
    description: String,
    duration: String,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.size(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = description,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = duration,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TransitConnector() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .padding(start = 11.dp) // Center the line with the icons
        ) {
            Divider(
                modifier = Modifier
                    .height(16.dp)
                    .width(2.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                thickness = 2.dp
            )
        }
        
        Spacer(modifier = Modifier.size(16.dp))
    }
} 