@file:OptIn(ExperimentalMaterial3Api::class)
package com.osemu.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osemu.app.data.model.Badge
import com.osemu.app.data.model.BadgeCategory
import com.osemu.app.data.model.BadgeRarity
import com.osemu.app.ui.theme.LocalOsEmuExtras
import com.osemu.app.ui.theme.OsEmuColors

@Composable
fun BadgeScreen(
    badges: List<Badge>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = LocalOsEmuExtras.current
    var selectedCategory by remember { mutableStateOf<BadgeCategory?>(null) }
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }

    val filteredBadges = if (selectedCategory != null) {
        badges.filter { it.category == selectedCategory }
    } else badges

    val unlockedCount = badges.count { it.isUnlocked }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            extras.topScreenGradientStart,
                            extras.topScreenGradientEnd
                        )
                    )
                )
        ) {
            Column {
                TopAppBar(
                    title = { Text("Badge Arcade") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )

                // Progress bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$unlockedCount / ${badges.size} badges",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${(unlockedCount * 100 / badges.size.coerceAtLeast(1))}%",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = unlockedCount.toFloat() / badges.size.coerceAtLeast(1),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = OsEmuColors.Yellow,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Category filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CategoryChip("All", selectedCategory == null) { selectedCategory = null }
            BadgeCategory.entries.take(4).forEach { category ->
                CategoryChip(
                    "${category.emoji} ${category.displayName}",
                    selectedCategory == category
                ) {
                    selectedCategory = if (selectedCategory == category) null else category
                }
            }
        }

        // Badge grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredBadges) { badge ->
                BadgeCard(
                    badge = badge,
                    onClick = { selectedBadge = badge }
                )
            }
        }
    }

    // Badge detail dialog
    selectedBadge?.let { badge ->
        BadgeDetailDialog(
            badge = badge,
            onDismiss = { selectedBadge = null }
        )
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            fontSize = 11.sp,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BadgeCard(
    badge: Badge,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (badge.isUnlocked) {
            rarityColor(badge.rarity).copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        label = "badgeBg"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (badge.isUnlocked) 1f else 0.5f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Emoji icon
            Text(
                text = if (badge.isUnlocked) badge.iconEmoji else "?",
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = badge.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 13.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Rarity stars
            Text(
                text = "\u2605".repeat(badge.rarity.stars),
                fontSize = 8.sp,
                color = rarityColor(badge.rarity)
            )
        }
    }
}

@Composable
private fun BadgeDetailDialog(
    badge: Badge,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(badge.iconEmoji, fontSize = 40.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(badge.name)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = badge.description,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Rarity
                Surface(
                    color = rarityColor(badge.rarity).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${"⭐".repeat(badge.rarity.stars)} ${badge.rarity.displayName}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = rarityColor(badge.rarity)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${badge.category.emoji} ${badge.category.displayName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                if (badge.isUnlocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Unlocked!",
                        color = OsEmuColors.Green,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}

private fun rarityColor(rarity: BadgeRarity): Color = when (rarity) {
    BadgeRarity.COMMON -> Color(0xFF9E9E9E)
    BadgeRarity.UNCOMMON -> Color(0xFF4CAF50)
    BadgeRarity.RARE -> Color(0xFF2196F3)
    BadgeRarity.EPIC -> Color(0xFF9C27B0)
    BadgeRarity.LEGENDARY -> Color(0xFFFF9800)
}
