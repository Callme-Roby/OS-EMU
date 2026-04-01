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
    val scanStatus: String = ""
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val gameRepository = GameRepository(database.gameDao(), database.saveStateDao())
    private val settingsRepository = SettingsRepository(application)
    private val emulatorEngine = EmulatorEngine(application)
    private val gameScanner = GameScanner(application, gameRepository)

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private var fastForwardEnabled = false
    private var sessionStartTime: Long = 0

    init {
        // Collect all data streams
        viewModelScope.launch {
            gameRepository.getAllGames().collect { games ->
                _uiState.update { it.copy(allGames = games, totalGameCount = games.size) }
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

    fun pauseEmulator() {
        emulatorEngine.pause()
    }

    fun resumeEmulator() {
        emulatorEngine.resume()
    }

    fun stopEmulator() {
        val sessionMs = System.currentTimeMillis() - sessionStartTime
        val currentGame = _uiState.value.currentGame

        if (_uiState.value.autoSaveEnabled) {
            emulatorEngine.autoSave()
        }

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
                        gameId = game.id,
                        slotIndex = slot,
                        filePath = statePath,
                        screenshotPath = screenshotPath,
                        isAutoSave = slot == SaveState.AUTO_SAVE_SLOT
                    )
                )
            }
        }
    }

    fun loadState(slot: Int) {
        emulatorEngine.loadState(slot)
    }

    fun toggleFastForward() {
        fastForwardEnabled = !fastForwardEnabled
        emulatorEngine.setFastForward(fastForwardEnabled)
    }

    // --- Settings ---
    fun setTheme(theme: Theme) {
        viewModelScope.launch { settingsRepository.setTheme(theme.id) }
    }

    fun setAutoSave(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoSave(enabled) }
    }

    fun setAutoSaveInterval(seconds: Int) {
        viewModelScope.launch { settingsRepository.setAutoSaveInterval(seconds) }
    }

    fun setShowFps(show: Boolean) {
        viewModelScope.launch { settingsRepository.setShowFps(show) }
    }

    fun setVibration(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setVibration(enabled) }
    }

    fun setAudioVolume(volume: Float) {
        viewModelScope.launch { settingsRepository.setAudioVolume(volume) }
    }

    fun setTouchOverlayOpacity(opacity: Float) {
        viewModelScope.launch { settingsRepository.setTouchOverlayOpacity(opacity) }
    }

    // --- Scanning ---

    /**
     * Scans common ROM directories on the device.
     * Covers most popular locations where users store ROMs.
     */
    fun scanDefaultPaths() {
        viewModelScope.launch {
            val storage = Environment.getExternalStorageDirectory().absolutePath
            val defaultPaths = listOf(
                "$storage/Roms",
                "$storage/ROMs",
                "$storage/roms",
                "$storage/Download",
                "$storage/Download/Roms",
                "$storage/Download/ROMs",
                "$storage/Downloads",
                "$storage/Downloads/Roms",
                "$storage/Downloads/ROMs",
                "$storage/RetroArch/roms",
                "$storage/RetroArch/ROMs",
                "$storage/Games",
                "$storage/games",
                "$storage/Emulation",
                "$storage/Emulation/roms",
                "$storage/Documents/Roms",
                "$storage/Documents/ROMs"
            )
            for (path in defaultPaths) {
                if (File(path).exists()) {
                    gameScanner.scanDirectory(path)
                }
            }

            // If nothing was found, update status
            if (_uiState.value.totalGameCount == 0 && !_uiState.value.isScanning) {
                _uiState.update {
                    it.copy(scanStatus = "No ROMs found. Use \"Add ROMs\" to pick a folder.")
                }
            }
        }
    }

    fun scanPath(path: String) {
        viewModelScope.launch {
            gameScanner.scanDirectory(path)
        }
    }

    /**
     * Scan a URI from the SAF folder picker.
     */
    fun scanUri(uri: android.net.Uri) {
        viewModelScope.launch {
            gameScanner.scanUri(uri)
        }
    }
}
