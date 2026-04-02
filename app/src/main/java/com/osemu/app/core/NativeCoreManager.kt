package com.osemu.app.core

import android.content.Context
import com.osemu.app.data.model.Console
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
 * Manages downloading and loading native libretro cores (.so files)
 * for consoles too heavy for WebAssembly (3DS, GameCube, Wii, Switch).
 *
 * Cores are downloaded from the official Libretro buildbot on first use
 * and cached locally.
 */
class NativeCoreManager(private val context: Context) {

    private val coresDir: File
        get() = File(context.filesDir, "cores").also { it.mkdirs() }

    private val _downloadProgress = MutableStateFlow(CoreDownloadState())
    val downloadProgress: StateFlow<CoreDownloadState> = _downloadProgress.asStateFlow()

    /**
     * Consoles that need native cores (too heavy for WASM).
     */
    val nativeConsoles = setOf(
        Console.N3DS,
        Console.GAMECUBE,
        Console.WII,
        Console.WIIU,
        Console.SWITCH,
        Console.PS2,
        Console.DREAMCAST,
        Console.SATURN
    )

    fun needsNativeCore(console: Console): Boolean = console in nativeConsoles

    /**
     * Native core info for each heavy console.
     * Uses Libretro buildbot URLs for Android arm64-v8a cores.
     */
    fun getCoreInfo(console: Console): NativeCoreInfo? = when (console) {
        Console.N3DS -> NativeCoreInfo(
            coreName = "citra",
            displayName = "Citra (3DS)",
            fileName = "citra_libretro_android.so",
            downloadUrl = "https://buildbot.libretro.com/nightly/android/latest/arm64-v8a/citra_libretro_android.so.zip",
            estimatedSizeMb = 15
        )
        Console.GAMECUBE -> NativeCoreInfo(
            coreName = "dolphin",
            displayName = "Dolphin (GameCube)",
            fileName = "dolphin_libretro_android.so",
            downloadUrl = "https://buildbot.libretro.com/nightly/android/latest/arm64-v8a/dolphin_libretro_android.so.zip",
            estimatedSizeMb = 25
        )
        Console.WII -> NativeCoreInfo(
            coreName = "dolphin",
            displayName = "Dolphin (Wii)",
            fileName = "dolphin_libretro_android.so",
            downloadUrl = "https://buildbot.libretro.com/nightly/android/latest/arm64-v8a/dolphin_libretro_android.so.zip",
            estimatedSizeMb = 25
        )
        Console.DREAMCAST -> NativeCoreInfo(
            coreName = "flycast",
            displayName = "Flycast (Dreamcast)",
            fileName = "flycast_libretro_android.so",
            downloadUrl = "https://buildbot.libretro.com/nightly/android/latest/arm64-v8a/flycast_libretro_android.so.zip",
            estimatedSizeMb = 10
        )
        Console.SATURN -> NativeCoreInfo(
            coreName = "mednafen_saturn",
            displayName = "Mednafen Saturn",
            fileName = "mednafen_saturn_libretro_android.so",
            downloadUrl = "https://buildbot.libretro.com/nightly/android/latest/arm64-v8a/mednafen_saturn_libretro_android.so.zip",
            estimatedSizeMb = 8
        )
        Console.PS2 -> NativeCoreInfo(
            coreName = "pcsx2",
            displayName = "PCSX2 (PS2)",
            fileName = "pcsx2_libretro_android.so",
            downloadUrl = "https://buildbot.libretro.com/nightly/android/latest/arm64-v8a/pcsx2_libretro_android.so.zip",
            estimatedSizeMb = 30
        )
        else -> null
    }

    /**
     * Checks if the native core is already downloaded.
     */
    fun isCoreInstalled(console: Console): Boolean {
        val info = getCoreInfo(console) ?: return false
        return File(coresDir, info.fileName).exists()
    }

    /**
     * Gets the path to the installed core .so file.
     */
    fun getCorePath(console: Console): String? {
        val info = getCoreInfo(console) ?: return null
        val file = File(coresDir, info.fileName)
        return if (file.exists()) file.absolutePath else null
    }

    /**
     * Downloads the core for the given console.
     * The core is downloaded as a .zip and extracted.
     */
    suspend fun downloadCore(console: Console): Boolean {
        val info = getCoreInfo(console) ?: return false

        _downloadProgress.value = CoreDownloadState(
            isDownloading = true,
            coreName = info.displayName,
            progress = 0f,
            status = "Connecting..."
        )

        return withContext(Dispatchers.IO) {
            try {
                val url = URL(info.downloadUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.connect()

                val totalSize = connection.contentLength.toLong()
                val isZip = info.downloadUrl.endsWith(".zip")
                val tempFile = if (isZip) {
                    File(coresDir, "${info.fileName}.zip")
                } else {
                    File(coresDir, info.fileName)
                }

                _downloadProgress.value = _downloadProgress.value.copy(
                    status = "Downloading ${info.displayName}...",
                    totalSizeMb = info.estimatedSizeMb
                )

                // Download
                connection.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        val buffer = ByteArray(8192)
                        var downloaded = 0L
                        var bytesRead: Int

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloaded += bytesRead

                            val progress = if (totalSize > 0) {
                                downloaded.toFloat() / totalSize
                            } else {
                                -1f
                            }

                            _downloadProgress.value = _downloadProgress.value.copy(
                                progress = progress,
                                downloadedMb = (downloaded / (1024 * 1024)).toInt()
                            )
                        }
                    }
                }

                // Extract if zip
                if (isZip) {
                    _downloadProgress.value = _downloadProgress.value.copy(
                        status = "Extracting..."
                    )
                    extractZip(tempFile, coresDir, info.fileName)
                    tempFile.delete()
                }

                _downloadProgress.value = CoreDownloadState(
                    isDownloading = false,
                    status = "Ready!",
                    progress = 1f
                )

                true
            } catch (e: Exception) {
                _downloadProgress.value = CoreDownloadState(
                    isDownloading = false,
                    status = "Download failed: ${e.message}",
                    progress = 0f,
                    error = true
                )
                false
            }
        }
    }

    private fun extractZip(zipFile: File, destDir: File, targetName: String) {
        try {
            java.util.zip.ZipFile(zipFile).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (!entry.isDirectory && entry.name.endsWith(".so")) {
                        val destFile = File(destDir, targetName)
                        zip.getInputStream(entry).use { input ->
                            FileOutputStream(destFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        // Make executable
                        destFile.setExecutable(true)
                        return
                    }
                }
            }
        } catch (e: Exception) {
            // Extraction failed
        }
    }

    /**
     * Deletes a downloaded core to free space.
     */
    fun deleteCore(console: Console) {
        val info = getCoreInfo(console) ?: return
        File(coresDir, info.fileName).delete()
    }

    /**
     * Returns total size of downloaded cores in MB.
     */
    fun getInstalledCoresSizeMb(): Int {
        val files = coresDir.listFiles() ?: return 0
        return (files.sumOf { it.length() } / (1024 * 1024)).toInt()
    }
}

data class NativeCoreInfo(
    val coreName: String,
    val displayName: String,
    val fileName: String,
    val downloadUrl: String,
    val estimatedSizeMb: Int
)

data class CoreDownloadState(
    val isDownloading: Boolean = false,
    val coreName: String = "",
    val progress: Float = 0f,
    val status: String = "",
    val downloadedMb: Int = 0,
    val totalSizeMb: Int = 0,
    val error: Boolean = false
)
