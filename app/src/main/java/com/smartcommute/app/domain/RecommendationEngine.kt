package com.smartcommute.app.domain

import com.smartcommute.app.data.db.VehicleEntity
import com.smartcommute.app.data.model.WeatherCondition
import com.smartcommute.app.util.UnitConverter

data class DriveCostDetails(
    val distanceKm: Double,
    val fuelConsumedUnits: Double,
    val fuelCost: Double,
    val parkingFee: Double,
    val maintenanceCost: Double,
    val totalDriveCost: Double
)

data class ComparisonResult(
    val driveCostDetails: DriveCostDetails,
    val transitFare: Double,
    val pureFinancialSavings: Double, // Cost difference: driveCost - transitFare (positive = transit is cheaper)
    val driveTimeMinutes: Int,
    val transitTimeMinutes: Int,
    val weatherCondition: WeatherCondition,
    val recommendedMode: TransportMode,
    val recommendationReason: String,
    val scoreDrive: Double,
    val scoreTransit: Double
)

enum class TransportMode(val displayName: String, val icon: String) {
    DRIVE("Driving", "🚗"),
    PUBLIC_TRANSPORT("Public Transport", "🚌"),
    EQUAL("Either (Similar Value)", "⚖️")
}

object RecommendationEngine {

    /**
     * Calculates pure financial costs for driving.
     */
    fun calculateDriveCost(
        vehicle: VehicleEntity,
        distanceKm: Double,
        fuelPriceOverride: Double? = null,
        parkingFee: Double = 0.0,
        includeMaintenance: Boolean = true
    ): DriveCostDetails {
        val fuelPrice = fuelPriceOverride ?: vehicle.defaultFuelPrice
        val fuelConsumed = UnitConverter.calculateTotalFuelConsumed(
            distanceKm = distanceKm,
            efficiencyValue = vehicle.efficiencyValue,
            unit = vehicle.efficiencyUnit
        )
        val fuelCost = fuelConsumed * fuelPrice
        val maintenanceCost = if (includeMaintenance) {
            distanceKm * vehicle.getEffectiveMaintenanceCostPerKm()
        } else 0.0

        val totalCost = fuelCost + parkingFee + maintenanceCost

        return DriveCostDetails(
            distanceKm = distanceKm,
            fuelConsumedUnits = fuelConsumed,
            fuelCost = fuelCost,
            parkingFee = parkingFee,
            maintenanceCost = maintenanceCost,
            totalDriveCost = totalCost
        )
    }

    /**
     * Evaluates both options using both Pure Financial Comparison and Smart Multi-Factor Score.
     */
    fun evaluateCommute(
        vehicle: VehicleEntity,
        distanceKm: Double,
        fuelPrice: Double,
        parkingFee: Double,
        includeMaintenance: Boolean,
        driveTimeMinutes: Int,
        transitFare: Double,
        transitTimeMinutes: Int,
        weatherCondition: WeatherCondition
    ): ComparisonResult {
        val driveDetails = calculateDriveCost(
            vehicle = vehicle,
            distanceKm = distanceKm,
            fuelPriceOverride = fuelPrice,
            parkingFee = parkingFee,
            includeMaintenance = includeMaintenance
        )

        val totalDriveCost = driveDetails.totalDriveCost
        val pureFinancialSavings = totalDriveCost - transitFare // Positive means Transit is $ cheaper

        // --- Smart Recommendation Scoring Logic ---
        // Base score initialized to 100
        var driveScore = 100.0
        var transitScore = 100.0

        // 1. Cost Impact: Every $1 difference affects score by 5 points
        if (totalDriveCost < transitFare) {
            driveScore += (transitFare - totalDriveCost) * 5.0
        } else {
            transitScore += (totalDriveCost - transitFare) * 5.0
        }

        // 2. Time Difference Impact: Every 5 minutes difference affects score by 3 points
        val timeDiffMinutes = transitTimeMinutes - driveTimeMinutes
        if (timeDiffMinutes > 0) {
            // Driving is faster
            driveScore += (timeDiffMinutes / 5.0) * 3.0
        } else if (timeDiffMinutes < 0) {
            // Public transport is faster
            transitScore += (-timeDiffMinutes / 5.0) * 3.0
        }

        // 3. Weather Conditions Impact on Public Transport (walking to/waiting at station)
        val weatherPenalty = weatherCondition.transitInconvenienceFactor * 25.0
        transitScore -= weatherPenalty

        // Recommendation determination
        val scoreDiff = driveScore - transitScore
        val recommendedMode = when {
            scoreDiff > 10.0 -> TransportMode.DRIVE
            scoreDiff < -10.0 -> TransportMode.PUBLIC_TRANSPORT
            else -> TransportMode.EQUAL
        }

        // Build human-readable reason
        val reasonBuilder = StringBuilder()

        when (recommendedMode) {
            TransportMode.DRIVE -> {
                if (totalDriveCost < transitFare) {
                    reasonBuilder.append("Driving is cheaper by $${String.format("%.2f", -pureFinancialSavings)}. ")
                } else {
                    reasonBuilder.append("Driving is slightly more expensive, but ")
                }

                if (driveTimeMinutes < transitTimeMinutes) {
                    reasonBuilder.append("saves ${transitTimeMinutes - driveTimeMinutes} minutes of travel time. ")
                }
                if (weatherCondition.transitInconvenienceFactor > 0.3) {
                    reasonBuilder.append("Bad weather (${weatherCondition.label}) makes driving significantly more comfortable.")
                }
            }
            TransportMode.PUBLIC_TRANSPORT -> {
                if (transitFare < totalDriveCost) {
                    reasonBuilder.append("Public transport saves you $${String.format("%.2f", pureFinancialSavings)} per trip. ")
                }
                if (transitTimeMinutes < driveTimeMinutes) {
                    reasonBuilder.append("Transit is faster by ${driveTimeMinutes - transitTimeMinutes} minutes. ")
                }
                if (weatherCondition == WeatherCondition.CLEAR || weatherCondition == WeatherCondition.CLOUDY) {
                    reasonBuilder.append("Weather conditions are good for transit.")
                }
            }
            TransportMode.EQUAL -> {
                reasonBuilder.append("Both options are balanced when considering financial cost, travel duration, and weather.")
            }
        }

        return ComparisonResult(
            driveCostDetails = driveDetails,
            transitFare = transitFare,
            pureFinancialSavings = pureFinancialSavings,
            driveTimeMinutes = driveTimeMinutes,
            transitTimeMinutes = transitTimeMinutes,
            weatherCondition = weatherCondition,
            recommendedMode = recommendedMode,
            recommendationReason = reasonBuilder.toString().trim(),
            scoreDrive = driveScore,
            scoreTransit = transitScore
        )
    }
}
