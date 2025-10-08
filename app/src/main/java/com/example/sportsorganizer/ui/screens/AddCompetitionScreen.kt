package com.example.sportsorganizer.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportsorganizer.data.local.daos.CompetitionDao
import com.example.sportsorganizer.ui.viewmodel.AddCompetitionViewModel
import com.example.sportsorganizer.ui.viewmodel.AddCompetitionViewModelFactory

@Suppress("ktlint:standard:function-naming")
@Composable
fun OrganizeScreen(competitionDao: CompetitionDao) {
    Scaffold { innerPadding: PaddingValues ->
        val viewModel: AddCompetitionViewModel =
            viewModel(
                factory = AddCompetitionViewModelFactory(competitionDao),
            )

        var name by remember { mutableStateOf("") }
        var organizerId by remember { mutableStateOf("") }
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
            Text(text = "Create Competition", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
            OutlinedTextField(value = organizerId, onValueChange = { organizerId = it }, label = { Text("Organizer ID") })
            Button(onClick = {
                val organizer = organizerId.toLongOrNull() ?: 0L
                viewModel.createCompetition(name.ifBlank { null }, organizer)
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
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(competitions) { competition ->
                        Text(
                            text = "${competition.competitionName ?: "Unnamed"} (ID: ${competition.id}, Organizer: ${competition.organizer})",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier =
                                Modifier
                                    .fillMaxWidth(0.8f)
                                    .padding(horizontal = 8.dp),
                        )
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
