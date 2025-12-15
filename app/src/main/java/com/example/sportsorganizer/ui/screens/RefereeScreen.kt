package com.example.sportsorganizer.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportsorganizer.R
import com.example.sportsorganizer.data.local.entities.Match
import com.example.sportsorganizer.data.repository.CompetitionRepository
import com.example.sportsorganizer.ui.viewmodel.RefereeLoginState
import com.example.sportsorganizer.ui.viewmodel.RefereeViewModel
import com.example.sportsorganizer.ui.viewmodel.RefereeViewModelFactory

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefereeScreen(
    onUpPress: () -> Unit,
    repository: CompetitionRepository
) {
    val viewModel: RefereeViewModel = viewModel(factory = RefereeViewModelFactory(repository))
    val loginState by viewModel.loginState.collectAsState()
    val selectedField by viewModel.selectedField.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.referee_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onUpPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_back_description),
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (val state = loginState) {
                is RefereeLoginState.Idle, is RefereeLoginState.Error -> {
                    RefereeLoginContent(
                        error = (state as? RefereeLoginState.Error)?.message,
                        onLogin = { id, pass -> viewModel.login(id, pass) }
                    )
                }
                RefereeLoginState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is RefereeLoginState.Success -> {
                    if (selectedField == null) {
                        FieldSelectionContent(
                            fieldCount = state.competition.fieldCount ?: 1,
                            onFieldSelected = { viewModel.selectField(it) }
                        )
                    } else {
                        MatchControlContent(
                            viewModel = viewModel,
                            fieldNumber = selectedField!!
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RefereeLoginContent(error: String?, onLogin: (String, String) -> Unit) {
    var competitionId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Referee Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = competitionId,
            onValueChange = { competitionId = it },
            label = { Text("Competition ID") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Referee Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { onLogin(competitionId, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}

@Composable
fun FieldSelectionContent(fieldCount: Int, onFieldSelected: (Int) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Select Your Field",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(fieldCount) { index ->
                val fieldNum = index + 1
                Button(
                    onClick = { onFieldSelected(fieldNum) },
                    modifier = Modifier.height(80.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Field $fieldNum", fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
fun MatchControlContent(viewModel: RefereeViewModel, fieldNumber: Int) {
    val matches by viewModel.matches.collectAsState()
    val teamNames by viewModel.teamNames.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Field $fieldNumber Matches",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (matches.isEmpty()) {
            Text("No matches scheduled for this field.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(matches) { match ->
                    val team1Name = teamNames[match.team1Id] ?: "Team ${match.team1Id}"
                    val team2Name = teamNames[match.team2Id] ?: "Team ${match.team2Id}"
                    
                    MatchCard(
                        match = match,
                        team1Name = team1Name,
                        team2Name = team2Name,
                        onScoreUpdate = { m, s1, s2 -> viewModel.updateMatchScore(m, s1, s2) },
                        onStatusUpdate = { m, status -> viewModel.updateMatchStatus(m, status) }
                    )
                }
            }
        }
    }
}

@Composable
fun MatchCard(
    match: Match,
    team1Name: String,
    team2Name: String,
    onScoreUpdate: (Match, Int, Int) -> Unit,
    onStatusUpdate: (Match, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (match.status == "in_progress") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (match.status) {
                        "scheduled" -> "SCHEDULED"
                        "in_progress" -> "LIVE"
                        "finished" -> "FINISHED"
                        else -> match.status.uppercase()
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (match.status == "in_progress") Color.Red else Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Scoreboard
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team 1
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(team1Name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text("${match.score1}", style = MaterialTheme.typography.displayMedium)
                    
                    if (match.status == "in_progress") {
                        Row {
                            Button(onClick = { if (match.score1 > 0) onScoreUpdate(match, match.score1 - 1, match.score2) }) { Text("-") }
                            Spacer(modifier = Modifier.width(4.dp))
                            Button(onClick = { onScoreUpdate(match, match.score1 + 1, match.score2) }) { Text("+") }
                        }
                    }
                }
                
                Text("-", style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(horizontal = 8.dp))

                // Team 2
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(team2Name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text("${match.score2}", style = MaterialTheme.typography.displayMedium)
                    
                    if (match.status == "in_progress") {
                        Row {
                            Button(onClick = { if (match.score2 > 0) onScoreUpdate(match, match.score1, match.score2 - 1) }) { Text("-") }
                            Spacer(modifier = Modifier.width(4.dp))
                            Button(onClick = { onScoreUpdate(match, match.score1, match.score2 + 1) }) { Text("+") }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (match.status == "scheduled") {
                    Button(
                        onClick = { onStatusUpdate(match, "in_progress") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Start Match")
                    }
                } else if (match.status == "in_progress") {
                    Button(
                        onClick = { onStatusUpdate(match, "finished") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Finish Match")
                    }
                } else {
                    Button(
                        onClick = { onStatusUpdate(match, "in_progress") }, // Reopen if needed
                        colors = ButtonDefaults.textButtonColors()
                    ) {
                        Text("Reopen Match")
                    }
                }
            }
        }
    }
}
