package com.osemu.app.ui.components

import androidx.compose.foundation.background
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
import com.osemu.app.ui.theme.LocalOsEmuExtras
import java.text.SimpleDateFormat
import java.util.*

/**
 * 3DS-style top status bar showing time, date, battery, wifi.
 */
@Composable
fun StatusBar3DS(
    modifier: Modifier = Modifier,
    batteryLevel: Int = 75,
    wifiConnected: Boolean = true
) {
    val extras = LocalOsEmuExtras.current
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
            .background(extras.statusBarColor)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: WiFi & connection status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (wifiConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                contentDescription = "WiFi",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            if (wifiConnected) {
                Text(
                    text = "Internet",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Center: Date & Time
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = currentDate,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Right: Battery
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val batteryIcon = if (batteryLevel > 20) Icons.Default.BatteryFull
                else Icons.Default.BatteryAlert
            Icon(
                imageVector = batteryIcon,
                contentDescription = "Battery $batteryLevel%",
                tint = if (batteryLevel > 20) Color.White else Color(0xFFFF5252),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
