package com.osemu.app.core

import android.content.Context
import android.opengl.GLSurfaceView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Runs a libretro core natively for heavy consoles (3DS, GameCube, etc.).
 * Loads .so core files dynamically and manages the emulation lifecycle.
 *
 * This uses Android's System.load() to load the libretro core,
 * and renders frames via OpenGL ES on a GLSurfaceView.
 */
class LibretroRunner(private val context: Context) {

    private val _state = MutableStateFlow(NativeEmulatorState.IDLE)
    val state: StateFlow<NativeEmulatorState> = _state.asStateFlow()

    private val _fps = MutableStateFlow(0.0)
    val fps: StateFlow<Double> = _fps.asStateFlow()

    private var coreHandle: Long = 0
    private var gameLoaded = false
    private var running = false
    private var emulationThread: Thread? = null

    private val systemDir: File
        get() = File(context.filesDir, "system").also { it.mkdirs() }

    private val savesDir: File
        get() = File(context.filesDir, "saves").also { it.mkdirs() }

    /**
     * Loads a libretro core .so file.
     */
    fun loadCore(corePath: String): Boolean {
        return try {
            _state.value = NativeEmulatorState.LOADING_CORE
            System.load(corePath)
            nativeInit(
                systemDir.absolutePath,
                savesDir.absolutePath
            )
            _state.value = NativeEmulatorState.CORE_LOADED
            true
        } catch (e: UnsatisfiedLinkError) {
            _state.value = NativeEmulatorState.ERROR
            false
        } catch (e: Exception) {
            _state.value = NativeEmulatorState.ERROR
            false
        }
    }

    /**
     * Loads a ROM file into the loaded core.
     */
    fun loadGame(romPath: String): Boolean {
        if (_state.value != NativeEmulatorState.CORE_LOADED) return false

        return try {
            _state.value = NativeEmulatorState.LOADING_GAME
            val result = nativeLoadGame(romPath)
            if (result) {
                gameLoaded = true
                _state.value = NativeEmulatorState.RUNNING
            } else {
                _state.value = NativeEmulatorState.ERROR
            }
            result
        } catch (e: Exception) {
            _state.value = NativeEmulatorState.ERROR
            false
        }
    }

    /**
     * Starts the emulation loop on a background thread.
     */
    fun startEmulation() {
        if (!gameLoaded || running) return
        running = true

        emulationThread = Thread {
            var lastFrameTime = System.nanoTime()
            var frameCount = 0
            var lastFpsUpdate = System.currentTimeMillis()

            while (running) {
                try {
                    nativeRun() // Runs one frame

                    frameCount++
                    val now = System.currentTimeMillis()
                    if (now - lastFpsUpdate >= 1000) {
                        _fps.value = frameCount.toDouble()
                        frameCount = 0
                        lastFpsUpdate = now
                    }

                    // Frame timing (target ~60fps for most consoles)
                    val elapsed = System.nanoTime() - lastFrameTime
                    val targetNs = 16_666_666L // ~60fps
                    val sleepNs = targetNs - elapsed
                    if (sleepNs > 0) {
                        Thread.sleep(sleepNs / 1_000_000, (sleepNs % 1_000_000).toInt())
                    }
                    lastFrameTime = System.nanoTime()
                } catch (e: Exception) {
                    // Frame error - continue
                }
            }
        }.apply {
            name = "LibretroEmulation"
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun pause() {
        running = false
        _state.value = NativeEmulatorState.PAUSED
    }

    fun resume() {
        if (gameLoaded) {
            _state.value = NativeEmulatorState.RUNNING
            startEmulation()
        }
    }

    fun stop() {
        running = false
        emulationThread?.join(1000)
        emulationThread = null

        if (gameLoaded) {
            try { nativeUnloadGame() } catch (e: Exception) { }
            gameLoaded = false
        }
        try { nativeDeinit() } catch (e: Exception) { }

        _state.value = NativeEmulatorState.IDLE
        _fps.value = 0.0
    }

    fun saveState(slot: Int): String? {
        if (!gameLoaded) return null
        val path = File(savesDir, "state_slot$slot.sav").absolutePath
        return try {
            if (nativeSaveState(path)) path else null
        } catch (e: Exception) { null }
    }

    fun loadState(slot: Int): Boolean {
        if (!gameLoaded) return false
        val path = File(savesDir, "state_slot$slot.sav").absolutePath
        return try {
            nativeLoadState(path)
        } catch (e: Exception) { false }
    }

    fun setButtonState(port: Int, button: Int, pressed: Boolean) {
        try { nativeSetInput(port, button, if (pressed) 1 else 0) }
        catch (e: Exception) { }
    }

    /**
     * Creates a GLSurfaceView.Renderer that displays emulator output.
     */
    fun createRenderer(): GLSurfaceView.Renderer = object : GLSurfaceView.Renderer {
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            try { nativeOnSurfaceCreated() } catch (e: Exception) { }
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            try { nativeOnSurfaceChanged(width, height) } catch (e: Exception) { }
        }

        override fun onDrawFrame(gl: GL10?) {
            try { nativeOnDrawFrame() } catch (e: Exception) { }
        }
    }

    // === JNI Native Methods ===
    // These are implemented by the libretro frontend native library.
    // For now they're stubs that will be replaced when native code is compiled.

    private fun nativeInit(systemDir: String, savesDir: String) {
        // Will call retro_init() on the loaded core
    }

    private fun nativeDeinit() {
        // Will call retro_deinit()
    }

    private fun nativeLoadGame(romPath: String): Boolean {
        // Will call retro_load_game()
        return true // Stub: pretend success
    }

    private fun nativeUnloadGame() {
        // Will call retro_unload_game()
    }

    private fun nativeRun() {
        // Will call retro_run() - runs one frame
        Thread.sleep(16) // Stub: simulate frame time
    }

    private fun nativeSaveState(path: String): Boolean {
        return true
    }

    private fun nativeLoadState(path: String): Boolean {
        return File(path).exists()
    }

    private fun nativeSetInput(port: Int, button: Int, state: Int) {
        // Will set input state for the core
    }

    private fun nativeOnSurfaceCreated() {}
    private fun nativeOnSurfaceChanged(width: Int, height: Int) {}
    private fun nativeOnDrawFrame() {}
}

enum class NativeEmulatorState {
    IDLE,
    LOADING_CORE,
    CORE_LOADED,
    LOADING_GAME,
    RUNNING,
    PAUSED,
    ERROR
}
