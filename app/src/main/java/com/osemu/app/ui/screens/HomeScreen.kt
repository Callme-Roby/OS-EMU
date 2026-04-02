@file:OptIn(ExperimentalFoundationApi::class)
package com.osemu.app.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.osemu.app.data.model.Badge
import com.osemu.app.data.model.Game
import com.osemu.app.ui.components.*
import com.osemu.app.ui.theme.LocalOsEmuExtras
import com.osemu.app.ui.theme.OsEmuColors

/**
 * Wii U-style home screen with dual-screen layout.
 *
 * PORTRAIT: Top screen (game banner) + bottom screen (icon grid)
 * LANDSCAPE: Bottom screen only, full width
 *
 * Top screen: shows selected game's banner/art, save slots bottom-left,
 *   action buttons bottom-right (Select/Back/Menu/Details)
 * Bottom screen: icon grid (games + decorative icons), top bar with
 *   nav icons, profile avatar top-right, Edit button
 */
@Composable
fun HomeScreen(
    recentGames: List<Game>,
    favoriteGames: List<Game>,
    allGames: List<Game>,
    totalGameCount: Int,
    selectedGameIndex: Int?,
    onGameSelected: (Int) -> Unit,
    onGameLaunched: (Game) -> Unit,
    onGameDetail: (Game) -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToThemes: () -> Unit,
    onNavigateToCollections: () -> Unit,
    onNavigateToBadges: () -> Unit,
    onNavigateToMedia: () -> Unit,
    onScanGames: () -> Unit,
    onPickFolder: () -> Unit,
    isScanning: Boolean,
    scanStatus: String,
    badges: List<Badge>,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Grid rows config (1-4)
    var gridRows by remember { mutableIntStateOf(4) }
    var isEditMode by remember { mutableStateOf(false) }

    // Selected game for top screen display
    val selectedGame = remember(selectedGameIndex, allGames) {
        selectedGameIndex?.let { idx ->
            if (idx < allGames.size) allGames[idx] else null
        } ?: recentGames.firstOrNull()
    }

    // Currently playing game (takes 2 cells)
    val currentlyPlaying = recentGames.firstOrNull()

    if (isLandscape) {
        // LANDSCAPE: Bottom screen only, full width
        BottomScreen(
            allGames = allGames,
            currentlyPlaying = currentlyPlaying,
            gridRows = gridRows,
            isEditMode = isEditMode,
            onGridRowsChange = { gridRows = it },
            onEditToggle = { isEditMode = !isEditMode },
            onGameSelected = { game ->
                val idx = allGames.indexOf(game)
                if (idx >= 0) onGameSelected(idx)
            },
            onGameLaunched = onGameLaunched,
            onGameDetail = onGameDetail,
            onNavigateToLibrary = onNavigateToLibrary,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToThemes = onNavigateToThemes,
            onNavigateToCollections = onNavigateToCollections,
            onNavigateToBadges = onNavigateToBadges,
            onNavigateToMedia = onNavigateToMedia,
            onScanGames = onScanGames,
            onPickFolder = onPickFolder,
            isScanning = isScanning,
            scanStatus = scanStatus,
            modifier = modifier
        )
    } else {
        // PORTRAIT: Top screen + Hinge + Bottom screen
        Column(modifier = modifier.fillMaxSize()) {
            // === TOP SCREEN ===
            TopScreen(
                selectedGame = selectedGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.38f)
            )

            // === HINGE ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(OsEmuColors.HingeDark, OsEmuColors.HingeLight, OsEmuColors.HingeDark)
                        )
                    )
            )

            // === BOTTOM SCREEN ===
            BottomScreen(
                allGames = allGames,
                currentlyPlaying = currentlyPlaying,
                gridRows = gridRows,
                isEditMode = isEditMode,
                onGridRowsChange = { gridRows = it },
                onEditToggle = { isEditMode = !isEditMode },
                onGameSelected = { game ->
                    val idx = allGames.indexOf(game)
                    if (idx >= 0) onGameSelected(idx)
                },
                onGameLaunched = onGameLaunched,
                onGameDetail = onGameDetail,
                onNavigateToLibrary = onNavigateToLibrary,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToThemes = onNavigateToThemes,
                onNavigateToCollections = onNavigateToCollections,
                onNavigateToBadges = onNavigateToBadges,
                onNavigateToMedia = onNavigateToMedia,
                onScanGames = onScanGames,
                onPickFolder = onPickFolder,
                isScanning = isScanning,
                scanStatus = scanStatus,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.62f)
            )
        }
    }
}

// ==================== TOP SCREEN ====================

