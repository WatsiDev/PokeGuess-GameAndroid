package com.watsidev.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.watsidev.myapplication.data.repository.PokemonRepository
import com.watsidev.myapplication.ui.game.GameMode
import com.watsidev.myapplication.ui.game.GameScreen
import com.watsidev.myapplication.ui.game.GameViewModel
import com.watsidev.myapplication.ui.home.HomeScreen
import com.watsidev.myapplication.ui.pokedex.PokedexScreen
import com.watsidev.myapplication.ui.settings.SettingsScreen
import com.watsidev.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: PokemonRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GameViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            val darkTheme = when (uiState.theme) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onNavigateToDaily = {
                                viewModel.setGameMode(GameMode.DAILY)
                                navController.navigate("game")
                            },
                            onNavigateToInfinite = {
                                viewModel.setGameMode(GameMode.INFINITE)
                                navController.navigate("game")
                            },
                            onNavigateToPokedex = { navController.navigate("pokedex") },
                            onNavigateToSettings = { navController.navigate("settings") }
                        )
                    }
                    composable("game") {
                        GameScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("pokedex") {
                        PokedexScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            currentTheme = uiState.theme,
                            onThemeSelected = { viewModel.setTheme(it) },
                            onNavigateBack = { navController.popBackStack() },
                            onResetProgress = { viewModel.resetAllProgress() }
                        )
                    }
                }
            }
        }
    }
}
