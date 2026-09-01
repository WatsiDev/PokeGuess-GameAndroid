package com.watsidev.pokeguessredux.ui.pokedex

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.watsidev.pokeguessredux.R
import coil.request.ImageRequest
import com.watsidev.pokeguessredux.ui.components.BannerAd
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokedexScreen(
    viewModel: PokedexViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onPokemonClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagingItems = viewModel.pokemonPagingData.collectAsLazyPagingItems()
    
    LaunchedEffect(uiState.discoveredIds.size) {
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.pokedex_title), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            BannerAd(adUnitId = "ca-app-pub-3940256099942544/6300978111")
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Column(
                    modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { /* ViewModel handles initial list */ }) {
                        Text("Retry")
                    }
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    modifier = Modifier.padding(innerPadding).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        DiscoveryProgress(
                            discovered = uiState.discoveredIds.size,
                            total = uiState.totalPokemonCount
                        )
                    }

                    items(
                        count = pagingItems.itemCount,
                        key = { index -> pagingItems[index] ?: "loading_$index" }
                    ) { index ->
                        val name = pagingItems[index]
                        if (name != null) {
                            val id = index + 1
                            val isDiscovered = uiState.discoveredIds.contains(id)
                            PokedexEntry(
                                id = id, 
                                name = name, 
                                isDiscovered = isDiscovered,
                                onClick = { onPokemonClick(name) }
                            )
                        } else {
                            // Placeholder while loading
                            Box(modifier = Modifier.aspectRatio(1f).background(Color.LightGray.copy(alpha = 0.1f)))
                        }
                    }

                    // Handle Loading/Error states at the bottom
                    pagingItems.apply {
                        when {
                            loadState.append is LoadState.Loading -> {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                    }
                                }
                            }
                            loadState.append is LoadState.Error -> {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Text(stringResource(R.string.error_loading_more), color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoveryProgress(discovered: Int, total: Int) {
    val progress = if (total > 0) discovered.toFloat() / total else 0f
    
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.discovery_progress),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "$discovered / $total",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer
        )
        Text(
            text = stringResource(R.string.complete_percent, (progress * 100).toInt()),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
        )
    }
}

@Composable
fun PokedexEntry(id: Int, name: String, isDiscovered: Boolean, onClick: () -> Unit) {
    // Official artwork URL construction
    val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
    
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(enabled = isDiscovered, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDiscovered) MaterialTheme.colorScheme.surfaceVariant else Color.LightGray.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDiscovered) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = String.format(Locale.getDefault(), "#%03d", id),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDiscovered) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.align(Alignment.TopStart)
                )
                
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = if (isDiscovered) name else stringResource(R.string.undiscovered),
                    modifier = Modifier.size(64.dp),
                    colorFilter = if (!isDiscovered) ColorFilter.tint(Color.Black) else null,
                    alpha = if (isDiscovered) 1f else 0.5f
                )
            }
            
            if (isDiscovered) {
                Text(
                    text = name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    fontSize = 10.sp
                )
            } else {
                Text(
                    text = "???",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
        }
    }
}
