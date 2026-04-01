package com.osemu.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osemu.app.data.model.Game
import com.osemu.app.data.model.IconStyle
import com.osemu.app.ui.components.*
import com.osemu.app.ui.theme.LocalOsEmuExtras
import com.osemu.app.ui.theme.OsEmuColors

/**
 * Main home screen with 3DS-inspired dual-panel layout.
 */
@Composable
fun HomeScreen(
    recentGames: List<Game>,
    favoriteGames: List<Game>,
    totalGameCount: Int,
    selectedGameIndex: Int?,
    onGameSelected: (Int) -> Unit,
    onGameLaunched: (Game) -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToThemes: () -> Unit,
    onScanGames: () -> Unit,
    onPickFolder: () -> Unit,
    isScanning: Boolean,
    scanStatus: String,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val extras = LocalOsEmuExtras.current

    val selectedGame = selectedGameIndex?.let {
        if (it < recentGames.size) recentGames[it] else null
    }

    if (isLandscape) {
        Row(modifier = modifier.fillMaxSize()) {
            TopScreenPanel(
                selectedGame = selectedGame,
                totalGameCount = totalGameCount,
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight()
            )
            BottomScreenPanel(
                recentGames = recentGames,
                favoriteGames = favoriteGames,
                selectedGameIndex = selectedGameIndex,
                onGameSelected = onGameSelected,
                onGameLaunched = onGameLaunched,
                onNavigateToLibrary = onNavigateToLibrary,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToThemes = onNavigateToThemes,
                onScanGames = onScanGames,
                onPickFolder = onPickFolder,
                isScanning = isScanning,
                scanStatus = scanStatus,
                isLandscape = true,
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight()
            )
        }
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            TopScreenPanel(
                selectedGame = selectedGame,
                totalGameCount = totalGameCount,
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxWidth()
            )

            HingeDivider()

            BottomScreenPanel(
                recentGames = recentGames,
                favoriteGames = favoriteGames,
                selectedGameIndex = selectedGameIndex,
                onGameSelected = onGameSelected,
                onGameLaunched = onGameLaunched,
                onNavigateToLibrary = onNavigateToLibrary,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToThemes = onNavigateToThemes,
                onScanGames = onScanGames,
                onPickFolder = onPickFolder,
                isScanning = isScanning,
                scanStatus = scanStatus,
                isLandscape = false,
                modifier = Modifier
                    .weight(0.62f)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun HingeDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        OsEmuColors.HingeDark,
                        OsEmuColors.HingeLight,
                        OsEmuColors.HingeDark
                    )
                )
            )
    )
}

/**
 * Top screen panel - blue gradient background with game info or branding.
 */
@Composable
private fun TopScreenPanel(
    selectedGame: Game?,
    totalGameCount: Int,
    modifier: Modifier = Modifier
) {
    val extras = LocalOsEmuExtras.current

    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        extras.topScreenGradientStart,
                        extras.topScreenGradientEnd
                    )
                )
            ),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status bar at very top
        StatusBar3DS()

        // Main content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = selectedGame,
                transitionSpec = {
                    fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
                },
                label = "topScreenContent"
            ) { game ->
                if (game != null) {
                    GameInfoDisplay(game = game)
                } else {
                    WelcomeDisplay(totalGameCount = totalGameCount)
                }
            }
        }
    }
}

