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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osemu.app.core.EmulatorState
import com.osemu.app.data.model.*
import com.osemu.app.ui.theme.OsEmuColors

/**
 * Full-screen emulator view with console-specific touch controls.
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

    val layout = remember(game.console) { ControllerLayouts.forConsole(game.console) }

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
                .fillMaxHeight(0.42f)
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
            // Game render area with console-specific aspect ratio
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .aspectRatio(layout.screenRatio)
                    .clip(RoundedCornerShape(6.dp))
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = OsEmuColors.Blue400.copy(alpha = 0.4f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = game.title,
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.titleSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = game.console.displayName,
                                color = Color.White.copy(alpha = 0.4f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = game.title,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
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

        // Menu button
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

        // Console-specific touch controls
        ConsoleControls(
            layout = layout,
            opacity = touchOverlayOpacity.coerceAtLeast(0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.58f)
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

// ==================== Console-Specific Controls ====================

@Composable
private fun ConsoleControls(
    layout: ControllerLayout,
    opacity: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.alpha(opacity)) {

        // Shoulder buttons (top)
        if (layout.shoulderButtons.isNotEmpty()) {
            val leftShoulders = layout.shoulderButtons.filter { it.side == ShoulderSide.LEFT }
            val rightShoulders = layout.shoulderButtons.filter { it.side == ShoulderSide.RIGHT }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left shoulders stacked
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    leftShoulders.forEach { btn ->
                        ShoulderButton(btn.label)
                    }
                }
                // Right shoulders stacked
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    rightShoulders.forEach { btn ->
                        ShoulderButton(btn.label)
                    }
                }
            }
        }

        // D-Pad (bottom-left)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 36.dp)
        ) {
            DPad()
        }

        // Analog stick indicator (if console has it)
        if (layout.analogStick) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 56.dp, top = 8.dp)
            ) {
                AnalogStick("L")
            }
        }

        // C-Stick / Right analog (if console has it)
        if (layout.cStick) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 56.dp, top = 8.dp)
            ) {
                AnalogStick("R")
            }
        }

        // Action buttons (bottom-right) - adapt based on button count
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 36.dp)
        ) {
            when (layout.actionButtons.size) {
                2 -> TwoButtonLayout(layout.actionButtons)
                4 -> DiamondButtonLayout(layout.actionButtons)
                6 -> SixButtonLayout(layout.actionButtons)
                else -> DiamondButtonLayout(layout.actionButtons.take(4))
            }
        }

        // Extra buttons (e.g., N64 C-buttons) - placed above main action buttons
        if (layout.extraButtons.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
            ) {
                DiamondButtonLayout(layout.extraButtons.take(4), buttonSize = 32.dp)
            }
        }

        // Center buttons (Start, Select, etc.)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            layout.centerButtons.forEach { btn ->
                SmallButton(btn.label)
            }
        }
    }
}

// ==================== D-Pad ====================

@Composable
private fun DPad() {
    Box(
        modifier = Modifier
            .size(136.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DPadButton("\u25B2")
            Row(verticalAlignment = Alignment.CenterVertically) {
                DPadButton("\u25C0")
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                )
                DPadButton("\u25B6")
            }
            DPadButton("\u25BC")
        }
    }
}

@Composable
private fun DPadButton(symbol: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isPressed) Color.White.copy(alpha = 0.45f)
                else Color.White.copy(alpha = 0.22f)
            )
            .clickable(interactionSource = interactionSource, indication = null) { },
        contentAlignment = Alignment.Center
    ) {
        Text(text = symbol, color = Color.White, fontSize = 13.sp)
    }
}

// ==================== Action Button Layouts ====================

/**
 * Two-button layout (GB, GBA, NES, Game Gear, Master System)
 * Horizontal: [B] [A]  or  [1] [2]
 */
@Composable
private fun TwoButtonLayout(buttons: List<ActionButtonDef>) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position buttons: LEFT/BOTTOM first, RIGHT second
            val sorted = buttons.sortedBy {
                when (it.position) {
                    ButtonPosition.LEFT, ButtonPosition.BOTTOM -> 0
                    else -> 1
                }
            }
            sorted.forEach { btn ->
                ActionButton(
                    label = btn.label,
                    color = btn.color,
                    size = 48.dp
                )
            }
        }
    }
}

/**
 * Diamond layout (SNES, NDS, 3DS, PS1, PSP, etc.)
 *      [Top]
 * [Left]   [Right]
 *    [Bottom]
 */
@Composable
private fun DiamondButtonLayout(
    buttons: List<ActionButtonDef>,
    buttonSize: Dp = 40.dp
) {
    val top = buttons.find { it.position == ButtonPosition.TOP }
    val right = buttons.find { it.position == ButtonPosition.RIGHT }
    val bottom = buttons.find { it.position == ButtonPosition.BOTTOM }
    val left = buttons.find { it.position == ButtonPosition.LEFT }

    Box(
        modifier = Modifier
            .size(buttonSize * 3 + 16.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            top?.let { ActionButton(it.label, it.color, buttonSize) }
                ?: Spacer(modifier = Modifier.size(buttonSize))

            Row(verticalAlignment = Alignment.CenterVertically) {
                left?.let { ActionButton(it.label, it.color, buttonSize) }
                    ?: Spacer(modifier = Modifier.size(buttonSize))

                Spacer(modifier = Modifier.width(buttonSize - 8.dp))

                right?.let { ActionButton(it.label, it.color, buttonSize) }
                    ?: Spacer(modifier = Modifier.size(buttonSize))
            }

            bottom?.let { ActionButton(it.label, it.color, buttonSize) }
                ?: Spacer(modifier = Modifier.size(buttonSize))
        }
    }
}

/**
 * Six-button layout (Genesis, Saturn, Arcade)
 *  [X/4] [Y/5] [Z/6]
 *  [A/1] [B/2] [C/3]
 */
@Composable
private fun SixButtonLayout(buttons: List<ActionButtonDef>) {
    val topRow = buttons.filter {
        it.position in listOf(ButtonPosition.TOP_LEFT, ButtonPosition.TOP, ButtonPosition.TOP_RIGHT)
    }
    val bottomRow = buttons.filter {
        it.position in listOf(ButtonPosition.BOTTOM_LEFT, ButtonPosition.BOTTOM, ButtonPosition.BOTTOM_RIGHT)
    }

    Box(
        modifier = Modifier
            .width(152.dp)
            .height(108.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                topRow.forEach { btn ->
                    ActionButton(btn.label, btn.color, 38.dp)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                bottomRow.forEach { btn ->
                    ActionButton(btn.label, btn.color, 38.dp)
                }
            }
        }
    }
}

// ==================== Individual Buttons ====================

@Composable
private fun ActionButton(label: String, color: Color, size: Dp = 40.dp) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (isPressed) color.copy(alpha = 0.9f)
                else color.copy(alpha = 0.55f)
            )
            .clickable(interactionSource = interactionSource, indication = null) { },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = if (label.length > 2) 10.sp else 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AnalogStick(label: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(interactionSource = interactionSource, indication = null) { },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isPressed) Color.White.copy(alpha = 0.4f)
                    else Color.White.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
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
                else Color.White.copy(alpha = 0.15f)
            )
            .clickable(interactionSource = interactionSource, indication = null) { }
            .padding(horizontal = 12.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = if (label.length > 5) 8.sp else 10.sp,
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
            .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
            .background(
                if (isPressed) Color.White.copy(alpha = 0.4f)
                else Color.White.copy(alpha = 0.18f)
            )
            .clickable(interactionSource = interactionSource, indication = null) { }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==================== Menu & Dialogs ====================

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
