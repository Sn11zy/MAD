package com.example.sportsorganizer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sportsorganizer.data.local.daos.CompetitionDao
import com.example.sportsorganizer.data.local.entities.Competition
import com.example.sportsorganizer.data.remote.City
import com.example.sportsorganizer.data.repository.GeocodingRepository
import com.example.sportsorganizer.data.repository.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class AddCompetitionViewModel(
    private val competitionDao: CompetitionDao,
) : ViewModel() {
    sealed class CreationResult {
        data object Idle : CreationResult()

        data object Loading : CreationResult()

        data class Success(
            val competitionId: Long,
        ) : CreationResult()

        data class Error(
            val message: String,
        ) : CreationResult()
    }

    private val _creationResult: MutableStateFlow<CreationResult> =
        MutableStateFlow(CreationResult.Idle)
    val creationResult: StateFlow<CreationResult> = _creationResult

    private val _competitions = MutableStateFlow<List<Competition>>(emptyList())
    val competitions: StateFlow<List<Competition>> = _competitions.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<City>>(emptyList())
    val searchResults: StateFlow<List<City>> = _searchResults

    private val _selectedCity = MutableStateFlow<City?>(null)
    val selectedCity: StateFlow<City?> = _selectedCity

    private val geocodingRepository = GeocodingRepository()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            competitionDao.getAll().collect { competitions ->
                _competitions.value = competitions
            }
        }

        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            _searchQuery.debounce(300).collect { query ->
                if (query.isNotBlank()) {
                    when (val result = geocodingRepository.searchCity(query)) {
                        is Result.Success -> _searchResults.value = result.data
                        is Result.Error -> {
                            // Handle error, maybe expose it to the UI
                        }
                    }
                } else {
                    _searchResults.value = emptyList()
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        }
    }

    fun onCitySelected(city: City) {
        _selectedCity.value = city
        _searchQuery.value = "${city.name}, ${city.country}"
        _searchResults.value = emptyList()
    }

    fun createCompetition(
        competitionName: String?,
        organizerId: Long,
        eventDate: String,
    ) {
        val city = _selectedCity.value
        if (city == null) {
            _creationResult.value = CreationResult.Error("Please select a city")
            return
        }

        _creationResult.value = CreationResult.Loading
        viewModelScope.launch {
            try {
                val competition =
                    Competition(
                        competitionName = competitionName,
                        organizer = organizerId,
                        latitude = city.latitude,
                        longitude = city.longitude,
                        eventDate = eventDate,
                    )
                val newId = competitionDao.insertAll(competition)
                _creationResult.value = CreationResult.Success(newId.first())
            } catch (e: Exception) {
                _creationResult.value = CreationResult.Error(e.message ?: "Unknown error")
            }
        }
    }
}

class AddCompetitionViewModelFactory(
    private val competitionDao: CompetitionDao,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddCompetitionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddCompetitionViewModel(competitionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
