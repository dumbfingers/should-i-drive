package com.smartcommute.app.domain

import com.smartcommute.app.data.db.VehicleEntity
import com.smartcommute.app.data.model.EfficiencyUnit
import com.smartcommute.app.data.model.FuelType
import com.smartcommute.app.data.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationEngineTest {

    private val sampleCar = VehicleEntity(
        id = 1,
        name = "Sedan Test",
        fuelType = FuelType.PETROL,
        efficiencyValue = 8.0, // 8.0 L/100km -> 0.08 L/km
        efficiencyUnit = EfficiencyUnit.L_PER_100KM,
        defaultFuelPrice = 1.50, // $1.50 per Liter
        enableMaintenance = true,
        maintenanceCostPerKm = 0.05,
        annualServiceCost = 300.0,
        annualInsuranceCost = 900.0,
        annualRoadTax = 0.0,
        estimatedAnnualKm = 12000.0 // Fixed per km = (300+900)/12000 = 0.10. Total maintenance per km = 0.15
    )

    @Test
    fun testCalculateDriveCost() {
        val distanceKm = 20.0
        // Fuel consumed = 20 * 0.08 = 1.6 Liters
        // Fuel cost = 1.6 * $1.50 = $2.40
        // Maintenance per km = 0.05 + (1200 / 12000 = 0.10) = $0.15/km
        // Maintenance cost for 20km = 20 * 0.15 = $3.00
        // Parking fee = $5.00
        // Total Drive Cost = 2.40 + 3.00 + 5.00 = $10.40

        val result = RecommendationEngine.calculateDriveCost(
            vehicle = sampleCar,
            distanceKm = distanceKm,
            fuelPriceOverride = 1.50,
            parkingFee = 5.00,
            includeMaintenance = true
        )

        assertEquals(1.6, result.fuelConsumedUnits, 0.001)
        assertEquals(2.40, result.fuelCost, 0.01)
        assertEquals(3.00, result.maintenanceCost, 0.01)
        assertEquals(10.40, result.totalDriveCost, 0.01)
    }

    @Test
    fun testCommuteEvaluation_PublicTransportCheaperAndRainy() {
        // Drive cost ~$10.40, Transit fare $3.50.
        // Rain penalizes transit slightly, but price difference ($6.90 savings) strongly favors public transport.
        val result = RecommendationEngine.evaluateCommute(
            vehicle = sampleCar,
            distanceKm = 20.0,
            fuelPrice = 1.50,
            parkingFee = 5.00,
            includeMaintenance = true,
            driveTimeMinutes = 25,
            transitFare = 3.50,
            transitTimeMinutes = 30,
            weatherCondition = WeatherCondition.RAIN
        )

        assertEquals(TransportMode.PUBLIC_TRANSPORT, result.recommendedMode)
        assertTrue(result.pureFinancialSavings > 0)
    }

    @Test
    fun testCommuteEvaluation_DrivingRecommendedInStormWithLongTransit() {
        // Drive cost ~$6.00, Transit fare $5.00.
        // Transit takes 60 mins vs 20 mins drive, plus Stormy weather -> Driving should be recommended!
        val result = RecommendationEngine.evaluateCommute(
            vehicle = sampleCar,
            distanceKm = 10.0,
            fuelPrice = 1.50,
            parkingFee = 2.00,
            includeMaintenance = false,
            driveTimeMinutes = 20,
            transitFare = 5.00,
            transitTimeMinutes = 60,
            weatherCondition = WeatherCondition.STORMY
        )

        assertEquals(TransportMode.DRIVE, result.recommendedMode)
    }
}
