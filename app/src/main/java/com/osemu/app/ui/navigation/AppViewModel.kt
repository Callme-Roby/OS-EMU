package com.osemu.app.ui.navigation

import android.app.Application
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.osemu.app.core.EmulatorEngine
import com.osemu.app.core.EmulatorState
import com.osemu.app.core.GameScanner
import com.osemu.app.data.database.AppDatabase
import com.osemu.app.data.model.*
import com.osemu.app.data.repository.GameRepository
import com.osemu.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class AppUiState(
    val allGames: List<Game> = emptyList(),
    val recentGames: List<Game> = emptyList(),
    val favoriteGames: List<Game> = emptyList(),
    val totalGameCount: Int = 0,
    val selectedGameIndex: Int? = null,
    val selectedConsole: Console? = null,
    val searchQuery: String = "",
    val currentGame: Game? = null,
    val emulatorState: EmulatorState = EmulatorState.IDLE,
    val currentFps: Double = 0.0,
    val currentThemeId: String = Theme.DEFAULT.id,
    val currentTheme: Theme = Theme.DEFAULT,
    val autoSaveEnabled: Boolean = true,
    val autoSaveInterval: Int = 60,
    val showFps: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val audioVolume: Float = 1.0f,
    val touchOverlayOpacity: Float = 0.5f,
    val isScanning: Boolean = false,
    val scanStatus: String = "",
    // Collections
    val collections: List<Collection> = emptyList(),
    val collectionGameCounts: Map<Long, Int> = emptyMap(),
    // Badges
    val badges: List<Badge> = BadgeRegistry.allBadges,
    // Media
    val mediaItems: List<MediaItem> = emptyList()
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val gameRepository = GameRepository(database.gameDao(), database.saveStateDao())
    private val collectionDao = database.collectionDao()
    private val settingsRepository = SettingsRepository(application)
    private val emulatorEngine = EmulatorEngine(application)
    private val gameScanner = GameScanner(application, gameRepository)

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private var fastForwardEnabled = false
    private var sessionStartTime: Long = 0

    init {
        viewModelScope.launch {
            gameRepository.getAllGames().collect { games ->
                _uiState.update { it.copy(allGames = games, totalGameCount = games.size) }
                updateBadges()
            }
        }
        viewModelScope.launch {
            gameRepository.getRecentlyPlayed().collect { games ->
                _uiState.update { it.copy(recentGames = games) }
            }
        }
        viewModelScope.launch {
            gameRepository.getFavorites().collect { games ->
                _uiState.update { it.copy(favoriteGames = games) }
                updateBadges()
            }
        }
        viewModelScope.launch {
            settingsRepository.themeId.collect { themeId ->
                val theme = Theme.builtInThemes.find { it.id == themeId } ?: Theme.DEFAULT
                _uiState.update { it.copy(currentThemeId = themeId, currentTheme = theme) }
            }
        }
        viewModelScope.launch {
            settingsRepository.autoSaveEnabled.collect { enabled ->
                _uiState.update { it.copy(autoSaveEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.autoSaveInterval.collect { interval ->
                _uiState.update { it.copy(autoSaveInterval = interval) }
            }
        }
        viewModelScope.launch {
            settingsRepository.showFps.collect { show ->
                _uiState.update { it.copy(showFps = show) }
            }
        }
        viewModelScope.launch {
            settingsRepository.vibrationEnabled.collect { enabled ->
                _uiState.update { it.copy(vibrationEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.audioVolume.collect { volume ->
                _uiState.update { it.copy(audioVolume = volume) }
            }
        }
        viewModelScope.launch {
            settingsRepository.touchOverlayOpacity.collect { opacity ->
                _uiState.update { it.copy(touchOverlayOpacity = opacity) }
            }
        }
        viewModelScope.launch {
            emulatorEngine.state.collect { state ->
                _uiState.update { it.copy(emulatorState = state) }
            }
        }
        viewModelScope.launch {
            emulatorEngine.fps.collect { fps ->
                _uiState.update { it.copy(currentFps = fps) }
            }
        }
        viewModelScope.launch {
            gameScanner.isScanning.collect { scanning ->
                _uiState.update { it.copy(isScanning = scanning) }
            }
        }
        viewModelScope.launch {
            gameScanner.scanProgress.collect { progress ->
                _uiState.update { it.copy(scanStatus = progress.status) }
            }
        }
        // Collections
        viewModelScope.launch {
            collectionDao.getAllCollections().collect { collections ->
                _uiState.update { it.copy(collections = collections) }
                updateBadges()
            }
        }
    }

    // --- Badge System ---
    private fun updateBadges() {
        val state = _uiState.value
        val updatedBadges = BadgeRegistry.allBadges.map { badge ->
            val unlocked = checkBadgeRequirement(badge.requirement, state)
            badge.copy(
                isUnlocked = unlocked,
                unlockedAt = if (unlocked) badge.unlockedAt ?: System.currentTimeMillis() else null
            )
        }
        _uiState.update { it.copy(badges = updatedBadges) }
    }

    private fun checkBadgeRequirement(req: BadgeRequirement, state: AppUiState): Boolean = when (req) {
        is BadgeRequirement.GamesInLibrary -> state.totalGameCount >= req.count
        is BadgeRequirement.FirstGame -> state.totalGameCount >= 1
        is BadgeRequirement.GamesPlayed ->
            state.allGames.count { it.lastPlayed != null } >= req.count
        is BadgeRequirement.TotalPlayTime ->
            state.allGames.sumOf { it.totalPlayTimeMs } >= req.hours * 3_600_000L
        is BadgeRequirement.ConsolesUsed ->
            state.allGames.filter { it.lastPlayed != null }.map { it.console }.distinct().size >= req.count
        is BadgeRequirement.FavoritesAdded -> state.favoriteGames.size >= req.count
        is BadgeRequirement.FirstFavorite -> state.favoriteGames.isNotEmpty()
        is BadgeRequirement.CollectionsCreated -> state.collections.size >= req.count
        is BadgeRequirement.ThemeChanged -> state.currentThemeId != Theme.DEFAULT.id
        is BadgeRequirement.AllConsoles -> {
            val played = state.allGames.filter { it.lastPlayed != null }.map { it.console }.distinct()
            played.size >= Console.entries.size
        }
    }

    // --- Game Selection ---
    fun selectGame(index: Int) {
        _uiState.update { it.copy(selectedGameIndex = index) }
    }

    fun selectConsole(console: Console?) {
        _uiState.update { it.copy(selectedConsole = console) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleFavorite(game: Game) {
        viewModelScope.launch {
            gameRepository.toggleFavorite(game.id, !game.favorite)
        }
    }

    // --- Emulation ---
    fun loadGame(gameId: Long) {
        viewModelScope.launch {
            val game = gameRepository.getGameById(gameId) ?: return@launch
            _uiState.update { it.copy(currentGame = game) }
            val config = EmulatorConfig(
                gameId = game.id,
                console = game.console,
                autoSaveEnabled = _uiState.value.autoSaveEnabled,
                autoSaveIntervalSeconds = _uiState.value.autoSaveInterval
            )
            emulatorEngine.loadGame(game.filePath, game.console, config)
            sessionStartTime = System.currentTimeMillis()
        }
    }

    fun pauseEmulator() { emulatorEngine.pause() }
    fun resumeEmulator() { emulatorEngine.resume() }

    fun stopEmulator() {
        val sessionMs = System.currentTimeMillis() - sessionStartTime
        val currentGame = _uiState.value.currentGame
        if (_uiState.value.autoSaveEnabled) { emulatorEngine.autoSave() }
        emulatorEngine.stop()
        if (currentGame != null && sessionMs > 0) {
            viewModelScope.launch {
                gameRepository.recordPlaySession(currentGame.id, sessionMs)
            }
        }
        _uiState.update { it.copy(currentGame = null) }
    }

    fun saveState(slot: Int) {
        val game = _uiState.value.currentGame ?: return
        val statePath = emulatorEngine.saveState(slot)
        val screenshotPath = emulatorEngine.takeScreenshot()
        if (statePath != null) {
            viewModelScope.launch {
                gameRepository.saveSaveState(
                    SaveState(
                        gameId = game.id, slotIndex = slot,
                        filePath = statePath, screenshotPath = screenshotPath,
                        isAutoSave = slot == SaveState.AUTO_SAVE_SLOT
                    )
                )
            }
        }
    }

    fun loadState(slot: Int) { emulatorEngine.loadState(slot) }
    fun toggleFastForward() {
        fastForwardEnabled = !fastForwardEnabled
        emulatorEngine.setFastForward(fastForwardEnabled)
    }

    // --- Settings ---
    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme.id)
            updateBadges()
        }
    }
    fun setAutoSave(enabled: Boolean) { viewModelScope.launch { settingsRepository.setAutoSave(enabled) } }
    fun setAutoSaveInterval(seconds: Int) { viewModelScope.launch { settingsRepository.setAutoSaveInterval(seconds) } }
    fun setShowFps(show: Boolean) { viewModelScope.launch { settingsRepository.setShowFps(show) } }
    fun setVibration(enabled: Boolean) { viewModelScope.launch { settingsRepository.setVibration(enabled) } }
    fun setAudioVolume(volume: Float) { viewModelScope.launch { settingsRepository.setAudioVolume(volume) } }
    fun setTouchOverlayOpacity(opacity: Float) { viewModelScope.launch { settingsRepository.setTouchOverlayOpacity(opacity) } }

    // --- Collections ---
    fun createCollection(name: String, description: String) {
        viewModelScope.launch {
            collectionDao.insertCollection(
                Collection(name = name, description = description)
            )
        }
    }

    fun deleteCollection(collection: Collection) {
        viewModelScope.launch { collectionDao.deleteCollection(collection) }
    }

    fun addGameToCollection(collectionId: Long, gameId: Long) {
        viewModelScope.launch {
            collectionDao.addGameToCollection(CollectionGame(collectionId, gameId))
        }
    }

    fun removeGameFromCollection(collectionId: Long, gameId: Long) {
        viewModelScope.launch {
            collectionDao.removeGameFromCollection(collectionId, gameId)
        }
    }

    // --- Scanning ---
    fun scanDefaultPaths() {
        viewModelScope.launch {
            val storage = Environment.getExternalStorageDirectory().absolutePath
            val defaultPaths = listOf(
                "$storage/Roms", "$storage/ROMs", "$storage/roms",
                "$storage/Download", "$storage/Download/Roms", "$storage/Download/ROMs",
                "$storage/Downloads", "$storage/Downloads/Roms", "$storage/Downloads/ROMs",
                "$storage/RetroArch/roms", "$storage/RetroArch/ROMs",
                "$storage/Games", "$storage/games",
                "$storage/Emulation", "$storage/Emulation/roms",
                "$storage/Documents/Roms", "$storage/Documents/ROMs"
            )
            for (path in defaultPaths) {
                if (File(path).exists()) { gameScanner.scanDirectory(path) }
            }
            if (_uiState.value.totalGameCount == 0 && !_uiState.value.isScanning) {
                _uiState.update { it.copy(scanStatus = "No ROMs found. Use \"Add ROMs\" to pick a folder.") }
            }
        }
    }

    fun scanPath(path: String) {
        viewModelScope.launch { gameScanner.scanDirectory(path) }
    }

    fun scanUri(uri: Uri) {
        viewModelScope.launch { gameScanner.scanUri(uri) }
    }
}