@Composable
private fun TopScreen(
    selectedGame: Game?,
    modifier: Modifier = Modifier
) {
    val extras = LocalOsEmuExtras.current

    Box(
        modifier = modifier.background(Color.Black)
    ) {
        if (selectedGame != null) {
            // Game banner/box art as background
            if (selectedGame.boxArtPath != null) {
                AsyncImage(
                    model = selectedGame.boxArtPath,
                    contentDescription = selectedGame.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Gradient placeholder with game title
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    extras.topScreenGradientStart,
                                    extras.topScreenGradientEnd
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            getConsoleIcon(selectedGame.console.manufacturer),
                            null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedGame.title,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedGame.console.displayName,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Top bar overlay: "+ X more" left, time/date/battery right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LT button area
                Surface(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "LT",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Clock & battery
                StatusBar3DS()
            }

            // Bottom-left: Save slot selector (Media preview)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(width = 100.dp, height = 60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Image,
                                null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                            Text("Media", color = Color.White, fontSize = 9.sp)
                        }
                    }
                }
            }

            // Bottom-right: Action labels
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                WiiUActionLabel("\u24B6", "Select") // Ⓐ
                WiiUActionLabel("\u24B7", "Back")   // Ⓑ
                WiiUActionLabel("\u24CD", "Menu")   // ⓧ (approximation)
                WiiUActionLabel("\u24CE", "Details") // ⓨ
            }

            // Bottom-center: Achievements counter
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mii icon placeholder
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Text("Achievements", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
            }
        } else {
            // No game selected - welcome screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                extras.topScreenGradientStart,
                                extras.topScreenGradientEnd
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("OS-EMU", style = MaterialTheme.typography.headlineMedium,
                        color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Select a game below", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun WiiUActionLabel(symbol: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(symbol, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}

// ==================== BOTTOM SCREEN ====================

@Composable
private fun BottomScreen(
    allGames: List<Game>,
    currentlyPlaying: Game?,
    gridRows: Int,
    isEditMode: Boolean,
    onGridRowsChange: (Int) -> Unit,
    onEditToggle: () -> Unit,
    onGameSelected: (Game) -> Unit,
    onGameLaunched: (Game) -> Unit,
    onGameDetail: (Game) -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToThemes: () -> Unit,
    onNavigateToCollections: () -> Unit,
    onNavigateToBadges: () -> Unit,
    onNavigateToMedia: () -> Unit,
    onScanGames: () -> Unit,
    onPickFolder: () -> Unit,
    isScanning: Boolean,
    scanStatus: String,
    modifier: Modifier = Modifier
) {
    val extras = LocalOsEmuExtras.current

    Column(
        modifier = modifier.background(extras.bottomScreenBackground)
    ) {
        // === TOP NAV BAR (Wii U style) ===
        WiiUNavBar(
            gridRows = gridRows,
            onGridRowsChange = onGridRowsChange,
            onNavigateToLibrary = onNavigateToLibrary,
            onNavigateToCollections = onNavigateToCollections,
            onNavigateToBadges = onNavigateToBadges,
            onNavigateToMedia = onNavigateToMedia,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToThemes = onNavigateToThemes
        )

        // === EDIT BUTTON ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                onClick = onEditToggle,
                color = if (isEditMode) OsEmuColors.Blue500.copy(alpha = 0.15f)
                else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Edit", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    Icon(Icons.Default.Edit, null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            }
        }

        // === ICON GRID ===
        if (allGames.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No games yet", style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onScanGames, shape = RoundedCornerShape(10.dp)) {
                            Icon(Icons.Default.Search, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Scan", fontSize = 12.sp)
                        }
                        Button(onClick = onPickFolder, shape = RoundedCornerShape(10.dp)) {
                            Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add ROMs", fontSize = 12.sp)
                        }
                    }
                    if (isScanning || scanStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(scanStatus.ifEmpty { "Scanning..." },
                            fontSize = 11.sp, color = OsEmuColors.Blue500)
                    }
                }
            }
        } else {
            // Build grid items: currently playing (2 cells) + games + decorative icons
            val gridItems = buildGridItems(allGames, currentlyPlaying)
            val columns = gridRows.coerceIn(1, 5)

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(gridItems, key = { it.id }) { item ->
                    when (item) {
                        is GridItem.GameIcon -> {
                            WiiUGameTile(
                                game = item.game,
                                isCurrentlyPlaying = item.isCurrentlyPlaying,
                                onClick = {
                                    onGameSelected(item.game)
                                    onGameLaunched(item.game)
                                },
                                onLongClick = { onGameDetail(item.game) }
                            )
                        }
                        is GridItem.DecoIcon -> {
                            WiiUDecoTile(
                                icon = item.icon,
                                label = item.label,
                                color = item.color,
                                onClick = item.onClick
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== WII U NAV BAR ====================

@Composable
private fun WiiUNavBar(
    gridRows: Int,
    onGridRowsChange: (Int) -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToCollections: () -> Unit,
    onNavigateToBadges: () -> Unit,
    onNavigateToMedia: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToThemes: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LB button
        Surface(
            color = Color.Gray.copy(alpha = 0.15f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.clickable {
                onGridRowsChange((gridRows - 1).coerceIn(1, 5))
            }
        ) {
            Text("LB", fontSize = 9.sp, color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Nav icons row
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WiiUNavIcon(Icons.Default.Home, "Home", Color(0xFF4FC3F7), onNavigateToLibrary)
            WiiUNavIcon(Icons.Default.Gamepad, "Games", Color(0xFF78909C), onNavigateToCollections)
            WiiUNavIcon(Icons.Default.EmojiEvents, "Trophies", Color(0xFFFFB300), onNavigateToBadges)
            WiiUNavIcon(Icons.Default.Group, "Social", Color(0xFF66BB6A), {})
            WiiUNavIcon(Icons.Default.PlayCircle, "Play", Color(0xFFEF5350), onNavigateToMedia)
            WiiUNavIcon(Icons.Default.GridView, "Grid", Color(0xFF7E57C2), onNavigateToThemes)
            WiiUNavIcon(Icons.Default.Settings, "Settings", Color(0xFF8D6E63), onNavigateToSettings)
        }

        Spacer(modifier = Modifier.width(6.dp))

        // RB button
        Surface(
            color = Color.Gray.copy(alpha = 0.15f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.clickable {
                onGridRowsChange((gridRows + 1).coerceIn(1, 5))
            }
        ) {
            Text("RB", fontSize = 9.sp, color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Profile avatar (top-right)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(OsEmuColors.Red.copy(alpha = 0.15f))
                .border(1.dp, OsEmuColors.Red.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null,
                tint = OsEmuColors.Red, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun WiiUNavIcon(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(32.dp)
    ) {
        Icon(icon, label, tint = tint, modifier = Modifier.size(20.dp))
    }
}

// ==================== GRID TILES ====================

@Composable
private fun WiiUGameTile(
    game: Game,
    isCurrentlyPlaying: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(2.dp, shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = shape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (game.boxArtPath != null) {
                AsyncImage(
                    model = game.boxArtPath,
                    contentDescription = game.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Colored gradient with console icon
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    getConsoleTint(game.console.manufacturer).copy(alpha = 0.15f),
                                    getConsoleTint(game.console.manufacturer).copy(alpha = 0.05f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            getConsoleIcon(game.console.manufacturer),
                            null,
                            tint = getConsoleTint(game.console.manufacturer),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = game.title,
                            fontSize = 9.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            lineHeight = 11.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            // "Jump back in!" badge for currently playing
            if (isCurrentlyPlaying) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp,
                        topStart = 0.dp, topEnd = 0.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("\u24B6", color = Color.White, fontSize = 10.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Jump back in!", color = Color.White, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun WiiUDecoTile(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(1.dp, shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, label, tint = color, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(2.dp))
                Text(label, fontSize = 8.sp, color = color.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center, maxLines = 1)
            }
        }
    }
}

// ==================== GRID ITEM MODEL ====================

private sealed class GridItem(val id: String) {
    data class GameIcon(
        val game: Game,
        val isCurrentlyPlaying: Boolean
    ) : GridItem("game_${game.id}")

    data class DecoIcon(
        val icon: ImageVector,
        val label: String,
        val color: Color,
        val onClick: () -> Unit
    ) : GridItem("deco_$label")
}

/**
 * Builds the grid with: currently playing game first, then other games,
 * then decorative system icons (Shop, Mii, etc.)
 */
private fun buildGridItems(
    allGames: List<Game>,
    currentlyPlaying: Game?
): List<GridItem> {
    val items = mutableListOf<GridItem>()

    // Currently playing game first (will take 2 cells visual weight)
    if (currentlyPlaying != null) {
        items.add(GridItem.GameIcon(currentlyPlaying, isCurrentlyPlaying = true))
    }

    // Other games
    allGames.filter { it.id != currentlyPlaying?.id }.forEach { game ->
        items.add(GridItem.GameIcon(game, isCurrentlyPlaying = false))
    }

    // Decorative system icons (non-functional, like Wii U)
    items.add(GridItem.DecoIcon(Icons.Default.ShoppingCart, "Shop", Color(0xFFFF9800)) {})
    items.add(GridItem.DecoIcon(Icons.Default.Download, "Updates", Color(0xFF29B6F6)) {})
    items.add(GridItem.DecoIcon(Icons.Default.Person, "Mii", Color(0xFF4CAF50)) {})
    items.add(GridItem.DecoIcon(Icons.Default.Tv, "TV", Color(0xFF78909C)) {})
    items.add(GridItem.DecoIcon(Icons.Default.Gamepad, "Controller", Color(0xFF5C6BC0)) {})

    return items
}
