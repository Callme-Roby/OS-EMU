@file:OptIn(ExperimentalFoundationApi::class)
package com.osemu.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osemu.app.data.model.Badge
import com.osemu.app.data.model.Game
import com.osemu.app.ui.components.*
import com.osemu.app.ui.theme.LocalOsEmuExtras
import com.osemu.app.ui.theme.OsEmuColors
import kotlinx.coroutines.launch

/**
 * Main home screen with swipe navigation and Wii U-style widgets.
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
    val extras = LocalOsEmuExtras.current
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val tabs = listOf(
        TabItem(Icons.Default.Home, "Home", "home"),
        TabItem(Icons.Default.Favorite, "Favorites", "favorites"),
        TabItem(Icons.Default.Star, "Badges", "badges")
    )

    Column(modifier = modifier.fillMaxSize()) {
        // Top screen with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.32f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            extras.topScreenGradientStart,
                            extras.topScreenGradientEnd
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                StatusBar3DS()

                // Animated content based on page
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val selectedGame = selectedGameIndex?.let {
                        if (it < recentGames.size) recentGames[it] else null
                    }

                    AnimatedContent(
                        targetState = pagerState.currentPage,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "topContent"
                    ) { page ->
                        when (page) {
                            0 -> {
                                if (selectedGame != null) {
                                    TopScreenGameInfo(game = selectedGame)
                                } else {
                                    TopScreenWelcome(totalGameCount = totalGameCount)
                                }
                            }
                            1 -> TopScreenTitle("Favorites", "${favoriteGames.size} games")
                            2 -> TopScreenTitle(
                                "Badge Arcade",
                                "${badges.count { it.isUnlocked }} / ${badges.size} unlocked"
                            )
                        }
                    }
                }

                // Page dots
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (pagerState.currentPage == index) 8.dp else 5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index)
                                        Color.White
                                    else Color.White.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }
        }

        // Hinge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(OsEmuColors.HingeDark, OsEmuColors.HingeLight, OsEmuColors.HingeDark)
                    )
                )
        )

        // Bottom screen - swipe pages
        Column(
            modifier = Modifier
                .weight(0.68f)
                .fillMaxWidth()
                .background(extras.bottomScreenBackground)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> HomePageContent(
                        recentGames = recentGames,
                        allGames = allGames,
                        totalGameCount = totalGameCount,
                        favoriteGames = favoriteGames,
                        onGameSelected = onGameSelected,
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
                        scanStatus = scanStatus
                    )
                    1 -> FavoritesPageContent(
                        favoriteGames = favoriteGames,
                        onGameLaunched = onGameLaunched,
                        onGameDetail = onGameDetail
                    )
                    2 -> BadgesPreviewContent(
                        badges = badges,
                        onNavigateToBadges = onNavigateToBadges
                    )
                }
            }

            // Bottom tab bar
            TabBar3DS(
                tabs = tabs,
                selectedIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    scope.launch { pagerState.animateScrollToPage(index) }
                }
            )
        }
    }
}

// ==================== Top Screen Content ====================

@Composable
private fun TopScreenWelcome(totalGameCount: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("OS-EMU", style = MaterialTheme.typography.headlineSmall,
            color = Color.White, fontWeight = FontWeight.Bold)
        Text("Retro Emulator", fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            if (totalGameCount > 0) "$totalGameCount games" else "Add games to start",
            fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun TopScreenGameInfo(game: Game) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    getConsoleIcon(game.console.manufacturer), null,
                    tint = Color.White, modifier = Modifier.size(30.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(game.title, style = MaterialTheme.typography.titleMedium,
            color = Color.White, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, maxLines = 2)
        Spacer(modifier = Modifier.height(4.dp))
        Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
            Text(game.console.displayName,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                fontSize = 11.sp, color = Color.White)
        }
    }
}

@Composable
private fun TopScreenTitle(title: String, subtitle: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall,
            color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
    }
}

// ==================== Bottom Screen Pages ====================

@Composable
private fun HomePageContent(
    recentGames: List<Game>,
    allGames: List<Game>,
    totalGameCount: Int,
    favoriteGames: List<Game>,
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
    scanStatus: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Quick launch grid
        WidgetCard(
            title = "Quick Launch",
            icon = Icons.Default.Apps,
            accentColor = OsEmuColors.Blue500
        ) {
            QuickLaunchGrid(
                onLibrary = onNavigateToLibrary,
                onCollections = onNavigateToCollections,
                onBadges = onNavigateToBadges,
                onMedia = onNavigateToMedia,
                onThemes = onNavigateToThemes,
                onSettings = onNavigateToSettings
            )
        }

        // Scan / Add ROMs
        if (totalGameCount == 0 || isScanning || scanStatus.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = OsEmuColors.Blue100
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (isScanning) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp, color = OsEmuColors.Blue500
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(scanStatus.ifEmpty { "Scanning..." },
                                fontSize = 12.sp, color = OsEmuColors.Blue700)
                        }
                    } else if (scanStatus.isNotEmpty()) {
                        Text(scanStatus, fontSize = 12.sp, color = OsEmuColors.Blue700)
                    }

                    if (totalGameCount == 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onScanGames,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Search, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Auto Scan", fontSize = 12.sp)
                            }
                            Button(
                                onClick = onPickFolder,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add ROMs", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Stats widget
        if (totalGameCount > 0) {
            WidgetCard(
                title = "Statistics",
                icon = Icons.Default.Info,
                accentColor = OsEmuColors.Green
            ) {
                val totalPlayTime = allGames.sumOf { it.totalPlayTimeMs }
                val consolesUsed = allGames.map { it.console }.distinct().size
                StatsWidgetContent(
                    totalGames = totalGameCount,
                    totalPlayTimeMs = totalPlayTime,
                    favoriteCount = favoriteGames.size,
                    consolesUsed = consolesUsed
                )
            }
        }

        // Recently played widget
        if (recentGames.isNotEmpty()) {
            WidgetCard(
                title = "Recently Played",
                icon = Icons.Default.History,
                accentColor = OsEmuColors.Orange,
                onSeeAll = onNavigateToLibrary
            ) {
                GameCarousel(
                    games = recentGames.take(10),
                    onGameClick = { game -> onGameDetail(game) }
                )
            }
        }

        // Favorites widget
        if (favoriteGames.isNotEmpty()) {
            WidgetCard(
                title = "Favorites",
                icon = Icons.Default.Favorite,
                accentColor = OsEmuColors.Red
            ) {
                GameCarousel(
                    games = favoriteGames.take(10),
                    onGameClick = { game -> onGameDetail(game) }
                )
            }
        }

        // Add ROMs button at bottom
        if (totalGameCount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onScanGames,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Search, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Scan", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onPickFolder,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add ROMs", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun FavoritesPageContent(
    favoriteGames: List<Game>,
    onGameLaunched: (Game) -> Unit,
    onGameDetail: (Game) -> Unit
) {
    if (favoriteGames.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.FavoriteBorder, null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No favorites yet", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                Text("Long press a game to add it", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Group by console
            val grouped = favoriteGames.groupBy { it.console }
            grouped.forEach { (console, games) ->
                WidgetCard(
                    title = console.displayName,
                    icon = Icons.Default.Star,
                    accentColor = getConsoleTint(console.manufacturer)
                ) {
                    GameCarousel(
                        games = games,
                        onGameClick = { onGameDetail(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgesPreviewContent(
    badges: List<Badge>,
    onNavigateToBadges: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Unlocked badges
        val unlocked = badges.filter { it.isUnlocked }
        if (unlocked.isNotEmpty()) {
            WidgetCard(
                title = "Unlocked (${unlocked.size})",
                icon = Icons.Default.CheckCircle,
                accentColor = OsEmuColors.Green,
                onSeeAll = onNavigateToBadges
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    unlocked.take(6).forEach { badge ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(badge.iconEmoji, fontSize = 24.sp)
                            Text(badge.name, fontSize = 8.sp, maxLines = 1,
                                textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        // Next badges to unlock
        val locked = badges.filter { !it.isUnlocked }.take(6)
        WidgetCard(
            title = "Next Badges",
            icon = Icons.Default.Lock,
            accentColor = OsEmuColors.Gray500,
            onSeeAll = onNavigateToBadges
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                locked.forEach { badge ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("?", fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
                        Text(badge.name, fontSize = 8.sp, maxLines = 1,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                    }
                }
            }
        }

        Button(
            onClick = onNavigateToBadges,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Star, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("View All Badges")
        }
    }
}
