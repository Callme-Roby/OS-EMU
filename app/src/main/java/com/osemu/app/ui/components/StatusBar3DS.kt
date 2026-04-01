package com.osemu.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

/**
 * 3DS-style top status bar showing time, date, battery, wifi.
 * Transparent over the gradient background.
 */
@Composable
fun StatusBar3DS(
    modifier: Modifier = Modifier,
    batteryLevel: Int = 75,
    wifiConnected: Boolean = true
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("MM/dd (EEE)", Locale.getDefault())
            currentTime = timeFormat.format(now.time)
            currentDate = dateFormat.format(now.time)
            kotlinx.coroutines.delay(1000)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: WiFi
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (wifiConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                contentDescription = "WiFi",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(14.dp)
            )
        }

        // Center: Date & Time
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = currentDate,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Right: Battery
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val batteryIcon = if (batteryLevel > 20) Icons.Default.BatteryFull
                else Icons.Default.BatteryAlert
            Icon(
                imageVector = batteryIcon,
                contentDescription = "Battery $batteryLevel%",
                tint = if (batteryLevel > 20) Color.White.copy(alpha = 0.9f) else Color(0xFFFF5252),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
