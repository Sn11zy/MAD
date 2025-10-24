package com.example.sportsorganizer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportsorganizer.data.local.daos.CompetitionDao
import com.example.sportsorganizer.data.local.entities.Competition
import com.example.sportsorganizer.data.repository.WeatherRepository
import com.example.sportsorganizer.ui.viewmodel.UiState
import com.example.sportsorganizer.ui.viewmodel.WeatherViewModel
import com.example.sportsorganizer.ui.viewmodel.WeatherViewModelFactory

@Composable
fun CompetitionDetailScreen(competitionId: Long, competitionDao: CompetitionDao) {
    val competition = produceState<Competition?>(initialValue = null, producer = {
        value = competitionDao.findById(competitionId)
    })

    val viewModel: WeatherViewModel = viewModel(
        factory = WeatherViewModelFactory(WeatherRepository())
    )

    Scaffold { innerPadding: PaddingValues ->
        competition.value?.let {
            viewModel.fetchWeather(it.latitude, it.longitude, it.eventDate)

            val uiState by viewModel.uiState.collectAsState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = it.competitionName ?: "No name", style = MaterialTheme.typography.headlineMedium)

                when (uiState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is UiState.Success -> {
                        val weather = (uiState as UiState.Success).weather.daily
                        Text("Max Temperature: ${weather.temperatureMax.first()}°C")
                        Text("Precipitation Probability: ${weather.precipitationProbabilityMax.first()}%")
                        Text("Max Wind Speed: ${weather.windSpeedMax.first()} km/h")
                    }
                    is UiState.Error -> {
                        Text(text = (uiState as UiState.Error).message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
