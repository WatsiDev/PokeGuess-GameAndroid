package com.watsidev.pokeguessredux.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.watsidev.pokeguessredux.R
import com.watsidev.pokeguessredux.ui.components.BannerAd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryDifficultyScreen(
    onDifficultySelected: (MemoryDifficulty) -> Unit,
    onNavigateBack: () -> Unit
) {
    val difficulties = listOf(
        Triple(MemoryDifficulty.EASY, stringResource(R.string.diff_easy), stringResource(R.string.diff_easy_desc)),
        Triple(MemoryDifficulty.NORMAL, stringResource(R.string.diff_normal), stringResource(R.string.diff_normal_desc)),
        Triple(MemoryDifficulty.HARD, stringResource(R.string.diff_hard), stringResource(R.string.diff_hard_desc)),
        Triple(MemoryDifficulty.EXPERT, stringResource(R.string.diff_expert), stringResource(R.string.diff_expert_desc)),
        Triple(MemoryDifficulty.MASTER, stringResource(R.string.diff_master), stringResource(R.string.diff_master_desc))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.difficulty_selection), fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            difficulties.forEach { (diff, title, desc) ->
                DifficultyCard(
                    title = title,
                    subtitle = desc,
                    onClick = { onDifficultySelected(diff) }
                )
            }
        }
    }
}

@Composable
fun DifficultyCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
