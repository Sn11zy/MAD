package com.example.sportsorganizer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                FloatingActionButton(onClick = { onNavigate("user") }) {
                    Icon(imageVector = Icons.Filled.AccountCircle, contentDescription = "User")
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier.padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(onClick = { onNavigate("about") }) {
                    Text("About")
                }
            }
        }
    ) { innerPadding: PaddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = { onNavigate("organize") }) { Text("Organize") }
                Button(onClick = { onNavigate("referee") }) { Text("Referee") }
                Button(onClick = { onNavigate("competitor") }) { Text("Competitor") }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(onNavigate = {})
    }
}
