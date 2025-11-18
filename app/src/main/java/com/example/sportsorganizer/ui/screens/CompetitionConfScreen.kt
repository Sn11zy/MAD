package com.example.sportsorganizer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportsorganizer.R
import com.example.sportsorganizer.data.local.daos.CompetitionConfigDao
import com.example.sportsorganizer.data.local.daos.CompetitionDao
import com.example.sportsorganizer.data.local.entities.CompetitionConfig
import com.example.sportsorganizer.ui.viewmodel.CompetitionConfigViewModel
import com.example.sportsorganizer.ui.viewmodel.CompetitionConfigViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionConfigScreen(
    competitionId: Long,
    competitionDao: CompetitionDao,
    competitionConfigDao: CompetitionConfigDao,
    onConfirmDone: () -> Unit = {},
    onUpPress: () -> Unit,
    ) {
    val vm: CompetitionConfigViewModel = viewModel(factory = CompetitionConfigViewModelFactory(competitionConfigDao, competitionId))
    val cfg by vm.config.collectAsState(initial = null)

    // Local UI state as explicit MutableState objects
    val nameState = remember { mutableStateOf("") }
    val addressState = remember { mutableStateOf("") }
    val dateState = remember { mutableStateOf("") }
    val maxParticipantsState = remember { mutableStateOf("") }

    // New config fields
    val sportTypeState = remember { mutableStateOf("") }
    val numberOfFieldsState = remember { mutableStateOf("") }
    val scoringModeState = remember { mutableStateOf("") } // TIME or POINTS
    val scoringValueState = remember { mutableStateOf("") }
    val isSuddenDeathState = remember { mutableStateOf(false) }
    val numberOfSetsState = remember { mutableStateOf("") }
    val numberOfTeamsState = remember { mutableStateOf("") }

    // Load competition basic info once
    LaunchedEffect(competitionId) {
        val comp = competitionDao.findById(competitionId)
        comp?.let {
            nameState.value = it.competitionName
            dateState.value = it.eventDate
            // prefill address if available
            addressState.value = it.address ?: ""
        }
    }

    // Populate fields when config loads
    LaunchedEffect(cfg) {
        cfg?.let {
            addressState.value = it.address ?: ""
            maxParticipantsState.value = it.maxParticipants?.toString() ?: ""

            // new fields
            sportTypeState.value = it.sportType ?: ""
            numberOfFieldsState.value = it.numberOfFields?.toString() ?: ""
            scoringModeState.value = it.scoringMode ?: "TIME"
            scoringValueState.value = it.scoringValue?.toString() ?: ""
            isSuddenDeathState.value = it.isSuddenDeath
            numberOfSetsState.value = it.numberOfSets?.toString() ?: ""
            numberOfTeamsState.value = it.numberOfTeams?.toString() ?: ""
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "CONF", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
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
        }
    ) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Competition name (read-only)
            OutlinedTextField(
                value = nameState.value,
                onValueChange = {},
                label = { Text("Competition name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = addressState.value,
                onValueChange = { addressState.value = it },
                label = { Text("Address (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dateState.value,
                    onValueChange = { dateState.value = it },
                    label = { Text("Date") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sport type and number of fields
            OutlinedTextField(
                value = sportTypeState.value,
                onValueChange = { sportTypeState.value = it },
                label = { Text("Sport type (e.g. Volleyball)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = numberOfFieldsState.value,
                onValueChange = { numberOfFieldsState.value = it.filter { ch -> ch.isDigit() } },
                label = { Text("Number of fields available (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Scoring mode
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = scoringModeState.value,
                    onValueChange = { scoringModeState.value = it },
                    label = { Text("Scoring mode (TIME or POINTS)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = scoringValueState.value,
                    onValueChange = { scoringValueState.value = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Scoring value (minutes or points)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = numberOfSetsState.value,
                onValueChange = { numberOfSetsState.value = it.filter { ch -> ch.isDigit() } },
                label = { Text("Number of sets per match (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = numberOfTeamsState.value,
                onValueChange = { numberOfTeamsState.value = it.filter { ch -> ch.isDigit() } },
                label = { Text("Number of teams") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = maxParticipantsState.value,
                onValueChange = { maxParticipantsState.value = it.filter { ch: Char -> ch.isDigit() } },
                label = { Text("Max participants (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val mp = maxParticipantsState.value.toIntOrNull()
                        val nf = numberOfFieldsState.value.toIntOrNull()
                        val sv = scoringValueState.value.toIntOrNull()
                        val ns = numberOfSetsState.value.toIntOrNull()
                        val nt = numberOfTeamsState.value.toIntOrNull()
                        val newCfg = CompetitionConfig(
                            competitionId = competitionId,
                            address = addressState.value.takeIf { it.isNotBlank() },
                            maxParticipants = mp,

                            sportType = sportTypeState.value.takeIf { it.isNotBlank() },
                            numberOfFields = nf,
                            scoringMode = scoringModeState.value.takeIf { it.isNotBlank() },
                            scoringValue = sv,
                            isSuddenDeath = isSuddenDeathState.value,
                            numberOfSets = ns,
                            numberOfTeams = nt
                        )
                        vm.upsertConfig(newCfg)
                        onConfirmDone()
                    }) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompetitionConfigScreenPreview() {
    // Provide mock DAOs for preview
    val mockCompetitionDao = object : CompetitionDao {
        override fun getAll(): kotlinx.coroutines.flow.Flow<List<com.example.sportsorganizer.data.local.entities.Competition>> = kotlinx.coroutines.flow.flowOf(emptyList())
        override suspend fun findById(id: Long): com.example.sportsorganizer.data.local.entities.Competition? = null
        override suspend fun loadAllByIds(ids: IntArray): List<com.example.sportsorganizer.data.local.entities.Competition> = emptyList()
        override suspend fun findByName(name: String): com.example.sportsorganizer.data.local.entities.Competition? = null
        override suspend fun insertAll(vararg competition: com.example.sportsorganizer.data.local.entities.Competition): List<Long> = emptyList()
        override suspend fun delete(competition: com.example.sportsorganizer.data.local.entities.Competition) {}
    }

    val mockConfigDao = object : CompetitionConfigDao {
        override suspend fun upsert(config: CompetitionConfig) = 0L
        override fun getByCompetitionIdFlow(competitionId: Long) = kotlinx.coroutines.flow.flowOf(null as CompetitionConfig?)
        override suspend fun getByCompetitionId(competitionId: Long) = null
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CompetitionConfigScreen(
                competitionId = 1L,
                competitionDao = mockCompetitionDao,
                competitionConfigDao = mockConfigDao,
                onConfirmDone = {},
                onUpPress = {}
            )
        }
    }
}
