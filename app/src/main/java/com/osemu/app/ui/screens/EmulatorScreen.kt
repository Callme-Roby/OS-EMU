package com.osemu.app.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.osemu.app.core.*
import com.osemu.app.data.model.Game
import com.osemu.app.ui.theme.OsEmuColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Full-screen emulator view with hybrid engine:
 * - EmulatorJS (WebView/WASM) for lighter consoles (GBA, NES, SNES, NDS, PS1...)
 * - Native libretro cores for heavy consoles (3DS, GameCube, Wii...)
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
    val coreManager = remember { NativeCoreManager(context) }
    val useNative = remember(game.console) { coreManager.needsNativeCore(game.console) }
    var hasError by remember { mutableStateOf(false) }

    if (hasError) {
        ErrorScreen(game, onExit)
        return
    }

    if (useNative) {
        NativeEmulatorScreen(
            game = game,
            coreManager = coreManager,
            onExit = onExit,
            modifier = modifier
        )
    } else {
        WebViewEmulatorScreen(
            game = game,
            onExit = onExit,
            onError = { hasError = true },
            modifier = modifier
        )
    }
}

// ==================== WebView Emulator (light consoles) ====================

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebViewEmulatorScreen(
    game: Game,
    onExit: () -> Unit,
    onError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var loadingStatus by remember { mutableStateOf("Preparing...") }
    var showExitConfirm by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val server = remember { LocalWebServer() }
    var serverUrl by remember { mutableStateOf<String?>(null) }
    var loadFailed by remember { mutableStateOf(false) }
    val ejsDataManager = remember { EmulatorJSDataManager(context) }

    LaunchedEffect(game.id) {
        try {
            withContext(Dispatchers.IO) {
                // Step 1: Ensure EmulatorJS data is installed locally
                val useLocal = if (ejsDataManager.isInstalled()) {
                    true
                } else {
                    loadingStatus = "Downloading emulator engine (one-time)..."
                    ejsDataManager.downloadAndInstall()
                }

                // Step 2: Prepare ROM file
                loadingStatus = "Preparing ROM..."
                val romFile = EmulatorJSEngine.prepareRomFile(context, game.filePath)
                if (romFile != null) {
                    loadingStatus = "Starting emulator..."
                    val html = EmulatorJSEngine.generateEmulatorHtml(
                        romFile.name, game.console, useLocalData = useLocal
                    )
                    server.setContent(html, romFile)
                    if (useLocal) {
                        server.setStaticDir(ejsDataManager.getDataDir())
                    }
                    server.start()
                    var retries = 0
                    while (server.actualPort == 0 && retries < 20) {
                        kotlinx.coroutines.delay(50)
                        retries++
                    }
                    if (server.actualPort > 0) {
                        serverUrl = "http://127.0.0.1:${server.actualPort}/"
                    } else {
                        loadingStatus = "Failed to start local server"
                        loadFailed = true
                    }
                } else {
                    loadingStatus = "Could not load ROM file"
                    loadFailed = true
                }
            }
        } catch (e: Exception) {
            loadingStatus = "Error: ${e.message}"
            loadFailed = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            server.stop()
            webViewRef?.destroy()
        }
    }

    BackHandler { showExitConfirm = true }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF0a0a1a))) {
        val url = serverUrl
        if (url != null) {
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
        if (!loadFailed) {
            EmulatorLoadingOverlay(isLoading, game, loadingStatus)
        }

        // Error state - ROM could not be loaded
        if (loadFailed) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFF1a1a2e)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, null, tint = OsEmuColors.Yellow,
                        modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Could not load game", color = Color.White,
                        fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(loadingStatus, color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        server.stop()
                        webViewRef?.destroy()
                        webViewRef = null
                        onExit()
                    }, shape = RoundedCornerShape(12.dp)) {
                        Text("Go Back")
                    }
                }
            }
        }

        // Exit button
        ExitButton(
            modifier = Modifier.align(Alignment.TopStart),
            onClick = { showExitConfirm = true }
        )

        // Exit dialog
        if (showExitConfirm) {
            ExitConfirmDialog(
                gameName = game.title,
                onExit = {
                    showExitConfirm = false
                    server.stop()
                    webViewRef?.destroy()
                    webViewRef = null
                    onExit()
                },
                onDismiss = { showExitConfirm = false }
            )
        }
    }
}

// ==================== Native Emulator (heavy consoles: 3DS, GC, etc.) ====================

