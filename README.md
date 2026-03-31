# OS-EMU - Retro Game Emulator Frontend

A Nintendo 3DS-inspired Android emulator frontend that provides a beautiful, console-like experience for playing retro and modern games.

## Features

### 3DS-Inspired Interface
- **Dual-panel layout**: Top screen shows game info, bottom screen shows icon grid (just like a real 3DS)
- **Status bar**: Real-time clock, battery indicator, WiFi status
- **Animated icon grid**: Bouncy selection animations, rounded app tiles
- **Tab navigation**: Quick access to Home, Library, Favorites, Settings

### Game Library
- **Multi-console support**: NES, SNES, N64, Game Boy, GBA, DS, 3DS, GameCube, Wii, Wii U, Switch, Master System, Genesis, Saturn, Dreamcast, Game Gear, PS1, PS2, PSP, Arcade
- **Automatic ROM scanning**: Recursively scans directories for game files
- **Smart sorting**: Games organized by console and alphabetical order
- **Search & filter**: Quick search with console filter chips
- **Grid & list views**: Toggle between icon grid and detailed list

### Emulation
- **Libretro core integration**: Uses standard libretro cores for maximum compatibility
- **Auto-save**: Automatic save state creation at configurable intervals
- **10 manual save slots**: Per game, with screenshot previews
- **Fast forward**: Speed up gameplay
- **Shader support**: CRT, LCD, smoothing, sharpening, and custom shader presets

### Customization
- **5 built-in themes**: 3DS White, Dark Mode, Nintendo Red, Sega Blue, Coral Pink
- **Custom backgrounds**: Set your own top and bottom screen images
- **Icon styles**: Rounded, Square, Circle, Squircle
- **Touch overlay**: Adjustable opacity and scale

### Adaptive Layout
- **Smartphone portrait**: Classic 3DS stacked layout
- **Smartphone landscape**: Side-by-side panels
- **Gaming handhelds**: Optimized for Ayn Thor, Steam Deck, ROG Ally
- **Tablets**: Larger icons and expanded grids
- **TV mode**: Gamepad-first navigation

### Controls
- **On-screen touch controls**: D-pad, face buttons, shoulders, start/select
- **Physical gamepad support**: Xbox, PlayStation, and generic controllers
- **Handheld controls**: Native support for built-in gamepad buttons

## Architecture

```
com.osemu.app/
├── core/                    # Emulator engine & ROM scanner
│   ├── EmulatorEngine.kt    # Libretro core lifecycle management
│   └── GameScanner.kt       # ROM file discovery & import
├── data/
│   ├── model/               # Data classes (Game, Console, SaveState, Theme)
│   ├── database/            # Room database, DAOs
│   └── repository/          # Data access layer
├── ui/
│   ├── theme/               # 3DS-inspired Material 3 theme
│   ├── components/          # Reusable UI components (StatusBar, GameIcon, TabBar)
│   ├── screens/             # Full screens (Home, Library, Settings, Emulator, Themes)
│   └── navigation/          # Nav graph & ViewModel
└── utils/                   # Adaptive layout, input management
```

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Database**: Room
- **Preferences**: DataStore
- **Image loading**: Coil
- **Navigation**: Navigation Compose
- **Architecture**: MVVM with StateFlow

## Building

```bash
./gradlew assembleDebug
```

## Adding Cores

Place libretro `.so` core files in the app's internal `cores/` directory:
```
/data/data/com.osemu.app/files/cores/
```

Core files should follow the naming convention: `{corename}_libretro_android.so`

## Supported File Extensions

| Console | Extensions |
|---------|-----------|
| NES | .nes, .unf, .fds |
| SNES | .sfc, .smc |
| N64 | .n64, .z64, .v64 |
| Game Boy | .gb, .gbc |
| GBA | .gba |
| DS | .nds |
| 3DS | .3ds, .cia, .cxi |
| GameCube | .iso, .gcm, .rvz |
| Wii | .iso, .wbfs, .rvz |
| Genesis | .md, .gen, .smd |
| PS1 | .bin, .cue, .chd, .pbp |
| PS2 | .iso, .chd |
| PSP | .iso, .cso |
| Arcade | .zip, .7z |

## License

This project is for educational purposes.
