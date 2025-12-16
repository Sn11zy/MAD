package com.example.sportsorganizer.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import java.time.Instant
import java.time.ZoneId

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
        var refereePassword by remember { mutableStateOf("") }
        var competitionPassword by remember { mutableStateOf("") }
        var startDate by remember { mutableStateOf("") }
        var endDate by remember { mutableStateOf("") }
        var sport by remember { mutableStateOf("") }
        var fieldCount by remember { mutableStateOf("") }
        var numberOfTeams by remember { mutableStateOf("") }
        
        // Date Pickers
        var showStartDatePicker by remember { mutableStateOf(false) }
        var showEndDatePicker by remember { mutableStateOf(false) }
        val startDatePickerState = rememberDatePickerState()
        val endDatePickerState = rememberDatePickerState()
        
        // New Inputs
        var numberOfGroups by remember { mutableStateOf("1") }
        var qualifiersPerGroup by remember { mutableStateOf("2") }
        var pointsPerWin by remember { mutableStateOf("3") }
        var pointsPerDraw by remember { mutableStateOf("1") }
        
        // Dropdown states
        var scoringType by remember { mutableStateOf("Points") }
        var isScoringExpanded by remember { mutableStateOf(false) }
        val scoringOptions = listOf("Points", "Time")

        var tournamentMode by remember { mutableStateOf("Knockout") }
        var isModeExpanded by remember { mutableStateOf(false) }
        val modeOptions = listOf("Knockout", "Group Stage", "Combined")

        val contextForSession = LocalContext.current
        val sessionManager = remember { SessionManager(contextForSession) }
        var loggedInUserId by remember { mutableStateOf(sessionManager.getLoggedInUserId()) }
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

        if (showStartDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        startDatePickerState.selectedDateMillis?.let { millis ->
                            startDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().toString()
                        }
                        showStartDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = startDatePickerState)
            }
        }

        if (showEndDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        endDatePickerState.selectedDateMillis?.let { millis ->
                            endDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().toString()
                        }
                        showEndDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = endDatePickerState)
            }
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        sessionManager.clearSession()
                        loggedInUserId = null
                        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                        onUpPress()
                    }) {
                        Text("Logout")
                    }
                }
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
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
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
                
                OutlinedTextField(value = refereePassword, onValueChange = { refereePassword = it }, label = { Text("Referee Password") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = competitionPassword, onValueChange = { competitionPassword = it }, label = { Text("Competition Password") }, modifier = Modifier.fillMaxWidth())
                
                // Date Pickers
                OutlinedTextField(
                    value = startDate,
                    onValueChange = {},
                    label = { Text("Start Date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is PressInteraction.Release) {
                                        showStartDatePicker = true
                                    }
                                }
                            }
                        }
                )

                OutlinedTextField(
                    value = endDate,
                    onValueChange = {},
                    label = { Text("End Date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is PressInteraction.Release) {
                                        showEndDatePicker = true
                                    }
                                }
                            }
                        }
                )

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
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
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
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
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
                
                // Conditional Inputs based on Mode
                if (tournamentMode == "Group Stage" || tournamentMode == "Combined") {
                     OutlinedTextField(value = numberOfGroups, onValueChange = { numberOfGroups = it }, label = { Text("Number of Groups") }, modifier = Modifier.fillMaxWidth())
                }
                
                if (tournamentMode == "Combined") {
                     OutlinedTextField(value = qualifiersPerGroup, onValueChange = { qualifiersPerGroup = it }, label = { Text("Teams advancing per group") }, modifier = Modifier.fillMaxWidth())
                }

                Text("Advanced Settings", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = pointsPerWin, onValueChange = { pointsPerWin = it }, label = { Text("Pts/Win") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = pointsPerDraw, onValueChange = { pointsPerDraw = it }, label = { Text("Pts/Draw") }, modifier = Modifier.weight(1f))
                }

                Button(onClick = {
                    val organizer = loggedInUserId ?: return@Button
                    val fields = fieldCount.toIntOrNull() ?: 1
                    val teams = numberOfTeams.toIntOrNull() ?: 2
                    val groups = numberOfGroups.toIntOrNull() ?: 1
                    val qualifiers = qualifiersPerGroup.toIntOrNull()
                    val pWin = pointsPerWin.toIntOrNull() ?: 3
                    val pDraw = pointsPerDraw.toIntOrNull() ?: 1
                    
                    viewModel.createCompetition(
                        competitionName = name,
                        userId = organizer,
                        refereePassword = refereePassword,
                        competitionPassword = competitionPassword,
                        startDate = startDate,
                        endDate = endDate,
                        sport = sport,
                        fieldCount = fields,
                        scoringType = scoringType,
                        numberOfTeams = teams,
                        tournamentMode = tournamentMode,
                        numberOfGroups = groups,
                        qualifiersPerGroup = qualifiers,
                        pointsPerWin = pWin,
                        pointsPerDraw = pDraw
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
                    val id = (result as AddCompetitionViewModel.CreationResult.Success).competitionId
                    onNavigate("teamNaming/$id")
                    // Reset fields
                    name = ""
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
