package com.example.sportsorganizer.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun CompetitionDetailScreen(
    onUpPress: () -> Unit,
    competitionId: Long,
    competitionRepository: CompetitionRepository,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val competition by produceState<Competition?>(initialValue = null, key1 = competitionId) {
        value = competitionRepository.getCompetitionById(competitionId)
    }

    // State for matches and teams
    val matches = remember { mutableStateListOf<Match>() }
    val teams = remember { mutableStateListOf<Team>() }
    val isLoadingData = remember { mutableStateOf(true) }
    
    // Edit Dialog State
    var editingMatch by remember { mutableStateOf<Match?>(null) }

    LaunchedEffect(competitionId) {
        try {
            val fetchedMatches = competitionRepository.getMatchesForCompetition(competitionId)
            matches.clear()
            matches.addAll(fetchedMatches)
            
            val fetchedTeams = competitionRepository.getTeamsForCompetition(competitionId)
            teams.clear()
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

    if (editingMatch != null) {
        EditMatchDialog(
            match = editingMatch!!,
            onDismiss = { editingMatch = null },
            onConfirm = { updatedMatch ->
                scope.launch {
                    try {
                        competitionRepository.updateMatch(updatedMatch)
                        // Refresh
                        val newMatches = competitionRepository.getMatchesForCompetition(competitionId)
                        matches.clear()
                        matches.addAll(newMatches)
                        Toast.makeText(context, "Match updated", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error updating match: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        editingMatch = null
                    }
                }
            }
        )
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
                
                // Combined Mode Logic: Generate Knockout Button
                if (currentCompetition.tournamentMode == "Combined") {
                    val hasKnockout = matches.any { it.stage?.contains("Round") == true || it.stage?.contains("Semi") == true || it.stage?.contains("Final") == true }
                    if (!hasKnockout) {
                        Button(
                            onClick = {
                                scope.launch {
                                    Toast.makeText(context, "Generating Knockout Stage...", Toast.LENGTH_SHORT).show()
                                    try {
                                        val result = competitionRepository.generateKnockoutStage(competitionId)
                                        // Refresh matches
                                        val newMatches = competitionRepository.getMatchesForCompetition(competitionId)
                                        matches.clear()
                                        matches.addAll(newMatches)
                                        
                                        if (result > 0) {
                                            Toast.makeText(context, "Knockout Stage Generated! ($result matches)", Toast.LENGTH_SHORT).show()
                                        } else if (result == -1) {
                                            Toast.makeText(context, "Knockout Stage already exists (refresh maybe?)", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "No matches generated (0 teams qualified?)", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Generate Knockout Stage")
                        }
                    } else {
                        Text("Knockout Stage Active", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Text("Matches", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))

                val visibleMatches = matches.filter { it.team1Id != null || it.team2Id != null }

                if (isLoadingData.value) {
                    CircularProgressIndicator()
                } else if (visibleMatches.isEmpty()) {
                    Text("No matches generated yet.")
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(visibleMatches) { match ->
                            val team1 = teams.find { it.id == match.team1Id }?.teamName ?: "TBD"
                            val team2 = teams.find { it.id == match.team2Id }?.teamName ?: "TBD"
                            
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Field ${match.fieldNumber ?: "-"}: $team1 vs $team2")
                                        Text("Score: ${match.score1} - ${match.score2}", style = MaterialTheme.typography.bodyLarge)
                                        Text("Status: ${match.status}", style = MaterialTheme.typography.bodySmall)
                                        if (match.stage != null) {
                                            Text(match.stage, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    IconButton(onClick = { editingMatch = match }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Match")
                                    }
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

@Composable
fun EditMatchDialog(
    match: Match,
    onDismiss: () -> Unit,
    onConfirm: (Match) -> Unit
) {
    var score1 by remember { mutableStateOf(match.score1.toString()) }
    var score2 by remember { mutableStateOf(match.score2.toString()) }
    var status by remember { mutableStateOf(match.status) }
    
    val statuses = listOf("scheduled", "in_progress", "finished")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Match") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = score1,
                    onValueChange = { score1 = it },
                    label = { Text("Score 1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = score2,
                    onValueChange = { score2 = it },
                    label = { Text("Score 2") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                Text("Status:", modifier = Modifier.padding(top = 8.dp))
                statuses.forEach { s ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = status == s,
                            onClick = { status = s }
                        )
                        Text(s)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val s1 = score1.toIntOrNull() ?: match.score1
                    val s2 = score2.toIntOrNull() ?: match.score2
                    onConfirm(match.copy(score1 = s1, score2 = s2, status = status))
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
