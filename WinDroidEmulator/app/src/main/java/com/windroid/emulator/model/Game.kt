package com.windroid.emulator.model

data class Game(
    val id: String,
    val name: String,
    val packageName: String,
    val executablePath: String,
    val iconPath: String?,
    val coverImagePath: String?,
    val installDate: Long,
    val lastPlayed: Long,
    val playTime: Long, // in minutes
    val isInstalled: Boolean,
    val gameSize: Long, // in bytes
    val settings: GameSettings
)

data class GameSettings(
    val resolutionWidth: Int = 1280,
    val resolutionHeight: Int = 720,
    val fullscreen: Boolean = true,
    val vsync: Boolean = false,
    val fpsLimit: Int = 60,
    val graphicsQuality: GraphicsQuality = GraphicsQuality.MEDIUM,
    val audioVolume: Int = 80,
    val audioEnabled: Boolean = true,
    val controlsMapping: Map<String, String> = emptyMap(),
    val customArguments: String = "",
    val environmentVariables: Map<String, String> = emptyMap()
)

enum class GraphicsQuality {
    LOW,
    MEDIUM,
    HIGH,
    ULTRA
}

data class BackupFile(
    val gameId: String,
    val backupPath: String,
    val backupDate: Long,
    val backupSize: Long,
    val backupType: BackupType
)

enum class BackupType {
    FULL,
    SAVE_DATA_ONLY,
    SETTINGS_ONLY
}

data class ArchiveFile(
    val name: String,
    val path: String,
    val size: Long,
    val isDirectory: Boolean,
    val lastModified: Long,
    val mimeType: String?
)
