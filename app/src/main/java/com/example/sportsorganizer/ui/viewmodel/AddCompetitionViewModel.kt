package com.example.sportsorganizer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sportsorganizer.data.local.daos.CompetitionDao
import com.example.sportsorganizer.data.local.entities.Competition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddCompetitionViewModel(
    private val competitionDao: CompetitionDao
) : ViewModel() {

    sealed class CreationResult {
        data object Idle : CreationResult()
        data object Loading : CreationResult()
        data class Success(val competitionId: Long) : CreationResult()
        data class Error(val message: String) : CreationResult()
    }

    private val _creationResult: MutableStateFlow<CreationResult> =
        MutableStateFlow(CreationResult.Idle)
    val creationResult: StateFlow<CreationResult> = _creationResult

    fun createCompetition(competitionName: String?, organizerId: Long) {
        _creationResult.value = CreationResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newCompetitionId: Long = System.currentTimeMillis()
                val competition = Competition(
                    id = newCompetitionId,
                    competitionName = competitionName,
                    organizer = organizerId
                )
                competitionDao.insertAll(competition)
                _creationResult.value = CreationResult.Success(newCompetitionId)
            } catch (e: Exception) {
                _creationResult.value = CreationResult.Error(e.message ?: "Unknown error")
            }
        }
    }
}

class AddCompetitionViewModelFactory(
    private val competitionDao: CompetitionDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddCompetitionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddCompetitionViewModel(competitionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


