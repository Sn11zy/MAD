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

sealed class UiState {
    object Loading : UiState()

    data class Success(
        val weather: WeatherResponse,
    ) : UiState()

    data class Error(
        val message: String,
    ) : UiState()
}

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

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

class WeatherViewModelFactory(
    private val weatherRepository: WeatherRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(weatherRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
