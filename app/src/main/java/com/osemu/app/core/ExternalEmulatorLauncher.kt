package com.osemu.app.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.osemu.app.data.model.Console
import java.io.File

/**
 * Launches ROM files using external emulator apps installed on the device.
 * Maps each console to known emulator package names and tries to launch them.
 */
object ExternalEmulatorLauncher {

    /**
     * Known emulator packages per console type.
     * Ordered by popularity - the first installed one will be used.
     */
    private val emulatorPackages = mapOf(
        // GBA
        Console.GBA to listOf(
            "com.fastemulator.gba",       // My Boy! Free
            "com.fastemulator.gbafull",    // My Boy! Paid
            "io.mgba.mgba",              // mGBA
            "com.retroarch",              // RetroArch
            "com.retroarch.aarch64",      // RetroArch 64-bit
            "it.chibotto.vgba",           // VGBAnext
            "com.explusalpha.GbaEmu",     // GBA.emu
        ),
        // GBC / GB
        Console.GBC to listOf(
            "com.fastemulator.gbc",       // My OldBoy! Free
            "com.fastemulator.gbcfull",   // My OldBoy! Paid
            "io.mgba.mgba",              // mGBA
            "com.retroarch",
            "com.retroarch.aarch64",
            "com.explusalpha.GbcEmu",     // GBC.emu
        ),
        // NES
        Console.NES to listOf(
            "com.retroarch",
            "com.retroarch.aarch64",
            "com.explusalpha.NesEmu",     // NES.emu
            "com.nostalgiaemulators.neslite", // Nostalgia.NES
        ),
        // SNES
        Console.SNES to listOf(
            "com.retroarch",
            "com.retroarch.aarch64",
            "com.explusalpha.Snes9xPlus", // Snes9x EX+
            "com.bubblezapgames.supergnes", // SuperRetro16
        ),
        // N64
        Console.N64 to listOf(
            "org.mupen64plusae.v3.fzurita", // Mupen64Plus FZ
            "org.mupen64plusae.v3.fzurita.pro",
            "com.retroarch",
            "com.retroarch.aarch64",
        ),
        // NDS
        Console.NDS to listOf(
            "com.dsemu.drastic",          // DraStic
            "com.retroarch",
            "com.retroarch.aarch64",
        ),
        // N3DS
        Console.N3DS to listOf(
            "org.citra.citra_emu",        // Citra
            "org.citra.citra_emu.canary",
        ),
        // PSP
        Console.PSP to listOf(
            "org.ppsspp.ppsspp",          // PPSSPP Free
            "org.ppsspp.ppssppgold",      // PPSSPP Gold
        ),
        // PS1
        Console.PS1 to listOf(
            "com.epsxe.ePSXe",            // ePSXe
            "com.retroarch",
            "com.retroarch.aarch64",
            "com.explusalpha.PsxEmu",     // PCSXr
        ),
        // Genesis / Mega Drive
        Console.GENESIS to listOf(
            "com.retroarch",
            "com.retroarch.aarch64",
            "com.explusalpha.MdEmu",      // MD.emu
        ),
        // Dreamcast
        Console.DREAMCAST to listOf(
            "com.reicast.emulator",       // Reicast
            "com.flycast.emulator",       // Flycast
            "com.retroarch",
            "com.retroarch.aarch64",
        ),
        // GameCube / Wii
        Console.GAMECUBE to listOf(
            "org.dolphinemu.dolphinemu",  // Dolphin
        ),
        Console.WII to listOf(
            "org.dolphinemu.dolphinemu",
        ),
        // Switch
        Console.SWITCH to listOf(
            "org.yuzu.yuzu_emu",          // Yuzu
        ),
    )

    /**
     * Tries to launch the ROM with an installed external emulator.
     * Returns true if an emulator was found and launched.
     */
    fun launchRom(context: Context, filePath: String, console: Console): Boolean {
        val pm = context.packageManager

        // Get known emulators for this console
        val knownPackages = emulatorPackages[console] ?: emptyList()

        // Try known emulator packages first
        for (pkg in knownPackages) {
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                return tryLaunchWithPackage(context, filePath, pkg)
            }
        }

        // Fallback: try generic Intent.ACTION_VIEW with the file
        return tryGenericLaunch(context, filePath)
    }

    private fun tryLaunchWithPackage(context: Context, filePath: String, packageName: String): Boolean {
        return try {
            val uri = getFileUri(context, filePath)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, getMimeType(filePath))
                setPackage(packageName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun tryGenericLaunch(context: Context, filePath: String): Boolean {
        return try {
            val uri = getFileUri(context, filePath)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, getMimeType(filePath))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Open with emulator")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(chooser)
                true
            } else {
                Toast.makeText(
                    context,
                    "No emulator found for this ROM. Install one from the Play Store.",
                    Toast.LENGTH_LONG
                ).show()
                false
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Could not open ROM: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }

    private fun getFileUri(context: Context, filePath: String): Uri {
        // SAF URIs are already content:// URIs
        if (filePath.startsWith("content://")) {
            return Uri.parse(filePath)
        }
        // For file paths, use FileProvider
        val file = File(filePath)
        return try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            // Fallback to direct file URI
            Uri.fromFile(file)
        }
    }

    private fun getMimeType(filePath: String): String {
        return "application/octet-stream"
    }

    /**
     * Returns a list of emulator app names installed on the device for the given console.
     */
    fun getInstalledEmulators(context: Context, console: Console): List<String> {
        val pm = context.packageManager
        val packages = emulatorPackages[console] ?: return emptyList()
        return packages.filter { pkg ->
            pm.getLaunchIntentForPackage(pkg) != null
        }
    }
}
