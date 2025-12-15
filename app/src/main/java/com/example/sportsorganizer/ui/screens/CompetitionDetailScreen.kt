package com.example.sportsorganizer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportsorganizer.R
import com.example.sportsorganizer.data.local.entities.Competition
import com.example.sportsorganizer.data.local.entities.Match
import com.example.sportsorganizer.data.local.entities.Team
import com.example.sportsorganizer.data.repository.CompetitionRepository
import com.example.sportsorganizer.data.repository.WeatherRepository
import com.example.sportsorganizer.ui.viewmodel.UiState
import com.example.sportsorganizer.ui.viewmodel.WeatherViewModel
import com.example.sportsorganizer.ui.viewmodel.WeatherViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun CompetitionDetailScreen(
    onUpPress: () -> Unit,
    competitionId: Long,
    competitionRepository: CompetitionRepository,
    onNavigate: (String) -> Unit
) {
    val competition by produceState<Competition?>(initialValue = null, key1 = competitionId) {
        value = competitionRepository.getCompetitionById(competitionId)
    }

    // State for matches and teams
    val matches = remember { mutableStateListOf<Match>() }
    val teams = remember { mutableStateListOf<Team>() }
    val isLoadingData = remember { mutableStateOf(true) }

    LaunchedEffect(competitionId) {
        try {
            val fetchedMatches = competitionRepository.getMatchesForCompetition(competitionId)
            matches.addAll(fetchedMatches)
            
            val fetchedTeams = competitionRepository.getTeamsForCompetition(competitionId)
            teams.addAll(fetchedTeams)
        } catch (e: Exception) {
            // Handle error silently or log
        } finally {
            isLoadingData.value = false
        }
    }

    val viewModel: WeatherViewModel =
        viewModel(
            factory = WeatherViewModelFactory(WeatherRepository()),
        )

    LaunchedEffect(competition) {
        competition?.let {
            if (it.latitude != null && it.longitude != null && it.eventDate != null) {
                viewModel.fetchWeather(it.latitude, it.longitude, it.eventDate)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.competition_title)) },
                navigationIcon = {
                    IconButton(onClick = onUpPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_back_description),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            )
        },
    ) { innerPadding ->
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
                Text(text = currentCompetition.competitionName, style = MaterialTheme.typography.headlineMedium)
                
                // Weather Section
                if (currentCompetition.latitude != null && currentCompetition.longitude != null && currentCompetition.eventDate != null) {
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
                            Text("Max Temperature: ${weather.temperatureMax.firstOrNull() ?: "-"}°C")
                            Text("Precipitation Probability: ${weather.precipitationProbabilityMax.firstOrNull() ?: "-"}%")
                            Text("Max Wind Speed: ${weather.windSpeedMax.firstOrNull() ?: "-"} km/h")
                        }
                        is UiState.Error -> {
                            Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                } else {
                     Text("Location or Date not set for this competition.")
                }

                Button(
                    onClick = { onNavigate("teamNaming/$competitionId") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Teams & Configuration")
                }

                Text("Matches", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))

                if (isLoadingData.value) {
                    CircularProgressIndicator()
                } else if (matches.isEmpty()) {
                    Text("No matches generated yet.")
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(matches) { match ->
                            val team1 = teams.find { it.id == match.team1Id }?.teamName ?: "Unknown"
                            val team2 = teams.find { it.id == match.team2Id }?.teamName ?: "Unknown"
                            
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Field ${match.fieldNumber}: $team1 vs $team2")
                                    Text("Status: ${match.status}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
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
