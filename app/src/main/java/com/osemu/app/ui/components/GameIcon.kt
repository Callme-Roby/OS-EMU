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

/**
 * 3DS-style app icon tile used in home menu grids.
 */
@Composable
fun GameIcon(
    game: Game,
    isSelected: Boolean = false,
    iconSize: Dp = 64.dp,
    iconStyle: IconStyle = IconStyle.ROUNDED,
    onClick: () -> Unit = {}
) {
    val extras = LocalOsEmuExtras.current
    val shape = when (iconStyle) {
        IconStyle.ROUNDED -> RoundedCornerShape(12.dp)
        IconStyle.SQUARE -> RoundedCornerShape(4.dp)
        IconStyle.CIRCLE -> RoundedCornerShape(50)
        IconStyle.SQUIRCLE -> RoundedCornerShape(16.dp)
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
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
                .shadow(if (isSelected) 8.dp else 2.dp, shape)
                .clip(shape)
                .background(extras.iconBackgroundColor)
                .then(
                    if (isSelected) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
                    } else {
                        Modifier.border(1.dp, extras.iconBorderColor, shape)
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
                // Default icon based on console
                Icon(
                    imageVector = getConsoleIcon(game.console.manufacturer),
                    contentDescription = game.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(iconSize * 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = game.title,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * System menu icon (Settings, eShop-style, etc.)
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
        IconStyle.ROUNDED -> RoundedCornerShape(12.dp)
        IconStyle.SQUARE -> RoundedCornerShape(4.dp)
        IconStyle.CIRCLE -> RoundedCornerShape(50)
        IconStyle.SQUIRCLE -> RoundedCornerShape(16.dp)
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
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
                        Modifier.border(1.dp, extras.iconBorderColor, shape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(iconSize * 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
