package com.watsidev.pokeguessredux.ui.pokedex

import android.app.ProgressDialog.show
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.watsidev.pokeguessredux.R
import com.watsidev.pokeguessredux.data.model.Pokemon
import com.watsidev.pokeguessredux.ui.theme.PokemonTypeColors
import com.watsidev.pokeguessredux.data.model.EvolutionStep
import java.util.*
import com.watsidev.pokeguessredux.ui.utils.BottomCurvedShape
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    pokemonName: String,
    viewModel: PokemonDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(pokemonName) {
        viewModel.loadPokemon(pokemonName)
    }

    Scaffold(
        topBar = {
            val mainTypeColor = uiState.pokemon?.types?.firstOrNull()?.let { PokemonTypeColors.getColorForType(it) } ?: MaterialTheme.colorScheme.primary
            TopAppBar(
                title = { Text(stringResource(R.string.pokedex), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Favorite */ }) {
                        Icon(Icons.Default.StarBorder, contentDescription = stringResource(R.string.favorite), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = mainTypeColor
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                uiState.pokemon?.let { pokemon ->
                    PokemonDetailContent(pokemon)
                }
            }
        }
    }
}

@Composable
fun PokemonDetailContent(pokemon: Pokemon) {
    val scrollState = rememberScrollState()
    val mainTypeColor = PokemonTypeColors.getColorForType(pokemon.types.firstOrNull() ?: "normal")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Header with Image and Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(shape = BottomCurvedShape)
                .background(mainTypeColor)
        ) {


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pokemon.name.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "#%03d", pokemon.id),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pokemon.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = pokemon.name,
                    modifier = Modifier
                        .size(220.dp)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Types
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                pokemon.types.forEach { type ->
                    TypeBadge(type = type)
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Summary (Category, Height, Weight)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoItem(label = stringResource(R.string.category), value = pokemon.category)
                VerticalDivider(modifier = Modifier.height(40.dp))
                InfoItem(label = stringResource(R.string.height), value = String.format(Locale.getDefault(), "%.1f m", pokemon.height / 10f))
                VerticalDivider(modifier = Modifier.height(40.dp))
                InfoItem(label = stringResource(R.string.weight), value = String.format(Locale.getDefault(), "%.1f kg", pokemon.weight / 10f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Evolution Section
            Text(
                text = stringResource(R.string.evolution),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = mainTypeColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            EvolutionChainView(pokemon.evolutionChain)

            Spacer(modifier = Modifier.height(32.dp))

            // Base Stats
            Text(
                text = stringResource(R.string.base_stats),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = mainTypeColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            pokemon.stats.forEach { stat ->
                StatRow(statName = stat.name, statValue = stat.value, maxStat = 255, color = mainTypeColor)
            }
            
            val totalStats = pokemon.stats.sumOf { it.value }
            StatRow(statName = stringResource(R.string.stat_total), statValue = totalStats, maxStat = 700, color = mainTypeColor, isBold = true)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TypeBadge(type: String) {
    Surface(
        color = PokemonTypeColors.getColorForType(type),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = type.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun EvolutionChainView(chain: List<EvolutionStep>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        chain.forEachIndexed { index, step ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(step.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = step.name,
                    modifier = Modifier.size(60.dp)
                )
                Text(text = step.name.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium)
                Text(text = String.format(Locale.getDefault(), "#%03d", step.id), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            
            if (index < chain.size - 1) {
                val nextStep = chain[index + 1]
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = nextStep.trigger.replace("-", " ").replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    if (nextStep.minLevel != null) {
                        Text(text = stringResource(R.string.level_short, nextStep.minLevel), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Should be ArrowForward but using back flipped
                        contentDescription = stringResource(R.string.evolves_to),
                        modifier = Modifier.size(16.dp).graphicsLayer(scaleX = -1f),
                        tint = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun StatRow(statName: String, statValue: Int, maxStat: Int, color: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = statName.uppercase().replace("SPECIAL-ATTACK", "ATK.SP").replace("SPECIAL-DEFENSE", "DEF.SP").replace("SPEED", "SPD"),
            modifier = Modifier.width(60.dp),
            style = if (isBold) MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.labelMedium
        )
        Text(
            text = statValue.toString(),
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { statValue.toFloat() / maxStat },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = Color.LightGray.copy(alpha = 0.3f)
        )
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ShowPreviewPokemonDetailScreen() {
    PokemonDetailContent(
        pokemon = Pokemon(
            id = 94,
            name = "Gengar",
            height = 12,
            weight = 12,
            types = listOf("poison", "ghost"),
            evolutionaryStage = 3,
            generation = 1,
            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/94.png",
            category = "Shadow",
        )
    )
}
