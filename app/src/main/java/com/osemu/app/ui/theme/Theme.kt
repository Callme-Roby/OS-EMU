package com.osemu.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.osemu.app.data.model.Theme as AppTheme

private val LightColorScheme = lightColorScheme(
    primary = OsEmuColors.Blue500,
    onPrimary = Color.White,
    primaryContainer = OsEmuColors.Blue100,
    onPrimaryContainer = OsEmuColors.Blue700,
    secondary = OsEmuColors.Blue300,
    onSecondary = Color.White,
    secondaryContainer = OsEmuColors.Blue100,
    onSecondaryContainer = OsEmuColors.Blue700,
    tertiary = OsEmuColors.Green,
    onTertiary = Color.White,
    background = OsEmuColors.GrayLight,
    onBackground = OsEmuColors.Gray900,
    surface = OsEmuColors.White,
    onSurface = OsEmuColors.Gray900,
    surfaceVariant = OsEmuColors.Gray100,
    onSurfaceVariant = OsEmuColors.Gray700,
    outline = OsEmuColors.Gray300,
    error = OsEmuColors.Red,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = OsEmuColors.Blue400,
    onPrimary = OsEmuColors.DarkBg,
    primaryContainer = OsEmuColors.DarkSurfaceVariant,
    onPrimaryContainer = OsEmuColors.Blue200,
    secondary = OsEmuColors.Blue300,
    onSecondary = OsEmuColors.DarkBg,
    secondaryContainer = OsEmuColors.DarkSurfaceVariant,
    onSecondaryContainer = OsEmuColors.Blue200,
    tertiary = OsEmuColors.Green,
    onTertiary = OsEmuColors.DarkBg,
    background = OsEmuColors.DarkBg,
    onBackground = OsEmuColors.Gray100,
    surface = OsEmuColors.DarkSurface,
    onSurface = OsEmuColors.Gray100,
    surfaceVariant = OsEmuColors.DarkSurfaceVariant,
    onSurfaceVariant = OsEmuColors.Gray300,
    outline = OsEmuColors.Gray500,
    error = OsEmuColors.Red,
    onError = Color.White
)

fun buildColorScheme(theme: AppTheme, isDark: Boolean): ColorScheme {
    val primary = Color(theme.primaryColor)
    val secondary = Color(theme.secondaryColor)
    val background = Color(theme.backgroundColor)
    val surface = Color(theme.surfaceColor)

    return if (isDark) {
        darkColorScheme(
            primary = primary,
            secondary = secondary,
            background = background,
            surface = surface,
            onPrimary = Color.White,
            onBackground = OsEmuColors.Gray100,
            onSurface = OsEmuColors.Gray100
        )
    } else {
        lightColorScheme(
            primary = primary,
            secondary = secondary,
            background = background,
            surface = surface,
            onPrimary = Color.White,
            onBackground = OsEmuColors.Gray900,
            onSurface = OsEmuColors.Gray900
        )
    }
}

// Custom theme extensions for 3DS-specific styling
data class OsEmuThemeExtras(
    val topScreenGradientStart: Color = OsEmuColors.TopScreenGradientStart,
    val topScreenGradientEnd: Color = OsEmuColors.TopScreenGradientEnd,
    val topScreenBackground: Color = OsEmuColors.TopScreenGradientStart,
    val bottomScreenBackground: Color = OsEmuColors.BottomScreenBg,
    val statusBarColor: Color = OsEmuColors.StatusBarBlue,
    val iconBackgroundColor: Color = OsEmuColors.IconCardBg,
    val iconBorderColor: Color = OsEmuColors.IconCardBorder,
    val tabBarColor: Color = OsEmuColors.TabBarBg,
    val selectedTabColor: Color = OsEmuColors.TabSelected,
    val unselectedTabColor: Color = OsEmuColors.TabUnselected
)

val LocalOsEmuExtras = staticCompositionLocalOf { OsEmuThemeExtras() }

@Composable
fun OsEmuTheme(
    appTheme: AppTheme = AppTheme.DEFAULT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (appTheme == AppTheme.DEFAULT || appTheme == AppTheme.DARK) {
        if (darkTheme || appTheme == AppTheme.DARK) DarkColorScheme else LightColorScheme
    } else {
        buildColorScheme(appTheme, darkTheme)
    }

    val extras = if (darkTheme || appTheme == AppTheme.DARK) {
        OsEmuThemeExtras(
            topScreenGradientStart = OsEmuColors.DarkSurfaceVariant,
            topScreenGradientEnd = OsEmuColors.DarkBg,
            topScreenBackground = OsEmuColors.DarkBg,
            bottomScreenBackground = OsEmuColors.DarkSurface,
            statusBarColor = OsEmuColors.DarkSurfaceVariant,
            iconBackgroundColor = OsEmuColors.DarkSurface,
            iconBorderColor = OsEmuColors.DarkSurfaceVariant,
            tabBarColor = OsEmuColors.DarkSurfaceVariant,
            selectedTabColor = OsEmuColors.Blue400,
            unselectedTabColor = OsEmuColors.Gray500
        )
    } else {
        OsEmuThemeExtras(
            topScreenGradientStart = Color(appTheme.primaryColor).copy(alpha = 0.8f),
            topScreenGradientEnd = Color(appTheme.primaryColor),
            topScreenBackground = Color(appTheme.backgroundColor),
            bottomScreenBackground = Color(appTheme.surfaceColor),
            statusBarColor = Color(appTheme.primaryColor),
            iconBackgroundColor = Color(appTheme.surfaceColor),
            iconBorderColor = OsEmuColors.Gray200,
            tabBarColor = OsEmuColors.TabBarBg,
            selectedTabColor = Color(appTheme.primaryColor),
            unselectedTabColor = OsEmuColors.TabUnselected
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = extras.topScreenGradientEnd.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    CompositionLocalProvider(LocalOsEmuExtras provides extras) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = OsEmuTypography,
            content = content
        )
    }
}
