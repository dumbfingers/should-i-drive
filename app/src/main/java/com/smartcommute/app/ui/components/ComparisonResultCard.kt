package com.smartcommute.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartcommute.app.domain.ComparisonResult
import com.smartcommute.app.domain.TransportMode

@Composable
fun ComparisonResultCard(
    result: ComparisonResult,
    onSaveTrip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Recommendation Verdict Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = when (result.recommendedMode) {
                            TransportMode.DRIVE -> MaterialTheme.colorScheme.primaryContainer
                            TransportMode.PUBLIC_TRANSPORT -> MaterialTheme.colorScheme.secondaryContainer
                            TransportMode.EQUAL -> MaterialTheme.colorScheme.tertiaryContainer
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = result.recommendedMode.icon, fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Recommendation: ${result.recommendedMode.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = result.recommendationReason,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Pure Financial Breakdown
            Text(
                text = "Pure Financial Comparison",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Driving Financial Card
                FinancialOptionBox(
                    title = "Driving",
                    icon = Icons.Default.DirectionsCar,
                    totalCost = result.driveCostDetails.totalDriveCost,
                    isCheaper = result.driveCostDetails.totalDriveCost < result.transitFare,
                    breakdownLines = listOf(
                        "Fuel (${String.format("%.1f", result.driveCostDetails.fuelConsumedUnits)} L/kWh)" to "$${String.format("%.2f", result.driveCostDetails.fuelCost)}",
                        "Parking" to "$${String.format("%.2f", result.driveCostDetails.parkingFee)}",
                        "Maintenance" to "$${String.format("%.2f", result.driveCostDetails.maintenanceCost)}"
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Transit Financial Card
                FinancialOptionBox(
                    title = "Public Transport",
                    icon = Icons.Default.DirectionsBus,
                    totalCost = result.transitFare,
                    isCheaper = result.transitFare < result.driveCostDetails.totalDriveCost,
                    breakdownLines = listOf(
                        "Fare" to "$${String.format("%.2f", result.transitFare)}",
                        "Parking" to "$0.00",
                        "Maintenance" to "$0.00"
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Cost Savings Summary Line
            val diff = Math.abs(result.pureFinancialSavings)
            val cheaperOption = if (result.pureFinancialSavings > 0) "Public Transport" else "Driving"
            Text(
                text = "💰 Financial Verdict: $cheaperOption is cheaper by $${String.format("%.2f", diff)} per trip.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(16.dp))

            // Save Trip Button
            Button(
                onClick = onSaveTrip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save")
                Spacer(Modifier.width(8.dp))
                Text("Save Trip Comparison to History")
            }
        }
    }
}

@Composable
fun FinancialOptionBox(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    totalCost: Double,
    isCheaper: Boolean,
    breakdownLines: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isCheaper) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$${String.format("%.2f", totalCost)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            breakdownLines.forEach { (label, valStr) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(valStr, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
