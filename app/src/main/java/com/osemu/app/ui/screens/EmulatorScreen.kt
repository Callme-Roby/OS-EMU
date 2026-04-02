package com.osemu.app.ui.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.osemu.app.core.EmulatorJSEngine
import com.osemu.app.core.EmulatorState
import com.osemu.app.core.LocalWebServer
import com.osemu.app.data.model.Game
import com.osemu.app.ui.theme.OsEmuColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Full-screen emulator view using EmulatorJS in a WebView.
 * Runs actual emulation via libretro cores compiled to WebAssembly.
 */
@SuppressLint("SetJavaScriptEnabled")
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var loadingStatus by remember { mutableStateOf("Preparing ROM...") }
    var showExitConfirm by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Local web server to serve ROM + HTML to WebView
    val server = remember { LocalWebServer() }
    var serverUrl by remember { mutableStateOf<String?>(null) }

    // Prepare ROM and start server
    LaunchedEffect(game.id) {
        withContext(Dispatchers.IO) {
            loadingStatus = "Preparing ROM..."
            val romFile = EmulatorJSEngine.prepareRomFile(context, game.filePath)

            if (romFile != null) {
                loadingStatus = "Starting emulator..."
                val html = EmulatorJSEngine.generateEmulatorHtml(romFile.name, game.console)
                server.setContent(html, romFile)
                server.start()

                // Wait for server port
                var retries = 0
                while (server.actualPort == 0 && retries < 20) {
                    kotlinx.coroutines.delay(50)
                    retries++
                }

                if (server.actualPort > 0) {
                    serverUrl = "http://127.0.0.1:${server.actualPort}/"
                } else {
                    loadingStatus = "Failed to start local server"
                }
            } else {
                loadingStatus = "Could not load ROM file"
            }
        }
    }

    // Clean up server on dispose
    DisposableEffect(Unit) {
        onDispose {
            server.stop()
            webViewRef?.destroy()
        }
    }

    // Back button handling
    BackHandler {
        showExitConfirm = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0a0a1a))
    ) {
        val url = serverUrl
        if (url != null) {
            // WebView with EmulatorJS
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true
                            mediaPlaybackRequiresUserGesture = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(false)
                            builtInZoomControls = false
                            displayZoomControls = false
                            cacheMode = WebSettings.LOAD_DEFAULT
                            databaseEnabled = true
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }
                        }

                        webChromeClient = WebChromeClient()

                        setBackgroundColor(android.graphics.Color.parseColor("#0a0a1a"))
                        webViewRef = this

                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Loading overlay
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1a1a2e)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = OsEmuColors.Blue400,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = game.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = game.console.displayName,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = loadingStatus,
                        color = OsEmuColors.Blue400,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Exit button (top-left, small)
        IconButton(
            onClick = { showExitConfirm = true },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Exit",
                tint = Color.White.copy(alpha = 0.5f)
            )
        }

        // Exit confirmation dialog
        if (showExitConfirm) {
            AlertDialog(
                onDismissRequest = { showExitConfirm = false },
                title = { Text("Exit Game?") },
                text = { Text("Do you want to stop playing ${game.title}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExitConfirm = false
                            server.stop()
                            webViewRef?.destroy()
                            webViewRef = null
                            onExit()
                        }
                    ) {
                        Text("Exit", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitConfirm = false }) {
                        Text("Continue Playing")
                    }
                }
            )
        }
    }
}
