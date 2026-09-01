package com.watsidev.pokeguessredux.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(difficulties) { (diff, title, desc) ->
                val (color, icon) = when (diff) {
                    MemoryDifficulty.EASY -> Color(0xFF81C784) to Icons.Default.Star
                    MemoryDifficulty.NORMAL -> Color(0xFF64B5F6) to Icons.Default.Psychology
                    MemoryDifficulty.HARD -> Color(0xFFFFB74D) to Icons.Default.Psychology
                    MemoryDifficulty.EXPERT -> Color(0xFFE57373) to Icons.Default.Psychology
                    MemoryDifficulty.MASTER -> Color(0xFFBA68C8) to Icons.Default.Psychology
                }

                DifficultyCard(
                    title = title,
                    subtitle = desc,
                    color = color,
                    icon = icon,
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
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
                color = color.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = color
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
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
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(20.dp).graphicsLayer(scaleX = -1f),
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
