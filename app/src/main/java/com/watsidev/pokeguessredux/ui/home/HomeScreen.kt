package com.watsidev.pokeguessredux.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watsidev.pokeguessredux.BuildConfig
import com.watsidev.pokeguessredux.R
import com.watsidev.pokeguessredux.ui.components.BannerAd

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
                title = { Text(stringResource(R.string.home_title), fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                icon = Icons.Default.CatchingPokemon,
                onClick = onNavigateToMemory
            )
            HomeCard(
                title = stringResource(R.string.pokedex),
                subtitle = stringResource(R.string.pokedex_subtitle),
                icon = Icons.Default.CatchingPokemon,
                onClick = onNavigateToPokedex
            )
            HomeCard(
                title = stringResource(R.string.settings),
                subtitle = stringResource(R.string.settings_subtitle),
                icon = Icons.Default.Settings,
                onClick = onNavigateToSettings
            )
            
            Spacer(modifier = Modifier.weight(1.0f))
            
            Text(
                text = stringResource(R.string.version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            
            BannerAd(adUnitId = "ca-app-pub-3940256099942544/6300978111")
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
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
