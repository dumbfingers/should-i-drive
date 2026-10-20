package com.smartcommute.app.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.smartcommute.app.data.db.VehicleEntity
import com.smartcommute.app.ui.components.AddEditVehicleDialog
import com.smartcommute.app.ui.components.ComparisonResultCard
import com.smartcommute.app.ui.components.TripHistorySection
import com.smartcommute.app.ui.components.VehicleSelectorCard
import com.smartcommute.app.ui.components.WeatherCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var editingVehicle by remember { mutableStateOf<VehicleEntity?>(null) }
    var showVehicleDialog by remember { mutableStateOf(false) }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchDeviceLocation(context, viewModel)
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Smart Commute",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Drive vs Public Transport Decision Assistant",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Weather Card with Location fetch action
            WeatherCard(
                currentCondition = state.weatherCondition,
                temperatureCelsius = state.temperatureCelsius,
                weatherDescription = state.weatherDescription,
                isLoading = state.isWeatherLoading,
                onRefreshWeather = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fetchDeviceLocation(context, viewModel)
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                onConditionSelected = { cond -> viewModel.updateWeatherCondition(cond) }
            )

            // Vehicle Selection Card
            VehicleSelectorCard(
                vehicles = state.vehicles,
                selectedVehicle = state.selectedVehicle,
                onSelectVehicle = { v -> viewModel.selectVehicle(v) },
                onAddVehicleClicked = {
                    editingVehicle = null
                    showVehicleDialog = true
                },
                onEditVehicleClicked = { v ->
                    editingVehicle = v
                    showVehicleDialog = true
                }
            )

            // Destination & Trip Inputs Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Trip & Route Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.startDestination,
                            onValueChange = { viewModel.updateInputs(startDestination = it) },
                            label = { Text("Start Origin") },
                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = state.endDestination,
                            onValueChange = { viewModel.updateInputs(endDestination = it) },
                            label = { Text("End Destination") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = state.distanceKmText,
                        onValueChange = { viewModel.updateInputs(distanceKmText = it) },
                        label = { Text("Distance (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Driving Parameters
                    Text("Driving Factors", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.driveTimeMinutesText,
                            onValueChange = { viewModel.updateInputs(driveTimeMinutesText = it) },
                            label = { Text("Drive Time (min)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = state.fuelPriceText,
                            onValueChange = { viewModel.updateInputs(fuelPriceText = it) },
                            label = { Text("Fuel Price (${state.selectedVehicle?.fuelType?.unitLabel ?: "$/L"})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.parkingFeeText,
                            onValueChange = { viewModel.updateInputs(parkingFeeText = it) },
                            label = { Text("Parking Fee ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Maintenance", fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Switch(
                                checked = state.includeMaintenance,
                                onCheckedChange = { viewModel.updateInputs(includeMaintenance = it) }
                            )
                        }
                    }

                    // Public Transport Parameters
                    Text("Public Transport Factors", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.transitFareText,
                            onValueChange = { viewModel.updateInputs(transitFareText = it) },
                            label = { Text("Transit Fare ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = { Icon(Icons.Default.DirectionsBus, contentDescription = null) },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = state.transitTimeMinutesText,
                            onValueChange = { viewModel.updateInputs(transitTimeMinutesText = it) },
                            label = { Text("Transit Time (min)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Real-time Comparison Result
            state.comparisonResult?.let { result ->
                ComparisonResultCard(
                    result = result,
                    onSaveTrip = { viewModel.saveCurrentTripToHistory() }
                )
            }

            // Trip History Section
            TripHistorySection(
                historyList = state.historyList,
                onLoadComparison = { item -> viewModel.loadTripFromHistory(item) },
                onDeleteComparison = { item -> viewModel.deleteTripFromHistory(item) },
                onClearAllHistory = { viewModel.clearAllHistory() }
            )
        }
    }

    if (showVehicleDialog) {
        AddEditVehicleDialog(
            vehicle = editingVehicle,
            onDismiss = { showVehicleDialog = false },
            onSave = { v ->
                viewModel.saveVehicle(v)
                showVehicleDialog = false
            }
        )
    }
}

@SuppressLint("MissingPermission")
private fun fetchDeviceLocation(context: android.content.Context, viewModel: MainViewModel) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                viewModel.fetchWeatherForLocation(location.latitude, location.longitude)
            } else {
                // Default fallback lat/lon (e.g., San Francisco)
                viewModel.fetchWeatherForLocation(37.7749, -122.4194)
            }
        }.addOnFailureListener {
            viewModel.fetchWeatherForLocation(37.7749, -122.4194)
        }
    } catch (e: Exception) {
        viewModel.fetchWeatherForLocation(37.7749, -122.4194)
    }
}
