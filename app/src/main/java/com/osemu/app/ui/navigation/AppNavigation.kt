package com.osemu.app.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.osemu.app.ui.screens.*

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Library : Screen("library")
    data object Settings : Screen("settings")
    data object Themes : Screen("themes")
    data object Emulator : Screen("emulator/{gameId}") {
        fun createRoute(gameId: Long) = "emulator/$gameId"
    }
}

@Composable
fun AppNavigation(
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val uiState by appViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                recentGames = uiState.recentGames,
                favoriteGames = uiState.favoriteGames,
                totalGameCount = uiState.totalGameCount,
                selectedGameIndex = uiState.selectedGameIndex,
                onGameSelected = { appViewModel.selectGame(it) },
                onGameLaunched = { game ->
                    navController.navigate(Screen.Emulator.createRoute(game.id))
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
                onScanGames = { appViewModel.scanDefaultPaths() }
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
                    navController.navigate(Screen.Emulator.createRoute(game.id))
                },
                onGameLongPress = { game ->
                    appViewModel.toggleFavorite(game)
                },
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
                onManageCores = { /* TODO: Core management screen */ },
                onManageScanPaths = { /* TODO: Scan path management */ },
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
                onCustomBackgroundTop = { /* TODO: Image picker */ },
                onCustomBackgroundBottom = { /* TODO: Image picker */ },
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
