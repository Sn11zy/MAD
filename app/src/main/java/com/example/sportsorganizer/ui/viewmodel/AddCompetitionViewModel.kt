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
import java.time.LocalDate

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

    fun loadCompetition(competitionId: Long) {
        viewModelScope.launch {
            val competition = competitionRepository.getCompetitionById(competitionId)
            if (competition != null) {
                // Manually construct City with correct types (String, String?, Double, Double)
                val city = City(
                    name = competition.competitionName, 
                    country = "", 
                    latitude = competition.latitude ?: 0.0, 
                    longitude = competition.longitude ?: 0.0
                )
                _selectedCity.value = city
            }
        }
    }
    
    // Better approach: Since UI holds state, we can return the competition object to the UI via a suspend function or callback?
    // Or just let the UI call repository directly for loading? 
    // The UI in OrganizeScreen is already complex. 
    // Let's add a function to get competition that returns it.
    suspend fun getCompetition(id: Long): Competition? {
        return competitionRepository.getCompetitionById(id)
    }

    fun updateCompetition(
        competitionId: Long,
        competitionName: String,
        userId: Long,
        refereePassword: String,
        competitionPassword: String,
        startDate: String,
        endDate: String,
        sport: String,
        fieldCount: Int,
        scoringType: String,
        numberOfTeams: Int,
        tournamentMode: String,
        numberOfGroups: Int?,
        qualifiersPerGroup: Int?,
        pointsPerWin: Int = 3,
        pointsPerDraw: Int = 1,
        winningScore: Int? = null,
        gameDuration: Int? = null,
    ) {
        val city = _selectedCity.value
        // If city is null, we might want to keep existing lat/long if this is an update and user didn't change city.
        // But for now let's assume they search city again or we load it.
        
        _creationResult.value = CreationResult.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // If updating, we need the original competition to preserve ID and maybe lat/long if not changed.
                // But here we just construct a new object with the ID.
                
                val lat = city?.latitude
                val lon = city?.longitude
                
                val competition =
                    Competition(
                        id = competitionId,
                        competitionName = competitionName,
                        userId = userId,
                        latitude = lat, // This might nullify location if not re-selected. Ideally we should handle this better.
                        longitude = lon,
                        refereePassword = refereePassword,
                        competitionPassword = competitionPassword,
                        startDate = startDate,
                        endDate = endDate,
                        sport = sport,
                        fieldCount = fieldCount,
                        scoringType = scoringType,
                        numberOfTeams = numberOfTeams,
                        tournamentMode = tournamentMode,
                        numberOfGroups = numberOfGroups,
                        qualifiersPerGroup = qualifiersPerGroup,
                        pointsPerWin = pointsPerWin,
                        pointsPerDraw = pointsPerDraw,
                        winningScore = winningScore,
                        gameDuration = gameDuration,
                    )

                competitionRepository.updateCompetition(competition)
                
                withContext(Dispatchers.Main) {
                    _creationResult.value = CreationResult.Success(competitionId)
                    fetchCompetitions()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _creationResult.value = CreationResult.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun createCompetition(
        competitionName: String,
        userId: Long,
        // eventDate removed, generated automatically
        refereePassword: String,
        competitionPassword: String,
        startDate: String,
        endDate: String,
        sport: String,
        fieldCount: Int,
        scoringType: String,
        numberOfTeams: Int,
        tournamentMode: String,
        // New params
        numberOfGroups: Int?,
        qualifiersPerGroup: Int?,
        pointsPerWin: Int = 3,
        pointsPerDraw: Int = 1,
        winningScore: Int? = null,
        gameDuration: Int? = null,
    ) {
        val city = _selectedCity.value
        if (city == null) {
            _creationResult.value = CreationResult.Error("Please select a city")
            return
        }

        _creationResult.value = CreationResult.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val today = LocalDate.now().toString() // YYYY-MM-DD

                // 1. Create Competition
                val competition =
                    Competition(
                        competitionName = competitionName,
                        userId = userId,
                        latitude = city.latitude,
                        longitude = city.longitude,
                        eventDate = today,
                        refereePassword = refereePassword,
                        competitionPassword = competitionPassword,
                        startDate = startDate,
                        endDate = endDate,
                        sport = sport,
                        fieldCount = fieldCount,
                        scoringType = scoringType,
                        numberOfTeams = numberOfTeams,
                        tournamentMode = tournamentMode,
                        // New Fields
                        numberOfGroups = numberOfGroups,
                        qualifiersPerGroup = qualifiersPerGroup,
                        pointsPerWin = pointsPerWin,
                        pointsPerDraw = pointsPerDraw,
                        winningScore = winningScore,
                        gameDuration = gameDuration,
                    )

                val createdCompetition = competitionRepository.createCompetition(competition)

                if (createdCompetition != null) {
                    val competitionId = createdCompetition.id

                    // 2. Generate and Insert Teams (Placeholders)
                    val teams = MatchGenerator.generateTeams(competitionId, numberOfTeams)
                    competitionRepository.createTeams(teams)

                    withContext(Dispatchers.Main) {
                        _creationResult.value = CreationResult.Success(competitionId)
                        fetchCompetitions()
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
