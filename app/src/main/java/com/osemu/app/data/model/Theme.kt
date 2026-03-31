package com.osemu.app.data.model

import androidx.compose.ui.graphics.Color

data class Theme(
    val id: String,
    val name: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val backgroundColor: Long,
    val surfaceColor: Long,
    val topScreenBackground: String? = null, // path to custom image
    val bottomScreenBackground: String? = null,
    val iconStyle: IconStyle = IconStyle.ROUNDED,
    val fontScale: Float = 1.0f,
    val isBuiltIn: Boolean = true
) {
    companion object {
        val DEFAULT = Theme(
            id = "default_white",
            name = "3DS White",
            primaryColor = 0xFF4FC3F7,
            secondaryColor = 0xFF81D4FA,
            backgroundColor = 0xFFF5F5F5,
            surfaceColor = 0xFFFFFFFF
        )

        val DARK = Theme(
            id = "dark",
            name = "Dark Mode",
            primaryColor = 0xFF4FC3F7,
            secondaryColor = 0xFF0288D1,
            backgroundColor = 0xFF1A1A2E,
            surfaceColor = 0xFF16213E
        )

        val NINTENDO_RED = Theme(
            id = "nintendo_red",
            name = "Nintendo Red",
            primaryColor = 0xFFE60012,
            secondaryColor = 0xFFFF1744,
            backgroundColor = 0xFFFFF5F5,
            surfaceColor = 0xFFFFFFFF
        )

        val SEGA_BLUE = Theme(
            id = "sega_blue",
            name = "Sega Blue",
            primaryColor = 0xFF0060A8,
            secondaryColor = 0xFF1976D2,
            backgroundColor = 0xFFF0F4FF,
            surfaceColor = 0xFFFFFFFF
        )

        val PINK = Theme(
            id = "pink",
            name = "Coral Pink",
            primaryColor = 0xFFFF6B9D,
            secondaryColor = 0xFFF48FB1,
            backgroundColor = 0xFFFFF0F5,
            surfaceColor = 0xFFFFFFFF
        )

        val builtInThemes = listOf(DEFAULT, DARK, NINTENDO_RED, SEGA_BLUE, PINK)
    }
}

enum class IconStyle(val displayName: String) {
    ROUNDED("Rounded"),
    SQUARE("Square"),
    CIRCLE("Circle"),
    SQUIRCLE("Squircle")
}
