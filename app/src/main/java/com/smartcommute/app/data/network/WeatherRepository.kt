package com.smartcommute.app.data.network

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.smartcommute.app.data.model.WeatherCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class WeatherInfo(
    val condition: WeatherCondition,
    val temperatureCelsius: Double,
    val description: String
)

object WeatherRepository {

    /**
     * Fetches current weather data from Open-Meteo free API for a given lat/lon.
     */
    suspend fun fetchWeather(latitude: Double, longitude: Double): Result<WeatherInfo> = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current_weather=true"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JsonParser.parseString(jsonText).asJsonObject
                val currentWeather = root.getAsJsonObject("current_weather")

                val temp = currentWeather.get("temperature").asDouble
                val weatherCode = currentWeather.get("weathercode").asInt

                val condition = mapWmoCodeToCondition(weatherCode, temp)
                val description = mapWmoCodeToDescription(weatherCode)

                Result.success(WeatherInfo(condition, temp, description))
            } else {
                Result.failure(Exception("HTTP error code: ${connection.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapWmoCodeToCondition(code: Int, temperatureCelsius: Double): WeatherCondition {
        if (temperatureCelsius >= 35.0) return WeatherCondition.EXTREME_HEAT
        if (temperatureCelsius <= -10.0) return WeatherCondition.EXTREME_COLD

        return when (code) {
            0, 1 -> WeatherCondition.CLEAR
            2, 3 -> WeatherCondition.CLOUDY
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> WeatherCondition.RAIN
            71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOW
            95, 96, 99 -> WeatherCondition.STORMY
            else -> WeatherCondition.CLEAR
        }
    }

    private fun mapWmoCodeToDescription(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1, 2, 3 -> "Mainly clear, partly cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain"
            71, 73, 75 -> "Snow fall"
            80, 81, 82 -> "Rain showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Fair"
        }
    }
}