@Composable
private fun NativeEmulatorScreen(
    game: Game,
    coreManager: NativeCoreManager,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var screenState by remember { mutableStateOf(NativeScreenState.CHECKING) }
    var showExitConfirm by remember { mutableStateOf(false) }
    val downloadState by coreManager.downloadProgress.collectAsState()
    val libretroRunner = remember { LibretroRunner(context) }

    val coreInfo = remember(game.console) { coreManager.getCoreInfo(game.console) }
    val isInstalled = remember(game.console) { coreManager.isCoreInstalled(game.console) }

    // Check core status on launch
    LaunchedEffect(game.id) {
        if (isInstalled) {
            screenState = NativeScreenState.LOADING_GAME
            launchNativeGame(context, coreManager, libretroRunner, game) { state ->
                screenState = state
            }
        } else {
            screenState = NativeScreenState.NEED_DOWNLOAD
        }
    }

    DisposableEffect(Unit) {
        onDispose { libretroRunner.stop() }
    }

    BackHandler { showExitConfirm = true }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF0a0a1a))) {
        when (screenState) {
            NativeScreenState.CHECKING -> {
                EmulatorLoadingOverlay(true, game, "Checking core...")
            }

            NativeScreenState.NEED_DOWNLOAD -> {
                // Core download screen
                CoreDownloadScreen(
                    game = game,
                    coreInfo = coreInfo,
                    downloadState = downloadState,
                    onDownload = {
                        screenState = NativeScreenState.DOWNLOADING
                        scope.launch {
                            val success = coreManager.downloadCore(game.console)
                            if (success) {
                                screenState = NativeScreenState.LOADING_GAME
                                launchNativeGame(context, coreManager, libretroRunner, game) { state ->
                                    screenState = state
                                }
                            } else {
                                screenState = NativeScreenState.NEED_DOWNLOAD
                            }
                        }
                    },
                    onBack = onExit
                )
            }

            NativeScreenState.DOWNLOADING -> {
                CoreDownloadScreen(
                    game = game,
                    coreInfo = coreInfo,
                    downloadState = downloadState,
                    onDownload = {},
                    onBack = onExit
                )
            }

            NativeScreenState.LOADING_GAME -> {
                EmulatorLoadingOverlay(true, game, "Loading game...")
            }

            NativeScreenState.RUNNING -> {
                // GL Surface for native rendering
                AndroidView(
                    factory = { ctx ->
                        GLSurfaceView(ctx).apply {
                            setEGLContextClientVersion(3)
                            setRenderer(libretroRunner.createRenderer())
                            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            NativeScreenState.ERROR -> {
                ErrorScreen(game, onExit)
            }
        }

        // Exit button
        ExitButton(
            modifier = Modifier.align(Alignment.TopStart),
            onClick = { showExitConfirm = true }
        )

        if (showExitConfirm) {
            ExitConfirmDialog(
                gameName = game.title,
                onExit = {
                    showExitConfirm = false
                    libretroRunner.stop()
                    onExit()
                },
                onDismiss = { showExitConfirm = false }
            )
        }
    }
}

private suspend fun launchNativeGame(
    context: Context,
    coreManager: NativeCoreManager,
    runner: LibretroRunner,
    game: Game,
    onStateChange: (NativeScreenState) -> Unit
) {
    withContext(Dispatchers.IO) {
        val corePath = coreManager.getCorePath(game.console)
        if (corePath == null) {
            onStateChange(NativeScreenState.NEED_DOWNLOAD)
            return@withContext
        }

        // Prepare ROM
        val romFile = EmulatorJSEngine.prepareRomFile(context, game.filePath)
        if (romFile == null) {
            onStateChange(NativeScreenState.ERROR)
            return@withContext
        }

        // Load core
        if (!runner.loadCore(corePath)) {
            onStateChange(NativeScreenState.ERROR)
            return@withContext
        }

        // Load game
        if (!runner.loadGame(romFile.absolutePath)) {
            onStateChange(NativeScreenState.ERROR)
            return@withContext
        }

        runner.startEmulation()
        onStateChange(NativeScreenState.RUNNING)
    }
}

private enum class NativeScreenState {
    CHECKING, NEED_DOWNLOAD, DOWNLOADING, LOADING_GAME, RUNNING, ERROR
}

// ==================== Shared UI Components ====================

@Composable
private fun CoreDownloadScreen(
    game: Game,
    coreInfo: NativeCoreInfo?,
    downloadState: CoreDownloadState,
    onDownload: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a2e)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Download,
                contentDescription = null,
                tint = OsEmuColors.Blue400,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = game.console.displayName,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This console requires a native emulation core",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (downloadState.isDownloading) {
                // Download progress
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = downloadState.status,
                        color = OsEmuColors.Blue400,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (downloadState.progress >= 0) {
                        LinearProgressIndicator(
                            progress = downloadState.progress,
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = OsEmuColors.Blue400,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(downloadState.progress * 100).toInt()}%",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = OsEmuColors.Blue400,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                    }
                }
            } else {
                // Download button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = coreInfo?.displayName ?: "Emulation Core",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "~${coreInfo?.estimatedSizeMb ?: 0} MB download",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDownload,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OsEmuColors.Blue500
                    )
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download & Play")
                }

                if (downloadState.error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = downloadState.status,
                        color = OsEmuColors.Red,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBack) {
                Text("Back", color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun ErrorScreen(game: Game, onExit: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1a1a2e)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = OsEmuColors.Yellow,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Failed to load ${game.console.displayName} core",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "The emulation core may not be compatible with your device",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onExit,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Go Back")
            }
        }
    }
}

@Composable
private fun EmulatorLoadingOverlay(
    visible: Boolean,
    game: Game,
    status: String
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF1a1a2e)),
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
                    text = status,
                    color = OsEmuColors.Blue400,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ExitButton(modifier: Modifier, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = modifier.padding(4.dp)
    ) {
        Icon(
            Icons.Default.Close,
            contentDescription = "Exit",
            tint = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun ExitConfirmDialog(
    gameName: String,
    onExit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exit Game?") },
        text = { Text("Do you want to stop playing $gameName?") },
        confirmButton = {
            TextButton(onClick = onExit) {
                Text("Exit", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Continue Playing")
            }
        }
    )
}
