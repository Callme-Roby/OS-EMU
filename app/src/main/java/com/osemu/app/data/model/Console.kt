package com.osemu.app.data.model

/**
 * Supported console platforms with their libretro core identifiers.
 */
enum class Console(
    val displayName: String,
    val manufacturer: String,
    val year: Int,
    val coreNames: List<String>,
    val fileExtensions: List<String>,
    val iconRes: String,
    val generation: Int
) {
    // --- Nintendo ---
    NES(
        displayName = "Nintendo Entertainment System",
        manufacturer = "Nintendo",
        year = 1983,
        coreNames = listOf("fceumm", "nestopia", "mesen"),
        fileExtensions = listOf("nes", "unf", "unif", "fds"),
        iconRes = "ic_console_nes",
        generation = 3
    ),
    SNES(
        displayName = "Super Nintendo",
        manufacturer = "Nintendo",
        year = 1990,
        coreNames = listOf("snes9x", "bsnes", "mesen-s"),
        fileExtensions = listOf("sfc", "smc", "swc"),
        iconRes = "ic_console_snes",
        generation = 4
    ),
    N64(
        displayName = "Nintendo 64",
        manufacturer = "Nintendo",
        year = 1996,
        coreNames = listOf("mupen64plus_next", "parallel_n64"),
        fileExtensions = listOf("n64", "z64", "v64", "ndd"),
        iconRes = "ic_console_n64",
        generation = 5
    ),
    GBC(
        displayName = "Game Boy / Color",
        manufacturer = "Nintendo",
        year = 1989,
        coreNames = listOf("gambatte", "mgba", "sameboy"),
        fileExtensions = listOf("gb", "gbc"),
        iconRes = "ic_console_gbc",
        generation = 4
    ),
    GBA(
        displayName = "Game Boy Advance",
        manufacturer = "Nintendo",
        year = 2001,
        coreNames = listOf("mgba", "vba_next", "gpsp"),
        fileExtensions = listOf("gba"),
        iconRes = "ic_console_gba",
        generation = 6
    ),
    NDS(
        displayName = "Nintendo DS",
        manufacturer = "Nintendo",
        year = 2004,
        coreNames = listOf("desmume", "melonds"),
        fileExtensions = listOf("nds", "dsi"),
        iconRes = "ic_console_nds",
        generation = 7
    ),
    N3DS(
        displayName = "Nintendo 3DS",
        manufacturer = "Nintendo",
        year = 2011,
        coreNames = listOf("citra"),
        fileExtensions = listOf("3ds", "cia", "cxi", "app"),
        iconRes = "ic_console_3ds",
        generation = 8
    ),
    GAMECUBE(
        displayName = "GameCube",
        manufacturer = "Nintendo",
        year = 2001,
        coreNames = listOf("dolphin"),
        fileExtensions = listOf("iso", "gcm", "gcz", "ciso", "rvz"),
        iconRes = "ic_console_gc",
        generation = 6
    ),
    WII(
        displayName = "Wii",
        manufacturer = "Nintendo",
        year = 2006,
        coreNames = listOf("dolphin"),
        fileExtensions = listOf("iso", "wbfs", "gcz", "ciso", "rvz", "wad"),
        iconRes = "ic_console_wii",
        generation = 7
    ),
    WIIU(
        displayName = "Wii U",
        manufacturer = "Nintendo",
        year = 2012,
        coreNames = listOf("cemu"),
        fileExtensions = listOf("wud", "wux", "rpx", "wua"),
        iconRes = "ic_console_wiiu",
        generation = 8
    ),
    SWITCH(
        displayName = "Nintendo Switch",
        manufacturer = "Nintendo",
        year = 2017,
        coreNames = listOf("yuzu", "ryujinx"),
        fileExtensions = listOf("nsp", "xci", "nro", "nso"),
        iconRes = "ic_console_switch",
        generation = 9
    ),

    // --- Sega ---
    MASTER_SYSTEM(
        displayName = "Master System",
        manufacturer = "Sega",
        year = 1985,
        coreNames = listOf("genesis_plus_gx", "picodrive"),
        fileExtensions = listOf("sms", "sg"),
        iconRes = "ic_console_sms",
        generation = 3
    ),
    GENESIS(
        displayName = "Mega Drive / Genesis",
        manufacturer = "Sega",
        year = 1988,
        coreNames = listOf("genesis_plus_gx", "picodrive", "blastem"),
        fileExtensions = listOf("md", "gen", "smd", "bin"),
        iconRes = "ic_console_genesis",
        generation = 4
    ),
    SATURN(
        displayName = "Sega Saturn",
        manufacturer = "Sega",
        year = 1994,
        coreNames = listOf("mednafen_saturn", "yabause", "kronos"),
        fileExtensions = listOf("iso", "bin", "cue", "chd"),
        iconRes = "ic_console_saturn",
        generation = 5
    ),
    DREAMCAST(
        displayName = "Dreamcast",
        manufacturer = "Sega",
        year = 1998,
        coreNames = listOf("flycast", "reicast"),
        fileExtensions = listOf("gdi", "cdi", "chd", "cue"),
        iconRes = "ic_console_dreamcast",
        generation = 6
    ),
    GAME_GEAR(
        displayName = "Game Gear",
        manufacturer = "Sega",
        year = 1990,
        coreNames = listOf("genesis_plus_gx"),
        fileExtensions = listOf("gg"),
        iconRes = "ic_console_gg",
        generation = 4
    ),

    // --- Sony ---
    PS1(
        displayName = "PlayStation",
        manufacturer = "Sony",
        year = 1994,
        coreNames = listOf("pcsx_rearmed", "mednafen_psx", "duckstation", "swanstation"),
        fileExtensions = listOf("bin", "cue", "iso", "chd", "pbp", "img"),
        iconRes = "ic_console_ps1",
        generation = 5
    ),
    PS2(
        displayName = "PlayStation 2",
        manufacturer = "Sony",
        year = 2000,
        coreNames = listOf("pcsx2", "play"),
        fileExtensions = listOf("iso", "bin", "chd", "cso", "gz"),
        iconRes = "ic_console_ps2",
        generation = 6
    ),
    PSP(
        displayName = "PlayStation Portable",
        manufacturer = "Sony",
        year = 2004,
        coreNames = listOf("ppsspp"),
        fileExtensions = listOf("iso", "cso", "pbp"),
        iconRes = "ic_console_psp",
        generation = 7
    ),

    // --- Arcade ---
    ARCADE(
        displayName = "Arcade",
        manufacturer = "Various",
        year = 1978,
        coreNames = listOf("fbneo", "mame2003_plus", "mame"),
        fileExtensions = listOf("zip", "7z"),
        iconRes = "ic_console_arcade",
        generation = 0
    );

    companion object {
        fun fromExtension(ext: String): List<Console> {
            return entries.filter { ext.lowercase() in it.fileExtensions }
        }

        fun byManufacturer(): Map<String, List<Console>> {
            return entries.groupBy { it.manufacturer }
        }

        fun byGeneration(): Map<Int, List<Console>> {
            return entries.groupBy { it.generation }.toSortedMap()
        }
    }
}
