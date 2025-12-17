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

/**
 * ViewModel for creating and editing competitions.
 *
 * Manages competition creation/update workflows including:
 * - City search and selection for location
 * - Competition configuration
 * - Team and match generation
 * - Competition list management
 *
 * @property competitionRepository Repository for competition data operations
 */
class AddCompetitionViewModel(
    private val competitionRepository: CompetitionRepository,
) : ViewModel() {
    /**
     * Sealed class representing the state of competition creation/update.
     */
    sealed class CreationResult {
        /** Initial idle state */
        data object Idle : CreationResult()

        /** Loading state during operation */
        data object Loading : CreationResult()

        /**
         * Success state after operation completes.
         *
         * @property competitionId The ID of the created/updated competition
         */
        data class Success(
            val competitionId: Long,
        ) : CreationResult()

        /**
         * Error state if operation fails.
         *
         * @property message Human-readable error message
         */
        data class Error(
            val message: String,
        ) : CreationResult()
    }

    private val _creationResult: MutableStateFlow<CreationResult> =
        MutableStateFlow(CreationResult.Idle)

    /**
     * Observable state flow of competition creation/update result.
     */
    val creationResult: StateFlow<CreationResult> = _creationResult

    private val _competitions = MutableStateFlow<List<Competition>>(emptyList())

    /**
     * Observable state flow of all competitions.
     */
    val competitions: StateFlow<List<Competition>> = _competitions.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    /**
     * Observable state flow of the current city search query.
     */
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<City>>(emptyList())

    /**
     * Observable state flow of city search results.
     */
    val searchResults: StateFlow<List<City>> = _searchResults

    private val _selectedCity = MutableStateFlow<City?>(null)

    /**
     * Observable state flow of the selected city for the competition location.
     */
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

    /**
     * Fetches all competitions from the repository.
     *
     * Updates the [competitions] state flow with the retrieved data.
     */
    fun fetchCompetitions() {
        viewModelScope.launch {
            try {
                _competitions.value = competitionRepository.getAllCompetitions()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Updates the city search query.
     *
     * Triggers a debounced search after 300ms of inactivity.
     * Clears search results if query is blank.
     *
     * @param query The search query string
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        }
    }

    /**
     * Handles city selection from search results.
     *
     * Updates the selected city, displays it in the search query,
     * and clears the search results.
     *
     * @param city The selected city
     */
    fun onCitySelected(city: City) {
        _selectedCity.value = city
        _searchQuery.value = "${city.name}, ${city.country ?: ""}"
        _searchResults.value = emptyList()
    }

    /**
     * Loads an existing competition for editing.
     *
     * Retrieves the competition and sets its location as the selected city.
     *
     * @param competitionId The ID of the competition to load
     */
    fun loadCompetition(competitionId: Long) {
        viewModelScope.launch {
            val competition = competitionRepository.getCompetitionById(competitionId)
            if (competition != null) {
                // Manually construct City with correct types (String, String?, Double, Double)
                val city =
                    City(
                        name = competition.competitionName,
                        country = "",
                        latitude = competition.latitude ?: 0.0,
                        longitude = competition.longitude ?: 0.0,
                    )
                _selectedCity.value = city
            }
        }
    }

    suspend fun getCompetition(id: Long): Competition? = competitionRepository.getCompetitionById(id)

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

        _creationResult.value = CreationResult.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lat = city?.latitude
                val lon = city?.longitude

                val competition =
                    Competition(
                        id = competitionId,
                        competitionName = competitionName,
                        userId = userId,
                        latitude = lat,
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

                // REGENERATION LOGIC START

                val existingMatches = competitionRepository.getMatchesForCompetition(competitionId)

                competitionRepository.deleteMatchesForCompetition(competitionId)

                val currentTeams = competitionRepository.getTeamsForCompetition(competitionId)
                if (currentTeams.size != numberOfTeams) {
                    competitionRepository.deleteTeamsForCompetition(competitionId)
                    val newTeams = MatchGenerator.generateTeams(competitionId, numberOfTeams)

                    var teamsWithGroups = newTeams
                    if (tournamentMode == "Group Stage" || tournamentMode == "Combined") {
                        teamsWithGroups = MatchGenerator.assignGroups(newTeams, numberOfGroups ?: 1)
                    }
                    competitionRepository.createTeams(teamsWithGroups)
                } else {
                    if (tournamentMode == "Group Stage" || tournamentMode == "Combined") {
                        val updatedTeams = MatchGenerator.assignGroups(currentTeams, numberOfGroups ?: 1)
                        competitionRepository.updateTeams(updatedTeams)
                    }
                }

                val teamsForGen = competitionRepository.getTeamsForCompetition(competitionId)

                if (tournamentMode == "Knockout") {
                    MatchGenerator.generateAndSaveKnockoutBracket(competitionRepository, competitionId, teamsForGen, fieldCount)
                } else {
                    val matches =
                        MatchGenerator.generateMatches(
                            competitionId,
                            teamsForGen,
                            tournamentMode,
                            fieldCount,
                            numberOfGroups ?: 1,
                        )
                    competitionRepository.createMatches(matches)
                }

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
        if (city == null) {
            _creationResult.value = CreationResult.Error("Please select a city")
            return
        }

        _creationResult.value = CreationResult.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val today = LocalDate.now().toString()

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
