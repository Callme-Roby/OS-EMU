package com.osemu.app.core

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Manages downloading and caching EmulatorJS data files locally.
 *
 * On first use, downloads the EmulatorJS stable release from GitHub
 * and extracts it to the app's internal storage. Subsequent launches
 * use the cached files — no internet needed.
 *
 * Files are served by LocalWebServer at /data/...
 */
class EmulatorJSDataManager(private val context: Context) {

    private val dataDir: File
        get() = File(context.filesDir, "emulatorjs").also { it.mkdirs() }

    private val _state = MutableStateFlow(EJSDownloadState())
    val state: StateFlow<EJSDownloadState> = _state.asStateFlow()

    /**
     * Checks if EmulatorJS data is already installed locally.
     */
    fun isInstalled(): Boolean {
        val loaderFile = File(dataDir, "loader.js")
        val emulatorJs = File(dataDir, "emulator.min.js")
        val emulatorJsFull = File(dataDir, "emulator.js")
        return loaderFile.exists() && loaderFile.length() > 0 &&
            (emulatorJs.exists() || emulatorJsFull.exists())
    }

    /**
     * Returns the local data directory path for serving files.
     */
    fun getDataDir(): File = dataDir

    /**
     * Downloads EmulatorJS data files and installs them locally.
     *
     * Strategy: download each essential file individually from the CDN.
     * This is more reliable than depending on a specific GitHub release zip.
     * WASM cores are downloaded on-demand by EmulatorJS itself (cached by the browser).
     */
    suspend fun downloadAndInstall(): Boolean {
        if (isInstalled()) return true

        _state.value = EJSDownloadState(
            isDownloading = true,
            status = "Downloading EmulatorJS...",
            progress = 0f
        )

        return withContext(Dispatchers.IO) {
            try {
                val totalFiles = CDN_FILES.size
                var downloaded = 0

                for (filePath in CDN_FILES) {
                    _state.value = _state.value.copy(
                        status = "Downloading ${filePath.substringAfterLast('/')}...",
                        progress = downloaded.toFloat() / totalFiles
                    )

                    val success = downloadFile(CDN_BASE + filePath, File(dataDir, filePath))
                    if (!success && filePath in ESSENTIAL_FILES) {
                        // Essential file failed - abort
                        _state.value = EJSDownloadState(
                            isDownloading = false,
                            status = "Failed to download $filePath",
                            error = true
                        )
                        return@withContext false
                    }
                    downloaded++
                }

                // Verify installation
                if (isInstalled()) {
                    _state.value = EJSDownloadState(
                        isDownloading = false,
                        status = "Ready",
                        progress = 1f
                    )
                    true
                } else {
                    _state.value = EJSDownloadState(
                        isDownloading = false,
                        status = "Installation incomplete",
                        error = true
                    )
                    false
                }
            } catch (e: Exception) {
                _state.value = EJSDownloadState(
                    isDownloading = false,
                    status = "Download failed: ${e.message}",
                    error = true
                )
                false
            }
        }
    }

    /**
     * Downloads a single file from URL to local destination.
     */
    private fun downloadFile(urlString: String, destFile: File): Boolean {
        return try {
            destFile.parentFile?.mkdirs()
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.instanceFollowRedirects = true
            connection.connect()

            if (connection.responseCode != 200) {
                connection.disconnect()
                return false
            }

            connection.inputStream.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            connection.disconnect()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Returns total size of cached EmulatorJS files in MB.
     */
    fun getCacheSizeMb(): Int {
        return (getDirectorySize(dataDir) / (1024 * 1024)).toInt()
    }

    private fun getDirectorySize(dir: File): Long {
        if (!dir.exists()) return 0
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    /**
     * Deletes all cached EmulatorJS data.
     */
    fun clearCache() {
        dataDir.deleteRecursively()
    }

    companion object {
        private const val CDN_BASE = "https://cdn.emulatorjs.org/stable/data/"

        /** Essential files that must exist for EmulatorJS to run */
        val ESSENTIAL_FILES = listOf(
            "loader.js",
            "emulator.min.js",
            "emulator.min.css"
        )

        /**
         * All files to download from CDN for local operation.
         * The WASM cores are large and downloaded on-demand by EmulatorJS
         * itself when a specific console is used (cached in WebView storage).
         */
        val CDN_FILES = listOf(
            // Core engine
            "loader.js",
            "emulator.min.js",
            "emulator.min.css",
            "version.json"
        )
    }
}

data class EJSDownloadState(
    val isDownloading: Boolean = false,
    val status: String = "",
    val progress: Float = 0f,
    val downloadedMb: Int = 0,
    val error: Boolean = false
)
