package com.example.sportsorganizer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun CompetitionDetailScreen(
    competitionId: Long,
    competitionDao: CompetitionDao,
) {
    val competition by produceState<Competition?>(initialValue = null, key1 = competitionId) {
        value = competitionDao.findById(competitionId)
    }

    val viewModel: WeatherViewModel =
        viewModel(
            factory = WeatherViewModelFactory(WeatherRepository()),
        )

    LaunchedEffect(competition) {
        competition?.let {
            viewModel.fetchWeather(it.latitude, it.longitude, it.eventDate)
        }
    }

    Scaffold { innerPadding: PaddingValues ->
        val currentCompetition = competition
        if (currentCompetition != null) {
            val uiState by viewModel.uiState.collectAsState()

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = currentCompetition.competitionName ?: "No name", style = MaterialTheme.typography.headlineMedium)

                Button(onClick = {
                    viewModel.fetchWeather(currentCompetition.latitude, currentCompetition.longitude, currentCompetition.eventDate)
                }) {
                    Text("Refresh Weather")
                }

                when (val state = uiState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is UiState.Success -> {
                        val weather = state.weather.daily
                        Text("Max Temperature: ${weather.temperatureMax.first()}°C")
                        Text("Precipitation Probability: ${weather.precipitationProbabilityMax.first()}%")
                        Text("Max Wind Speed: ${weather.windSpeedMax.first()} km/h")
                    }
                    is UiState.Error -> {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
