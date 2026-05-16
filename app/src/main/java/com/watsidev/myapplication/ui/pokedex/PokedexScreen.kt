package com.watsidev.myapplication.ui.pokedex

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.watsidev.myapplication.data.model.Pokemon
import com.watsidev.myapplication.data.repository.PokemonRepository
import com.watsidev.myapplication.ui.game.GameViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokedexScreen(viewModel: GameViewModel, repository: PokemonRepository) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var capturedPokemon by remember { mutableStateOf<List<Pokemon>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.capturedIds) {
        if (uiState.capturedIds.isNotEmpty()) {
            isLoading = true
            scope.launch {
                try {
                    val details = repository.getPokemonDetailsParallel(
                        uiState.capturedIds.map { id ->
                            // This is a bit of a hack since we need the name to fetch details
                            // In a real app, we'd store name or have an ID-based fetch
                            // For now, let's assume we can fetch by ID as string
                            id.toString()
                        }
                    )
                    capturedPokemon = details.sortedBy { it.id }
                } catch (e: Exception) {
                    // Handle error
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Pokedex", fontWeight = FontWeight.Bold) })
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (capturedPokemon.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No Pokemon captured yet!")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(capturedPokemon) { pokemon ->
                    CapturedPokemonCard(pokemon)
                }
            }
        }
    }
}

@Composable
fun CapturedPokemonCard(pokemon: Pokemon) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(pokemon.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = pokemon.name,
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
            Text(
                text = "#${pokemon.id} ${pokemon.name.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Gen ${pokemon.generation}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
