package com.osemu.app.core

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.osemu.app.data.model.Console
import com.osemu.app.data.model.Game
import com.osemu.app.data.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile

/**
 * Scans directories for ROM files and adds them to the game library.
 */
class GameScanner(
    private val context: Context,
    private val gameRepository: GameRepository
) {
    private val _scanProgress = MutableStateFlow(ScanProgress())
    val scanProgress: StateFlow<ScanProgress> = _scanProgress.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    suspend fun scanDirectory(path: String) {
        _isScanning.value = true
        _scanProgress.value = ScanProgress(status = "Scanning...")

        withContext(Dispatchers.IO) {
            val dir = File(path)
            if (!dir.exists() || !dir.isDirectory) {
                _scanProgress.value = ScanProgress(status = "Directory not found")
                _isScanning.value = false
                return@withContext
            }

            val romFiles = mutableListOf<File>()
            collectRomFiles(dir, romFiles)

            val total = romFiles.size
            _scanProgress.value = ScanProgress(total = total, status = "Processing...")

            var added = 0
            for ((index, file) in romFiles.withIndex()) {
                val ext = file.extension.lowercase()

                // For zip files, peek inside to detect actual console
                val detectedConsoles = if (ext == "zip") {
                    detectConsoleFromZip(file)
                } else {
                    Console.fromExtension(ext)
                }

                if (detectedConsoles.isNotEmpty()) {
                    val existingGame = gameRepository.getGameByPath(file.absolutePath)
                    if (existingGame == null) {
                        val game = Game(
                            title = cleanRomName(file.nameWithoutExtension),
                            filePath = file.absolutePath,
                            console = detectedConsoles.first(),
                            fileSize = file.length()
                        )
                        gameRepository.addGame(game)
                        added++
                    }
                }

                _scanProgress.value = ScanProgress(
                    current = index + 1,
                    total = total,
                    added = added,
                    status = "Scanning: ${file.name}"
                )
            }

            _scanProgress.value = ScanProgress(
                current = total,
                total = total,
                added = added,
                status = "Done! Added $added games."
            )
        }

        _isScanning.value = false
    }

    suspend fun scanUri(uri: Uri) {
        _isScanning.value = true
        withContext(Dispatchers.IO) {
            val documentFile = DocumentFile.fromTreeUri(context, uri)
            if (documentFile != null) {
                scanDocumentTree(documentFile)
            }
        }
        _isScanning.value = false
    }

    private suspend fun scanDocumentTree(dir: DocumentFile) {
        val files = dir.listFiles()
        for (file in files) {
            if (file.isDirectory) {
                scanDocumentTree(file)
            } else {
                val name = file.name ?: continue
                val ext = name.substringAfterLast('.', "").lowercase()

                // For zip files via SAF, try to detect console from zip contents
                val detectedConsoles = if (ext == "zip") {
                    detectConsoleFromSafZip(file) ?: Console.fromExtension(ext)
                } else {
                    Console.fromExtension(ext)
                }

                if (detectedConsoles.isNotEmpty()) {
                    val filePath = file.uri.toString()
                    val existing = gameRepository.getGameByPath(filePath)
                    if (existing == null) {
                        val game = Game(
                            title = cleanRomName(name.substringBeforeLast('.')),
                            filePath = filePath,
                            console = detectedConsoles.first(),
                            fileSize = file.length()
                        )
                        gameRepository.addGame(game)
                    }
                }
            }
        }
    }

    private fun collectRomFiles(dir: File, results: MutableList<File>) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                collectRomFiles(file, results)
            } else {
                val ext = file.extension.lowercase()
                // Include .zip files - we'll inspect them later to detect the console
                if (ext == "zip" || Console.fromExtension(ext).isNotEmpty()) {
                    results.add(file)
                }
            }
        }
    }

    /**
     * Opens a zip file and checks the extensions of files inside
     * to determine which console the ROM belongs to.
     * This allows ROMs stored as .zip (like My Boy GBA, etc.) to be detected.
     */
    private fun detectConsoleFromZip(file: File): List<Console> {
        return try {
            ZipFile(file).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (!entry.isDirectory) {
                        val entryExt = entry.name.substringAfterLast('.', "").lowercase()
                        val consoles = Console.fromExtension(entryExt)
                        if (consoles.isNotEmpty()) {
                            return@use consoles
                        }
                    }
                }
                // If no ROM extension found inside, fall back to ARCADE mapping
                Console.fromExtension("zip")
            }
        } catch (e: Exception) {
            // If zip can't be read, fall back to extension-based detection
            Console.fromExtension("zip")
        }
    }

    /**
     * For SAF-accessed zip files, copy to temp to inspect contents.
     * Returns null if inspection fails (caller falls back to extension-based).
     */
    private fun detectConsoleFromSafZip(documentFile: DocumentFile): List<Console>? {
        return try {
            val inputStream = context.contentResolver.openInputStream(documentFile.uri) ?: return null
            val tempFile = File.createTempFile("rom_inspect", ".zip", context.cacheDir)
            try {
                tempFile.outputStream().use { out ->
                    inputStream.copyTo(out)
                }
                val result = detectConsoleFromZip(tempFile)
                result.ifEmpty { null }
            } finally {
                tempFile.delete()
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun cleanRomName(name: String): String {
        return name
            .replace(Regex("\\s*\\([^)]*\\)"), "")  // Remove (USA), (Europe), etc.
            .replace(Regex("\\s*\\[[^]]*]"), "")      // Remove [!], [b1], etc.
            .replace(Regex("\\s*v\\d+\\.\\d+"), "")   // Remove version numbers
            .trim()
    }
}

data class ScanProgress(
    val current: Int = 0,
    val total: Int = 0,
    val added: Int = 0,
    val status: String = ""
)
