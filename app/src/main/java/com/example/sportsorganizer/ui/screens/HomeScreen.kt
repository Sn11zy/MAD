package com.example.sportsorganizer.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsorganizer.data.local.session.SessionManager

@Suppress("ktlint:standard:function-naming")
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onToggleTheme: () -> Unit,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val isLoggedIn = sessionManager.getLoggedInUserId() != null

    Scaffold(
        topBar = {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            ) {
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.align(Alignment.TopStart),
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                        contentDescription = "Toggle Theme",
                    )
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier.padding(16.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Button(onClick = { onNavigate("about") }) { Text("About") }
            }
        },
    ) { innerPadding: PaddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(onClick = {
                        if (isLoggedIn) onNavigate("organize") else onNavigate("user")
                    }) { Text(if (isLoggedIn) "Organize" else "Organizer Login") }
                    Button(onClick = { onNavigate("referee") }) { Text("Referee") }
                    Button(onClick = { onNavigate("competitor") }) { Text("Competitor") }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(onNavigate = {}, onToggleTheme = {})
    }
}
