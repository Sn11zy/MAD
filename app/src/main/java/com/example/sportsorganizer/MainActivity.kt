package com.example.sportsorganizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sportsorganizer.ui.screens.AboutScreen
import com.example.sportsorganizer.ui.screens.CompetitorScreen
import com.example.sportsorganizer.ui.screens.HomeScreen
import com.example.sportsorganizer.ui.screens.OrganizeScreen
import com.example.sportsorganizer.ui.screens.RefereeScreen
import com.example.sportsorganizer.ui.screens.UserScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(onNavigate = { route -> navController.navigate(route) })
                        }
                        composable("organize") { OrganizeScreen() }
                        composable("referee") { RefereeScreen() }
                        composable("competitor") { CompetitorScreen() }
                        composable("user") { UserScreen() }
                        composable("about") { AboutScreen() }
                    }
                }
            }
        }
    }
}


