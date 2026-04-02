package com.osemu.app.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.osemu.app.core.NativeCoreManager
import com.osemu.app.ui.screens.*

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Library : Screen("library")
    data object Settings : Screen("settings")
    data object Themes : Screen("themes")
    data object Collections : Screen("collections")
    data object Badges : Screen("badges")
    data object Media : Screen("media")
    data object GameDetail : Screen("game_detail/{gameId}") {
        fun createRoute(gameId: Long) = "game_detail/$gameId"
    }
    data object Emulator : Screen("emulator/{gameId}") {
        fun createRoute(gameId: Long) = "emulator/$gameId"
    }
}

@Composable
fun AppNavigation(
    appViewModel: AppViewModel,
    onPickFolder: () -> Unit,
    onScanWithPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val uiState by appViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val nativeCoreManager = remember { NativeCoreManager(context) }

    // Heavy consoles that should try external emulators first
    val launchGame: (com.osemu.app.data.model.Game) -> Unit = remember(navController) {
        { game ->
            if (nativeCoreManager.needsNativeCore(game.console)) {
                // Try external emulator first for heavy consoles
                val launched = appViewModel.launchExternal(game)
                if (!launched) {
                    // No external emulator found - fall back to internal (native core download)
                    navController.navigate(Screen.Emulator.createRoute(game.id))
                }
            } else {
                // Light consoles: use internal EmulatorJS WebView
                navController.navigate(Screen.Emulator.createRoute(game.id))
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                recentGames = uiState.recentGames,
                favoriteGames = uiState.favoriteGames,
                allGames = uiState.allGames,
                totalGameCount = uiState.totalGameCount,
                selectedGameIndex = uiState.selectedGameIndex,
                onGameSelected = { appViewModel.selectGame(it) },
                onGameLaunched = launchGame,
                onGameDetail = { game ->
                    navController.navigate(Screen.GameDetail.createRoute(game.id))
                },
                onNavigateToLibrary = {
                    navController.navigate(Screen.Library.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToThemes = {
                    navController.navigate(Screen.Themes.route)
                },
                onNavigateToCollections = {
                    navController.navigate(Screen.Collections.route)
                },
                onNavigateToBadges = {
                    navController.navigate(Screen.Badges.route)
                },
                onNavigateToMedia = {
                    navController.navigate(Screen.Media.route)
                },
                onScanGames = onScanWithPermission,
                onPickFolder = onPickFolder,
                isScanning = uiState.isScanning,
                scanStatus = uiState.scanStatus,
                badges = uiState.badges
            )
        }

        composable(Screen.Library.route) {
            GameLibraryScreen(
                games = uiState.allGames,
                selectedConsole = uiState.selectedConsole,
                onConsoleSelected = { appViewModel.selectConsole(it) },
                searchQuery = uiState.searchQuery,
                onSearchQueryChanged = { appViewModel.updateSearchQuery(it) },
                onGameSelected = { game ->
                    navController.navigate(Screen.GameDetail.createRoute(game.id))
                },
                onGameLongPress = { game ->
                    appViewModel.toggleFavorite(game)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.GameDetail.route,
            arguments = listOf(navArgument("gameId") { type = NavType.LongType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getLong("gameId") ?: return@composable
            val game = uiState.allGames.find { it.id == gameId }

            if (game != null) {
                GameDetailScreen(
                    game = game,
                    onPlay = { launchGame(game) },
                    onToggleFavorite = { appViewModel.toggleFavorite(game) },
                    onSetBoxArt = { /* TODO: Image picker for box art */ },
                    onAddToCollection = {
                        navController.navigate(Screen.Collections.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Collections.route) {
            CollectionsScreen(
                collections = uiState.collections,
                collectionGameCounts = uiState.collectionGameCounts,
                onCollectionClick = { /* TODO: Collection detail */ },
                onCreateCollection = { name, desc ->
                    appViewModel.createCollection(name, desc)
                },
                onDeleteCollection = { appViewModel.deleteCollection(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Badges.route) {
            BadgeScreen(
                badges = uiState.badges,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Media.route) {
            MediaScreen(
                mediaItems = uiState.mediaItems,
                onAddMedia = { /* TODO: Media picker */ },
                onMediaClick = { /* TODO: Media viewer */ },
                onSetAsWallpaper = { /* TODO: Set wallpaper */ },
                onSetAsMenuMusic = { /* TODO: Set menu music */ },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                autoSaveEnabled = uiState.autoSaveEnabled,
                onAutoSaveToggle = { appViewModel.setAutoSave(it) },
                autoSaveInterval = uiState.autoSaveInterval,
                onAutoSaveIntervalChange = { appViewModel.setAutoSaveInterval(it) },
                showFps = uiState.showFps,
                onShowFpsToggle = { appViewModel.setShowFps(it) },
                vibrationEnabled = uiState.vibrationEnabled,
                onVibrationToggle = { appViewModel.setVibration(it) },
                audioVolume = uiState.audioVolume,
                onAudioVolumeChange = { appViewModel.setAudioVolume(it) },
                touchOverlayOpacity = uiState.touchOverlayOpacity,
                onTouchOverlayOpacityChange = { appViewModel.setTouchOverlayOpacity(it) },
                onManageCores = { /* TODO */ },
                onManageScanPaths = { /* TODO */ },
                onNavigateToThemes = {
                    navController.navigate(Screen.Themes.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Themes.route) {
            ThemeScreen(
                currentThemeId = uiState.currentThemeId,
                onThemeSelected = { appViewModel.setTheme(it) },
                onCustomBackgroundTop = { /* TODO */ },
                onCustomBackgroundBottom = { /* TODO */ },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Emulator.route,
            arguments = listOf(navArgument("gameId") { type = NavType.LongType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getLong("gameId") ?: return@composable

            LaunchedEffect(gameId) {
                appViewModel.loadGame(gameId)
            }

            val currentGame = uiState.currentGame

            if (currentGame != null) {
                EmulatorScreen(
                    game = currentGame,
                    emulatorState = uiState.emulatorState,
                    fps = uiState.currentFps,
                    showFps = uiState.showFps,
                    touchOverlayOpacity = uiState.touchOverlayOpacity,
                    onPause = { appViewModel.pauseEmulator() },
                    onResume = { appViewModel.resumeEmulator() },
                    onSaveState = { slot -> appViewModel.saveState(slot) },
                    onLoadState = { slot -> appViewModel.loadState(slot) },
                    onToggleFastForward = { appViewModel.toggleFastForward() },
                    onExit = {
                        appViewModel.stopEmulator()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
