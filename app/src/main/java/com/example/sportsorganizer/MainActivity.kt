package com.example.sportsorganizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.sportsorganizer.data.local.dbs.AppDatabase
import com.example.sportsorganizer.data.local.dbs.MIGRATION_1_2
import com.example.sportsorganizer.ui.screens.AboutScreen
import com.example.sportsorganizer.ui.screens.CompetitorScreen
import com.example.sportsorganizer.ui.screens.HomeScreen
import com.example.sportsorganizer.ui.screens.OrganizeScreen
import com.example.sportsorganizer.ui.screens.RefereeScreen
import com.example.sportsorganizer.ui.screens.UserScreen
import com.example.sportsorganizer.ui.screens.CompetitionDetailScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    val db = remember {
                        Room.databaseBuilder(
                            applicationContext,
                            AppDatabase::class.java,
                            "sports_organizer.db"
                        ).addMigrations(MIGRATION_1_2).build()
                    }
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                    ) {
                        composable("home") {
                            HomeScreen(
                                onNavigate = { route -> navController.navigate(route) },
                                competitionDao = db.competitionDao()
                            )
                        }
                        composable("organize") { OrganizeScreen(competitionDao = db.competitionDao()) }
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

                        composable("user") { UserScreen(userDao = db.userDao()) }
                        composable(
                            "competitionDetail/{competitionId}",
                            arguments = listOf(navArgument("competitionId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val competitionId = backStackEntry.arguments?.getLong("competitionId")
                            if (competitionId != null) {
                                CompetitionDetailScreen(
                                    competitionId = competitionId,
                                    competitionDao = db.competitionDao()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
