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
                val consoles = Console.fromExtension(ext)

                if (consoles.isNotEmpty()) {
                    val existingGame = gameRepository.getGameByPath(file.absolutePath)
                    if (existingGame == null) {
                        val game = Game(
                            title = cleanRomName(file.nameWithoutExtension),
                            filePath = file.absolutePath,
                            console = consoles.first(),
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
                val consoles = Console.fromExtension(ext)

                if (consoles.isNotEmpty()) {
                    val filePath = file.uri.toString()
                    val existing = gameRepository.getGameByPath(filePath)
                    if (existing == null) {
                        val game = Game(
                            title = cleanRomName(name.substringBeforeLast('.')),
                            filePath = filePath,
                            console = consoles.first(),
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
                if (Console.fromExtension(ext).isNotEmpty()) {
                    results.add(file)
                }
            }
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
