package com.osemu.app.data.model

/**
 * Per-game emulator settings.
 */
data class GameSettings(
    val gameId: Long,
    val preferredCore: String = "",
    val aspectRatio: AspectRatio = AspectRatio.AUTO,
    val internalResolution: Int = 1,
    val shaderPreset: String? = null,
    val fastForwardSpeed: Float = 2.0f,
    val enableRewind: Boolean = false,
    val audioVolume: Float = 1.0f,
    val touchOverlayOpacity: Float = 0.5f,
    val touchOverlayScale: Float = 1.0f,
    val controlLayout: ControlLayoutType = ControlLayoutType.DEFAULT,
    val buttonMapping: Map<String, String> = emptyMap()
)

enum class ControlLayoutType(val displayName: String) {
    DEFAULT("Default"),
    NINTENDO("Nintendo Style (A right, B bottom)"),
    XBOX("Xbox Style (A bottom, B right)"),
    CUSTOM("Custom")
}

/**
 * Media item for custom backgrounds and music.
 */
data class MediaItem(
    val id: Long = 0,
    val name: String,
    val filePath: String,
    val type: MediaType,
    val thumbnailPath: String? = null,
    val dateAdded: Long = System.currentTimeMillis()
)

enum class MediaType {
    IMAGE,
    VIDEO,
    MUSIC
}

/**
 * Widget configuration for home screen.
 */
data class HomeWidget(
    val id: String,
    val type: WidgetType,
    val title: String,
    val sortOrder: Int = 0,
    val isVisible: Boolean = true,
    val config: Map<String, String> = emptyMap()
)

enum class WidgetType(val displayName: String) {
    RECENT_GAMES("Recently Played"),
    FAVORITE_GAMES("Favorites"),
    COLLECTION("Collection"),
    QUICK_LAUNCH("Quick Launch"),
    STATS("Play Statistics"),
    ACHIEVEMENTS("Achievements"),
    BADGES("My Badges")
}
