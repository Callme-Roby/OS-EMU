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
import com.osemu.app.data.model.Console
import com.osemu.app.data.model.Game
import com.osemu.app.data.model.IconStyle
import com.osemu.app.ui.components.*
import com.osemu.app.ui.theme.LocalOsEmuExtras
import com.osemu.app.ui.theme.OsEmuColors

/**
 * Main home screen with 3DS-inspired dual-panel layout.
 *
 * Portrait: Top panel (info/preview) + Bottom panel (icon grid)
 * Landscape: Left panel (info) + Right panel (icon grid)
 *
 * This mimics the 3DS home menu with its upper screen showing
 * game info and lower screen showing the icon grid.
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
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val extras = LocalOsEmuExtras.current

    val selectedGame = selectedGameIndex?.let {
        if (it < recentGames.size) recentGames[it] else null
    }

    if (isLandscape) {
        // Landscape: side-by-side panels (like 3DS held sideways)
        Row(modifier = modifier.fillMaxSize()) {
            // Left panel = "Top screen" showing game info
            TopScreenPanel(
                selectedGame = selectedGame,
                totalGameCount = totalGameCount,
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight()
            )

            // Right panel = "Bottom screen" with icon grid
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
                isLandscape = true,
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight()
            )
        }
    } else {
        // Portrait: stacked panels (classic 3DS layout)
        Column(modifier = modifier.fillMaxSize()) {
            // Top screen
            TopScreenPanel(
                selectedGame = selectedGame,
                totalGameCount = totalGameCount,
                modifier = Modifier
                    .weight(0.40f)
                    .fillMaxWidth()
            )

            // Divider mimicking 3DS hinge
            HingeDivider()

            // Bottom screen
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
                isLandscape = false,
                modifier = Modifier
                    .weight(0.60f)
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
            .height(6.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        OsEmuColors.Gray300,
                        OsEmuColors.Gray200,
                        OsEmuColors.Gray300
                    )
                )
            )
    )
}

/**
 * Top screen panel - shows selected game info or welcome screen.
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
            .background(extras.topScreenBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StatusBar3DS()

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState = selectedGame,
            transitionSpec = {
                fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
            },
            label = "topScreenContent"
        ) { game ->
            if (game != null) {
                // Selected game info
                GameInfoDisplay(game = game)
            } else {
                // Welcome / idle screen
                WelcomeDisplay(totalGameCount = totalGameCount)
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
        // Game title
        Text(
            text = game.title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Console badge
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = game.console.displayName,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Play time & last played
        if (game.totalPlayTimeMs > 0) {
            val hours = game.totalPlayTimeMs / 3_600_000
            val minutes = (game.totalPlayTimeMs % 3_600_000) / 60_000
            Text(
                text = "Play time: ${hours}h ${minutes}m",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        // Favorite indicator
        if (game.favorite) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    tint = OsEmuColors.Red,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Favorite",
                    style = MaterialTheme.typography.labelSmall,
                    color = OsEmuColors.Red
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
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "OS-EMU",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (totalGameCount > 0) "$totalGameCount games in library"
            else "Add games to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
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
        // Tab bar at top
        TabBar3DS(
            tabs = tabs,
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        // Content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
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
                    isLandscape = isLandscape
                )
                1 -> {
                    // Navigate to full library
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
                label = "Scan",
                color = OsEmuColors.Green,
                onClick = onScanGames
            )
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
