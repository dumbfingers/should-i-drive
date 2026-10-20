package com.smartcommute.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartcommute.app.data.model.WeatherCondition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherCard(
    currentCondition: WeatherCondition,
    temperatureCelsius: Double,
    weatherDescription: String,
    isLoading: Boolean,
    onRefreshWeather: () -> Unit,
    onConditionSelected: (WeatherCondition) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = currentCondition.iconRes, fontSize = 28.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${currentCondition.label} • ${temperatureCelsius.toInt()}°C",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (weatherDescription.isNotBlank()) weatherDescription else "Live Weather",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                IconButton(onClick = onRefreshWeather, enabled = !isLoading) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Weather"
                    )
                }
            }

            // Dropdown override option
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = "Override Weather: ${currentCondition.label}",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    WeatherCondition.values().forEach { cond ->
                        DropdownMenuItem(
                            text = { Text("${cond.iconRes} ${cond.label}") },
                            onClick = {
                                onConditionSelected(cond)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
