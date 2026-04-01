package com.osemu.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.osemu.app.data.model.Game
import com.osemu.app.data.model.IconStyle
import com.osemu.app.ui.theme.LocalOsEmuExtras
import com.osemu.app.ui.theme.OsEmuColors

/**
 * 3DS-style app icon tile used in home menu grids.
 */
@Composable
fun GameIcon(
    game: Game,
    isSelected: Boolean = false,
    iconSize: Dp = 60.dp,
    iconStyle: IconStyle = IconStyle.ROUNDED,
    onClick: () -> Unit = {}
) {
    val extras = LocalOsEmuExtras.current
    val shape = when (iconStyle) {
        IconStyle.ROUNDED -> RoundedCornerShape(14.dp)
        IconStyle.SQUARE -> RoundedCornerShape(4.dp)
        IconStyle.CIRCLE -> RoundedCornerShape(50)
        IconStyle.SQUIRCLE -> RoundedCornerShape(18.dp)
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )

    // Console-specific colors for icon background
    val iconBgColor = when (game.console.manufacturer) {
        "Nintendo" -> Color(0xFFE8F5E9)
        "Sega" -> Color(0xFFE3F2FD)
        "Sony" -> Color(0xFFEDE7F6)
        else -> extras.iconBackgroundColor
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(iconSize + 16.dp)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .scale(scale)
                .shadow(if (isSelected) 6.dp else 2.dp, shape)
                .clip(shape)
                .background(if (game.boxArtPath != null) Color.Transparent else iconBgColor)
                .then(
                    if (isSelected) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
                    } else {
                        Modifier.border(0.5.dp, extras.iconBorderColor, shape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (game.boxArtPath != null) {
                AsyncImage(
                    model = game.boxArtPath,
                    contentDescription = game.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = getConsoleIcon(game.console.manufacturer),
                    contentDescription = game.title,
                    tint = getConsoleTint(game.console.manufacturer),
                    modifier = Modifier.size(iconSize * 0.45f)
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = game.title,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 12.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * System menu icon (Settings, Library, etc.)
 */
@Composable
fun SystemIcon(
    icon: ImageVector,
    label: String,
    color: Color = MaterialTheme.colorScheme.primary,
    isSelected: Boolean = false,
    iconSize: Dp = 56.dp,
    iconStyle: IconStyle = IconStyle.ROUNDED,
    onClick: () -> Unit = {}
) {
    val extras = LocalOsEmuExtras.current
    val shape = when (iconStyle) {
        IconStyle.ROUNDED -> RoundedCornerShape(14.dp)
        IconStyle.SQUARE -> RoundedCornerShape(4.dp)
        IconStyle.CIRCLE -> RoundedCornerShape(50)
        IconStyle.SQUIRCLE -> RoundedCornerShape(18.dp)
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "sysIconScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(iconSize + 16.dp)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .scale(scale)
                .shadow(if (isSelected) 6.dp else 2.dp, shape)
                .clip(shape)
                .background(extras.iconBackgroundColor)
                .then(
                    if (isSelected) {
                        Modifier.border(2.dp, color, shape)
                    } else {
                        Modifier.border(0.5.dp, extras.iconBorderColor, shape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(iconSize * 0.45f)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 12.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

fun getConsoleIcon(manufacturer: String): ImageVector {
    return when (manufacturer) {
        "Nintendo" -> Icons.Default.Star
        "Sega" -> Icons.Default.Star
        "Sony" -> Icons.Default.Star
        else -> Icons.Default.Star
    }
}

fun getConsoleTint(manufacturer: String): Color {
    return when (manufacturer) {
        "Nintendo" -> OsEmuColors.Red
        "Sega" -> OsEmuColors.Blue600
        "Sony" -> OsEmuColors.Purple
        else -> OsEmuColors.Blue500
    }
}
