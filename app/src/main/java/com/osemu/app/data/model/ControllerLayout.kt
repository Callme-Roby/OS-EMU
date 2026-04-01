package com.osemu.app.data.model

import androidx.compose.ui.graphics.Color

/**
 * Defines the controller layout for each console.
 * Each console has its own set of buttons, colors, and layout style.
 */
data class ControllerLayout(
    val console: Console,
    val dpad: Boolean = true,
    val analogStick: Boolean = false,
    val cStick: Boolean = false,
    val actionButtons: List<ActionButtonDef>,
    val shoulderButtons: List<ShoulderButtonDef> = emptyList(),
    val centerButtons: List<CenterButtonDef>,
    val extraButtons: List<ActionButtonDef> = emptyList(),
    val screenRatio: Float = 4f / 3f
)

data class ActionButtonDef(
    val label: String,
    val color: Color,
    val position: ButtonPosition
)

data class ShoulderButtonDef(
    val label: String,
    val side: ShoulderSide
)

data class CenterButtonDef(
    val label: String
)

enum class ButtonPosition {
    TOP, BOTTOM, LEFT, RIGHT,
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
}

enum class ShoulderSide {
    LEFT, RIGHT
}

object ControllerLayouts {

    fun forConsole(console: Console): ControllerLayout = when (console) {

        // --- Game Boy / Game Boy Color ---
        Console.GBC -> ControllerLayout(
            console = console,
            actionButtons = listOf(
                ActionButtonDef("A", Color(0xFFAB2236), ButtonPosition.RIGHT),
                ActionButtonDef("B", Color(0xFFAB2236), ButtonPosition.BOTTOM)
            ),
            centerButtons = listOf(
                CenterButtonDef("SELECT"),
                CenterButtonDef("START")
            ),
            screenRatio = 10f / 9f
        )

        // --- Game Boy Advance ---
        Console.GBA -> ControllerLayout(
            console = console,
            actionButtons = listOf(
                ActionButtonDef("A", Color(0xFF7B2D8B), ButtonPosition.RIGHT),
                ActionButtonDef("B", Color(0xFF7B2D8B), ButtonPosition.BOTTOM)
            ),
            shoulderButtons = listOf(
                ShoulderButtonDef("L", ShoulderSide.LEFT),
                ShoulderButtonDef("R", ShoulderSide.RIGHT)
            ),
            centerButtons = listOf(
                CenterButtonDef("SELECT"),
                CenterButtonDef("START")
            ),
            screenRatio = 3f / 2f
        )

        // --- NES ---
        Console.NES -> ControllerLayout(
            console = console,
            actionButtons = listOf(
                ActionButtonDef("A", Color(0xFFCB3234), ButtonPosition.RIGHT),
                ActionButtonDef("B", Color(0xFFCB3234), ButtonPosition.LEFT)
            ),
            centerButtons = listOf(
                CenterButtonDef("SELECT"),
                CenterButtonDef("START")
            )
        )

        // --- SNES ---
        Console.SNES -> ControllerLayout(
            console = console,
            actionButtons = listOf(
                ActionButtonDef("X", Color(0xFF4169E1), ButtonPosition.TOP),
                ActionButtonDef("A", Color(0xFFDE3232), ButtonPosition.RIGHT),
                ActionButtonDef("B", Color(0xFFEFBF00), ButtonPosition.BOTTOM),
                ActionButtonDef("Y", Color(0xFF22AA22), ButtonPosition.LEFT)
            ),
            shoulderButtons = listOf(
                ShoulderButtonDef("L", ShoulderSide.LEFT),
                ShoulderButtonDef("R", ShoulderSide.RIGHT)
            ),
            centerButtons = listOf(
                CenterButtonDef("SELECT"),
                CenterButtonDef("START")
            )
        )

        // --- Nintendo 64 ---
        Console.N64 -> ControllerLayout(
            console = console,
            analogStick = true,
            actionButtons = listOf(
                ActionButtonDef("A", Color(0xFF2266CC), ButtonPosition.BOTTOM),
                ActionButtonDef("B", Color(0xFF22AA44), ButtonPosition.LEFT)
            ),
            extraButtons = listOf(
                ActionButtonDef("C\u25B2", Color(0xFFE8B818), ButtonPosition.TOP),
                ActionButtonDef("C\u25B6", Color(0xFFE8B818), ButtonPosition.RIGHT),
                ActionButtonDef("C\u25BC", Color(0xFFE8B818), ButtonPosition.BOTTOM),
                ActionButtonDef("C\u25C0", Color(0xFFE8B818), ButtonPosition.LEFT)
            ),
            shoulderButtons = listOf(
                ShoulderButtonDef("L", ShoulderSide.LEFT),
                ShoulderButtonDef("R", ShoulderSide.RIGHT),
                ShoulderButtonDef("Z", ShoulderSide.LEFT)
            ),
            centerButtons = listOf(
                CenterButtonDef("START")
            )
        )

        // --- Nintendo DS ---
        Console.NDS -> ControllerLayout(
            console = console,
            actionButtons = listOf(
                ActionButtonDef("X", Color(0xFF6E6E6E), ButtonPosition.TOP),
                ActionButtonDef("A", Color(0xFF6E6E6E), ButtonPosition.RIGHT),
                ActionButtonDef("B", Color(0xFF6E6E6E), ButtonPosition.BOTTOM),
                ActionButtonDef("Y", Color(0xFF6E6E6E), ButtonPosition.LEFT)
            ),
            shoulderButtons = listOf(
                ShoulderButtonDef("L", ShoulderSide.LEFT),
                ShoulderButtonDef("R", ShoulderSide.RIGHT)
            ),
            centerButtons = listOf(
                CenterButtonDef("SELECT"),
                CenterButtonDef("START")
            ),
            screenRatio = 256f / 192f
        )

        // --- Nintendo 3DS ---
        Console.N3DS -> ControllerLayout(
            console = console,
            analogStick = true,
            cStick = true,
            actionButtons = listOf(
                ActionButtonDef("X", Color(0xFF6E6E6E), ButtonPosition.TOP),
                ActionButtonDef("A", Color(0xFF6E6E6E), ButtonPosition.RIGHT),
                ActionButtonDef("B", Color(0xFF6E6E6E), ButtonPosition.BOTTOM),
                ActionButtonDef("Y", Color(0xFF6E6E6E), ButtonPosition.LEFT)
            ),
            shoulderButtons = listOf(
                ShoulderButtonDef("L", ShoulderSide.LEFT),
                ShoulderButtonDef("R", ShoulderSide.RIGHT),
                ShoulderButtonDef("ZL", ShoulderSide.LEFT),
                ShoulderButtonDef("ZR", ShoulderSide.RIGHT)
            ),
            centerButtons = listOf(
                CenterButtonDef("SELECT"),
                CenterButtonDef("START")
            ),
            screenRatio = 5f / 3f
        )

        // --- GameCube ---
        Console.GAMECUBE -> ControllerLayout(
            console = console,
            analogStick = true,
            cStick = true,
            actionButtons = listOf(
                ActionButtonDef("A", Color(0xFF22BB44), ButtonPosition.BOTTOM),
                ActionButtonDef("B", Color(0xFFDD2222), ButtonPosition.LEFT),
                ActionButtonDef("X", Color(0xFF888888), ButtonPosition.RIGHT),
                ActionButtonDef("Y", Color(0xFF888888), ButtonPosition.TOP)
            ),
            shoulderButtons = listOf(
                ShoulderButtonDef("L", ShoulderSide.LEFT),
                ShoulderButtonDef("R", ShoulderSide.RIGHT),
                ShoulderButtonDef("Z", ShoulderSide.RIGHT)
            ),
            centerButtons = listOf(
                CenterButtonDef("START")
            ),
            screenRatio = 4f / 3f
        )

        // --- Wii ---
        Console.WII -> ControllerLayout(
            console = console,
            analogStick = true,
            actionButtons = listOf(
                ActionButtonDef("A", Color(0xFF4488DD), ButtonPosition.BOTTOM),
                ActionButtonDef("B", Color(0xFF4488DD), ButtonPosition.TOP),
                ActionButtonDef("1", Color(0xFF888888), ButtonPosition.LEFT),
                ActionButtonDef("2", Color(0xFF888888), ButtonPosition.RIGHT)
            ),
            shoulderButtons = listOf(
                ShoulderButtonDef("Z", ShoulderSide.LEFT),
                ShoulderButtonDef("C", ShoulderSide.RIGHT)
            ),
            centerButtons = listOf(
                CenterButtonDef("-"),
                CenterButtonDef("HOME"),
                CenterButtonDef("+")
            )
        )

        // --- Wii U ---
        Console.WIIU -> ControllerLayout(
            console = console,
            analogStick = true,
            cStick = true,
            actionButtons = listOf(
                ActionButtonDef("X", Color(0xFF4488DD), ButtonPosition.TOP),
                ActionButtonDef("A", Color(0xFF22BB44), ButtonPosition.RIGHT),
                ActionButtonDef("B", Color(0xFFDD2222), ButtonPosition.BOTTOM),
                ActionButtonDef("Y", Color(0xFF4488DD), ButtonPosition.LEFT)
            ),
            shoulderButtons = listOf(
                ShoulderButtonDef("L", ShoulderSide.LEFT),
                ShoulderButtonDef("R", ShoulderSide.RIGHT),
                ShoulderButtonDef("ZL", ShoulderSide.LEFT),
                ShoulderButtonDef("ZR", ShoulderSide.RIGHT)
            ),
            centerButtons = listOf(
                CenterButtonDef("-"),
                CenterButtonDef("HOME"),
                CenterButtonDef("+")
            ),
            screenRatio = 16f / 9f
        )

        // --- Switch ---
        Console.SWITCH -> ControllerLayout(
            console = console,
            analogStick = true,
            cStick = true,
            actionButtons = listOf(
                ActionButtonDef("X", Color(0xFF6E6E6E), ButtonPosition.TOP),
                ActionButtonDef("A", Color(0xFF6E6E6E), ButtonPosition.RIGHT),
                ActionButtonDef("B", Color(0xFF6E6E6E), ButtonPosition.BOTTOM),
                ActionButtonDef("Y", Color(0xFF6E6E6E), ButtonPosition.LEFT)
            ),
            shoulderButtons = listOf(
                ShoulderButtonDef("L", ShoulderSide.LEFT),
                ShoulderButtonDef("R", ShoulderSide.RIGHT),
                ShoulderButtonDef("ZL", ShoulderSide.LEFT),
                ShoulderButtonDef("ZR", ShoulderSide.RIGHT)
            ),
            centerButtons = listOf(
                CenterButtonDef("-"),
                CenterButtonDef("HOME"),
                CenterButtonDef("+")
            ),
            screenRatio = 16f / 9f
        )

        // --- Master System ---
        Console.MASTER_SYSTEM -> ControllerLayout(
            console = console,
            actionButtons = listOf(
                ActionButtonDef("1", Color(0xFF333333), ButtonPosition.LEFT),
                ActionButtonDef("2", Color(0xFF333333), ButtonPosition.RIGHT)
            ),
            centerButtons = listOf(
                CenterButtonDef("PAUSE")
            )
        )

        // --- Genesis / Mega Drive ---
        Console.GENESIS -> ControllerLayout(
            console = console,
            actionButtons = listOf(
                ActionButtonDef("A", Color(0xFF333333), ButtonPosition.BOTTOM_LEFT),
                ActionButtonDef("B", Color(0xFF333333), ButtonPosition.BOTTOM),
                ActionButtonDef("C", Color(0xFF333333), ButtonPosition.BOTTOM_RIGHT),
                ActionButtonDef("X", Color(0xFF555555), ButtonPosition.TOP_LEFT),
                ActionButtonDef("Y", Color(0xFF555555), ButtonPosition.TOP),
                ActionButtonDef("Z", Color(0xFF555555), ButtonPosition.TOP_RIGHT)
            ),
            centerButtons = listOf(
                CenterButtonDef("MODE"),
                CenterButtonDef("START")
            )
        )

        // --- Saturn ---
        Console.SATURN -> ControllerLayout(
            console = console,
            actionButtons = listOf(
                ActionButtonDef("A", Color(0xFF3366CC), ButtonPosition.BOTTOM_LEFT),
                ActionButtonDef("B", Color(0xFF3366CC), ButtonPosition.BOTTOM),
                ActionButtonDef("C", Color(0xFF3366CC), ButtonPosition.BOTTOM_RIGHT),
                ActionButtonDef("X", Color(0xFF3366CC), ButtonPosition.TOP_LEFT),
                ActionButtonDef("Y", Color(0xFF3366CC), ButtonPosition.TOP),
                ActionButtonDef("Z", Color(0xFF3366CC), ButtonPosition.TOP_RIGHT)
            ),
            shoulderButtons = listOf(
                ShoulderButtonDef("L", ShoulderSide.LEFT),
                ShoulderButtonDef("R", ShoulderSide.RIGHT)
            ),
            centerButtons = listOf(
                CenterButtonDef("START")
            )
        )

        // --- Dreamcast ---
        Console.DREAMCAST -> ControllerLayout(
            console = console,
            analogStick = true,
            actionButtons = listOf(
                ActionButtonDef("A", Color(0xFF22AA44), ButtonPosition.BOTTOM),
                ActionButtonDef("B", Color(0xFFDD2222), ButtonPosition.RIGHT),
                ActionButtonDef("X", Color(0xFF2266CC), ButtonPosition.LEFT),
                ActionButtonDef("Y", Color(0xFFEEBB22), ButtonPosition.TOP)
            ),
            shoulderButtons = listOf(
                ShoulderButtonDef("L", ShoulderSide.LEFT),
                ShoulderButtonDef("R", ShoulderSide.RIGHT)
            ),
            centerButtons = listOf(
                CenterButtonDef("START")
            )
        )

        // --- Game Gear ---
        Console.GAME_GEAR -> ControllerLayout(
            console = console,
            actionButtons = listOf(
                ActionButtonDef("1", Color(0xFF333333), ButtonPosition.LEFT),
                ActionButtonDef("2", Color(0xFF333333), ButtonPosition.RIGHT)
            ),
            centerButtons = listOf(
                CenterButtonDef("START")
            ),
            screenRatio = 10f / 9f
        )

        // --- PlayStation ---
        Console.PS1 -> ControllerLayout(
            console = console,
            actionButtons = listOf(
                ActionButtonDef("\u25B3", Color(0xFF44BB88), ButtonPosition.TOP),
                ActionButtonDef("\u25CB", Color(0xFFEE5555), ButtonPosition.RIGHT),
                ActionButtonDef("\u00D7", Color(0xFF6699DD), ButtonPosition.BOTTOM),
                ActionButtonDef("\u25A1", Color(0xFFDD77BB), ButtonPosition.LEFT)
            ),
            shoulderButtons = listOf(
                ShoulderButtonDef("L1", ShoulderSide.LEFT),
                ShoulderButtonDef("R1", ShoulderSide.RIGHT),
                ShoulderButtonDef("L2", ShoulderSide.LEFT),
                ShoulderButtonDef("R2", ShoulderSide.RIGHT)
            ),
            centerButtons = listOf(
                CenterButtonDef("SELECT"),
                CenterButtonDef("START")
            )
        )

        // --- PlayStation 2 ---
        Console.PS2 -> ControllerLayout(
            console = console,
            analogStick = true,
            cStick = true,
            actionButtons = listOf(
                ActionButtonDef("\u25B3", Color(0xFF44BB88), ButtonPosition.TOP),
                ActionButtonDef("\u25CB", Color(0xFFEE5555), ButtonPosition.RIGHT),
                ActionButtonDef("\u00D7", Color(0xFF6699DD), ButtonPosition.BOTTOM),
                ActionButtonDef("\u25A1", Color(0xFFDD77BB), ButtonPosition.LEFT)
            ),
            shoulderButtons = listOf(
                ShoulderButtonDef("L1", ShoulderSide.LEFT),
                ShoulderButtonDef("R1", ShoulderSide.RIGHT),
                ShoulderButtonDef("L2", ShoulderSide.LEFT),
                ShoulderButtonDef("R2", ShoulderSide.RIGHT)
            ),
            centerButtons = listOf(
                CenterButtonDef("SELECT"),
                CenterButtonDef("START")
            ),
            screenRatio = 4f / 3f
        )

        // --- PSP ---
        Console.PSP -> ControllerLayout(
            console = console,
            analogStick = true,
            actionButtons = listOf(
                ActionButtonDef("\u25B3", Color(0xFF44BB88), ButtonPosition.TOP),
                ActionButtonDef("\u25CB", Color(0xFFEE5555), ButtonPosition.RIGHT),
                ActionButtonDef("\u00D7", Color(0xFF6699DD), ButtonPosition.BOTTOM),
                ActionButtonDef("\u25A1", Color(0xFFDD77BB), ButtonPosition.LEFT)
            ),
            shoulderButtons = listOf(
                ShoulderButtonDef("L", ShoulderSide.LEFT),
                ShoulderButtonDef("R", ShoulderSide.RIGHT)
            ),
            centerButtons = listOf(
                CenterButtonDef("SELECT"),
                CenterButtonDef("START")
            ),
            screenRatio = 16f / 9f
        )

        // --- Arcade ---
        Console.ARCADE -> ControllerLayout(
            console = console,
            actionButtons = listOf(
                ActionButtonDef("1", Color(0xFFEE4444), ButtonPosition.BOTTOM_LEFT),
                ActionButtonDef("2", Color(0xFF4488EE), ButtonPosition.BOTTOM),
                ActionButtonDef("3", Color(0xFF44BB44), ButtonPosition.BOTTOM_RIGHT),
                ActionButtonDef("4", Color(0xFFEEBB22), ButtonPosition.TOP_LEFT),
                ActionButtonDef("5", Color(0xFFDD66DD), ButtonPosition.TOP),
                ActionButtonDef("6", Color(0xFF44DDDD), ButtonPosition.TOP_RIGHT)
            ),
            centerButtons = listOf(
                CenterButtonDef("COIN"),
                CenterButtonDef("START")
            )
        )
    }
}
