package com.example.sportsorganizer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sportsorganizer.data.local.entities.Competition
import com.example.sportsorganizer.data.local.entities.Match
import com.example.sportsorganizer.data.repository.CompetitionRepository
import com.example.sportsorganizer.utils.StandingsCalculator
import com.example.sportsorganizer.utils.TeamStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CompetitorUiState {
    object Idle : CompetitorUiState()

    object Loading : CompetitorUiState()

    data class Success(
        val competition: Competition,
        val standings: Map<String, List<TeamStats>>, // Changed to Map<GroupName, Stats>
        val matches: List<Match>,
        val teamNames: Map<Long, String>,
    ) : CompetitorUiState()

    data class Error(
        val message: String,
    ) : CompetitorUiState()
}

class CompetitorViewModel(
    private val repository: CompetitionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CompetitorUiState>(CompetitorUiState.Idle)
    val uiState: StateFlow<CompetitorUiState> = _uiState.asStateFlow()

    // To keep track of current ID for refreshing
    private var currentCompetitionId: Long? = null

    fun login(
        competitionIdStr: String,
        password: String,
    ) {
        val id = competitionIdStr.toLongOrNull()
        if (id == null) {
            _uiState.value = CompetitorUiState.Error("Invalid Competition ID")
            return
        }

        viewModelScope.launch {
            _uiState.value = CompetitorUiState.Loading
            try {
                val competition = repository.getCompetitionById(id)
                if (competition != null && competition.competitionPassword == password) {
                    currentCompetitionId = id
                    loadData(competition)
                } else {
                    _uiState.value = CompetitorUiState.Error("Invalid ID or Password")
                }
            } catch (e: Exception) {
                _uiState.value = CompetitorUiState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun refresh() {
        currentCompetitionId?.let { id ->
            viewModelScope.launch {
                try {
                    val competition = repository.getCompetitionById(id)
                    if (competition != null) {
                        loadData(competition)
                    }
                } catch (e: Exception) {
                    // Keep current state but maybe show error?
                    // For now silent fail on refresh or just log
                }
            }
        }
    }

    private suspend fun loadData(competition: Competition) {
        try {
            val teams = repository.getTeamsForCompetition(competition.id)
            val matches = repository.getMatchesForCompetition(competition.id)

            // Map team IDs to names
            val teamNames = teams.associate { it.id to it.teamName }

            // Calculate standings by group
            val groupMatches = matches.filter { it.stage?.startsWith("Group") == true }
            val standingsByGroup = mutableMapOf<String, List<TeamStats>>()

            val teamsByGroup = teams.groupBy { it.groupName ?: "General" }

            teamsByGroup.forEach { (groupName, groupTeams) ->
                val teamIds = groupTeams.map { it.id }
                val groupStandings = StandingsCalculator.calculateStandings(groupMatches, teamIds)
                standingsByGroup[groupName] = groupStandings
            }

            // Sort matches: In Progress, Scheduled, Finished
            // Filter future matches where no teams are assigned yet
            val activeMatches = matches.filter { it.team1Id != null || it.team2Id != null }

            val sortedMatches =
                activeMatches.sortedWith(
                    compareBy<Match> {
                        when (it.status) {
                            "in_progress" -> 0
                            "scheduled" -> 1
                            else -> 2
                        }
                    }.thenBy { it.id },
                )

            _uiState.value =
                CompetitorUiState.Success(
                    competition = competition,
                    standings = standingsByGroup,
                    matches = sortedMatches,
                    teamNames = teamNames,
                )
        } catch (e: Exception) {
            _uiState.value = CompetitorUiState.Error("Failed to load data: ${e.message}")
        }
    }
}

class CompetitorViewModelFactory(
    private val repository: CompetitionRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompetitorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CompetitorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
