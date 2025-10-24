package com.example.sportsorganizer.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportsorganizer.R
import com.example.sportsorganizer.data.local.daos.UserDao
import com.example.sportsorganizer.data.local.entities.User
import com.example.sportsorganizer.ui.viewmodel.CreateUserViewModel
import com.example.sportsorganizer.ui.viewmodel.CreateUserViewModelFactory
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun UserScreen(
    onUpPress: () -> Unit,
    userDao: UserDao,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.create_user_title)) },
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

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun UserScreenPreview() {
    MaterialTheme {
        UserScreen(
            onUpPress = {},
            userDao =
                object : UserDao {
                    override fun getAll(): List<User> = emptyList()

                    override fun loadAllByIds(ids: IntArray): List<User> = emptyList()

                    override fun findByUsername(username: String): User =
                        User(
                            id = 0,
                            firstName = "John",
                            lastName = "Doe",
                            username = username,
                            password = "password123",
                        )

                    override fun insertAll(vararg user: User) {}

                    override fun delete(user: User) {}
                },
        )
    }
}
