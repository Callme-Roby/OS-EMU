package com.osemu.app.core

import android.content.Context
import android.view.Surface
import com.osemu.app.data.model.Console
import com.osemu.app.data.model.EmulatorConfig
import com.osemu.app.data.model.ShaderPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Core emulator engine that manages libretro core lifecycle.
 * This is the bridge between the Android UI and native emulation cores.
 */
class EmulatorEngine(private val context: Context) {

    private val _state = MutableStateFlow(EmulatorState.IDLE)
    val state: StateFlow<EmulatorState> = _state.asStateFlow()

    private val _fps = MutableStateFlow(0.0)
    val fps: StateFlow<Double> = _fps.asStateFlow()

    private val _currentCore = MutableStateFlow<CoreInfo?>(null)
    val currentCore: StateFlow<CoreInfo?> = _currentCore.asStateFlow()

    private var surface: Surface? = null
    private var config: EmulatorConfig = EmulatorConfig()

    private val coresDir: File
        get() = File(context.filesDir, "cores").also { it.mkdirs() }

    private val savesDir: File
        get() = File(context.filesDir, "saves").also { it.mkdirs() }

    private val statesDir: File
        get() = File(context.filesDir, "states").also { it.mkdirs() }

    private val systemDir: File
        get() = File(context.filesDir, "system").also { it.mkdirs() }

    fun getAvailableCores(): List<CoreInfo> {
        val coreFiles = coresDir.listFiles { file ->
            file.extension == "so" && file.name.endsWith("_libretro_android.so")
        } ?: emptyArray()

        return coreFiles.map { file ->
            val coreName = file.nameWithoutExtension
                .removeSuffix("_libretro_android")
            CoreInfo(
                name = coreName,
                path = file.absolutePath,
                supportedConsoles = Console.entries.filter { console ->
                    console.coreNames.any { it.equals(coreName, ignoreCase = true) }
                }
            )
        }
    }

    fun getCoresForConsole(console: Console): List<CoreInfo> {
        return getAvailableCores().filter { core ->
            console in core.supportedConsoles
        }
    }

    fun setSurface(surface: Surface?) {
        this.surface = surface
    }

    suspend fun loadGame(romPath: String, console: Console, config: EmulatorConfig): Boolean {
        this.config = config

        _state.value = EmulatorState.LOADING

        // Find appropriate core
        val cores = getCoresForConsole(console)
        if (cores.isEmpty()) {
            _state.value = EmulatorState.ERROR
            return false
        }

        val preferredCore = config.coreName.ifEmpty { console.coreNames.first() }
        val core = cores.find { it.name == preferredCore } ?: cores.first()

        _currentCore.value = core

        // In a real implementation, this would call JNI to:
        // 1. Load the libretro core .so file
        // 2. Initialize the core (retro_init)
        // 3. Load the ROM (retro_load_game)
        // 4. Set up audio/video callbacks
        // 5. Start the emulation loop

        _state.value = EmulatorState.RUNNING
        return true
    }

    fun pause() {
        if (_state.value == EmulatorState.RUNNING) {
            _state.value = EmulatorState.PAUSED
        }
    }

    fun resume() {
        if (_state.value == EmulatorState.PAUSED) {
            _state.value = EmulatorState.RUNNING
        }
    }

    fun stop() {
        _state.value = EmulatorState.IDLE
        _currentCore.value = null
        _fps.value = 0.0
    }

    fun saveState(slotIndex: Int): String? {
        val statePath = File(statesDir, "${config.gameId}_slot$slotIndex.state")
        // JNI: retro_serialize -> write to file
        return statePath.absolutePath
    }

    fun loadState(slotIndex: Int): Boolean {
        val statePath = File(statesDir, "${config.gameId}_slot$slotIndex.state")
        if (!statePath.exists()) return false
        // JNI: read file -> retro_unserialize
        return true
    }

    fun autoSave(): String? {
        return saveState(-1)
    }

    fun loadAutoSave(): Boolean {
        return loadState(-1)
    }

    fun takeScreenshot(): String? {
        val screenshotPath = File(
            statesDir,
            "${config.gameId}_${System.currentTimeMillis()}.png"
        )
        // JNI: capture framebuffer -> save as PNG
        return screenshotPath.absolutePath
    }

    fun setFastForward(enabled: Boolean) {
        // JNI: toggle fast-forward
    }

    fun applyShader(preset: ShaderPreset?) {
        // JNI: apply shader preset
    }

    fun setInputState(port: Int, device: Int, index: Int, id: Int, value: Short) {
        // JNI: retro_input_state callback
    }

    fun getSaveFilePath(gameId: Long): String {
        return File(savesDir, "${gameId}.srm").absolutePath
    }

    fun getStateFilePath(gameId: Long, slot: Int): String {
        return File(statesDir, "${gameId}_slot$slot.state").absolutePath
    }

    fun getScreenshotPath(gameId: Long, slot: Int): String {
        return File(statesDir, "${gameId}_slot${slot}_screenshot.png").absolutePath
    }
}

data class CoreInfo(
    val name: String,
    val path: String,
    val supportedConsoles: List<Console>
)

enum class EmulatorState {
    IDLE,
    LOADING,
    RUNNING,
    PAUSED,
    ERROR
}
