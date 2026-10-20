package com.smartcommute.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smartcommute.app.data.model.WeatherCondition

@Entity(tableName = "trip_comparisons")
data class TripComparisonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val startDestination: String,
    val endDestination: String,
    val distanceKm: Double,

    val vehicleId: Long,
    val vehicleName: String,
    val driveTimeMinutes: Int,
    val fuelPrice: Double,
    val parkingFee: Double,
    val driveMaintenanceCost: Double,
    val totalDriveCost: Double,

    val transitFare: Double,
    val transitTimeMinutes: Int,

    val weatherCondition: WeatherCondition,
    val temperatureCelsius: Double,

    val recommendedOption: String, // "DRIVE", "PUBLIC_TRANSPORT", or "EQUAL"
    val pureFinancialSavings: Double, // Cost difference (positive means transit is cheaper)
    val recommendationReason: String
)
