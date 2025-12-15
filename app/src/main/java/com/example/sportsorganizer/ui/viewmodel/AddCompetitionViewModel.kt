package com.example.sportsorganizer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sportsorganizer.data.local.entities.Competition
import com.example.sportsorganizer.data.remote.City
import com.example.sportsorganizer.data.repository.CompetitionRepository
import com.example.sportsorganizer.data.repository.GeocodingRepository
import com.example.sportsorganizer.data.repository.Result
import com.example.sportsorganizer.utils.MatchGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddCompetitionViewModel(
    private val competitionRepository: CompetitionRepository,
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
        fetchCompetitions()

        @OptIn(FlowPreview::class)
        viewModelScope.launch(Dispatchers.IO) {
            _searchQuery.debounce(300).collect { query ->
                if (query.isNotBlank()) {
                    try {
                        when (val result = geocodingRepository.searchCity(query)) {
                            is Result.Success -> _searchResults.value = result.data
                            is Result.Error -> {
                                _searchResults.value = emptyList()
                            }
                        }
                    } catch (e: Exception) {
                        _searchResults.value = emptyList()
                    }
                } else {
                    _searchResults.value = emptyList()
                }
            }
        }
    }
    
    fun fetchCompetitions() {
        viewModelScope.launch {
            try {
                _competitions.value = competitionRepository.getAllCompetitions()
            } catch (e: Exception) {
                // Handle error
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
        _searchQuery.value = "${city.name}, ${city.country ?: ""}"
        _searchResults.value = emptyList()
    }

    fun createCompetition(
        competitionName: String,
        userId: Long,
        eventDate: String,
        refereePassword: String,
        competitionPassword: String,
        startDate: String,
        endDate: String,
        sport: String,
        fieldCount: Int,
        scoringType: String,
        numberOfTeams: Int,
        tournamentMode: String
    ) {
        val city = _selectedCity.value
        if (city == null) {
            _creationResult.value = CreationResult.Error("Please select a city")
            return
        }

        _creationResult.value = CreationResult.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Create Competition
                val competition = Competition(
                    competitionName = competitionName,
                    userId = userId,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    date = eventDate, // Using 'date' as creation date or event date
                    refereePassword = refereePassword,
                    competitionPassword = competitionPassword,
                    startDate = startDate,
                    endDate = endDate,
                    sport = sport,
                    fieldCount = fieldCount,
                    scoringType = scoringType,
                    numberOfTeams = numberOfTeams,
                    tournamentMode = tournamentMode
                )
                
                val createdCompetition = competitionRepository.createCompetition(competition)
                
                if (createdCompetition != null) {
                    val competitionId = createdCompetition.id
                    
                    // 2. Generate and Insert Teams
                    val teams = MatchGenerator.generateTeams(competitionId, numberOfTeams)
                    val insertedTeams = competitionRepository.createTeams(teams)
                    
                    // 3. Generate and Insert Matches
                    val matches = MatchGenerator.generateMatches(
                        competitionId = competitionId,
                        teams = insertedTeams,
                        tournamentMode = tournamentMode,
                        fieldCount = fieldCount
                    )
                    competitionRepository.createMatches(matches)

                    withContext(Dispatchers.Main) {
                        _creationResult.value = CreationResult.Success(competitionId)
                        fetchCompetitions() // Refresh list
                    }
                } else {
                     withContext(Dispatchers.Main) {
                        _creationResult.value = CreationResult.Error("Failed to create competition")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _creationResult.value = CreationResult.Error(e.message ?: "Unknown error")
                }
            }
        }
    }
}

class AddCompetitionViewModelFactory(
    private val competitionRepository: CompetitionRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddCompetitionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddCompetitionViewModel(competitionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