@Composable
private fun GameInfoDisplay(game: Game) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Game icon placeholder
        Surface(
            modifier = Modifier.size(72.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = getConsoleIcon(game.console.manufacturer),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = game.title,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Console badge
        Surface(
            color = Color.White.copy(alpha = 0.2f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = game.console.displayName,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
        }

        if (game.totalPlayTimeMs > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            val hours = game.totalPlayTimeMs / 3_600_000
            val minutes = (game.totalPlayTimeMs % 3_600_000) / 60_000
            Text(
                text = "Play time: ${hours}h ${minutes}m",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        if (game.favorite) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    tint = OsEmuColors.Yellow,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Favorite",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun WelcomeDisplay(totalGameCount: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Logo area
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "OS-EMU",
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Retro Emulator",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (totalGameCount > 0) "$totalGameCount games in library"
            else "Add games to get started",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

/**
 * Bottom screen panel - icon grid with system apps and games.
 */
@Composable
private fun BottomScreenPanel(
    recentGames: List<Game>,
    favoriteGames: List<Game>,
    selectedGameIndex: Int?,
    onGameSelected: (Int) -> Unit,
    onGameLaunched: (Game) -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToThemes: () -> Unit,
    onScanGames: () -> Unit,
    onPickFolder: () -> Unit,
    isScanning: Boolean,
    scanStatus: String,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    val extras = LocalOsEmuExtras.current
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        TabItem(Icons.Default.Home, "Home", "home"),
        TabItem(Icons.Default.List, "Library", "library"),
        TabItem(Icons.Default.Favorite, "Favorites", "favorites"),
        TabItem(Icons.Default.Settings, "Settings", "settings")
    )

    Column(
        modifier = modifier.background(extras.bottomScreenBackground)
    ) {
        // Content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            when (selectedTab) {
                0 -> HomeIconGrid(
                    recentGames = recentGames,
                    selectedGameIndex = selectedGameIndex,
                    onGameSelected = onGameSelected,
                    onGameLaunched = onGameLaunched,
                    onNavigateToLibrary = onNavigateToLibrary,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToThemes = onNavigateToThemes,
                    onScanGames = onScanGames,
                    onPickFolder = onPickFolder,
                    isScanning = isScanning,
                    scanStatus = scanStatus,
                    isLandscape = isLandscape
                )
                1 -> {
                    LaunchedEffect(Unit) { onNavigateToLibrary() }
                }
                2 -> FavoriteIconGrid(
                    favoriteGames = favoriteGames,
                    onGameLaunched = onGameLaunched,
                    isLandscape = isLandscape
                )
                3 -> {
                    LaunchedEffect(Unit) { onNavigateToSettings() }
                }
            }
        }

        // Tab bar at bottom (like 3DS)
        TabBar3DS(
            tabs = tabs,
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}

@Composable
private fun HomeIconGrid(
    recentGames: List<Game>,
    selectedGameIndex: Int?,
    onGameSelected: (Int) -> Unit,
    onGameLaunched: (Game) -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToThemes: () -> Unit,
    onScanGames: () -> Unit,
    onPickFolder: () -> Unit,
    isScanning: Boolean,
    scanStatus: String,
    isLandscape: Boolean
) {
    val columns = if (isLandscape) 6 else 4

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        // System icons first row
        item {
            SystemIcon(
                icon = Icons.Default.Folder,
                label = "Library",
                color = OsEmuColors.Orange,
                onClick = onNavigateToLibrary
            )
        }
        item {
            SystemIcon(
                icon = Icons.Default.Build,
                label = "Settings",
                color = OsEmuColors.Gray500,
                onClick = onNavigateToSettings
            )
        }
        item {
            SystemIcon(
                icon = Icons.Default.Palette,
                label = "Themes",
                color = OsEmuColors.Pink,
                onClick = onNavigateToThemes
            )
        }
        item {
            SystemIcon(
                icon = Icons.Default.Search,
                label = "Auto Scan",
                color = OsEmuColors.Green,
                onClick = onScanGames
            )
        }

        // Add folder picker icon
        item {
            SystemIcon(
                icon = Icons.Default.Add,
                label = "Add ROMs",
                color = OsEmuColors.Blue500,
                onClick = onPickFolder
            )
        }

        // Scanning status
        if (isScanning || scanStatus.isNotEmpty()) {
            item(span = { GridItemSpan(columns) }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    color = OsEmuColors.Blue100,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = OsEmuColors.Blue500
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = scanStatus.ifEmpty { "Scanning..." },
                            style = MaterialTheme.typography.bodySmall,
                            color = OsEmuColors.Blue700
                        )
                    }
                }
            }
        }

        // Recent games
        if (recentGames.isNotEmpty()) {
            item(span = { GridItemSpan(columns) }) {
                Text(
                    text = "Recently Played",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                )
            }

            itemsIndexed(recentGames.take(12)) { index, game ->
                GameIcon(
                    game = game,
                    isSelected = selectedGameIndex == index,
                    onClick = {
                        onGameSelected(index)
                        onGameLaunched(game)
                    }
                )
            }
        }

        // Empty state with helper
        if (recentGames.isEmpty()) {
            item(span = { GridItemSpan(columns) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No games yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap \"Add ROMs\" to select a folder",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteIconGrid(
    favoriteGames: List<Game>,
    onGameLaunched: (Game) -> Unit,
    isLandscape: Boolean
) {
    val columns = if (isLandscape) 6 else 4

    if (favoriteGames.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No favorites yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(favoriteGames) { game ->
                GameIcon(
                    game = game,
                    onClick = { onGameLaunched(game) }
                )
            }
        }
    }
}
