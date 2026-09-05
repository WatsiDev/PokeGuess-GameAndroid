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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import com.watsidev.pokeguessredux.ui.utils.AudioHelper
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.watsidev.pokeguessredux.R
import com.watsidev.pokeguessredux.data.local.DiscoveryEntity
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

    val context = LocalContext.current

    Scaffold(
        topBar = {
            val mainTypeColor = uiState.pokemon?.types?.firstOrNull()?.let { PokemonTypeColors.getColorForType(it) } ?: MaterialTheme.colorScheme.primary
            val discovery = uiState.discovery
            val hasBothVersions = discovery != null && discovery.isNormal && discovery.isShiny

            TopAppBar(
                title = { Text(stringResource(R.string.pokedex), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
                    }
                },
                actions = {
                    uiState.pokemon?.let { pokemon ->
                        IconButton(onClick = { AudioHelper.playPokemonCry(context, pokemon.id) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Play Cry",
                                tint = Color.White
                            )
                        }
                    }
                    if (hasBothVersions) {
                        IconButton(onClick = { viewModel.toggleShiny() }) {
                            Icon(
                                imageVector = if (uiState.isShowingShiny) Icons.Filled.Star else Icons.Default.StarBorder,
                                contentDescription = "Toggle Shiny Version",
                                tint = if (uiState.isShowingShiny) Color(0xFFFFD700) else Color.White
                            )
                        }
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
                    PokemonDetailContent(
                        pokemon = pokemon,
                        discovery = uiState.discovery,
                        isShowingShiny = uiState.isShowingShiny
                    )
                }
            }
        }
    }
}

