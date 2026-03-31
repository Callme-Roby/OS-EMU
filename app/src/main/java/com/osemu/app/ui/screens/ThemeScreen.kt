package com.osemu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.osemu.app.data.model.Theme
import com.osemu.app.ui.theme.LocalOsEmuExtras

/**
 * Theme customization screen - allows selecting built-in themes
 * and customizing backgrounds, similar to 3DS HOME Menu Settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(
    currentThemeId: String,
    onThemeSelected: (Theme) -> Unit,
    onCustomBackgroundTop: () -> Unit,
    onCustomBackgroundBottom: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = LocalOsEmuExtras.current

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("HOME Menu Settings") },
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

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section: Built-in themes
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Themes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(Theme.builtInThemes) { theme ->
                ThemePreviewCard(
                    theme = theme,
                    isSelected = theme.id == currentThemeId,
                    onClick = { onThemeSelected(theme) }
                )
            }

            // Section: Custom backgrounds
            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Custom Backgrounds",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            item {
                CustomBackgroundCard(
                    label = "Top Screen",
                    icon = Icons.Default.Wallpaper,
                    onClick = onCustomBackgroundTop
                )
            }

            item {
                CustomBackgroundCard(
                    label = "Bottom Screen",
                    icon = Icons.Default.Image,
                    onClick = onCustomBackgroundBottom
                )
            }
        }
    }
}

@Composable
private fun ThemePreviewCard(
    theme: Theme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .then(
                if (isSelected) Modifier.border(
                    3.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(12.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        ),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Preview top screen
            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth()
                    .background(Color(theme.backgroundColor))
            )

            // Preview status bar
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .fillMaxWidth()
                    .background(Color(theme.primaryColor))
            )

            // Preview bottom screen
            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth()
                    .background(Color(theme.surfaceColor))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mini icon previews
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(theme.primaryColor).copy(alpha = 0.3f))
                            )
                        }
                    }

                    // Theme name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = Color(theme.primaryColor),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = theme.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (theme == Theme.DARK) Color.White else Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomBackgroundCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
