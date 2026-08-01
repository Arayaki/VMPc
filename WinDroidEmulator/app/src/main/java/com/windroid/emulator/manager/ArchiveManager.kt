package com.windroid.emulator.manager

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ArchiveExtractor(private val context: Context) {
    
    suspend fun extractArchive(archivePath: String, destinationPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(archivePath)
            if (!file.exists()) {
                return@withContext false
            }
            
            val extension = file.extension.lowercase()
            when (extension) {
                "zip" -> extractZip(archivePath, destinationPath)
                "rar" -> extractRar(archivePath, destinationPath)
                "7z" -> extract7z(archivePath, destinationPath)
                else -> extractGeneric(archivePath, destinationPath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun extractZip(archivePath: String, destinationPath: String): Boolean {
        return try {
            val destDir = File(destinationPath)
            if (!destDir.exists()) {
                destDir.mkdirs()
            }
            
            ZipFile(File(archivePath)).use { zipFile ->
                zipFile.entries.asSequence().forEach { entry ->
                    extractZipEntry(zipFile, entry, destDir)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun extractZipEntry(zipFile: ZipFile, entry: ZipArchiveEntry, destDir: File) {
        val outputFile = File(destDir, entry.name)
        
        if (entry.isDirectory) {
            outputFile.mkdirs()
            return
        }
        
        // Ensure parent directory exists
        outputFile.parentFile?.mkdirs()
        
        zipFile.getInputStream(entry).use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
    }
    
    private fun extractRar(archivePath: String, destinationPath: String): Boolean {
        // RAR extraction using SevenZipJBinding or fallback to basic implementation
        return try {
            // For now, use a basic implementation
            // In production, integrate with SevenZipJBinding for full RAR support
            extractWithSevenZip(archivePath, destinationPath)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun extract7z(archivePath: String, destinationPath: String): Boolean {
        return try {
            extractWithSevenZip(archivePath, destinationPath)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun extractWithSevenZip(archivePath: String, destinationPath: String): Boolean {
        // Placeholder for SevenZipJBinding implementation
        // This would use net.sf.sevenzipjbinding library
        return try {
            // Basic implementation using Java's built-in support where available
            val file = File(archivePath)
            val destDir = File(destinationPath)
            if (!destDir.exists()) {
                destDir.mkdirs()
            }
            
            // Try generic extraction
            extractGeneric(archivePath, destinationPath)
        } catch (e: Exception) {
            false
        }
    }
    
    private fun extractGeneric(archivePath: String, destinationPath: String): Boolean {
        return try {
            val destDir = File(destinationPath)
            if (!destDir.exists()) {
                destDir.mkdirs()
            }
            
            FileInputStream(archivePath).use { fis ->
                ZipInputStream(fis).use { zis ->
                    var entry: ZipEntry?
                    while (zis.nextEntry.also { entry = it } != null) {
                        val entryName = entry!!.name
                        val outputFile = File(destDir, entryName)
                        
                        if (entry!!.isDirectory) {
                            outputFile.mkdirs()
                        } else {
                            outputFile.parentFile?.mkdirs()
                            FileOutputStream(outputFile).use { output ->
                                zis.copyTo(output)
                            }
                        }
                        zis.closeEntry()
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun listArchiveContents(archivePath: String): List<ArchiveFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<ArchiveFile>()
        try {
            val file = File(archivePath)
            if (!file.exists()) {
                return@withContext files
            }
            
            val extension = file.extension.lowercase()
            when (extension) {
                "zip" -> {
                    ZipFile(file).use { zipFile ->
                        zipFile.entries.asSequence().forEach { entry ->
                            files.add(
                                ArchiveFile(
                                    name = entry.name.substringAfterLast('/').ifEmpty { entry.name.dropLast(1) },
                                    path = entry.name,
                                    size = entry.size,
                                    isDirectory = entry.isDirectory,
                                    lastModified = entry.lastModifiedDate?.time ?: 0,
                                    mimeType = getMimeType(entry.name)
                                )
                            )
                        }
                    }
                }
                else -> {
                    // Generic listing
                    FileInputStream(archivePath).use { fis ->
                        ZipInputStream(fis).use { zis ->
                            var entry: ZipEntry?
                            while (zis.nextEntry.also { entry = it } != null) {
                                files.add(
                                    ArchiveFile(
                                        name = entry!!.name.substringAfterLast('/').ifEmpty { entry!!.name.dropLast(1) },
                                        path = entry!!.name,
                                        size = entry!!.size,
                                        isDirectory = entry!!.isDirectory,
                                        lastModified = entry!!.time,
                                        mimeType = getMimeType(entry!!.name)
                                    )
                                )
                                zis.closeEntry()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        files
    }
    
    private fun getMimeType(fileName: String): String? {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "exe" -> "application/x-msdownload"
            "dll" -> "application/x-msdownload"
            "png", "jpg", "jpeg", "gif", "bmp" -> "image/$extension"
            "txt" -> "text/plain"
            "ini", "cfg", "conf" -> "text/plain"
            "zip", "rar", "7z" -> "application/archive"
            else -> null
        }
    }
}

class ArchiveArchiver(private val context: Context) {
    
    suspend fun createArchive(outputPath: String, filesToArchive: List<File>): Boolean = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(outputPath)
            outputFile.parentFile?.mkdirs()
            
            ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
                filesToArchive.forEach { file ->
                    addFileToZip(zos, file, "")
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun addFileToZip(zos: ZipOutputStream, file: File, parentPath: String) {
        val entryName = if (parentPath.isEmpty()) file.name else "$parentPath/${file.name}"
        
        if (file.isDirectory) {
            file.listFiles()?.forEach { child ->
                addFileToZip(zos, child, entryName)
            }
        } else {
            val entry = ZipEntry(entryName)
            zos.putNextEntry(entry)
            FileInputStream(file).use { fis ->
                fis.copyTo(zos)
            }
            zos.closeEntry()
        }
    }
}
