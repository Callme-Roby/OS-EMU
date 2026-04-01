@file:OptIn(ExperimentalMaterial3Api::class)
package com.osemu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.osemu.app.data.model.Game
import com.osemu.app.ui.components.getConsoleIcon
import com.osemu.app.ui.components.getConsoleTint
import com.osemu.app.ui.theme.LocalOsEmuExtras
import com.osemu.app.ui.theme.OsEmuColors

@Composable
fun GameDetailScreen(
    game: Game,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSetBoxArt: () -> Unit,
    onAddToCollection: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = LocalOsEmuExtras.current
    val scrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxSize()) {
        // Top bar with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            extras.topScreenGradientStart,
                            extras.topScreenGradientEnd
                        )
                    )
                )
        ) {
            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }

            // Game art / icon
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(120.dp)
                    .clip(RoundedCornerShape(20.dp)),
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
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getConsoleIcon(game.console.manufacturer),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = game.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Console badge
            Surface(
                color = getConsoleTint(game.console.manufacturer).copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = game.console.displayName,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = getConsoleTint(game.console.manufacturer)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Play button
                Button(
                    onClick = onPlay,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OsEmuColors.Green
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play")
                }

                // Favorite button
                OutlinedButton(
                    onClick = onToggleFavorite,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        if (game.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (game.favorite) OsEmuColors.Red else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Game Info", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    InfoRow("Console", game.console.displayName)
                    InfoRow("Manufacturer", game.console.manufacturer)
                    InfoRow("Region", game.region.displayName)

                    if (game.fileSize > 0) {
                        val sizeMb = game.fileSize / (1024 * 1024)
                        InfoRow("Size", "${sizeMb} MB")
                    }

                    if (game.totalPlayTimeMs > 0) {
                        val hours = game.totalPlayTimeMs / 3_600_000
                        val minutes = (game.totalPlayTimeMs % 3_600_000) / 60_000
                        InfoRow("Play Time", "${hours}h ${minutes}m")
                    }

                    if (game.lastPlayed != null) {
                        val daysAgo = (System.currentTimeMillis() - game.lastPlayed) / 86_400_000
                        InfoRow("Last Played", when {
                            daysAgo == 0L -> "Today"
                            daysAgo == 1L -> "Yesterday"
                            else -> "$daysAgo days ago"
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Set box art
                    TextButton(
                        onClick = onSetBoxArt,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Set Custom Box Art")
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(18.dp))
                    }

                    // Add to collection
                    TextButton(
                        onClick = onAddToCollection,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Collection")
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}
