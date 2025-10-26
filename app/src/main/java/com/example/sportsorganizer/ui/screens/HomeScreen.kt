package com.example.sportsorganizer.ui.screens

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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsorganizer.data.local.daos.CompetitionDao
import com.example.sportsorganizer.data.local.entities.Competition

@Suppress("ktlint:standard:function-naming")
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    competitionDao: CompetitionDao,
) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.padding(16.dp),
                contentAlignment = Alignment.TopEnd,
            ) {
                FloatingActionButton(onClick = { onNavigate("user") }) {
                    Icon(imageVector = Icons.Filled.AccountCircle, contentDescription = "User")
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier.padding(16.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Button(onClick = { onNavigate("about") }) {
                    Text("About")
                }
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
                    Button(onClick = { onNavigate("organize") }) { Text("Organize") }
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
        HomeScreen(
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
