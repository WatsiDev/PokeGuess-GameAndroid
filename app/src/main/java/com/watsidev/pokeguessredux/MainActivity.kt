package com.watsidev.pokeguessredux

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
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
import com.watsidev.pokeguessredux.ui.game.GenerationScreen
import com.watsidev.pokeguessredux.ui.game.MemoryDifficultyScreen
import com.watsidev.pokeguessredux.ui.game.MemoryGameScreen
import com.watsidev.pokeguessredux.ui.game.MemoryDifficulty
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

                if (uiState.shouldShowUpdateNotice) {
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text(stringResource(R.string.update_notice_title)) },
                        text = { Text(stringResource(R.string.update_notice_message)) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.dismissUpdateNotice() }) {
                                Text(stringResource(R.string.ok))
                            }
                        }
                    )
                }
                
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
                            onNavigateToGenerations = {
                                navController.navigate("generations")
                            },
                            onNavigateToMemory = {
                                navController.navigate("memory_difficulty")
                            },
                            onNavigateToPokedex = { navController.navigate("pokedex") },
                            onNavigateToSettings = { navController.navigate("settings") }
                        )
                    }
                    composable("generations") {
                        GenerationScreen(
                            onGenerationSelected = { gen ->
                                viewModel.setGameMode(GameMode.GENERATION, gen)
                                navController.navigate("game")
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("memory_difficulty") {
                        MemoryDifficultyScreen(
                            onDifficultySelected = { diff ->
                                navController.navigate("memory_game/${diff.name}")
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("memory_game/{difficultyName}") { backStackEntry ->
                        val diffName = backStackEntry.arguments?.getString("difficultyName") ?: "EASY"
                        val difficulty = MemoryDifficulty.valueOf(diffName)
                        MemoryGameScreen(
                            difficulty = difficulty,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("game") {
                        GameScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("memory_difficulty") {
                        MemoryDifficultyScreen(
                            onDifficultySelected = { diff ->
                                navController.navigate("memory_game/${diff.name}")
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("memory_game/{difficultyName}") { backStackEntry ->
                        val diffName = backStackEntry.arguments?.getString("difficultyName") ?: "EASY"
                        val difficulty = MemoryDifficulty.valueOf(diffName)
                        MemoryGameScreen(
                            difficulty = difficulty,
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
                    composable("memory_difficulty") {
                        MemoryDifficultyScreen(
                            onDifficultySelected = { diff ->
                                navController.navigate("memory_game/${diff.name}")
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("memory_game/{difficultyName}") { backStackEntry ->
                        val diffName = backStackEntry.arguments?.getString("difficultyName") ?: "EASY"
                        val difficulty = MemoryDifficulty.valueOf(diffName)
                        MemoryGameScreen(
                            difficulty = difficulty,
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
