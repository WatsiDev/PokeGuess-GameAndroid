package com.watsidev.pokeguessredux.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.watsidev.pokeguessredux.R
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.watsidev.pokeguessredux.data.model.Pokemon
import com.watsidev.pokeguessredux.domain.model.Direction
import com.watsidev.pokeguessredux.domain.model.MatchState
import com.watsidev.pokeguessredux.domain.model.PokemonComparison
import com.watsidev.pokeguessredux.ui.theme.CorrectGreen
import com.watsidev.pokeguessredux.ui.theme.IncorrectRed
import com.watsidev.pokeguessredux.ui.theme.PartialYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isGameOver && uiState.targetPokemon != null) {
        VictoryModal(
            pokemon = uiState.targetPokemon!!,
            gameMode = uiState.gameMode,
            timeUntilNext = uiState.timeUntilNext,
            onPlayAgain = { viewModel.setGameMode(GameMode.INFINITE) },
            onGoHome = onNavigateBack
        )
    }

    Scaffold(
        topBar = {
            val title = when(uiState.gameMode) {
                GameMode.DAILY -> stringResource(R.string.daily_challenge)
                GameMode.INFINITE -> stringResource(R.string.infinite_mode)
                GameMode.GENERATION -> uiState.selectedGeneration?.let { stringResource(R.string.generation_x, it) } ?: ""
            }
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    Text(
                        stringResource(R.string.streak, uiState.streak),
                        modifier = Modifier.padding(end = 16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            GameContent(uiState, viewModel)
        }
    }
}

@Composable
fun GameContent(uiState: GameUiState, viewModel: GameViewModel) {
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp > 600

    if (isWideScreen) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SearchSection(uiState, viewModel)
            }
            Column(modifier = Modifier.weight(1.5f)) {
                GuessList(guesses = uiState.guesses)
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchSection(uiState, viewModel)
            Spacer(modifier = Modifier.height(16.dp))
            GuessList(guesses = uiState.guesses)
        }
    }
}

@Composable
fun SearchSection(uiState: GameUiState, viewModel: GameViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Guess #${uiState.guesses.size + 1}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        SearchBar(
            query = uiState.searchQuery,
            onQueryChanged = { viewModel.onSearchQueryChanged(it) },
            results = uiState.searchResults,
            onResultSelected = { viewModel.makeGuess(it) },
            enabled = !uiState.isGameOver,
            isSearching = uiState.isSearching
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    results: List<Pokemon>,
    onResultSelected: (String) -> Unit,
    enabled: Boolean,
    isSearching: Boolean
) {
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.search_pokemon)) },
            enabled = enabled,
            leadingIcon = { 
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            },
            singleLine = true
        )

        AnimatedVisibility(visible = results.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(results) { pokemon ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    pokemon.name.replaceFirstChar { it.uppercase() },
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            supportingContent = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Gen ${pokemon.generation}", style = MaterialTheme.typography.labelSmall)
                                    Text("•", style = MaterialTheme.typography.labelSmall)
                                    Text("Stage ${pokemon.evolutionaryStage}", style = MaterialTheme.typography.labelSmall)
                                    Text("•", style = MaterialTheme.typography.labelSmall)
                                    pokemon.types.forEach { type ->
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = type,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                    Text("•", style = MaterialTheme.typography.labelSmall)
                                    Text("${pokemon.height / 10.0}m", style = MaterialTheme.typography.labelSmall)
                                    Text("${pokemon.weight / 10.0}kg", style = MaterialTheme.typography.labelSmall)
                                }
                            },
                            leadingContent = {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(pokemon.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = pokemon.name,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            },
                            modifier = Modifier.clickable { onResultSelected(pokemon.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VictoryModal(
    pokemon: Pokemon,
    gameMode: GameMode,
    timeUntilNext: String,
    onPlayAgain: () -> Unit,
    onGoHome: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Modal is mandatory on win */ },
        confirmButton = {
            if (gameMode == GameMode.INFINITE) {
                Button(onClick = onPlayAgain) {
                    Text(stringResource(R.string.play_again))
                }
            } else {
                Button(onClick = onGoHome) {
                    Text(stringResource(R.string.home))
                }
            }
        },
        title = {
            Text(
                text = stringResource(R.string.catch_success),
                fontWeight = FontWeight.Black,
                color = CorrectGreen,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pokemon.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = pokemon.name,
                    modifier = Modifier.size(160.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = pokemon.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "#${pokemon.id}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.outline
                )
                
                if (gameMode == GameMode.DAILY) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.next_pokemon_in),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = timeUntilNext,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}

@Composable
fun GuessList(guesses: List<PokemonComparison>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(guesses) { guess ->
            GuessItem(guess)
        }
    }
}

@Composable
fun GuessItem(guess: PokemonComparison) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(guess.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = guess.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = guess.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttributeBox(stringResource(R.string.attr_gen), guess.generation.value.toString(), guess.generation.state, guess.generation.direction)
                AttributeBox(stringResource(R.string.attr_id), "#${guess.id.value}", guess.id.state, guess.id.direction)
                AttributeBox(stringResource(R.string.attr_stage), "S${guess.evolutionaryStage.value}", guess.evolutionaryStage.state, guess.evolutionaryStage.direction)
                AttributeBox(stringResource(R.string.attr_type), guess.types.value.joinToString("\n"), guess.types.state)
                AttributeBox(stringResource(R.string.attr_height), "${guess.height.value / 10.0}m", guess.height.state, guess.height.direction)
                AttributeBox(stringResource(R.string.attr_weight), "${guess.weight.value / 10.0}kg", guess.weight.state, guess.weight.direction)
            }
        }
    }
}

@Composable
fun AttributeBox(
    label: String,
    value: String,
    state: MatchState,
    direction: Direction = Direction.EQUAL
) {
    val backgroundColor = when (state) {
        MatchState.CORRECT -> CorrectGreen
        MatchState.PARTIAL -> PartialYellow
        MatchState.INCORRECT -> IncorrectRed
    }

    val contentColor = if (state == MatchState.PARTIAL) Color.Black else Color.White

    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                lineHeight = 12.sp
            )
            if (direction != Direction.EQUAL) {
                Icon(
                    imageVector = if (direction == Direction.HIGHER) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = contentColor
                )
            }
        }
    }
}
