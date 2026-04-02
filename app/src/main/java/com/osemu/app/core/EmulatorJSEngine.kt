package com.osemu.app.core

import android.content.Context
import android.net.Uri
import com.osemu.app.data.model.Console
import java.io.File
import java.io.FileOutputStream

/**
 * Maps consoles to EmulatorJS core names and generates
 * the HTML page that runs the emulator in a WebView.
 */
object EmulatorJSEngine {

    /**
     * EmulatorJS core names for each console.
     */
    fun getCoreForConsole(console: Console): String = when (console) {
        Console.GBA -> "mgba"
        Console.GBC -> "gambatte"
        Console.NES -> "fceumm"
        Console.SNES -> "snes9x"
        Console.N64 -> "mupen64plus_next"
        Console.NDS -> "melonds"
        Console.N3DS -> "citra"
        Console.PS1 -> "pcsx_rearmed"
        Console.PS2 -> "pcsx2"
        Console.PSP -> "ppsspp"
        Console.GENESIS -> "genesis_plus_gx"
        Console.MASTER_SYSTEM -> "genesis_plus_gx"
        Console.GAME_GEAR -> "genesis_plus_gx"
        Console.SATURN -> "yabause"
        Console.DREAMCAST -> "flycast"
        Console.GAMECUBE -> "dolphin"
        Console.WII -> "dolphin"
        Console.WIIU -> "dolphin"
        Console.SWITCH -> "mupen64plus_next" // No real Switch core in EmulatorJS
        Console.ARCADE -> "fbneo"
    }

    /**
     * Copies a ROM file (from any source: file path, content URI, zip) into
     * the app's internal cache so the WebView can access it via a local server.
     */
    fun prepareRomFile(context: Context, filePath: String): File? {
        val cacheDir = File(context.cacheDir, "roms").also { it.mkdirs() }

        return try {
            if (filePath.startsWith("content://")) {
                // SAF URI - copy via ContentResolver
                val uri = Uri.parse(filePath)
                val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "rom.bin"
                val destFile = File(cacheDir, sanitizeFileName(fileName))
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                destFile
            } else {
                // Direct file path
                val sourceFile = File(filePath)
                if (!sourceFile.exists()) return null

                // If it's a zip, check if we need to extract the ROM
                if (sourceFile.extension.lowercase() == "zip") {
                    extractRomFromZip(sourceFile, cacheDir)
                } else {
                    val destFile = File(cacheDir, sanitizeFileName(sourceFile.name))
                    sourceFile.copyTo(destFile, overwrite = true)
                    destFile
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extracts the first ROM file found inside a zip archive.
     */
    private fun extractRomFromZip(zipFile: File, destDir: File): File? {
        val romExtensions = Console.entries.flatMap { it.fileExtensions }.toSet()

        return try {
            java.util.zip.ZipFile(zipFile).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (!entry.isDirectory) {
                        val ext = entry.name.substringAfterLast('.', "").lowercase()
                        if (ext in romExtensions) {
                            val destFile = File(destDir, sanitizeFileName(entry.name))
                            zip.getInputStream(entry).use { input ->
                                FileOutputStream(destFile).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            return destFile
                        }
                    }
                }
            }
            // No ROM found inside zip, copy the zip itself
            val destFile = File(destDir, sanitizeFileName(zipFile.name))
            zipFile.copyTo(destFile, overwrite = true)
            destFile
        } catch (e: Exception) {
            null
        }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }

    /**
     * Generates the HTML page that loads EmulatorJS with the game.
     */
    fun generateEmulatorHtml(romFileName: String, console: Console): String {
        val core = getCoreForConsole(console)

        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>OS-EMU</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        html, body {
            width: 100%;
            height: 100%;
            background: #0a0a1a;
            overflow: hidden;
            touch-action: none;
        }
        #game {
            width: 100%;
            height: 100%;
        }
        /* Loading overlay */
        #loading {
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            z-index: 9999;
            color: white;
            font-family: -apple-system, BlinkMacSystemFont, sans-serif;
        }
        #loading .spinner {
            width: 48px; height: 48px;
            border: 4px solid rgba(255,255,255,0.1);
            border-top-color: #4FC3F7;
            border-radius: 50%;
            animation: spin 0.8s linear infinite;
        }
        @keyframes spin { to { transform: rotate(360deg); } }
        #loading .text { margin-top: 16px; font-size: 14px; opacity: 0.7; }
        #loading .title { margin-top: 8px; font-size: 18px; font-weight: bold; }
    </style>
</head>
<body>
    <div id="loading">
        <div class="spinner"></div>
        <div class="title">Loading game...</div>
        <div class="text">Downloading $core core</div>
    </div>
    <div id="game"></div>
    <script>
        // EmulatorJS configuration
        EJS_player = '#game';
        EJS_core = '$core';
        EJS_gameUrl = '/rom/$romFileName';
        EJS_pathtodata = 'https://cdn.emulatorjs.org/stable/data/';
        EJS_startOnLoaded = true;
        EJS_fullscreenOnLoaded = false;
        EJS_color = '#1976D2';
        EJS_backgroundColor = '#0a0a1a';
        EJS_defaultControls = false;

        // Hide loading when emulator is ready
        EJS_onGameStart = function() {
            var loading = document.getElementById('loading');
            if (loading) loading.style.display = 'none';
        };
    </script>
    <script src="https://cdn.emulatorjs.org/stable/data/loader.js"></script>
</body>
</html>
""".trimIndent()
    }
}
