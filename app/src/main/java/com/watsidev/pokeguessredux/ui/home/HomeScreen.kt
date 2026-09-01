package com.watsidev.pokeguessredux.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watsidev.pokeguessredux.BuildConfig
import com.watsidev.pokeguessredux.R
import com.watsidev.pokeguessredux.ui.components.BannerAd
import com.watsidev.pokeguessredux.ui.utils.BottomCurvedShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDaily: () -> Unit,
    onNavigateToInfinite: () -> Unit,
    onNavigateToGenerations: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToPokedex: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.home_title), 
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Curve
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(BottomCurvedShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Master your Pokémon knowledge",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Game Modes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                HomeCard(
                    title = stringResource(R.string.daily_challenge),
                    subtitle = stringResource(R.string.daily_challenge_subtitle),
                    icon = Icons.Default.Today,
                    onClick = onNavigateToDaily
                )
                HomeCard(
                    title = stringResource(R.string.infinite_mode),
                    subtitle = stringResource(R.string.infinite_mode_subtitle),
                    icon = Icons.Default.AllInclusive,
                    onClick = onNavigateToInfinite
                )
                HomeCard(
                    title = stringResource(R.string.generations_mode),
                    subtitle = stringResource(R.string.generations_subtitle),
                    icon = Icons.Default.CatchingPokemon,
                    onClick = onNavigateToGenerations
                )
                HomeCard(
                    title = stringResource(R.string.memory_mode),
                    subtitle = stringResource(R.string.memory_subtitle),
                    icon = Icons.Default.Memory,
                    onClick = onNavigateToMemory
                )

                Text(
                    text = "Tools",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                HomeCard(
                    title = stringResource(R.string.pokedex),
                    subtitle = stringResource(R.string.pokedex_subtitle),
                    icon = Icons.Default.Search,
                    onClick = onNavigateToPokedex
                )
                HomeCard(
                    title = stringResource(R.string.settings),
                    subtitle = stringResource(R.string.settings_subtitle),
                    icon = Icons.Default.Settings,
                    onClick = onNavigateToSettings
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = stringResource(R.string.version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                
                BannerAd(adUnitId = "ca-app-pub-3940256099942544/6300978111")
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun HomeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen({}, {}, {}, {}, {}, {})
    }
}
