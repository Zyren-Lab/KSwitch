/*
 * KSwitch - The GUI Backup Tool for Linux
 * Copyright (C) 2024-2025 ZyrenLab
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
 
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * AppEngine - Handles Installed Applications
 * Uses pm list packages for scanning and pm path for APK extraction
 */
object AppEngine {
    
    /**
     * Scan all user-installed apps (not system apps)
     */
    suspend fun scanInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val apps = mutableListOf<InstalledApp>()
        
        try {
            // Get list of user packages
            val listOutput = AdbClient.execute(listOf("shell", "pm", "list", "packages", "-3"), timeoutSeconds = 60)
            
            val packages = listOutput.lines()
                .filter { it.startsWith("package:") }
                .map { it.removePrefix("package:").trim() }
            
            packages.forEach { packageName ->
                try {
                    // Get APK path for each package
                    val pathOutput = AdbClient.execute(listOf("shell", "pm", "path", packageName), timeoutSeconds = 10)
                    val apkPath = pathOutput.lines()
                        .firstOrNull { it.startsWith("package:") }
                        ?.removePrefix("package:")
                        ?.trim()
                    
                    if (!apkPath.isNullOrEmpty()) {
                        apps.add(InstalledApp(packageName, apkPath))
                    }
                } catch (e: Exception) {
                    // Skip apps that fail (split APKs, etc.)
                }
            }
        } catch (e: Exception) {
            println("AppEngine scan error: ${e.message}")
        }
        
        apps
    }
    
    /**
     * Backup installed apps to ./backup/APKS/
     */
    suspend fun backupApps(
        apps: List<InstalledApp>,
        backupRoot: File,
        onProgress: (current: Int, total: Int, name: String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val apksDir = File(backupRoot, "APKS")
        apksDir.mkdirs()
        
        apps.forEachIndexed { index, app ->
            onProgress(index, apps.size, app.packageName)
            
            try {
                val localFile = File(apksDir, "${app.packageName}.apk")
                AdbClient.execute(listOf("pull", app.apkPath, localFile.absolutePath), timeoutSeconds = 120)
            } catch (e: Exception) {
                println("Failed to backup ${app.packageName}: ${e.message}")
            }
            
            onProgress(index + 1, apps.size, app.packageName)
        }
    }
    
    /**
     * Restore apps from ./backup/APKS/
     */
    suspend fun restoreApps(
        backupRoot: File,
        onProgress: (current: Int, total: Int, name: String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val apksDir = File(backupRoot, "APKS")
        if (!apksDir.exists()) return@withContext
        
        val apkFiles = apksDir.listFiles { f -> f.extension == "apk" } ?: return@withContext
        
        apkFiles.forEachIndexed { index, apkFile ->
            onProgress(index, apkFiles.size, apkFile.nameWithoutExtension)
            
            try {
                AdbClient.execute(listOf("install", "-r", apkFile.absolutePath), timeoutSeconds = 120)
            } catch (e: Exception) {
                println("Failed to install ${apkFile.name}: ${e.message}")
            }
            
            onProgress(index + 1, apkFiles.size, apkFile.nameWithoutExtension)
        }
    }
    
    /**
     * Count APKs in backup folder
     */
    fun countBackupApps(backupRoot: File): Int {
        val apksDir = File(backupRoot, "APKS")
        return apksDir.listFiles { f -> f.extension == "apk" }?.size ?: 0
    }
}
