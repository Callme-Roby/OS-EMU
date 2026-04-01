package com.osemu.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osemu.app.ui.theme.LocalOsEmuExtras

data class TabItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

/**
 * 3DS-style bottom tab bar with icons and labels.
 */
@Composable
fun TabBar3DS(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = LocalOsEmuExtras.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = extras.tabBarColor,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
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

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    extras.selectedTabColor.copy(alpha = 0.1f)
                                )
                            } else Modifier
                        )
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        color = iconColor,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
