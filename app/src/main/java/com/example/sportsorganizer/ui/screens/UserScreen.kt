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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportsorganizer.R
import com.example.sportsorganizer.data.local.entities.User
import com.example.sportsorganizer.data.local.session.SessionManager
import com.example.sportsorganizer.data.repository.UserRepository
import com.example.sportsorganizer.ui.viewmodel.CreateUserViewModel
import com.example.sportsorganizer.ui.viewmodel.CreateUserViewModelFactory
import com.example.sportsorganizer.utils.PasswordHashing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun UserScreen(
    onUpPress: () -> Unit,
    userRepository: UserRepository,
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
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            )
        },
    ) { innerPadding: PaddingValues ->
        val viewModel: CreateUserViewModel =
            viewModel(
                factory = CreateUserViewModelFactory(userRepository),
            )
        val context = LocalContext.current
        val result by viewModel.creationResult.collectAsState()

        val sessionManager = remember { SessionManager(context) }
        var loggedInUserId by remember { mutableStateOf(sessionManager.getLoggedInUserId()) }
        var loggedInUser by remember { mutableStateOf<User?>(null) }

        LaunchedEffect(loggedInUserId) {
            if (loggedInUserId != null) {
                try {
                    val user = userRepository.getUserById(loggedInUserId!!)
                    loggedInUser = user
                } catch (_: Exception) {
                    loggedInUser = null
                }
            } else {
                loggedInUser = null
            }
        }

        var first by remember { mutableStateOf("") }
        var last by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        var loginUsername by remember { mutableStateOf("") }
        var loginPassword by remember { mutableStateOf("") }

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
                OutlinedTextField(value = username, onValueChange = {
                    username = it
                }, label = { Text("Username") }, modifier = Modifier.testTag("create_username"))
                OutlinedTextField(value = password, onValueChange = {
                    password = it
                }, label = { Text("Password") }, modifier = Modifier.testTag("create_password"))
                Button(onClick = {
                    viewModel.createUser(
                        firstName = first.ifBlank { null },
                        lastName = last.ifBlank { null },
                        username = username,
                        password = password,
                    )
                }, modifier = Modifier.testTag("create_button")) { Text("Create") }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(text = "Login", style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(value = loginUsername, onValueChange = {
                    loginUsername = it
                }, label = { Text("Username") }, modifier = Modifier.testTag("login_username"))
                OutlinedTextField(value = loginPassword, onValueChange = {
                    loginPassword = it
                }, label = { Text("Password") }, modifier = Modifier.testTag("login_password"))
                Button(onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val user = userRepository.getUserByUsername(loginUsername.trim())
                            if (user != null && PasswordHashing.verifyPassword(loginPassword, user.passwordHash)) {
                                sessionManager.saveLoggedInUserId(user.id)
                                withContext(Dispatchers.Main) {
                                    loggedInUserId = user.id
                                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Invalid username or password", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    onUpPress()}, modifier = Modifier.testTag("login_button")) { Text("Login") }
        }

        LaunchedEffect(result) {
            when (result) {
                is CreateUserViewModel.CreationResult.Success -> {
                    // Assuming success means user was created. We need to fetch the ID or have the backend return it.
                    // For now, prompt user to login.
                     Toast.makeText(context, "User created! Please login.", Toast.LENGTH_SHORT).show()
                     first = ""
                     last = ""
                     username = ""
                     password = ""
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
