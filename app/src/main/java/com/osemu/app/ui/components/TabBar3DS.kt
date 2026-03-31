package com.osemu.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.osemu.app.ui.theme.LocalOsEmuExtras

data class TabItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

val homeTabs = listOf(
    TabItem(Icons.Default.Home, "Home", "home"),
    TabItem(Icons.Default.Edit, "Notes", "notes"),
    TabItem(Icons.Default.Face, "Mii", "mii"),
    TabItem(Icons.Default.Chat, "Messages", "messages"),
    TabItem(Icons.Default.Language, "Browser", "browser"),
    TabItem(Icons.Default.Group, "Friends", "friends")
)

/**
 * 3DS-style tab bar at the top/bottom of the screen.
 */
@Composable
fun TabBar3DS(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = LocalOsEmuExtras.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(extras.tabBarColor)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedIndex
            val iconColor by animateColorAsState(
                targetValue = if (isSelected) extras.selectedTabColor else extras.unselectedTabColor,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "tabColor"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                extras.selectedTabColor.copy(alpha = 0.15f)
                            )
                        } else Modifier
                    )
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
