package com.example.sportsorganizer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportsorganizer.data.local.daos.CompetitionDao
import com.example.sportsorganizer.ui.viewmodel.AddCompetitionViewModel
import com.example.sportsorganizer.ui.viewmodel.AddCompetitionViewModelFactory
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun OrganizeScreen(competitionDao: CompetitionDao) {
    Scaffold { innerPadding: PaddingValues ->
        val viewModel: AddCompetitionViewModel = viewModel(
            factory = AddCompetitionViewModelFactory(competitionDao)
        )

        var name by remember { mutableStateOf("") }
        var organizerId by remember { mutableStateOf("") }
        val context = LocalContext.current
        val result by viewModel.creationResult.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Create Competition", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
            OutlinedTextField(value = organizerId, onValueChange = { organizerId = it }, label = { Text("Organizer ID") })
            Button(onClick = {
                val organizer = organizerId.toLongOrNull() ?: 0L
                viewModel.createCompetition(name.ifBlank { null }, organizer)
            }) { Text("Create") }
        }

        LaunchedEffect(result) {
            when (result) {
                is AddCompetitionViewModel.CreationResult.Success -> {
                    Toast.makeText(context, "Competition created", Toast.LENGTH_SHORT).show()
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

