package com.smartcommute.app.util

import com.smartcommute.app.data.model.EfficiencyUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {

    @Test
    fun testLitersPer100Km() {
        // 10 L/100km -> 0.1 L/km
        val perKm = UnitConverter.calculateFuelConsumedPerKm(10.0, EfficiencyUnit.L_PER_100KM)
        assertEquals(0.1, perKm, 0.0001)

        val total = UnitConverter.calculateTotalFuelConsumed(50.0, 10.0, EfficiencyUnit.L_PER_100KM)
        assertEquals(5.0, total, 0.0001)
    }

    @Test
    fun testKwhPer100Km() {
        // 15 kWh/100km -> 0.15 kWh/km
        val perKm = UnitConverter.calculateFuelConsumedPerKm(15.0, EfficiencyUnit.KWH_PER_100KM)
        assertEquals(0.15, perKm, 0.0001)

        val total = UnitConverter.calculateTotalFuelConsumed(100.0, 15.0, EfficiencyUnit.KWH_PER_100KM)
        assertEquals(15.0, total, 0.0001)
    }

    @Test
    fun testMpgUs() {
        // 23.5215 MPG (US) is approx 10 L/100km -> 0.1 L/km
        val perKm = UnitConverter.calculateFuelConsumedPerKm(23.5215, EfficiencyUnit.MPG_US)
        assertEquals(0.1, perKm, 0.001)
    }

    @Test
    fun testMpgUk() {
        // 28.2481 MPG (UK) is approx 10 L/100km -> 0.1 L/km
        val perKm = UnitConverter.calculateFuelConsumedPerKm(28.2481, EfficiencyUnit.MPG_UK)
        assertEquals(0.1, perKm, 0.001)
    }
}
