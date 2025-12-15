package com.example.sportsorganizer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportsorganizer.R
import com.example.sportsorganizer.data.local.entities.Match
import com.example.sportsorganizer.data.repository.CompetitionRepository
import com.example.sportsorganizer.ui.viewmodel.CompetitorUiState
import com.example.sportsorganizer.ui.viewmodel.CompetitorViewModel
import com.example.sportsorganizer.ui.viewmodel.CompetitorViewModelFactory
import com.example.sportsorganizer.utils.TeamStats

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitorScreen(
    onUpPress: () -> Unit,
    repository: CompetitionRepository
) {
    val viewModel: CompetitorViewModel = viewModel(factory = CompetitorViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Competitor View") },
                navigationIcon = {
                    IconButton(onClick = onUpPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_back_description),
                        )
                    }
                },
                actions = {
                    if (uiState is CompetitorUiState.Success) {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is CompetitorUiState.Idle, is CompetitorUiState.Error -> {
                    CompetitorLoginContent(
                        error = (state as? CompetitorUiState.Error)?.message,
                        onLogin = { id -> viewModel.login(id) }
                    )
                }
                CompetitorUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is CompetitorUiState.Success -> {
                    CompetitorDashboard(state)
                }
            }
        }
    }
}

@Composable
fun CompetitorLoginContent(error: String?, onLogin: (String) -> Unit) {
    var competitionId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter Competition ID", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = competitionId,
            onValueChange = { competitionId = it },
            label = { Text("Competition ID") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { onLogin(competitionId) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Competition")
        }
    }
}

@Composable
fun CompetitorDashboard(state: CompetitorUiState.Success) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Matches", "Standings")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> MatchesList(state.matches, state.teamNames)
            1 -> StandingsTable(state.standings, state.teamNames)
        }
    }
}

@Composable
fun MatchesList(matches: List<Match>, teamNames: Map<Long, String>) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedField by remember { mutableStateOf<Int?>(null) }
    var showFieldDropdown by remember { mutableStateOf(false) }

    // Get unique fields for dropdown
    val availableFields = matches.mapNotNull { it.fieldNumber }.distinct().sorted()

    // Filter Logic
    val filteredMatches = matches.filter { match ->
        val team1 = teamNames[match.team1Id] ?: ""
        val team2 = teamNames[match.team2Id] ?: ""
        
        val matchesSearch = if (searchQuery.isBlank()) true else {
            team1.contains(searchQuery, ignoreCase = true) || team2.contains(searchQuery, ignoreCase = true)
        }
        
        val matchesField = if (selectedField == null) true else match.fieldNumber == selectedField
        
        matchesSearch && matchesField
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filters Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Team") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                }
            )
            
            Box {
                Button(onClick = { showFieldDropdown = true }) {
                    Icon(Icons.Default.FilterList, "Filter Field")
                    if (selectedField != null) {
                        Text(" Field $selectedField")
                    }
                }
                DropdownMenu(
                    expanded = showFieldDropdown,
                    onDismissRequest = { showFieldDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Fields") },
                        onClick = {
                            selectedField = null
                            showFieldDropdown = false
                        }
                    )
                    availableFields.forEach { field ->
                        DropdownMenuItem(
                            text = { Text("Field $field") },
                            onClick = {
                                selectedField = field
                                showFieldDropdown = false
                            }
                        )
                    }
                }
            }
        }

        if (filteredMatches.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No matches found matching criteria.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredMatches) { match ->
                    val team1Name = teamNames[match.team1Id] ?: "Team ${match.team1Id}"
                    val team2Name = teamNames[match.team2Id] ?: "Team ${match.team2Id}"
                    
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                 Text(
                                    text = when (match.status) {
                                        "in_progress" -> "LIVE"
                                        "finished" -> "FT"
                                        else -> "Scheduled"
                                    },
                                    color = if (match.status == "in_progress") Color.Red else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                if (match.fieldNumber != null) {
                                    Text("Field ${match.fieldNumber}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(team1Name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                Text(
                                    "${match.score1} - ${match.score2}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Text(team2Name, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            }
                            if (match.stage != null) {
                                Text(match.stage, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StandingsTable(standings: List<TeamStats>, teamNames: Map<Long, String>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(8.dp)
        ) {
            Text("#", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold)
            Text("Team", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("P", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            Text("W", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            Text("D", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            Text("L", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            Text("Pts", modifier = Modifier.width(40.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        }
        
        LazyColumn {
            itemsIndexed(standings) { index, stats ->
                val teamName = teamNames[stats.teamId] ?: "Team ${stats.teamId}"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("${index + 1}", modifier = Modifier.width(30.dp))
                    Text(teamName, modifier = Modifier.weight(1f))
                    Text("${stats.played}", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                    Text("${stats.won}", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                    Text("${stats.drawn}", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                    Text("${stats.lost}", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                    Text("${stats.points}", modifier = Modifier.width(40.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
