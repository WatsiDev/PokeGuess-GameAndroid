package com.watsidev.pokeguessredux.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.watsidev.pokeguessredux.R
import com.watsidev.pokeguessredux.ui.components.BannerAd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerationScreen(
    onGenerationSelected: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.generation_selection), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        bottomBar = {
            BannerAd(adUnitId = "ca-app-pub-3940256099942544/6300978111")
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items((1..9).toList()) { gen ->
                GenerationCard(
                    generation = gen,
                    onClick = { onGenerationSelected(gen) }
                )
            }
        }
    }
}

@Composable
fun GenerationCard(
    generation: Int,
    onClick: () -> Unit
) {
    val (pokemonId, color) = when (generation) {
        1 -> 25 to Color(0xFF4FC3F7) // Pikachu (Blue-ish)
        2 -> 152 to Color(0xFF81C784) // Chikorita (Green)
        3 -> 255 to Color(0xFFFF8A65) // Torchic (Orange)
        4 -> 387 to Color(0xFF9575CD) // Turtwig (Purple)
        5 -> 495 to Color(0xFF4DB6AC) // Snivy (Teal)
        6 -> 650 to Color(0xFFF06292) // Chespin (Pink)
        7 -> 722 to Color(0xFFAED581) // Rowlet (Lime)
        8 -> 810 to Color(0xFFFFD54F) // Grookey (Yellow)
        9 -> 906 to Color(0xFF64B5F6) // Sprigatito (Light Blue)
        else -> 1 to Color.Gray
    }

    val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$pokemonId.png"

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.7f), color)
                    )
                )
        ) {
            // Pokéball background decoration (simplified)
            Surface(
                color = Color.White.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = 80.dp, y = 40.dp)
            ) {}

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GEN",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = generation.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(90.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
