@file:OptIn(ExperimentalMaterial3Api::class)
package com.osemu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.osemu.app.data.model.Game
import com.osemu.app.ui.theme.OsEmuColors

/**
 * Wii U-style widget card for the home screen.
 */
@Composable
fun WidgetCard(
    title: String,
    icon: ImageVector,
    accentColor: Color = OsEmuColors.Blue500,
    onSeeAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (onSeeAll != null) {
                    TextButton(
                        onClick = onSeeAll,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("See all", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            content()
        }
    }
}

/**
 * Horizontal scrolling game carousel for widgets.
 */
@Composable
fun GameCarousel(
    games: List<Game>,
    onGameClick: (Game) -> Unit,
    modifier: Modifier = Modifier
) {
    if (games.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No games yet",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    } else {
        LazyRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(games) { game ->
                GameCarouselItem(game = game, onClick = { onGameClick(game) })
            }
        }
    }
}

@Composable
private fun GameCarouselItem(
    game: Game,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Box art or icon
        Card(
            modifier = Modifier.size(72.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (game.boxArtPath != null) {
                    AsyncImage(
                        model = game.boxArtPath,
                        contentDescription = game.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = consoleGradient(game.console.manufacturer)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getConsoleIcon(game.console.manufacturer),
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = game.title,
            fontSize = 10.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Play stats widget content.
 */
@Composable
fun StatsWidgetContent(
    totalGames: Int,
    totalPlayTimeMs: Long,
    favoriteCount: Int,
    consolesUsed: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem("Games", totalGames.toString(), OsEmuColors.Blue500)
        StatItem("Played", formatPlayTime(totalPlayTimeMs), OsEmuColors.Green)
        StatItem("Favorites", favoriteCount.toString(), OsEmuColors.Red)
        StatItem("Consoles", consolesUsed.toString(), OsEmuColors.Purple)
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

/**
 * Quick launch grid for system icons.
 */
@Composable
fun QuickLaunchGrid(
    onLibrary: () -> Unit,
    onCollections: () -> Unit,
    onBadges: () -> Unit,
    onMedia: () -> Unit,
    onThemes: () -> Unit,
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickLaunchItem("Library", Icons.Default.Folder, OsEmuColors.Orange, onLibrary)
        QuickLaunchItem("Collections", Icons.Default.List, OsEmuColors.Blue500, onCollections)
        QuickLaunchItem("Badges", Icons.Default.Star, OsEmuColors.Yellow, onBadges)
        QuickLaunchItem("Media", Icons.Default.Image, OsEmuColors.Pink, onMedia)
        QuickLaunchItem("Themes", Icons.Default.Palette, OsEmuColors.Purple, onThemes)
        QuickLaunchItem("Settings", Icons.Default.Build, OsEmuColors.Gray500, onSettings)
    }
}

@Composable
private fun QuickLaunchItem(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

private fun consoleGradient(manufacturer: String): List<Color> = when (manufacturer) {
    "Nintendo" -> listOf(Color(0xFFE53935), Color(0xFFC62828))
    "Sega" -> listOf(Color(0xFF1E88E5), Color(0xFF1565C0))
    "Sony" -> listOf(Color(0xFF5E35B1), Color(0xFF4527A0))
    else -> listOf(Color(0xFF757575), Color(0xFF616161))
}

private fun formatPlayTime(ms: Long): String {
    val hours = ms / 3_600_000
    val minutes = (ms % 3_600_000) / 60_000
    return if (hours > 0) "${hours}h" else "${minutes}m"
}