@Composable
fun PokemonDetailContent(
    pokemon: Pokemon,
    discovery: DiscoveryEntity? = null,
    isShowingShiny: Boolean = false
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val mainTypeColor = PokemonTypeColors.getColorForType(pokemon.types.firstOrNull() ?: "normal")

    val onlyShinyDiscovered = discovery != null && discovery.isShiny && !discovery.isNormal
    val showShinyImage = isShowingShiny || onlyShinyDiscovered

    val displayImageUrl = if (showShinyImage) {
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/shiny/${pokemon.id}.png"
    } else {
        pokemon.imageUrl
    }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = pokemon.name.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = String.format(Locale.getDefault(), "#%03d", pokemon.id),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(displayImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = pokemon.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    if (showShinyImage) {
                        val loopComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.shining_loop))
                        LottieAnimation(
                            composition = loopComposition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Types & Badges
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

            // Special Badges (Shiny / Daily)
            if (discovery != null && (discovery.isShiny || discovery.isDaily)) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (discovery.isShiny) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF8E1),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("✨", fontSize = 12.sp)
                                Text("SHINY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color(0xFFF57F17))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (discovery.isDaily) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("📅", fontSize = 12.sp)
                                Text("DAILY CATCH", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
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

            EvolutionChainView(pokemon.evolutionChain, currentPokemonName = pokemon.name)

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

data class EvolutionNode(
    val step: EvolutionStep,
    val children: List<EvolutionNode>
)

fun isLinearTree(node: EvolutionNode): Boolean {
    if (node.children.size > 1) return false
    return node.children.all { isLinearTree(it) }
}

fun buildEvolutionTree(chain: List<EvolutionStep>): EvolutionNode? {
    if (chain.isEmpty()) return null

    val hasExplicitParentIds = chain.size > 1 && chain.drop(1).any { it.parentId != null }

    val stepsWithParentId = if (hasExplicitParentIds) {
        chain
    } else {
        inferParentIds(chain)
    }

    val rootStep = stepsWithParentId.firstOrNull { it.parentId == null } ?: stepsWithParentId.first()

    fun buildNode(step: EvolutionStep): EvolutionNode {
        val childrenSteps = stepsWithParentId.filter { it.parentId == step.id && it.id != step.id }
        val childrenNodes = childrenSteps.map { buildNode(it) }
        return EvolutionNode(step, childrenNodes)
    }

    return buildNode(rootStep)
}

private fun inferParentIds(chain: List<EvolutionStep>): List<EvolutionStep> {
    if (chain.size <= 1) return chain

    val result = mutableListOf<EvolutionStep>()
    result.add(chain[0].copy(parentId = null))

    val rootId = chain[0].id
    var currentParentId = rootId

    for (i in 1 until chain.size) {
        val step = chain[i]
        if (i == 1) {
            result.add(step.copy(parentId = rootId))
            currentParentId = step.id
        } else {
            result.add(step.copy(parentId = currentParentId))
        }
    }

    return result
}

@Composable
fun EvolutionChainView(chain: List<EvolutionStep>, currentPokemonName: String = "") {
    val tree = remember(chain) { buildEvolutionTree(chain) }

    if (tree == null) return

    if (isLinearTree(tree)) {
        LinearEvolutionChainView(chain = chain, currentPokemonName = currentPokemonName)
    } else {
        BranchingEvolutionChainView(rootNode = tree, currentPokemonName = currentPokemonName)
    }
}

@Composable
fun LinearEvolutionChainView(chain: List<EvolutionStep>, currentPokemonName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        chain.forEachIndexed { index, step ->
            val isCurrent = step.name.equals(currentPokemonName, ignoreCase = true)
            EvolutionStepCard(step = step, isCurrentPokemon = isCurrent)

            if (index < chain.size - 1) {
                val nextStep = chain[index + 1]
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (nextStep.trigger.isNotEmpty()) {
                        Text(
                            text = nextStep.trigger.replace("-", " ").replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                    if (nextStep.minLevel != null) {
                        Text(
                            text = stringResource(R.string.level_short, nextStep.minLevel),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.evolves_to),
                        modifier = Modifier
                            .size(16.dp)
                            .graphicsLayer(scaleX = -1f),
                        tint = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun BranchingEvolutionChainView(rootNode: EvolutionNode, currentPokemonName: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BranchingEvolutionNodeView(node = rootNode, currentPokemonName = currentPokemonName)
    }
}

@Composable
fun BranchingEvolutionNodeView(node: EvolutionNode, currentPokemonName: String) {
    val isCurrent = node.step.name.equals(currentPokemonName, ignoreCase = true)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EvolutionStepCard(step = node.step, isCurrentPokemon = isCurrent)

        if (node.children.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))

            if (node.children.size == 1) {
                val child = node.children[0]
                EvolutionTriggerBadge(trigger = child.step.trigger, minLevel = child.step.minLevel)
                Spacer(modifier = Modifier.height(4.dp))
                BranchingEvolutionNodeView(node = child, currentPokemonName = currentPokemonName)
            } else {
                BranchingChildrenView(children = node.children, currentPokemonName = currentPokemonName)
            }
        }
    }
}

@Composable
fun BranchingChildrenView(children: List<EvolutionNode>, currentPokemonName: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val chunks = children.chunked(3)
            chunks.forEachIndexed { rowIndex, chunk ->
                if (rowIndex > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    chunk.forEach { childNode ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            EvolutionTriggerBadge(
                                trigger = childNode.step.trigger,
                                minLevel = childNode.step.minLevel
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            BranchingEvolutionNodeView(
                                node = childNode,
                                currentPokemonName = currentPokemonName
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EvolutionStepCard(
    step: EvolutionStep,
    isCurrentPokemon: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPokemon)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentPokemon) 3.dp else 1.dp),
        modifier = modifier.padding(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(step.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = step.name,
                modifier = Modifier.size(52.dp)
            )
            Text(
                text = step.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isCurrentPokemon) FontWeight.ExtraBold else FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                fontSize = 11.sp
            )
            Text(
                text = String.format(Locale.getDefault(), "#%03d", step.id),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun EvolutionTriggerBadge(
    trigger: String,
    minLevel: Int?,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ArrowDownward,
            contentDescription = stringResource(R.string.evolves_to),
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
        if (trigger.isNotEmpty() || minLevel != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (trigger.isNotEmpty()) {
                        Text(
                            text = trigger.replace("-", " ").replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (minLevel != null) {
                        Text(
                            text = stringResource(R.string.level_short, minLevel),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
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

@Preview(showBackground = true)
@Composable
fun BranchingEvolutionPreview() {
    val chain = listOf(
        EvolutionStep(id = 43, name = "oddish", imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/43.png"),
        EvolutionStep(id = 44, name = "gloom", trigger = "level-up", minLevel = 21, parentId = 43, imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/44.png"),
        EvolutionStep(id = 45, name = "vileplume", trigger = "use-item", parentId = 44, imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/45.png"),
        EvolutionStep(id = 182, name = "bellossom", trigger = "use-item", parentId = 44, imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/182.png")
    )
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EvolutionChainView(chain = chain, currentPokemonName = "gloom")
        }
    }
}
