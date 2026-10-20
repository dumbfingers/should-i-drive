package com.smartcommute.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.smartcommute.app.data.db.VehicleEntity
import com.smartcommute.app.data.model.EfficiencyUnit
import com.smartcommute.app.data.model.FuelType

val PRESET_COLOR_ICONS = listOf("preset_blue", "preset_green", "preset_red", "preset_dark", "preset_orange")

fun getPresetBgColor(presetKey: String?): Color {
    return when (presetKey) {
        "preset_blue" -> Color(0xFF1E88E5)
        "preset_green" -> Color(0xFF43A047)
        "preset_red" -> Color(0xFFE53935)
        "preset_dark" -> Color(0xFF37474F)
        "preset_orange" -> Color(0xFFFB8C00)
        else -> Color(0xFF1E88E5)
    }
}

@Composable
fun VehicleSelectorCard(
    vehicles: List<VehicleEntity>,
    selectedVehicle: VehicleEntity?,
    onSelectVehicle: (VehicleEntity) -> Unit,
    onAddVehicleClicked: () -> Unit,
    onEditVehicleClicked: (VehicleEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selected Vehicle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onAddVehicleClicked) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(Modifier.width(4.dp))
                    Text("Add Vehicle")
                }
            }

            if (vehicles.isEmpty()) {
                Text(
                    text = "No vehicles added yet. Please add a vehicle.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items(vehicles) { vehicle ->
                        val isSelected = selectedVehicle?.id == vehicle.id
                        VehicleItemChip(
                            vehicle = vehicle,
                            isSelected = isSelected,
                            onSelect = { onSelectVehicle(vehicle) },
                            onEdit = { onEditVehicleClicked(vehicle) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleItemChip(
    vehicle: VehicleEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderWidth = if (isSelected) 2.dp else 0.dp

    Card(
        modifier = Modifier
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VehicleAvatar(photoUri = vehicle.photoUri)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = vehicle.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${vehicle.fuelType.displayName} • ${vehicle.efficiencyValue} ${vehicle.efficiencyUnit.label}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Build, contentDescription = "Edit", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun VehicleAvatar(photoUri: String?, modifier: Modifier = Modifier.size(40.dp)) {
    if (!photoUri.isNullOrEmpty() && photoUri.startsWith("content://")) {
        AsyncImage(
            model = Uri.parse(photoUri),
            contentDescription = "Vehicle Photo",
            modifier = modifier
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
    } else {
        val bgColor = getPresetBgColor(photoUri)
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditVehicleDialog(
    vehicle: VehicleEntity?,
    onDismiss: () -> Unit,
    onSave: (VehicleEntity) -> Unit
) {
    var name by remember { mutableStateOf(vehicle?.name ?: "My Car") }
    var selectedFuelType by remember { mutableStateOf(vehicle?.fuelType ?: FuelType.PETROL) }
    var efficiencyValueText by remember { mutableStateOf(vehicle?.efficiencyValue?.toString() ?: "7.5") }
    var selectedEfficiencyUnit by remember { mutableStateOf(vehicle?.efficiencyUnit ?: EfficiencyUnit.L_PER_100KM) }
    var defaultFuelPriceText by remember { mutableStateOf(vehicle?.defaultFuelPrice?.toString() ?: selectedFuelType.defaultPricePerUnit.toString()) }
    var photoUri by remember { mutableStateOf(vehicle?.photoUri ?: "preset_blue") }

    // Maintenance costs
    var enableMaintenance by remember { mutableStateOf(vehicle?.enableMaintenance ?: true) }
    var maintenanceCostPerKmText by remember { mutableStateOf(vehicle?.maintenanceCostPerKm?.toString() ?: "0.08") }
    var annualServiceCostText by remember { mutableStateOf(vehicle?.annualServiceCost?.toString() ?: "300") }
    var annualInsuranceCostText by remember { mutableStateOf(vehicle?.annualInsuranceCost?.toString() ?: "800") }
    var annualRoadTaxText by remember { mutableStateOf(vehicle?.annualRoadTax?.toString() ?: "150") }
    var estimatedAnnualKmText by remember { mutableStateOf(vehicle?.estimatedAnnualKm?.toString() ?: "12000") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { photoUri = it.toString() }
    }

    var expandedFuelType by remember { mutableStateOf(false) }
    var expandedUnit by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (vehicle == null) "Add Vehicle" else "Edit Vehicle") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Vehicle Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Photo Selection
                Text("Vehicle Icon or Photo:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    VehicleAvatar(photoUri = photoUri, modifier = Modifier.size(50.dp))
                    OutlinedButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                        Icon(Icons.Default.Photo, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Gallery Photo")
                    }
                }

                // Preset Colors Selection
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PRESET_COLOR_ICONS.forEach { presetKey ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(getPresetBgColor(presetKey))
                                .border(
                                    width = if (photoUri == presetKey) 3.dp else 0.dp,
                                    color = if (photoUri == presetKey) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { photoUri = presetKey }
                        )
                    }
                }

                // Fuel Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedFuelType,
                    onExpandedChange = { expandedFuelType = !expandedFuelType }
                ) {
                    OutlinedTextField(
                        value = selectedFuelType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fuel / Power Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFuelType) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFuelType,
                        onDismissRequest = { expandedFuelType = false }
                    ) {
                        FuelType.values().forEach { fuel ->
                            DropdownMenuItem(
                                text = { Text(fuel.displayName) },
                                onClick = {
                                    selectedFuelType = fuel
                                    defaultFuelPriceText = fuel.defaultPricePerUnit.toString()
                                    expandedFuelType = false
                                }
                            )
                        }
                    }
                }

                // Efficiency Unit & Value
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = efficiencyValueText,
                        onValueChange = { efficiencyValueText = it },
                        label = { Text("Efficiency Value") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedUnit,
                        onExpandedChange = { expandedUnit = !expandedUnit },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedEfficiencyUnit.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedUnit,
                            onDismissRequest = { expandedUnit = false }
                        ) {
                            EfficiencyUnit.values().forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit.label) },
                                    onClick = {
                                        selectedEfficiencyUnit = unit
                                        expandedUnit = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = defaultFuelPriceText,
                    onValueChange = { defaultFuelPriceText = it },
                    label = { Text("Default Fuel Price (${selectedFuelType.unitLabel})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Maintenance Section
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Include Vehicle Maintenance Cost", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Switch(checked = enableMaintenance, onCheckedChange = { enableMaintenance = it })
                }

                if (enableMaintenance) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = maintenanceCostPerKmText,
                                onValueChange = { maintenanceCostPerKmText = it },
                                label = { Text("Direct Wear & Maintenance ($/km)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = annualServiceCostText,
                                onValueChange = { annualServiceCostText = it },
                                label = { Text("Annual Service / Repair ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = annualInsuranceCostText,
                                onValueChange = { annualInsuranceCostText = it },
                                label = { Text("Annual Insurance ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = annualRoadTaxText,
                                onValueChange = { annualRoadTaxText = it },
                                label = { Text("Annual Road Tax ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = estimatedAnnualKmText,
                                onValueChange = { estimatedAnnualKmText = it },
                                label = { Text("Estimated Annual Distance (km)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val effVal = efficiencyValueText.toDoubleOrNull() ?: 7.5
                    val fuelPrice = defaultFuelPriceText.toDoubleOrNull() ?: 1.5
                    val newVehicle = VehicleEntity(
                        id = vehicle?.id ?: 0,
                        name = name.ifBlank { "My Vehicle" },
                        fuelType = selectedFuelType,
                        efficiencyValue = effVal,
                        efficiencyUnit = selectedEfficiencyUnit,
                        photoUri = photoUri,
                        defaultFuelPrice = fuelPrice,
                        enableMaintenance = enableMaintenance,
                        maintenanceCostPerKm = maintenanceCostPerKmText.toDoubleOrNull() ?: 0.08,
                        annualServiceCost = annualServiceCostText.toDoubleOrNull() ?: 300.0,
                        annualInsuranceCost = annualInsuranceCostText.toDoubleOrNull() ?: 800.0,
                        annualRoadTax = annualRoadTaxText.toDoubleOrNull() ?: 150.0,
                        estimatedAnnualKm = estimatedAnnualKmText.toDoubleOrNull() ?: 12000.0
                    )
                    onSave(newVehicle)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
