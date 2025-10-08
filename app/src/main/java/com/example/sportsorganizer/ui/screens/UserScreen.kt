package com.example.sportsorganizer.ui.screens

import android.widget.Toast
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
import com.example.sportsorganizer.data.local.daos.UserDao
import com.example.sportsorganizer.ui.viewmodel.CreateUserViewModel
import com.example.sportsorganizer.ui.viewmodel.CreateUserViewModelFactory

@Composable
fun UserScreen(userDao: UserDao) {
    Scaffold { innerPadding: PaddingValues ->
        val viewModel: CreateUserViewModel =
            viewModel(
                factory = CreateUserViewModelFactory(userDao),
            )
        val context = LocalContext.current
        val result by viewModel.creationResult.collectAsState()

        var first by remember { mutableStateOf("") }
        var last by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Create User", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(value = first, onValueChange = { first = it }, label = { Text("First name") })
            OutlinedTextField(value = last, onValueChange = { last = it }, label = { Text("Last name") })
            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
            Button(onClick = {
                viewModel.createUser(
                    firstName = first.ifBlank { null },
                    lastName = last.ifBlank { null },
                    username = username,
                    password = password,
                )
            }) { Text("Create") }
        }

        LaunchedEffect(result) {
            when (result) {
                is CreateUserViewModel.CreationResult.Success -> {
                    Toast.makeText(context, "User created", Toast.LENGTH_SHORT).show()
                }
                is CreateUserViewModel.CreationResult.Error -> {
                    val msg = (result as CreateUserViewModel.CreationResult.Error).message
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }
}
