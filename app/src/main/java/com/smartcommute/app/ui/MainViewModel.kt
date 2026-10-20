package com.smartcommute.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartcommute.app.data.db.AppDatabase
import com.smartcommute.app.data.db.TripComparisonEntity
import com.smartcommute.app.data.db.VehicleEntity
import com.smartcommute.app.data.model.EfficiencyUnit
import com.smartcommute.app.data.model.FuelType
import com.smartcommute.app.data.model.WeatherCondition
import com.smartcommute.app.data.network.WeatherRepository
import com.smartcommute.app.domain.ComparisonResult
import com.smartcommute.app.domain.RecommendationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val vehicles: List<VehicleEntity> = emptyList(),
    val selectedVehicle: VehicleEntity? = null,
    val historyList: List<TripComparisonEntity> = emptyList(),

    // Trip Inputs
    val startDestination: String = "",
    val endDestination: String = "",
    val distanceKmText: String = "15.0",
    val driveTimeMinutesText: String = "25",
    val fuelPriceText: String = "1.50",
    val parkingFeeText: String = "4.00",
    val includeMaintenance: Boolean = true,

    val transitFareText: String = "3.50",
    val transitTimeMinutesText: String = "35",

    // Weather State
    val weatherCondition: WeatherCondition = WeatherCondition.CLEAR,
    val temperatureCelsius: Double = 22.0,
    val weatherDescription: String = "Fair",
    val isWeatherLoading: Boolean = false,

    // Calculated Result
    val comparisonResult: ComparisonResult? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val vehicleDao = db.vehicleDao()
    private val tripComparisonDao = db.tripComparisonDao()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // Observe Vehicles
        viewModelScope.launch {
            vehicleDao.getAllVehicles().collect { vehicles ->
                if (vehicles.isEmpty()) {
                    // Pre-populate default sample vehicles
                    val sample1 = VehicleEntity(
                        name = "Sedan (Petrol)",
                        fuelType = FuelType.PETROL,
                        efficiencyValue = 7.5,
                        efficiencyUnit = EfficiencyUnit.L_PER_100KM,
                        defaultFuelPrice = 1.50,
                        photoUri = "preset_blue"
                    )
                    val sample2 = VehicleEntity(
                        name = "Tesla Model 3 (EV)",
                        fuelType = FuelType.EV,
                        efficiencyValue = 15.0,
                        efficiencyUnit = EfficiencyUnit.KWH_PER_100KM,
                        defaultFuelPrice = 0.30,
                        photoUri = "preset_green"
                    )
                    vehicleDao.insertVehicle(sample1)
                    vehicleDao.insertVehicle(sample2)
                } else {
                    val currentSelected = _uiState.value.selectedVehicle
                    val selected = vehicles.find { it.id == currentSelected?.id } ?: vehicles.first()
                    _uiState.value = _uiState.value.copy(
                        vehicles = vehicles,
                        selectedVehicle = selected,
                        fuelPriceText = selected.defaultFuelPrice.toString()
                    )
                    calculateCommute()
                }
            }
        }

        // Observe Trip History
        viewModelScope.launch {
            tripComparisonDao.getAllComparisons().collect { history ->
                _uiState.value = _uiState.value.copy(historyList = history)
            }
        }

        // Fetch Initial Weather
        fetchWeatherForLocation(37.7749, -122.4194)
    }

    fun selectVehicle(vehicle: VehicleEntity) {
        _uiState.value = _uiState.value.copy(
            selectedVehicle = vehicle,
            fuelPriceText = vehicle.defaultFuelPrice.toString()
        )
        calculateCommute()
    }

    fun saveVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch {
            if (vehicle.id == 0L) {
                val id = vehicleDao.insertVehicle(vehicle)
                selectVehicle(vehicle.copy(id = id))
            } else {
                vehicleDao.updateVehicle(vehicle)
                selectVehicle(vehicle)
            }
        }
    }

    fun updateInputs(
        startDestination: String? = null,
        endDestination: String? = null,
        distanceKmText: String? = null,
        driveTimeMinutesText: String? = null,
        fuelPriceText: String? = null,
        parkingFeeText: String? = null,
        includeMaintenance: Boolean? = null,
        transitFareText: String? = null,
        transitTimeMinutesText: String? = null
    ) {
        _uiState.value = _uiState.value.copy(
            startDestination = startDestination ?: _uiState.value.startDestination,
            endDestination = endDestination ?: _uiState.value.endDestination,
            distanceKmText = distanceKmText ?: _uiState.value.distanceKmText,
            driveTimeMinutesText = driveTimeMinutesText ?: _uiState.value.driveTimeMinutesText,
            fuelPriceText = fuelPriceText ?: _uiState.value.fuelPriceText,
            parkingFeeText = parkingFeeText ?: _uiState.value.parkingFeeText,
            includeMaintenance = includeMaintenance ?: _uiState.value.includeMaintenance,
            transitFareText = transitFareText ?: _uiState.value.transitFareText,
            transitTimeMinutesText = transitTimeMinutesText ?: _uiState.value.transitTimeMinutesText
        )
        calculateCommute()
    }

    fun updateWeatherCondition(condition: WeatherCondition) {
        _uiState.value = _uiState.value.copy(weatherCondition = condition)
        calculateCommute()
    }

    fun fetchWeatherForLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWeatherLoading = true)
            val result = WeatherRepository.fetchWeather(lat, lon)
            result.onSuccess { info ->
                _uiState.value = _uiState.value.copy(
                    weatherCondition = info.condition,
                    temperatureCelsius = info.temperatureCelsius,
                    weatherDescription = info.description,
                    isWeatherLoading = false
                )
                calculateCommute()
            }.onFailure {
                _uiState.value = _uiState.value.copy(isWeatherLoading = false)
            }
        }
    }

    fun calculateCommute() {
        val state = _uiState.value
        val vehicle = state.selectedVehicle ?: return

        val distance = state.distanceKmText.toDoubleOrNull() ?: 15.0
        val fuelPrice = state.fuelPriceText.toDoubleOrNull() ?: vehicle.defaultFuelPrice
        val parkingFee = state.parkingFeeText.toDoubleOrNull() ?: 0.0
        val driveTime = state.driveTimeMinutesText.toIntOrNull() ?: 25

        val transitFare = state.transitFareText.toDoubleOrNull() ?: 3.50
        val transitTime = state.transitTimeMinutesText.toIntOrNull() ?: 35

        val result = RecommendationEngine.evaluateCommute(
            vehicle = vehicle,
            distanceKm = distance,
            fuelPrice = fuelPrice,
            parkingFee = parkingFee,
            includeMaintenance = state.includeMaintenance,
            driveTimeMinutes = driveTime,
            transitFare = transitFare,
            transitTimeMinutes = transitTime,
            weatherCondition = state.weatherCondition
        )

        _uiState.value = _uiState.value.copy(comparisonResult = result)
    }

    fun saveCurrentTripToHistory() {
        val state = _uiState.value
        val result = state.comparisonResult ?: return
        val vehicle = state.selectedVehicle ?: return

        viewModelScope.launch {
            val entity = TripComparisonEntity(
                startDestination = state.startDestination,
                endDestination = state.endDestination,
                distanceKm = result.driveCostDetails.distanceKm,
                vehicleId = vehicle.id,
                vehicleName = vehicle.name,
                driveTimeMinutes = result.driveTimeMinutes,
                fuelPrice = state.fuelPriceText.toDoubleOrNull() ?: vehicle.defaultFuelPrice,
                parkingFee = result.driveCostDetails.parkingFee,
                driveMaintenanceCost = result.driveCostDetails.maintenanceCost,
                totalDriveCost = result.driveCostDetails.totalDriveCost,
                transitFare = result.transitFare,
                transitTimeMinutes = result.transitTimeMinutes,
                weatherCondition = state.weatherCondition,
                temperatureCelsius = state.temperatureCelsius,
                recommendedOption = result.recommendedMode.name,
                pureFinancialSavings = result.pureFinancialSavings,
                recommendationReason = result.recommendationReason
            )
            tripComparisonDao.insertComparison(entity)
        }
    }

    fun loadTripFromHistory(item: TripComparisonEntity) {
        val vehicle = _uiState.value.vehicles.find { it.id == item.vehicleId } ?: _uiState.value.selectedVehicle
        _uiState.value = _uiState.value.copy(
            startDestination = item.startDestination,
            endDestination = item.endDestination,
            distanceKmText = item.distanceKm.toString(),
            driveTimeMinutesText = item.driveTimeMinutes.toString(),
            fuelPriceText = item.fuelPrice.toString(),
            parkingFeeText = item.parkingFee.toString(),
            transitFareText = item.transitFare.toString(),
            transitTimeMinutesText = item.transitTimeMinutes.toString(),
            weatherCondition = item.weatherCondition,
            temperatureCelsius = item.temperatureCelsius,
            selectedVehicle = vehicle
        )
        calculateCommute()
    }

    fun deleteTripFromHistory(item: TripComparisonEntity) {
        viewModelScope.launch {
            tripComparisonDao.deleteComparison(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            tripComparisonDao.clearAll()
        }
    }
}
