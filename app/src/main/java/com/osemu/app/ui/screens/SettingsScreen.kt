@file:OptIn(ExperimentalMaterial3Api::class)

package com.osemu.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.osemu.app.ui.theme.LocalOsEmuExtras

/**
 * Settings screen mimicking 3DS system settings with categorized sections.
 */
@Composable
fun SettingsScreen(
    autoSaveEnabled: Boolean,
    onAutoSaveToggle: (Boolean) -> Unit,
    autoSaveInterval: Int,
    onAutoSaveIntervalChange: (Int) -> Unit,
    showFps: Boolean,
    onShowFpsToggle: (Boolean) -> Unit,
    vibrationEnabled: Boolean,
    onVibrationToggle: (Boolean) -> Unit,
    audioVolume: Float,
    onAudioVolumeChange: (Float) -> Unit,
    touchOverlayOpacity: Float,
    onTouchOverlayOpacityChange: (Float) -> Unit,
    onManageCores: () -> Unit,
    onManageScanPaths: () -> Unit,
    onNavigateToThemes: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = LocalOsEmuExtras.current

    Column(modifier = modifier.fillMaxSize()) {
        // Top bar
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = extras.statusBarColor,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- Emulation Section ---
            item {
                SettingsSectionHeader(
                    icon = Icons.Default.Star,
                    title = "Emulation"
                )
            }

            item {
                SettingsToggleItem(
                    title = "Auto-save",
                    subtitle = "Automatically save progress",
                    icon = Icons.Default.Save,
                    checked = autoSaveEnabled,
                    onCheckedChange = onAutoSaveToggle
                )
            }

            item {
                SettingsSliderItem(
                    title = "Auto-save interval",
                    subtitle = "${autoSaveInterval}s",
                    icon = Icons.Default.Timer,
                    value = autoSaveInterval.toFloat(),
                    valueRange = 15f..300f,
                    steps = 18,
                    onValueChange = { onAutoSaveIntervalChange(it.toInt()) },
                    enabled = autoSaveEnabled
                )
            }

            item {
                SettingsToggleItem(
                    title = "Show FPS counter",
                    subtitle = "Display frame rate overlay",
                    icon = Icons.Default.Refresh,
                    checked = showFps,
                    onCheckedChange = onShowFpsToggle
                )
            }

            // --- Audio & Haptics ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(
                    icon = Icons.Default.VolumeUp,
                    title = "Audio & Haptics"
                )
            }

            item {
                SettingsSliderItem(
                    title = "Volume",
                    subtitle = "${(audioVolume * 100).toInt()}%",
                    icon = Icons.Default.VolumeUp,
                    value = audioVolume,
                    valueRange = 0f..1f,
                    onValueChange = onAudioVolumeChange
                )
            }

            item {
                SettingsToggleItem(
                    title = "Vibration",
                    subtitle = "Haptic feedback for controls",
                    icon = Icons.Default.Notifications,
                    checked = vibrationEnabled,
                    onCheckedChange = onVibrationToggle
                )
            }

            // --- Display ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(
                    icon = Icons.Default.Phone,
                    title = "Display"
                )
            }

            item {
                SettingsSliderItem(
                    title = "Touch overlay opacity",
                    subtitle = "${(touchOverlayOpacity * 100).toInt()}%",
                    icon = Icons.Default.Create,
                    value = touchOverlayOpacity,
                    valueRange = 0f..1f,
                    onValueChange = onTouchOverlayOpacityChange
                )
            }

            // --- Library ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(
                    icon = Icons.Default.Folder,
                    title = "Library"
                )
            }

            item {
                SettingsClickItem(
                    title = "Scan paths",
                    subtitle = "Manage ROM directories",
                    icon = Icons.Default.Folder,
                    onClick = onManageScanPaths
                )
            }

            item {
                SettingsClickItem(
                    title = "Emulator cores",
                    subtitle = "Manage libretro cores",
                    icon = Icons.Default.Build,
                    onClick = onManageCores
                )
            }

            // --- Appearance ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(
                    icon = Icons.Default.Palette,
                    title = "Appearance"
                )
            }

            item {
                SettingsClickItem(
                    title = "Themes",
                    subtitle = "Customize colors and backgrounds",
                    icon = Icons.Default.Palette,
                    onClick = onNavigateToThemes
                )
            }

            // --- About ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(
                    icon = Icons.Default.Info,
                    title = "About"
                )
            }

            item {
                SettingsInfoItem(
                    title = "Version",
                    value = "1.0.0",
                    icon = Icons.Default.Info
                )
            }

            item {
                SettingsInfoItem(
                    title = "OS-EMU",
                    value = "Retro Emulator Frontend",
                    icon = Icons.Default.Star
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun SettingsSliderItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    steps: Int = 0,
    enabled: Boolean = true
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.6f else 0.3f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.5f)
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.6f else 0.3f)
                    )
                }
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                enabled = enabled,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SettingsClickItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun SettingsInfoItem(
    title: String,
    value: String,
    icon: ImageVector
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
