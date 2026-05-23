package com.watsidev.pokeguessredux.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.watsidev.pokeguessredux.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryGameScreen(
    difficulty: MemoryDifficulty,
    viewModel: MemoryGameViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(difficulty) {
        viewModel.startGame(context, difficulty)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.time_left, uiState.timeLeft), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(R.string.pairs_found, uiState.pairsFound, uiState.totalPairs), style = MaterialTheme.typography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val columns = when {
                    uiState.cards.size <= 8 -> 2
                    uiState.cards.size <= 12 -> 3
                    else -> 4
                }
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.cards) { card ->
                        MemoryCardItem(card = card, onClick = { viewModel.onCardClicked(card) })
                    }
                }
            }

            if (uiState.isGameOver) {
                GameOverModal(
                    title = stringResource(R.string.game_over),
                    message = stringResource(R.string.time_up),
                    onRetry = { viewModel.startGame(context, difficulty) },
                    onGoHome = onNavigateBack
                )
            } else if (uiState.isVictory) {
                GameOverModal(
                    title = stringResource(R.string.victory),
                    message = stringResource(R.string.all_pairs_found),
                    onRetry = { viewModel.startGame(context, difficulty) },
                    onGoHome = onNavigateBack
                )
            }
        }
    }
}

@Composable
fun MemoryCardItem(card: MemoryCard, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardRotation"
    )

    Card(
        modifier = Modifier
            .aspectRatio(0.75f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !card.isFlipped && !card.isMatched) { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (rotation <= 90f) {
                // Back of the card
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CatchingPokemon,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                }
            } else {
                // Front of the card
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .graphicsLayer { rotationY = 180f },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(card.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = card.name,
                        modifier = Modifier.fillMaxSize(0.8f),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            if (card.isMatched) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
fun GameOverModal(
    title: String,
    message: String,
    onRetry: () -> Unit,
    onGoHome: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            Button(onClick = onRetry) {
                Text(stringResource(R.string.play_again))
            }
        },
        dismissButton = {
            TextButton(onClick = onGoHome) {
                Text(stringResource(R.string.home))
            }
        },
        title = {
            Text(text = title, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        },
        text = {
            Text(text = message, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    )
}
