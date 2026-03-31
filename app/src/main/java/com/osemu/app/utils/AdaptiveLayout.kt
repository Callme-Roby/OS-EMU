package com.osemu.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Device form factor detection for adaptive UI.
 * Supports smartphones (portrait/landscape), handhelds (Ayn Thor, Steam Deck),
 * tablets, and TV/desktop modes.
 */
enum class DeviceFormFactor {
    PHONE_PORTRAIT,      // Standard phone vertical
    PHONE_LANDSCAPE,     // Standard phone horizontal
    HANDHELD_LANDSCAPE,  // Gaming handhelds (Ayn Thor, Steam Deck, ROG Ally)
    TABLET_PORTRAIT,     // Tablet vertical
    TABLET_LANDSCAPE,    // Tablet horizontal
    TV                   // Android TV / large screen
}

data class LayoutConfig(
    val formFactor: DeviceFormFactor,
    val gridColumns: Int,
    val iconSize: Dp,
    val showDualPanel: Boolean,
    val topPanelWeight: Float,
    val bottomPanelWeight: Float,
    val useSidePanel: Boolean, // side-by-side instead of stacked
    val touchOverlayScale: Float,
    val statusBarHeight: Dp
)

@Composable
fun rememberLayoutConfig(): LayoutConfig {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    val isLandscape = screenWidthDp > screenHeightDp
    val smallestWidth = minOf(screenWidthDp, screenHeightDp)
    val largestWidth = maxOf(screenWidthDp, screenHeightDp)

    val formFactor = when {
        // TV or very large display
        smallestWidth >= 900 -> DeviceFormFactor.TV

        // Tablet
        smallestWidth >= 600 -> {
            if (isLandscape) DeviceFormFactor.TABLET_LANDSCAPE
            else DeviceFormFactor.TABLET_PORTRAIT
        }

        // Handheld gaming device (wide landscape, moderate height)
        isLandscape && largestWidth >= 700 && screenHeightDp in 300..500 -> {
            DeviceFormFactor.HANDHELD_LANDSCAPE
        }

        // Phone
        isLandscape -> DeviceFormFactor.PHONE_LANDSCAPE
        else -> DeviceFormFactor.PHONE_PORTRAIT
    }

    return when (formFactor) {
        DeviceFormFactor.PHONE_PORTRAIT -> LayoutConfig(
            formFactor = formFactor,
            gridColumns = 4,
            iconSize = 56.dp,
            showDualPanel = true,
            topPanelWeight = 0.38f,
            bottomPanelWeight = 0.62f,
            useSidePanel = false,
            touchOverlayScale = 1.0f,
            statusBarHeight = 28.dp
        )

        DeviceFormFactor.PHONE_LANDSCAPE -> LayoutConfig(
            formFactor = formFactor,
            gridColumns = 6,
            iconSize = 52.dp,
            showDualPanel = true,
            topPanelWeight = 0.4f,
            bottomPanelWeight = 0.6f,
            useSidePanel = true,
            touchOverlayScale = 0.9f,
            statusBarHeight = 24.dp
        )

        DeviceFormFactor.HANDHELD_LANDSCAPE -> LayoutConfig(
            formFactor = formFactor,
            gridColumns = 7,
            iconSize = 60.dp,
            showDualPanel = true,
            topPanelWeight = 0.35f,
            bottomPanelWeight = 0.65f,
            useSidePanel = true,
            touchOverlayScale = 1.1f,
            statusBarHeight = 24.dp
        )

        DeviceFormFactor.TABLET_PORTRAIT -> LayoutConfig(
            formFactor = formFactor,
            gridColumns = 5,
            iconSize = 72.dp,
            showDualPanel = true,
            topPanelWeight = 0.4f,
            bottomPanelWeight = 0.6f,
            useSidePanel = false,
            touchOverlayScale = 1.2f,
            statusBarHeight = 32.dp
        )

        DeviceFormFactor.TABLET_LANDSCAPE -> LayoutConfig(
            formFactor = formFactor,
            gridColumns = 8,
            iconSize = 72.dp,
            showDualPanel = true,
            topPanelWeight = 0.4f,
            bottomPanelWeight = 0.6f,
            useSidePanel = true,
            touchOverlayScale = 1.3f,
            statusBarHeight = 32.dp
        )

        DeviceFormFactor.TV -> LayoutConfig(
            formFactor = formFactor,
            gridColumns = 10,
            iconSize = 80.dp,
            showDualPanel = false,
            topPanelWeight = 0.3f,
            bottomPanelWeight = 0.7f,
            useSidePanel = true,
            touchOverlayScale = 0f, // no touch overlay on TV
            statusBarHeight = 36.dp
        )
    }
}
