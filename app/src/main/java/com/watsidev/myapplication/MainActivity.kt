package com.watsidev.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.watsidev.myapplication.data.repository.PokemonRepository
import com.watsidev.myapplication.ui.game.GameScreen
import com.watsidev.myapplication.ui.game.GameViewModel
import com.watsidev.myapplication.ui.pokedex.PokedexScreen
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
            MyApplicationTheme {
                val navController = rememberNavController()
                val viewModel: GameViewModel = viewModel()
                
                NavHost(navController = navController, startDestination = "game") {
                    composable("game") {
                        GameScreen(
                            viewModel = viewModel,
                            onNavigateToPokedex = { navController.navigate("pokedex") }
                        )
                    }
                    composable("pokedex") {
                        PokedexScreen(
                            viewModel = viewModel,
                            repository = repository
                        )
                    }
                }
            }
        }
    }
}
