package com.example.sportsorganizer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sportsorganizer.data.local.daos.CompetitionDao
import com.example.sportsorganizer.data.local.entities.Competition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    init {
        viewModelScope.launch(Dispatchers.IO) {
            competitionDao.getAll().collect { competitions ->
                _competitions.value = competitions
            }
        }
    }

    fun createCompetition(
        competitionName: String?,
        organizerId: Long,
        latitude: Double,
        longitude: Double,
        eventDate: String,
    ) {
        _creationResult.value = CreationResult.Loading
        viewModelScope.launch {
            try {
                val competition =
                    Competition(
                        competitionName = competitionName,
                        organizer = organizerId,
                        latitude = latitude,
                        longitude = longitude,
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
