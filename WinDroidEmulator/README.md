# WinDroid Emulator - PC Game Emulator for Android

A comprehensive Android application for emulating lightweight PC Windows games with advanced game management, archive extraction, and performance optimization features.

## Features

### 🎮 Game Library Management
- **Install Games**: Install games from .zip, .rar, and .7z archives
- **Uninstall Games**: Remove games and free up storage space
- **Game Metadata**: Automatic detection of executables, icons, and cover art
- **Play Time Tracking**: Track how long you've played each game
- **Last Played**: Keep track of your gaming history

### 💾 Backup & Restore System
- **Full Backup**: Create complete backups of installed games
- **Save Data Backup**: Backup only save files and progress
- **Settings Backup**: Backup game-specific settings
- **Quick Restore**: Restore games from backups instantly
- **Backup Management**: View and manage all your backups

### 📦 Archive Explorer
- **Multi-format Support**: Extract .zip, .rar, and .7z files
- **Browse Archives**: View archive contents before extraction
- **Selective Extraction**: Extract specific files or folders
- **File Information**: View file details (size, date, type)
- **Custom Extraction Path**: Choose where to extract files

### ⚡ Performance Optimization
- **CPU Allocation**: Configure number of CPU cores (1-4)
- **RAM Management**: Allocate 1GB - 4GB RAM for emulator
- **Graphics Backend**: Choose between Vulkan, OpenGL ES 3.0, or 2.0
- **Resolution Scaling**: From 360p to 1080p
- **FPS Limiting**: 30/60/90/120 FPS or unlimited
- **VSync Support**: Reduce screen tearing
- **Shader Cache**: Faster game loading with cached shaders
- **Audio Latency**: Configurable from 10ms to 80ms
- **Performance Mode**: System-wide optimizations

### 🎨 Graphics & Audio Settings
- Per-game graphics quality settings (Low/Medium/High/Ultra)
- Custom resolution configuration
- Audio volume and enhancement controls
- VSync toggle for smooth gameplay
- Frame skip options for demanding games

## Project Structure

```
WinDroidEmulator/
├── app/
│   ├── src/main/
│   │   ├── java/com/windroid/emulator/
│   │   │   ├── MainActivity.kt
│   │   │   ├── adapter/
│   │   │   │   ├── GameAdapter.kt
│   │   │   │   └── ArchiveAdapter.kt
│   │   │   ├── manager/
│   │   │   │   ├── GameStorageManager.kt
│   │   │   │   ├── ArchiveManager.kt
│   │   │   │   └── PerformanceOptimizer.kt
│   │   │   ├── model/
│   │   │   │   └── Game.kt
│   │   │   ├── ui/
│   │   │   │   ├── GameLibraryFragment.kt
│   │   │   │   ├── ArchiveExplorerFragment.kt
│   │   │   │   └── SettingsFragment.kt
│   │   │   └── util/
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── menu/
│   │   │   ├── navigation/
│   │   │   ├── values/
│   │   │   └── xml/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Technical Specifications

### Minimum Requirements
- Android 7.0 (API 24) or higher
- 2GB RAM minimum (4GB recommended)
- OpenGL ES 3.0 or Vulkan support
- 500MB free storage space

### Dependencies
- **AndroidX**: Core libraries and fragments
- **Material Design**: Modern UI components
- **Apache Commons Compress**: Archive handling
- **SevenZipJBinding**: Advanced archive support
- **Glide**: Image loading and caching
- **Kotlin Coroutines**: Async operations

## Building the Project

### Prerequisites
1. Android Studio Arctic Fox or later
2. JDK 17
3. Android SDK 34

### Build Steps
```bash
# Clone the repository
git clone <repository-url>
cd WinDroidEmulator

# Open in Android Studio
# Or build via command line:
./gradlew assembleDebug

# The APK will be generated at:
# app/build/outputs/apk/debug/app-debug.apk
```

## Usage Guide

### Installing a Game
1. Tap the "+" button in Game Library
2. Select a game archive (.zip, .rar, .7z)
3. Wait for extraction and installation
4. Tap the game to launch

### Creating a Backup
1. Long-press on a game in the library
2. Select "Create Backup"
3. Choose backup type (Full/Save Data/Settings)
4. Wait for backup completion

### Optimizing Performance
1. Go to Settings
2. Adjust CPU cores and RAM allocation
3. Select appropriate graphics backend
4. Set resolution and FPS limit
5. Enable shader cache for faster loading

## Architecture

The app follows MVVM (Model-View-ViewModel) architecture with:

- **Models**: Data classes for Game, GameSettings, BackupFile, ArchiveFile
- **Managers**: Business logic for storage, archives, and performance
- **UI**: Fragments with ViewBinding for type-safe view access
- **Adapters**: RecyclerView adapters with DiffUtil for efficient updates

## Key Components

### GameStorageManager
Handles all game-related operations:
- Installation and uninstallation
- Backup creation and restoration
- Settings management
- Storage space monitoring

### ArchiveExtractor/ArchiveArchiver
Manages archive operations:
- Multi-format extraction (ZIP, RAR, 7Z)
- Archive content listing
- Selective file extraction
- Archive creation for backups

### PerformanceOptimizer
Optimizes emulator performance:
- Dynamic resource allocation
- Graphics backend selection
- Latency reduction
- Stability monitoring

## License

This project is provided as-is for educational purposes.

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

## Disclaimer

This emulator is designed for running legally owned PC games on Android devices. Users are responsible for ensuring they have the right to run any games they install.
