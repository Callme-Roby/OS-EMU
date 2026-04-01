package com.osemu.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.osemu.app.data.model.Console
import com.osemu.app.data.model.Game
import com.osemu.app.ui.components.*
import com.osemu.app.ui.theme.LocalOsEmuExtras
import com.osemu.app.ui.theme.OsEmuColors

enum class LibraryViewMode { GRID, LIST }

/**
 * Full game library screen with console filtering tabs and search.
 * Games are sorted alphabetically within each console category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameLibraryScreen(
    games: List<Game>,
    selectedConsole: Console?,
    onConsoleSelected: (Console?) -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onGameSelected: (Game) -> Unit,
    onGameLongPress: (Game) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    var viewMode by remember { mutableStateOf(LibraryViewMode.GRID) }
    var showSearch by remember { mutableStateOf(false) }

    val extras = LocalOsEmuExtras.current

    // Group games by console
    val gamesByConsole = remember(games) {
        games.groupBy { it.console }.toSortedMap(compareBy { it.displayName })
    }

    // Available consoles (only ones with games)
    val availableConsoles = remember(gamesByConsole) {
        gamesByConsole.keys.toList()
    }

    // Filtered games
    val filteredGames = remember(games, selectedConsole, searchQuery) {
        games.filter { game ->
            (selectedConsole == null || game.console == selectedConsole) &&
                (searchQuery.isBlank() || game.title.contains(searchQuery, ignoreCase = true))
        }.sortedBy { it.title }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Top bar
        Surface(
            color = extras.statusBarColor,
            shadowElevation = 4.dp
        ) {
            Column {
                // Action bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = selectedConsole?.displayName ?: "Game Library",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    // Search toggle
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            if (showSearch) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }

                    // View mode toggle
                    IconButton(onClick = {
                        viewMode = if (viewMode == LibraryViewMode.GRID)
                            LibraryViewMode.LIST else LibraryViewMode.GRID
                    }) {
                        Icon(
                            if (viewMode == LibraryViewMode.GRID) Icons.Default.List
                            else Icons.Default.List,
                            contentDescription = "View mode",
                            tint = Color.White
                        )
                    }

                    // Game count badge
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${filteredGames.size}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Search bar
                AnimatedVisibility(visible = showSearch) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        placeholder = { Text("Search games...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White.copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                }

                // Console filter chips
                if (availableConsoles.size > 1) {
                    ConsoleFilterBar(
                        consoles = availableConsoles,
                        selectedConsole = selectedConsole,
                        onConsoleSelected = onConsoleSelected,
                        gamesByConsole = gamesByConsole
                    )
                }
            }
        }

        // Game grid/list
        if (filteredGames.isEmpty()) {
            EmptyLibraryState(searchQuery.isNotEmpty())
        } else {
            when (viewMode) {
                LibraryViewMode.GRID -> GameGrid(
                    games = filteredGames,
                    isLandscape = isLandscape,
                    onGameSelected = onGameSelected
                )
                LibraryViewMode.LIST -> GameList(
                    games = filteredGames,
                    onGameSelected = onGameSelected,
                    onGameLongPress = onGameLongPress
                )
            }
        }
    }
}

@Composable
private fun ConsoleFilterBar(
    consoles: List<Console>,
    selectedConsole: Console?,
    onConsoleSelected: (Console?) -> Unit,
    gamesByConsole: Map<Console, List<Game>>
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // "All" chip
        item {
            FilterChip(
                selected = selectedConsole == null,
                onClick = { onConsoleSelected(null) },
                label = {
                    Text(
                        "All",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.White.copy(alpha = 0.3f),
                    selectedLabelColor = Color.White,
                    labelColor = Color.White.copy(alpha = 0.7f)
                )
            )
        }

        items(consoles) { console ->
            val count = gamesByConsole[console]?.size ?: 0
            FilterChip(
                selected = selectedConsole == console,
                onClick = { onConsoleSelected(console) },
                label = {
                    Text(
                        "${console.displayName} ($count)",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.White.copy(alpha = 0.3f),
                    selectedLabelColor = Color.White,
                    labelColor = Color.White.copy(alpha = 0.7f)
                )
            )
        }
    }
}

@Composable
private fun GameGrid(
    games: List<Game>,
    isLandscape: Boolean,
    onGameSelected: (Game) -> Unit
) {
    val columns = when {
        isLandscape -> 6
        else -> 4
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(games, key = { it.id }) { game ->
            GameIcon(
                game = game,
                iconSize = 60.dp,
                onClick = { onGameSelected(game) }
            )
        }
    }
}

@Composable
private fun GameList(
    games: List<Game>,
    onGameSelected: (Game) -> Unit,
    onGameLongPress: (Game) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(games, key = { it.id }) { game ->
            GameListItem(
                game = game,
                onClick = { onGameSelected(game) }
            )
        }
    }
}

@Composable
private fun GameListItem(
    game: Game,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Console icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getConsoleIcon(game.console.manufacturer),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Game info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = game.console.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Favorite
            if (game.favorite) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    tint = OsEmuColors.Red,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Play indicator
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun EmptyLibraryState(isSearching: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isSearching) Icons.Default.Search else Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )
            Text(
                text = if (isSearching) "No games found" else "Your library is empty",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            if (!isSearching) {
                Text(
                    text = "Scan a folder to add ROMs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        }
    }
}
