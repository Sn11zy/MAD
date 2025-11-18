package com.example.sportsorganizer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sportsorganizer.data.local.daos.CompetitionConfigDao
import com.example.sportsorganizer.data.local.entities.CompetitionConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CompetitionConfigViewModel(
    private val competitionConfigDao: CompetitionConfigDao,
    private val competitionId: Long
) : ViewModel() {

    private val _config = MutableStateFlow<CompetitionConfig?>(null)
    val config: StateFlow<CompetitionConfig?> = _config.asStateFlow()

    init {
        viewModelScope.launch {
            competitionConfigDao.getByCompetitionIdFlow(competitionId).collect { cfg ->
                _config.value = cfg
            }
        }
    }

    fun upsertConfig(config: CompetitionConfig) {
        viewModelScope.launch {
            competitionConfigDao.upsert(config)
        }
    }
}

class CompetitionConfigViewModelFactory(
    private val competitionConfigDao: CompetitionConfigDao,
    private val competitionId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompetitionConfigViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CompetitionConfigViewModel(competitionConfigDao, competitionId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
