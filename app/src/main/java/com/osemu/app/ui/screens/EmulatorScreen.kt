package com.osemu.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osemu.app.core.EmulatorState
import com.osemu.app.data.model.Game
import com.osemu.app.ui.theme.OsEmuColors

/**
 * Full-screen emulator view with touch overlay controls
 * and an in-game menu for save states and settings.
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
    var saveMode by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    ) {
        // Game display area (top portion)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .align(Alignment.TopCenter)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            showMenu = !showMenu
                            if (showMenu) onPause() else onResume()
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Game render area placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                when (emulatorState) {
                    EmulatorState.LOADING -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = OsEmuColors.Blue400,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Loading...",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    EmulatorState.ERROR -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = OsEmuColors.Yellow,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Core not available",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    EmulatorState.PAUSED -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Pause,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "PAUSED",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> {
                        // Running or Idle - show game info as placeholder
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = OsEmuColors.Blue400.copy(alpha = 0.4f),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = game.title,
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = game.console.displayName,
                                color = Color.White.copy(alpha = 0.4f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Game title bar below screen
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = game.title,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                maxLines = 1
            )
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

        // Menu button (top-left, always visible)
        IconButton(
            onClick = {
                showMenu = !showMenu
                if (showMenu) onPause() else onResume()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
        ) {
            Icon(
                Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.White.copy(alpha = 0.7f)
            )
        }

        // Touch controls (always visible in bottom portion)
        TouchOverlay(
            opacity = touchOverlayOpacity.coerceAtLeast(0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .align(Alignment.BottomCenter)
        )

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
                MenuButton(Icons.Default.Folder, "Load State", onClick = onLoadState)
                MenuButton(Icons.Default.SkipNext, "Fast Forward", onClick = onToggleFastForward)

                Spacer(modifier = Modifier.height(8.dp))

                MenuButton(
                    icon = Icons.Default.Close,
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
                            if (isSaveMode) Icons.Default.Save else Icons.Default.Folder,
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
 * D-pad on left, action buttons on right, Start/Select center, L/R top.
 */
@Composable
private fun TouchOverlay(
    opacity: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.alpha(opacity)) {
        // L/R shoulders (top of control area)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ShoulderButton("L")
            ShoulderButton("R")
        }

        // D-Pad (bottom-left)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 40.dp)
        ) {
            // D-pad background circle
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DPadButton("Up")
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DPadButton("Left")
                        // Center dot
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                        DPadButton("Right")
                    }
                    DPadButton("Down")
                }
            }
        }

        // Action buttons (bottom-right) - diamond layout
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ActionButton("X", Color(0xFF42A5F5))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActionButton("Y", Color(0xFF66BB6A))
                        Spacer(modifier = Modifier.width(36.dp))
                        ActionButton("A", Color(0xFFEF5350))
                    }
                    ActionButton("B", Color(0xFFFFCA28))
                }
            }
        }

        // Start/Select (bottom-center)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SmallButton("SELECT")
            SmallButton("START")
        }
    }
}

@Composable
private fun DPadButton(direction: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isPressed) Color.White.copy(alpha = 0.5f)
                else Color.White.copy(alpha = 0.25f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { },
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
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ActionButton(label: String, color: Color) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(
                if (isPressed) color.copy(alpha = 0.9f)
                else color.copy(alpha = 0.55f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SmallButton(label: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isPressed) Color.White.copy(alpha = 0.35f)
                else Color.White.copy(alpha = 0.18f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ShoulderButton(label: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            .background(
                if (isPressed) Color.White.copy(alpha = 0.4f)
                else Color.White.copy(alpha = 0.2f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { }
            .padding(horizontal = 32.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
