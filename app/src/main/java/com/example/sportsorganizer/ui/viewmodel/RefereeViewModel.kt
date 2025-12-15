package com.example.sportsorganizer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sportsorganizer.data.local.entities.Competition
import com.example.sportsorganizer.data.local.entities.Match
import com.example.sportsorganizer.data.repository.CompetitionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RefereeLoginState {
    object Idle : RefereeLoginState()
    object Loading : RefereeLoginState()
    data class Success(val competition: Competition) : RefereeLoginState()
    data class Error(val message: String) : RefereeLoginState()
}

class RefereeViewModel(private val repository: CompetitionRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<RefereeLoginState>(RefereeLoginState.Idle)
    val loginState: StateFlow<RefereeLoginState> = _loginState.asStateFlow()

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    private val _teamNames = MutableStateFlow<Map<Long, String>>(emptyMap())
    val teamNames: StateFlow<Map<Long, String>> = _teamNames.asStateFlow()

    private val _selectedField = MutableStateFlow<Int?>(null)
    val selectedField: StateFlow<Int?> = _selectedField.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    fun login(competitionIdStr: String, password: String) {
        val competitionId = competitionIdStr.toLongOrNull()
        if (competitionId == null) {
            _loginState.value = RefereeLoginState.Error("Invalid Competition ID")
            return
        }

        viewModelScope.launch {
            _loginState.value = RefereeLoginState.Loading
            try {
                val competition = repository.getCompetitionById(competitionId)
                if (competition != null && competition.refereePassword == password) {
                    _loginState.value = RefereeLoginState.Success(competition)
                    fetchTeams(competitionId)
                } else {
                    _loginState.value = RefereeLoginState.Error("Invalid ID or Password")
                }
            } catch (e: Exception) {
                _loginState.value = RefereeLoginState.Error(e.message ?: "Login failed")
            }
        }
    }

    private fun fetchTeams(competitionId: Long) {
        viewModelScope.launch {
            try {
                val teams = repository.getTeamsForCompetition(competitionId)
                _teamNames.value = teams.associate { it.id to it.teamName }
            } catch (e: Exception) {
                _error.value = "Failed to fetch teams: ${e.message}"
            }
        }
    }

    fun selectField(fieldNumber: Int) {
        _selectedField.value = fieldNumber
        val state = _loginState.value
        if (state is RefereeLoginState.Success) {
            fetchMatches(state.competition.id, fieldNumber)
        }
    }

    fun fetchMatches(competitionId: Long, fieldNumber: Int) {
        viewModelScope.launch {
            try {
                val fetchedMatches = repository.getMatchesForField(competitionId, fieldNumber)
                // Sort matches: In Progress (0), Scheduled (1), Finished (2)
                _matches.value = fetchedMatches.sortedWith(
                    compareBy<Match> { 
                            when (it.status) {
                                "in_progress" -> 0
                                "scheduled" -> 1
                                else -> 2
                            }
                        }
                        .thenBy { it.id }
                )
            } catch (e: Exception) {
                 _error.value = "Failed to fetch matches: ${e.message}"
            }
        }
    }

    fun updateMatchScore(match: Match, newScore1: Int, newScore2: Int) {
        viewModelScope.launch {
            try {
                // Update local state optimistically
                val updatedMatch = match.copy(score1 = newScore1, score2 = newScore2)
                _matches.value = _matches.value.map { if (it.id == match.id) updatedMatch else it }
                
                // Update DB
                 repository.updateMatch(updatedMatch)
            } catch (e: Exception) {
                _error.value = "Failed to update score: ${e.message}"
                // Revert optimistic update? For now just show error.
            }
        }
    }

    fun updateMatchStatus(match: Match, newStatus: String) {
        viewModelScope.launch {
            try {
                val updatedMatch = match.copy(status = newStatus)
                _matches.value = _matches.value.map { if (it.id == match.id) updatedMatch else it }
                repository.updateMatch(updatedMatch)
                
                // If finished, refresh list order
                val state = _loginState.value
                val field = _selectedField.value
                if (state is RefereeLoginState.Success && field != null) {
                    fetchMatches(state.competition.id, field)
                }
            } catch (e: Exception) {
                _error.value = "Failed to update status: ${e.message}"
            }
        }
    }
}

class RefereeViewModelFactory(private val repository: CompetitionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RefereeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RefereeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
