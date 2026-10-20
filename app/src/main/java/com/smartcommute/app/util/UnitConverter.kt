package com.smartcommute.app.util

import com.smartcommute.app.data.model.EfficiencyUnit

object UnitConverter {
    /**
     * Converts any vehicle efficiency format (L/100km, MPG US, MPG UK, kWh/100km)
     * into fuel consumed per kilometer in native units (Liters/km or kWh/km).
     */
    fun calculateFuelConsumedPerKm(value: Double, unit: EfficiencyUnit): Double {
        if (value <= 0.0) return 0.0
        return when (unit) {
            EfficiencyUnit.L_PER_100KM -> value / 100.0
            EfficiencyUnit.KWH_PER_100KM -> value / 100.0
            EfficiencyUnit.MPG_US -> {
                // 1 US Gallon = ~3.78541 Liters, 1 Mile = ~1.60934 km
                // L/100km = 235.215 / MPG_US
                val l100km = 235.215 / value
                l100km / 100.0
            }
            EfficiencyUnit.MPG_UK -> {
                // 1 Imperial Gallon = ~4.54609 Liters, 1 Mile = ~1.60934 km
                // L/100km = 282.481 / MPG_UK
                val l100km = 282.481 / value
                l100km / 100.0
            }
        }
    }

    /**
     * Calculates total fuel required for a given distance in km.
     */
    fun calculateTotalFuelConsumed(distanceKm: Double, efficiencyValue: Double, unit: EfficiencyUnit): Double {
        val perKm = calculateFuelConsumedPerKm(efficiencyValue, unit)
        return distanceKm * perKm
    }
}
