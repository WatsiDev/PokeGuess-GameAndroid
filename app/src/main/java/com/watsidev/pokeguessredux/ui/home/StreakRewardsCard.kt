package com.watsidev.pokeguessredux.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watsidev.pokeguessredux.R
import com.watsidev.pokeguessredux.ui.game.ShinyBonusType

@Composable
fun StreakRewardsCard(
    currentStreak: Int,
    activeBonus: ShinyBonusType,
    consumedMilestones: Set<Int>
) {
    val nextMilestone = when {
        currentStreak < 7 -> 7
        currentStreak < 14 -> 14
        currentStreak < 30 -> 30
        else -> 30
    }

    val progress = (currentStreak.toFloat() / nextMilestone.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFFF6D00).copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFFF6D00),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.streak_rewards_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentStreak >= 30) "¡Máximo nivel de racha!" else stringResource(R.string.next_reward_in, nextMilestone - currentStreak),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "$currentStreak 🔥",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF6D00)
                )
            }

            // Active Bonus Banner
            if (activeBonus != ShinyBonusType.NONE) {
                val bannerText = when (activeBonus) {
                    ShinyBonusType.DOUBLE_RATE -> stringResource(R.string.active_bonus_2x)
                    ShinyBonusType.FIFTY_PERCENT -> stringResource(R.string.active_bonus_50)
                    ShinyBonusType.GUARANTEED -> stringResource(R.string.active_bonus_100)
                    else -> ""
                }
                Surface(
                    color = Color(0xFFFFD700).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = bannerText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8F00)
                        )
                    }
                }
            }

            // Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Color(0xFFFF6D00),
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            }

            // Milestones Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MilestoneItem(
                    days = 7,
                    title = "x2 Shiny",
                    isUnlocked = currentStreak >= 7,
                    isConsumed = 7 in consumedMilestones
                )
                MilestoneItem(
                    days = 14,
                    title = "50% Shiny",
                    isUnlocked = currentStreak >= 14,
                    isConsumed = 14 in consumedMilestones
                )
                MilestoneItem(
                    days = 30,
                    title = "100% Shiny",
                    isUnlocked = currentStreak >= 30,
                    isConsumed = 30 in consumedMilestones
                )
            }
        }
    }
}

@Composable
fun MilestoneItem(
    days: Int,
    title: String,
    isUnlocked: Boolean,
    isConsumed: Boolean
) {
    val bgColor = when {
        isConsumed -> MaterialTheme.colorScheme.surfaceVariant
        isUnlocked -> Color(0xFFFFD700).copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    val iconColor = when {
        isConsumed -> MaterialTheme.colorScheme.outline
        isUnlocked -> Color(0xFFFFA000)
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isUnlocked) Icons.Default.MilitaryTech else Icons.Default.Lock,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "$days Días",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isConsumed) "Usado" else title,
                style = MaterialTheme.typography.labelSmall,
                color = if (isConsumed) MaterialTheme.colorScheme.outline else iconColor,
                fontSize = 10.sp
            )
        }
    }
}
