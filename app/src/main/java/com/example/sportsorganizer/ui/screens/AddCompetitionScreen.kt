package com.example.sportsorganizer.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportsorganizer.R
import com.example.sportsorganizer.data.local.session.SessionManager
import com.example.sportsorganizer.data.repository.CompetitionRepository
import com.example.sportsorganizer.ui.viewmodel.AddCompetitionViewModel
import com.example.sportsorganizer.ui.viewmodel.AddCompetitionViewModelFactory

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizeScreen(
    onUpPress: () -> Unit,
    competitionRepository: CompetitionRepository,
    onNavigate: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.add_competition_title)) },
                navigationIcon = {
                    IconButton(onClick = onUpPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_back_description),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            )
        },
    ) { innerPadding ->
        val viewModel: AddCompetitionViewModel =
            viewModel(
                factory = AddCompetitionViewModelFactory(competitionRepository),
            )

        var name by remember { mutableStateOf("") }
        var eventDate by remember { mutableStateOf("") }
        var refereePassword by remember { mutableStateOf("") }
        var competitionPassword by remember { mutableStateOf("") }
        var startDate by remember { mutableStateOf("") }
        var endDate by remember { mutableStateOf("") }
        var sport by remember { mutableStateOf("") }
        var fieldCount by remember { mutableStateOf("") }
        var numberOfTeams by remember { mutableStateOf("") }
        
        // Dropdown states
        var scoringType by remember { mutableStateOf("Points") }
        var isScoringExpanded by remember { mutableStateOf(false) }
        val scoringOptions = listOf("Points", "Time")

        var tournamentMode by remember { mutableStateOf("Knockout") }
        var isModeExpanded by remember { mutableStateOf(false) }
        val modeOptions = listOf("Knockout", "Group Stage", "Combined")

        val contextForSession = LocalContext.current
        val sessionManager = remember { SessionManager(contextForSession) }
        val loggedInUserId = sessionManager.getLoggedInUserId()
        val searchQuery by viewModel.searchQuery.collectAsState()
        val searchResults by viewModel.searchResults.collectAsState()
        var expanded by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val result by viewModel.creationResult.collectAsState()
        val competitions by viewModel.competitions.collectAsState()
        val visibleCompetitions =
            remember(competitions, loggedInUserId) {
                if (loggedInUserId == null) emptyList() else competitions.filter { it.userId == loggedInUserId }
            }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (loggedInUserId != null) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                
                // City Search
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            label = { Text("City") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            searchResults.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text("${city.name}${city.country?.let { ", $it" } ?: ""}") },
                                    onClick = {
                                        viewModel.onCitySelected(city)
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }
                
                OutlinedTextField(value = eventDate, onValueChange = { eventDate = it }, label = { Text("Creation Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = refereePassword, onValueChange = { refereePassword = it }, label = { Text("Referee Password") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = competitionPassword, onValueChange = { competitionPassword = it }, label = { Text("Competition Password") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("End Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sport, onValueChange = { sport = it }, label = { Text("Sport") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = fieldCount, onValueChange = { fieldCount = it }, label = { Text("Field Count (Number)") }, modifier = Modifier.fillMaxWidth())
                
                // Scoring Type Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = isScoringExpanded,
                        onExpandedChange = { isScoringExpanded = !isScoringExpanded },
                    ) {
                        OutlinedTextField(
                            value = scoringType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Scoring Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isScoringExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                        )
                        ExposedDropdownMenu(
                            expanded = isScoringExpanded,
                            onDismissRequest = { isScoringExpanded = false },
                        ) {
                            scoringOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        scoringType = option
                                        isScoringExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(value = numberOfTeams, onValueChange = { numberOfTeams = it }, label = { Text("Number of Teams") }, modifier = Modifier.fillMaxWidth())
                
                // Tournament Mode Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = isModeExpanded,
                        onExpandedChange = { isModeExpanded = !isModeExpanded },
                    ) {
                        OutlinedTextField(
                            value = tournamentMode,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tournament Mode") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isModeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                        )
                        ExposedDropdownMenu(
                            expanded = isModeExpanded,
                            onDismissRequest = { isModeExpanded = false },
                        ) {
                            modeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        tournamentMode = option
                                        isModeExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                Button(onClick = {
                    val organizer = loggedInUserId
                    val fields = fieldCount.toIntOrNull() ?: 1
                    val teams = numberOfTeams.toIntOrNull() ?: 2
                    
                    viewModel.createCompetition(
                        competitionName = name,
                        userId = organizer,
                        eventDate = eventDate,
                        refereePassword = refereePassword,
                        competitionPassword = competitionPassword,
                        startDate = startDate,
                        endDate = endDate,
                        sport = sport,
                        fieldCount = fields,
                        scoringType = scoringType,
                        numberOfTeams = teams,
                        tournamentMode = tournamentMode
                    )
                }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Create") }
            }
            Text(
                text = "Your competitions",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp),
            )
            if (loggedInUserId == null) {
                Text(
                    text = "Log in to see your competitions",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(8.dp),
                )
            } else if (visibleCompetitions.isEmpty()) {
                Text(
                    text = "No competitions found",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(8.dp),
                )
            } else {
                 Column(modifier = Modifier.padding(top = 16.dp)) {
                    visibleCompetitions.forEach { competition ->
                        Card(
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                                    .clickable {
                                        onNavigate("competitionDetail/${competition.id}")
                                    },
                        ) {
                            Text(
                                text = competition.competitionName,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                }
            }
        }

        LaunchedEffect(result) {
            when (result) {
                is AddCompetitionViewModel.CreationResult.Success -> {
                    Toast.makeText(context, "Competition created", Toast.LENGTH_SHORT).show()
                    name = ""
                    // Clear fields logic could be improved here by resetting all state vars
                }
                is AddCompetitionViewModel.CreationResult.Error -> {
                    val msg = (result as AddCompetitionViewModel.CreationResult.Error).message
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }
}
