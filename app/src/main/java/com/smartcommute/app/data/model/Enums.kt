package com.smartcommute.app.data.model

enum class FuelType(val displayName: String, val defaultPricePerUnit: Double, val unitLabel: String) {
    PETROL("Petrol", 1.50, "$/L"),
    DIESEL("Diesel", 1.60, "$/L"),
    EV("Electric (EV)", 0.30, "$/kWh"),
    LPG("LPG", 0.90, "$/L"),
    OTHER("Other", 1.00, "$/unit")
}

enum class EfficiencyUnit(val label: String) {
    L_PER_100KM("L/100km"),
    MPG_US("MPG (US)"),
    MPG_UK("MPG (UK)"),
    KWH_PER_100KM("kWh/100km")
}

enum class WeatherCondition(val label: String, val iconRes: String, val transitInconvenienceFactor: Double) {
    CLEAR("Sunny / Clear", "☀️", 0.0),
    CLOUDY("Cloudy", "☁️", 0.0),
    RAIN("Rainy", "🌧️", 0.4),
    SNOW("Snowy", "❄️", 0.7),
    STORMY("Stormy / Severe", "⛈️", 0.9),
    EXTREME_HEAT("Extreme Heat", "🥵", 0.3),
    EXTREME_COLD("Extreme Cold", "🥶", 0.5)
}
