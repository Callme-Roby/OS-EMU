package com.osemu.app.data.model

data class ShaderPreset(
    val id: String,
    val name: String,
    val description: String,
    val shaderPath: String,
    val category: ShaderCategory,
    val isBuiltIn: Boolean = true,
    val parameters: Map<String, Float> = emptyMap()
)

enum class ShaderCategory(val displayName: String) {
    CRT("CRT / Scanlines"),
    LCD("LCD Effects"),
    SMOOTHING("Smoothing / Anti-Alias"),
    SHARPENING("Sharpening"),
    COLOR("Color Correction"),
    RETRO("Retro Style"),
    CUSTOM("Custom")
}

data class EmulatorConfig(
    val gameId: Long = 0,
    val console: Console = Console.NES,
    val coreName: String = "",
    val shaderPreset: String? = null,
    val aspectRatio: AspectRatio = AspectRatio.AUTO,
    val internalResolution: Int = 1, // multiplier
    val enableFastForward: Boolean = true,
    val fastForwardSpeed: Float = 2.0f,
    val enableRewind: Boolean = false,
    val rewindBufferSeconds: Int = 30,
    val autoSaveEnabled: Boolean = true,
    val autoSaveIntervalSeconds: Int = 60,
    val audioEnabled: Boolean = true,
    val audioVolume: Float = 1.0f,
    val enableVibration: Boolean = true,
    val touchOverlayOpacity: Float = 0.5f,
    val touchOverlayScale: Float = 1.0f
)

enum class AspectRatio(val displayName: String, val ratio: Float?) {
    AUTO("Auto", null),
    RATIO_4_3("4:3", 4f / 3f),
    RATIO_16_9("16:9", 16f / 9f),
    RATIO_16_10("16:10", 16f / 10f),
    RATIO_1_1("1:1", 1f),
    STRETCH("Stretch to fill", 0f)
}
