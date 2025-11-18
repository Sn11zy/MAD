package com.example.sportsorganizer.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportsorganizer.R
import com.example.sportsorganizer.data.local.daos.CompetitionDao
import com.example.sportsorganizer.data.local.entities.Competition
import com.example.sportsorganizer.ui.viewmodel.AddCompetitionViewModel
import com.example.sportsorganizer.ui.viewmodel.AddCompetitionViewModelFactory

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizeScreen(
    onUpPress: () -> Unit,
    competitionDao: CompetitionDao,
    onNavigate: (String) -> Unit,
) {
    val competitions by competitionDao.getAll().collectAsState(initial = emptyList())

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
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            )
        },
    ) { innerPadding: PaddingValues ->
        val viewModel: AddCompetitionViewModel =
            viewModel(
                factory = AddCompetitionViewModelFactory(competitionDao),
            )

        var name by remember { mutableStateOf("") }
        var organizerId by remember { mutableStateOf("") }
        var eventDate by remember { mutableStateOf("") }
        val searchQuery by viewModel.searchQuery.collectAsState()
        val searchResults by viewModel.searchResults.collectAsState()
        var expanded by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val result by viewModel.creationResult.collectAsState()
        val competitions by viewModel.competitions.collectAsState()

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
            OutlinedTextField(value = organizerId, onValueChange = { organizerId = it }, label = { Text("Organizer ID") })
            Box {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        label = { Text("City") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(),
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
            OutlinedTextField(value = eventDate, onValueChange = { eventDate = it }, label = { Text("Event Date (YYYY-MM-DD)") })
            Button(onClick = {
                val organizer = organizerId.toLongOrNull() ?: 0L
                viewModel.createCompetition(name, organizer, eventDate)
            }) { Text("Create") }
            Text(
                text = "Existing Competitions",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp),
            )
            if (competitions.isEmpty()) {
                Text(
                    text = "No competitions found",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(8.dp),
                )
            } else {
                LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                    items(competitions) { competition ->
                        Card(
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        onNavigate("competitionConfig/${competition.id}")
                                    },
                        ) {
                            Text(
                                text = competition.competitionName ?: "No name",
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
                    organizerId = ""
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

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun OrganizeScreenPreview() {
    MaterialTheme {
        OrganizeScreen(
            onUpPress = {},
            onNavigate = {},
            competitionDao =
                object : CompetitionDao {
                    override fun getAll(): kotlinx.coroutines.flow.Flow<List<Competition>> = kotlinx.coroutines.flow.flowOf(emptyList())

                    override suspend fun findById(id: Long): Competition? = null

                    override suspend fun loadAllByIds(ids: IntArray): List<Competition> = emptyList()

                    override suspend fun findByName(name: String): Competition? = null

                    override suspend fun insertAll(vararg competition: Competition): List<Long> = emptyList()

                    override suspend fun delete(competition: Competition) {}
                },
        )
    }
}
