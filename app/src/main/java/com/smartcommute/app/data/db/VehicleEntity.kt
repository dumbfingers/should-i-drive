package com.smartcommute.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smartcommute.app.data.model.EfficiencyUnit
import com.smartcommute.app.data.model.FuelType

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val fuelType: FuelType,
    val efficiencyValue: Double, // Value in selected efficiencyUnit
    val efficiencyUnit: EfficiencyUnit,
    val photoUri: String? = null, // URI string for gallery photo or preset icon identifier (e.g., "preset_blue_car")
    val defaultFuelPrice: Double, // Price per Liter/kWh

    // Detailed Maintenance parameters
    val enableMaintenance: Boolean = true,
    val maintenanceCostPerKm: Double = 0.08, // Custom direct maintenance cost per km
    val annualServiceCost: Double = 300.0,
    val annualInsuranceCost: Double = 800.0,
    val annualRoadTax: Double = 150.0,
    val estimatedAnnualKm: Double = 12000.0
) {
    /**
     * Calculates total maintenance & ownership cost per kilometer
     */
    fun getEffectiveMaintenanceCostPerKm(): Double {
        if (!enableMaintenance) return 0.0
        val fixedAnnualCosts = annualServiceCost + annualInsuranceCost + annualRoadTax
        val fixedPerKm = if (estimatedAnnualKm > 0) fixedAnnualCosts / estimatedAnnualKm else 0.0
        return maintenanceCostPerKm + fixedPerKm
    }
}
