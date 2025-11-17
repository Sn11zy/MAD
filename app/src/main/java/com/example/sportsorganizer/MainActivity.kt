package com.example.sportsorganizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.sportsorganizer.data.local.dbs.AppDatabase
import com.example.sportsorganizer.data.local.dbs.MIGRATION_1_2
import com.example.sportsorganizer.ui.screens.AboutScreen
import com.example.sportsorganizer.ui.screens.CompetitionDetailScreen
import com.example.sportsorganizer.ui.screens.CompetitorScreen
import com.example.sportsorganizer.ui.screens.HomeScreen
import com.example.sportsorganizer.ui.screens.OrganizeScreen
import com.example.sportsorganizer.ui.screens.RefereeScreen
import com.example.sportsorganizer.ui.screens.UserScreen
import com.example.sportsorganizer.ui.theme.SportsOrganizerTheme
import com.example.sportsorganizer.ui.viewmodel.ThemeViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    private val themeViewModel by viewModels<ThemeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            SportsOrganizerTheme(darkTheme = isDarkTheme) {
                Surface {
                    val navController = rememberNavController()
                    val db =
                        remember {
                            Room
                                .databaseBuilder(
                                    applicationContext,
                                    AppDatabase::class.java,
                                    "sports_organizer.db",
                                ).addMigrations(MIGRATION_1_2)
                                .build()
                        }
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                    ) {
                        composable("home") {
                            HomeScreen(
                                onNavigate = { route -> navController.navigate(route) },
                                competitionDao = db.competitionDao(),
                                onToggleTheme = { themeViewModel.toggleTheme() }
                            )
                        }
                        composable(
                            "organize",
                        ) {
                            OrganizeScreen(
                                onUpPress = { navController.navigateUp() },
                                onNavigate = { route ->
                                    navController.navigate(route)
                                },
                                competitionDao = db.competitionDao(),
                            )
                        }
                        composable("referee") {
                            RefereeScreen(onUpPress = {
                                navController.navigateUp()
                            })
                        }
                        composable("competitor") {
                            CompetitorScreen(
                                onUpPress = { navController.navigateUp() },
                            )
                        }
                        composable("about") {
                            AboutScreen(
                                onUpPress = { navController.navigateUp() },
                            )
                        }

                        composable("user") {
                            UserScreen(onUpPress = { navController.navigateUp() }, userDao = db.userDao())
                        }
                        composable(
                            "competitionDetail/{competitionId}",
                            arguments = listOf(navArgument("competitionId") { type = NavType.LongType }),
                        ) { backStackEntry ->
                            val competitionId = backStackEntry.arguments?.getLong("competitionId")
                            if (competitionId != null) {
                                CompetitionDetailScreen(
                                    onUpPress = { navController.navigateUp() },
                                    competitionId = competitionId,
                                    competitionDao = db.competitionDao(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
