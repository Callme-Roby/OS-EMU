package com.osemu.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.osemu.app.core.EmulatorState
import com.osemu.app.data.model.Game

/**
 * Full-screen emulator view with touch overlay controls
 * and an in-game menu for save states, shaders, and settings.
 */
@Composable
fun EmulatorScreen(
    game: Game,
    emulatorState: EmulatorState,
    fps: Double,
    showFps: Boolean,
    touchOverlayOpacity: Float,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSaveState: (Int) -> Unit,
    onLoadState: (Int) -> Unit,
    onToggleFastForward: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showSaveSlots by remember { mutableStateOf(false) }
    var saveMode by remember { mutableStateOf(true) } // true = save, false = load

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        // Emulator rendering surface placeholder
        // In real implementation, this would be a SurfaceView or TextureView
        // connected to the libretro video callback
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            showMenu = !showMenu
                            if (showMenu) onPause() else onResume()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (emulatorState == EmulatorState.LOADING) {
                CircularProgressIndicator(color = Color.White)
            }

            if (emulatorState == EmulatorState.IDLE) {
                Text(
                    text = "Ready to play: ${game.title}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // FPS counter
        if (showFps && emulatorState == EmulatorState.RUNNING) {
            Text(
                text = "%.1f FPS".format(fps),
                color = Color.Yellow,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // Touch overlay controls
        if (emulatorState == EmulatorState.RUNNING && !showMenu) {
            TouchOverlay(
                opacity = touchOverlayOpacity,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Menu button (always visible)
        IconButton(
            onClick = {
                showMenu = !showMenu
                if (showMenu) onPause() else onResume()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .alpha(0.6f)
        ) {
            Icon(
                Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.White
            )
        }

        // In-game menu overlay
        AnimatedVisibility(
            visible = showMenu,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            InGameMenu(
                game = game,
                onResume = {
                    showMenu = false
                    onResume()
                },
                onSaveState = {
                    saveMode = true
                    showSaveSlots = true
                },
                onLoadState = {
                    saveMode = false
                    showSaveSlots = true
                },
                onToggleFastForward = onToggleFastForward,
                onExit = onExit
            )
        }

        // Save/Load slot selector
        if (showSaveSlots) {
            SaveSlotDialog(
                isSaveMode = saveMode,
                onSlotSelected = { slot ->
                    if (saveMode) onSaveState(slot) else onLoadState(slot)
                    showSaveSlots = false
                },
                onDismiss = { showSaveSlots = false }
            )
        }
    }
}

@Composable
private fun InGameMenu(
    game: Game,
    onResume: () -> Unit,
    onSaveState: () -> Unit,
    onLoadState: () -> Unit,
    onToggleFastForward: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .padding(32.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1
                )

                Text(
                    text = game.console.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                MenuButton(Icons.Default.PlayArrow, "Resume", onClick = onResume)
                MenuButton(Icons.Default.Save, "Save State", onClick = onSaveState)
                MenuButton(Icons.Default.FileOpen, "Load State", onClick = onLoadState)
                MenuButton(Icons.Default.FastForward, "Fast Forward", onClick = onToggleFastForward)

                Spacer(modifier = Modifier.height(8.dp))

                MenuButton(
                    icon = Icons.Default.ExitToApp,
                    label = "Exit Game",
                    color = MaterialTheme.colorScheme.error,
                    onClick = onExit
                )
            }
        }
    }
}

@Composable
private fun MenuButton(
    icon: ImageVector,
    label: String,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
private fun SaveSlotDialog(
    isSaveMode: Boolean,
    onSlotSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isSaveMode) "Save State" else "Load State")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                (0..9).forEach { slot ->
                    OutlinedButton(
                        onClick = { onSlotSelected(slot) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            if (isSaveMode) Icons.Default.Save else Icons.Default.FileOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Slot ${slot + 1}")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * On-screen touch controls overlay.
 * D-pad, A/B/X/Y, Start/Select, L/R shoulders.
 */
@Composable
private fun TouchOverlay(
    opacity: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.alpha(opacity)) {
        // D-Pad (bottom-left)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DPadButton("Up")
            Row {
                DPadButton("Left")
                Spacer(modifier = Modifier.size(40.dp))
                DPadButton("Right")
            }
            DPadButton("Down")
        }

        // Action buttons (bottom-right)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActionButton("X", Color(0xFF4FC3F7))
            Row {
                ActionButton("Y", Color(0xFF66BB6A))
                Spacer(modifier = Modifier.size(40.dp))
                ActionButton("A", Color(0xFFEF5350))
            }
            ActionButton("B", Color(0xFFFFCA28))
        }

        // Start/Select (bottom-center)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SmallButton("SEL")
            SmallButton("START")
        }

        // L/R shoulders (top)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ShoulderButton("L")
            ShoulderButton("R")
        }
    }
}

@Composable
private fun DPadButton(direction: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (direction) {
                "Up" -> "\u25B2"
                "Down" -> "\u25BC"
                "Left" -> "\u25C0"
                "Right" -> "\u25B6"
                else -> ""
            },
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun ActionButton(label: String, color: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun SmallButton(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ShoulderButton(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, style = MaterialTheme.typography.labelMedium)
    }
}
