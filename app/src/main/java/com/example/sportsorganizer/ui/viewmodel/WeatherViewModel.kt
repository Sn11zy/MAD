package com.example.sportsorganizer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sportsorganizer.data.remote.WeatherResponse
import com.example.sportsorganizer.data.repository.Result
import com.example.sportsorganizer.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Sealed class representing the UI state for weather data.
 */
sealed class UiState {
    /**
     * Indicates that weather data is being loaded.
     */
    object Loading : UiState()

    /**
     * Indicates successful weather data retrieval.
     *
     * @property weather The retrieved weather forecast data
     */
    data class Success(
        val weather: WeatherResponse,
    ) : UiState()

    /**
     * Indicates an error occurred while fetching weather data.
     *
     * @property message Human-readable error message
     */
    data class Error(
        val message: String,
    ) : UiState()
}

/**
 * ViewModel for managing weather forecast data and UI state.
 *
 * Fetches weather information from the repository and exposes it as
 * observable UI state for composables to consume.
 *
 * @property weatherRepository Repository for fetching weather data
 */
class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)

    /**
     * Observable state flow of weather UI state.
     *
     * Emits [UiState.Loading], [UiState.Success], or [UiState.Error]
     * based on the weather fetch operation.
     */
    val uiState: StateFlow<UiState> = _uiState

    /**
     * Fetches weather forecast for the specified location and date.
     *
     * Updates [uiState] with loading, success, or error states as the
     * operation progresses.
     *
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @param date The date for the forecast in "YYYY-MM-DD" format
     */
    fun fetchWeather(
        latitude: Double,
        longitude: Double,
        date: String,
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = weatherRepository.getWeather(latitude, longitude, date)) {
                is Result.Success -> {
                    _uiState.value = UiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
            }
        }
    }
}

/**
 * Factory for creating [WeatherViewModel] instances with dependencies.
 *
 * Required because the ViewModel has constructor parameters that need
 * to be provided at creation time.
 *
 * @property weatherRepository The repository to inject into the ViewModel
 */
class WeatherViewModelFactory(
    private val weatherRepository: WeatherRepository,
) : ViewModelProvider.Factory {
    /**
     * Creates a new instance of the given ViewModel class.
     *
     * @param modelClass The class of the ViewModel to create
     * @return A new ViewModel instance
     * @throws IllegalArgumentException if the ViewModel class is not supported
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(weatherRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
