package com.windroid.emulator.manager

import android.content.Context
import android.content.SharedPreferences
import com.windroid.emulator.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class GameStorageManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("game_storage", Context.MODE_PRIVATE)
    private val gamesDir = File(context.filesDir, "games")
    private val backupDir = File(context.filesDir, "backups")
    private val tempDir = File(context.cacheDir, "temp")
    
    init {
        gamesDir.mkdirs()
        backupDir.mkdirs()
        tempDir.mkdirs()
    }
    
    suspend fun installGame(archivePath: String, gameId: String): Result<Game> = withContext(Dispatchers.IO) {
        try {
            val gameDir = File(gamesDir, gameId)
            if (!gameDir.exists()) {
                gameDir.mkdirs()
            }
            
            // Extract archive
            val extractor = ArchiveExtractor(context)
            val extractionResult = extractor.extractArchive(archivePath, gameDir.absolutePath)
            
            if (!extractionResult) {
                return@withContext Result.failure(Exception("Failed to extract archive"))
            }
            
            // Create game metadata
            val game = createGameMetadata(gameId, gameDir)
            saveGame(game)
            
            Result.success(game)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun createGameMetadata(gameId: String, gameDir: File): Game {
        val executableFile = findExecutableFile(gameDir)
        val iconFile = findIconFile(gameDir)
        val coverFile = findCoverFile(gameDir)
        
        return Game(
            id = gameId,
            name = gameDir.name,
            packageName = "com.windroid.game.$gameId",
            executablePath = executableFile?.absolutePath ?: "",
            iconPath = iconFile?.absolutePath,
            coverImagePath = coverFile?.absolutePath,
            installDate = System.currentTimeMillis(),
            lastPlayed = 0,
            playTime = 0,
            isInstalled = true,
            gameSize = calculateDirSize(gameDir),
            settings = GameSettings()
        )
    }
    
    private fun findExecutableFile(dir: File): File? {
        val executableExtensions = listOf("exe", "bat", "cmd", "sh")
        return dir.walkTopDown()
            .filter { it.isFile && it.extension.lowercase() in executableExtensions }
            .firstOrNull()
    }
    
    private fun findIconFile(dir: File): File? {
        val iconNames = listOf("icon.png", "icon.jpg", "icon.ico", "logo.png", "logo.jpg")
        return iconNames.firstNotNullOfOrNull { name ->
            dir.walkTopDown().find { it.name.equals(name, ignoreCase = true) }
        }
    }
    
    private fun findCoverFile(dir: File): File? {
        val coverNames = listOf("cover.png", "cover.jpg", "boxart.png", "boxart.jpg", "poster.png", "poster.jpg")
        return coverNames.firstNotNullOfOrNull { name ->
            dir.walkTopDown().find { it.name.equals(name, ignoreCase = true) }
        }
    }
    
    private fun calculateDirSize(dir: File): Long {
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }
    
    suspend fun uninstallGame(gameId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val gameDir = File(gamesDir, gameId)
            if (gameDir.exists()) {
                gameDir.deleteRecursively()
            }
            removeGame(gameId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createBackup(gameId: String, backupType: BackupType = BackupType.FULL): Result<BackupFile> = withContext(Dispatchers.IO) {
        try {
            val game = getGame(gameId) ?: return@withContext Result.failure(Exception("Game not found"))
            val gameDir = File(gamesDir, gameId)
            
            if (!gameDir.exists()) {
                return@withContext Result.failure(Exception("Game directory not found"))
            }
            
            val timestamp = System.currentTimeMillis()
            val backupFileName = "${gameId}_${timestamp}_${backupType.name}.zip"
            val backupFile = File(backupDir, backupFileName)
            
            val archiver = ArchiveArchiver(context)
            val filesToBackup = when (backupType) {
                BackupType.FULL -> listOf(gameDir)
                BackupType.SAVE_DATA_ONLY -> {
                    val saveDirs = listOf("saves", "savegames", "savedata").mapNotNull { name ->
                        File(gameDir, name).takeIf { it.exists() }
                    }
                    saveDirs
                }
                BackupType.SETTINGS_ONLY -> {
                    val settingsFile = File(gameDir, "settings.ini")
                    if (settingsFile.exists()) listOf(settingsFile) else emptyList()
                }
            }
            
            if (filesToBackup.isEmpty()) {
                return@withContext Result.failure(Exception("No files to backup"))
            }
            
            archiver.createArchive(backupFile, filesToBackup)
            
            val backup = BackupFile(
                gameId = gameId,
                backupPath = backupFile.absolutePath,
                backupDate = timestamp,
                backupSize = backupFile.length(),
                backupType = backupType
            )
            
            saveBackupInfo(backup)
            Result.success(backup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun restoreBackup(backupFile: BackupFile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupFilePath = File(backupFile.backupPath)
            if (!backupFilePath.exists()) {
                return@withContext Result.failure(Exception("Backup file not found"))
            }
            
            val gameDir = File(gamesDir, backupFile.gameId)
            if (!gameDir.exists()) {
                gameDir.mkdirs()
            }
            
            val extractor = ArchiveExtractor(context)
            val extractionResult = extractor.extractArchive(backupFile.backupPath, gameDir.absolutePath)
            
            if (!extractionResult) {
                return@withContext Result.failure(Exception("Failed to extract backup"))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteBackup(backupFile: BackupFile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupFilePath = File(backupFile.backupPath)
            if (backupFilePath.exists()) {
                backupFilePath.delete()
            }
            removeBackupInfo(backupFile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getGame(gameId: String): Game? {
        val gameJson = prefs.getString("game_$gameId", null) ?: return null
        return parseGameFromJson(gameJson)
    }
    
    fun getAllGames(): List<Game> {
        val games = mutableListOf<Game>()
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("game_") && value is String) {
                parseGameFromJson(value)?.let { games.add(it) }
            }
        }
        return games.sortedByDescending { it.lastPlayed }
    }
    
    fun getAllBackups(): List<BackupFile> {
        val backups = mutableListOf<BackupFile>()
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("backup_") && value is String) {
                parseBackupFromJson(value)?.let { backups.add(it) }
            }
        }
        return backups.sortedByDescending { it.backupDate }
    }
    
    private fun saveGame(game: Game) {
        val editor = prefs.edit()
        editor.putString("game_${game.id}", gameToJson(game))
        editor.apply()
    }
    
    private fun removeGame(gameId: String) {
        val editor = prefs.edit()
        editor.remove("game_$gameId")
        editor.apply()
    }
    
    private fun saveBackupInfo(backup: BackupFile) {
        val editor = prefs.edit()
        editor.putString("backup_${backup.gameId}_${backup.backupDate}", backupToJson(backup))
        editor.apply()
    }
    
    private fun removeBackupInfo(backup: BackupFile) {
        val editor = prefs.edit()
        editor.remove("backup_${backup.gameId}_${backup.backupDate}")
        editor.apply()
    }
    
    private fun gameToJson(game: Game): String {
        val json = JSONObject()
        json.put("id", game.id)
        json.put("name", game.name)
        json.put("packageName", game.packageName)
        json.put("executablePath", game.executablePath)
        json.put("iconPath", game.iconPath)
        json.put("coverImagePath", game.coverImagePath)
        json.put("installDate", game.installDate)
        json.put("lastPlayed", game.lastPlayed)
        json.put("playTime", game.playTime)
        json.put("isInstalled", game.isInstalled)
        json.put("gameSize", game.gameSize)
        
        val settingsJson = JSONObject()
        settingsJson.put("resolutionWidth", game.settings.resolutionWidth)
        settingsJson.put("resolutionHeight", game.settings.resolutionHeight)
        settingsJson.put("fullscreen", game.settings.fullscreen)
        settingsJson.put("vsync", game.settings.vsync)
        settingsJson.put("fpsLimit", game.settings.fpsLimit)
        settingsJson.put("graphicsQuality", game.settings.graphicsQuality.name)
        settingsJson.put("audioVolume", game.settings.audioVolume)
        settingsJson.put("audioEnabled", game.settings.audioEnabled)
        settingsJson.put("customArguments", game.settings.customArguments)
        json.put("settings", settingsJson)
        
        return json.toString()
    }
    
    private fun parseGameFromJson(jsonStr: String): Game? {
        return try {
            val json = JSONObject(jsonStr)
            val settingsJson = json.getJSONObject("settings")
            Game(
                id = json.getString("id"),
                name = json.getString("name"),
                packageName = json.getString("packageName"),
                executablePath = json.getString("executablePath"),
                iconPath = json.optString("iconPath", null),
                coverImagePath = json.optString("coverImagePath", null),
                installDate = json.getLong("installDate"),
                lastPlayed = json.getLong("lastPlayed"),
                playTime = json.getLong("playTime"),
                isInstalled = json.getBoolean("isInstalled"),
                gameSize = json.getLong("gameSize"),
                settings = GameSettings(
                    resolutionWidth = settingsJson.getInt("resolutionWidth"),
                    resolutionHeight = settingsJson.getInt("resolutionHeight"),
                    fullscreen = settingsJson.getBoolean("fullscreen"),
                    vsync = settingsJson.getBoolean("vsync"),
                    fpsLimit = settingsJson.getInt("fpsLimit"),
                    graphicsQuality = GraphicsQuality.valueOf(settingsJson.getString("graphicsQuality")),
                    audioVolume = settingsJson.getInt("audioVolume"),
                    audioEnabled = settingsJson.getBoolean("audioEnabled"),
                    customArguments = settingsJson.optString("customArguments", "")
                )
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun backupToJson(backup: BackupFile): String {
        val json = JSONObject()
        json.put("gameId", backup.gameId)
        json.put("backupPath", backup.backupPath)
        json.put("backupDate", backup.backupDate)
        json.put("backupSize", backup.backupSize)
        json.put("backupType", backup.backupType.name)
        return json.toString()
    }
    
    private fun parseBackupFromJson(jsonStr: String): BackupFile? {
        return try {
            val json = JSONObject(jsonStr)
            BackupFile(
                gameId = json.getString("gameId"),
                backupPath = json.getString("backupPath"),
                backupDate = json.getLong("backupDate"),
                backupSize = json.getLong("backupSize"),
                backupType = BackupType.valueOf(json.getString("backupType"))
            )
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun updateGameSettings(gameId: String, settings: GameSettings): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val game = getGame(gameId) ?: return@withContext Result.failure(Exception("Game not found"))
            val updatedGame = game.copy(settings = settings)
            saveGame(updatedGame)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getFreeSpace(): Long {
        return gamesDir.freeSpace
    }
    
    fun getTotalSpace(): Long {
        return gamesDir.totalSpace
    }
}
