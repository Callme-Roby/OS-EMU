package com.osemu.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.osemu.app.data.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "os_emu_settings"
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME_ID = stringPreferencesKey("theme_id")
        val CUSTOM_BG_TOP = stringPreferencesKey("custom_bg_top")
        val CUSTOM_BG_BOTTOM = stringPreferencesKey("custom_bg_bottom")
        val AUTO_SAVE_ENABLED = booleanPreferencesKey("auto_save_enabled")
        val AUTO_SAVE_INTERVAL = intPreferencesKey("auto_save_interval_seconds")
        val DEFAULT_SHADER = stringPreferencesKey("default_shader")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val AUDIO_VOLUME = floatPreferencesKey("audio_volume")
        val TOUCH_OVERLAY_OPACITY = floatPreferencesKey("touch_overlay_opacity")
        val TOUCH_OVERLAY_SCALE = floatPreferencesKey("touch_overlay_scale")
        val SHOW_FPS = booleanPreferencesKey("show_fps")
        val SCAN_PATHS = stringSetPreferencesKey("scan_paths")
        val CORES_PATH = stringPreferencesKey("cores_path")
        val ORIENTATION_LOCK = stringPreferencesKey("orientation_lock")
        val STARTUP_CONSOLE_FILTER = stringPreferencesKey("startup_console_filter")
    }

    val themeId: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.THEME_ID] ?: Theme.DEFAULT.id
    }

    val autoSaveEnabled: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.AUTO_SAVE_ENABLED] ?: true
    }

    val autoSaveInterval: Flow<Int> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.AUTO_SAVE_INTERVAL] ?: 60
    }

    val showFps: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.SHOW_FPS] ?: false
    }

    val scanPaths: Flow<Set<String>> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.SCAN_PATHS] ?: emptySet()
    }

    val coresPath: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.CORES_PATH] ?: ""
    }

    val vibrationEnabled: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.VIBRATION_ENABLED] ?: true
    }

    val audioVolume: Flow<Float> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.AUDIO_VOLUME] ?: 1.0f
    }

    val touchOverlayOpacity: Flow<Float> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.TOUCH_OVERLAY_OPACITY] ?: 0.5f
    }

    val customBackgroundTop: Flow<String?> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.CUSTOM_BG_TOP]
    }

    val customBackgroundBottom: Flow<String?> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.CUSTOM_BG_BOTTOM]
    }

    suspend fun setTheme(themeId: String) {
        context.settingsDataStore.edit { it[Keys.THEME_ID] = themeId }
    }

    suspend fun setAutoSave(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.AUTO_SAVE_ENABLED] = enabled }
    }

    suspend fun setAutoSaveInterval(seconds: Int) {
        context.settingsDataStore.edit { it[Keys.AUTO_SAVE_INTERVAL] = seconds }
    }

    suspend fun setShowFps(show: Boolean) {
        context.settingsDataStore.edit { it[Keys.SHOW_FPS] = show }
    }

    suspend fun setScanPaths(paths: Set<String>) {
        context.settingsDataStore.edit { it[Keys.SCAN_PATHS] = paths }
    }

    suspend fun setCoresPath(path: String) {
        context.settingsDataStore.edit { it[Keys.CORES_PATH] = path }
    }

    suspend fun setVibration(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled }
    }

    suspend fun setAudioVolume(volume: Float) {
        context.settingsDataStore.edit { it[Keys.AUDIO_VOLUME] = volume }
    }

    suspend fun setTouchOverlayOpacity(opacity: Float) {
        context.settingsDataStore.edit { it[Keys.TOUCH_OVERLAY_OPACITY] = opacity }
    }

    suspend fun setCustomBackgrounds(topPath: String?, bottomPath: String?) {
        context.settingsDataStore.edit { prefs ->
            if (topPath != null) prefs[Keys.CUSTOM_BG_TOP] = topPath
            else prefs.remove(Keys.CUSTOM_BG_TOP)
            if (bottomPath != null) prefs[Keys.CUSTOM_BG_BOTTOM] = bottomPath
            else prefs.remove(Keys.CUSTOM_BG_BOTTOM)
        }
    }
}
