package com.watsidev.pokeguessredux

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.watsidev.pokeguessredux.data.repository.PokemonRepository
import com.watsidev.pokeguessredux.ui.game.GameMode
import com.watsidev.pokeguessredux.ui.game.GameScreen
import com.watsidev.pokeguessredux.ui.game.GameViewModel
import com.watsidev.pokeguessredux.ui.home.HomeScreen
import com.watsidev.pokeguessredux.ui.pokedex.PokedexScreen
import com.watsidev.pokeguessredux.ui.pokedex.PokemonDetailScreen
import com.watsidev.pokeguessredux.ui.settings.SettingsScreen
import com.watsidev.pokeguessredux.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: PokemonRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("FATAL_CRASH", "Uncaught exception on thread ${thread.name}", throwable)
        }

        enableEdgeToEdge()
        setContent {
            val viewModel: GameViewModel = hiltViewModel()
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
                            onNavigateBack = { navController.popBackStack() },
                            onPokemonClick = { name ->
                                navController.navigate("pokemon_detail/$name")
                            }
                        )
                    }
                    composable("pokemon_detail/{pokemonName}") { backStackEntry ->
                        val pokemonName = backStackEntry.arguments?.getString("pokemonName") ?: ""
                        PokemonDetailScreen(
                            pokemonName = pokemonName,
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
