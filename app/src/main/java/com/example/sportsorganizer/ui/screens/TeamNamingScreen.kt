package com.example.sportsorganizer.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sportsorganizer.data.local.entities.Competition
import com.example.sportsorganizer.data.local.entities.Team
import com.example.sportsorganizer.data.repository.CompetitionRepository
import com.example.sportsorganizer.utils.MatchGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamNamingScreen(
    competitionId: Long,
    competitionRepository: CompetitionRepository,
    onConfirm: () -> Unit
) {
    val teams = remember { mutableStateListOf<Team>() }
    val isLoading = remember { mutableStateOf(true) }
    val competition = remember { mutableStateOf<Competition?>(null) }
    
    // Configuration states
    val configValue = remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(competitionId) {
        try {
            val fetchedTeams = competitionRepository.getTeamsForCompetition(competitionId)
            teams.clear()
            teams.addAll(fetchedTeams)
            
            val fetchedCompetition = competitionRepository.getCompetitionById(competitionId)
            competition.value = fetchedCompetition
            
            // Pre-fill if exists
            if (fetchedCompetition != null) {
                if (fetchedCompetition.scoringType == "Points") {
                    configValue.value = fetchedCompetition.winningScore?.toString() ?: ""
                } else if (fetchedCompetition.scoringType == "Time") {
                    configValue.value = fetchedCompetition.gameDuration?.toString() ?: ""
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading.value = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finalize Configuration") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        if (isLoading.value) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // Configuration Section
                competition.value?.let { comp ->
                    Text("Game Rules", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    if (comp.scoringType == "Points") {
                        OutlinedTextField(
                            value = configValue.value,
                            onValueChange = { configValue.value = it },
                            label = { Text("Winning Score (e.g. 21)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        )
                    } else if (comp.scoringType == "Time") {
                        OutlinedTextField(
                            value = configValue.value,
                            onValueChange = { configValue.value = it },
                            label = { Text("Game Duration (minutes)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        )
                    }
                }

                Text("Team Names", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(teams, key = { _, team -> team.id }) { index, team ->
                        OutlinedTextField(
                            value = team.teamName,
                            onValueChange = { newName ->
                                teams[index] = team.copy(teamName = newName)
                            },
                            label = { Text("Team ${index + 1}") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Button(
                    onClick = {
                        scope.launch {
                            isLoading.value = true
                            try {
                                // 1. Update teams (Assign groups if needed)
                                val comp = competition.value
                                if (comp != null) {
                                    // Assign groups if needed
                                    val numberOfGroups = comp.numberOfGroups ?: 1
                                    val assignedTeams = MatchGenerator.assignGroups(teams, numberOfGroups)
                                    competitionRepository.updateTeams(assignedTeams)
                                    
                                    // 2. Update Competition Config
                                    val value = configValue.value.toIntOrNull()
                                    val updatedComp = if (comp.scoringType == "Points") {
                                        comp.copy(winningScore = value)
                                    } else {
                                        comp.copy(gameDuration = value)
                                    }
                                    competitionRepository.updateCompetition(updatedComp)
                                    
                                    // 3. Generate matches
                                    if (comp.tournamentMode == "Knockout") {
                                        MatchGenerator.generateAndSaveKnockoutBracket(
                                            repo = competitionRepository,
                                            competitionId = competitionId,
                                            teams = assignedTeams,
                                            fieldCount = comp.fieldCount ?: 1
                                        )
                                    } else {
                                        val matches = MatchGenerator.generateMatches(
                                            competitionId = competitionId,
                                            teams = assignedTeams,
                                            tournamentMode = comp.tournamentMode ?: "Group Stage",
                                            fieldCount = comp.fieldCount ?: 1,
                                            numberOfGroups = numberOfGroups
                                        )
                                        competitionRepository.createMatches(matches)
                                    }
                                    
                                    Toast.makeText(context, "Teams updated & matches generated!", Toast.LENGTH_SHORT).show()
                                    onConfirm()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error saving: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading.value = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Confirm & Generate Matches")
                }
            }
        }
    }
}
