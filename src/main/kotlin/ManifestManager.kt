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
import java.text.SimpleDateFormat
import java.util.*

/**
 * BackupManifest - Records backup contents for easy restore
 */
data class BackupManifest(
    val version: String = "1.0",
    val timestamp: String = "",
    val deviceName: String = "",
    val categories: Map<String, List<ManifestItem>> = emptyMap()
)

data class ManifestItem(
    val localPath: String,
    val remotePath: String,
    val size: Long,
    val type: String // "file", "app", "contact", "calllog"
)

object ManifestManager {
    private const val MANIFEST_FILE = "backup_manifest.json"
    
    /**
     * Create manifest during backup
     */
    fun createManifest(
        backupDir: File,
        deviceName: String,
        filesBackedUp: List<TransferableItem>,
        appsBackedUp: List<InstalledApp>,
        contactsCount: Int,
        callLogsCount: Int
    ) {
        val categories = mutableMapOf<String, MutableList<ManifestItem>>()
        
        // Files
        filesBackedUp.forEach { item ->
            val category = when {
                item.localRelativePath.contains("DCIM") || 
                item.name.matches(Regex(".*\\.(jpg|jpeg|png|gif|webp|heic)$", RegexOption.IGNORE_CASE)) -> "Images"
                item.name.matches(Regex(".*\\.(mp4|mkv|avi|mov|3gp)$", RegexOption.IGNORE_CASE)) -> "Videos"
                item.name.matches(Regex(".*\\.(mp3|wav|m4a|flac|ogg)$", RegexOption.IGNORE_CASE)) -> "Audio"
                item.name.matches(Regex(".*\\.(zip|rar|7z|apk)$", RegexOption.IGNORE_CASE)) -> "Archives"
                item.name.matches(Regex(".*\\.(pdf|doc|docx|txt|xls|xlsx)$", RegexOption.IGNORE_CASE)) -> "Documents"
                else -> "Others"
            }
            
            val localFile = File(backupDir, item.localRelativePath)
            categories.getOrPut(category) { mutableListOf() }.add(
                ManifestItem(
                    localPath = item.localRelativePath,
                    remotePath = item.remotePath,
                    size = if (localFile.exists()) localFile.length() else 0,
                    type = "file"
                )
            )
        }
        
        // Apps
        if (appsBackedUp.isNotEmpty()) {
            categories["InstalledApps"] = appsBackedUp.map { app ->
                val apkFile = File(backupDir, "APKS/${app.packageName}.apk")
                ManifestItem(
                    localPath = "APKS/${app.packageName}.apk",
                    remotePath = app.apkPath,
                    size = if (apkFile.exists()) apkFile.length() else 0,
                    type = "app"
                )
            }.toMutableList()
        }
        
        // Contacts
        if (contactsCount > 0) {
            categories["Contacts"] = mutableListOf(
                ManifestItem("Contacts/contacts.vcf", "", 0, "contact")
            )
        }
        
        // Call Logs
        if (callLogsCount > 0) {
            categories["CallLogs"] = mutableListOf(
                ManifestItem("CallLog/call_logs.json", "", 0, "calllog")
            )
        }
        
        // Write manifest
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val manifestFile = File(backupDir, MANIFEST_FILE)
        
        val json = buildString {
            appendLine("{")
            appendLine("""  "version": "1.0",""")
            appendLine("""  "timestamp": "${dateFormat.format(Date())}",""")
            appendLine("""  "deviceName": "$deviceName",""")
            appendLine("""  "categories": {""")
            
            val categoryEntries = categories.entries.toList()
            categoryEntries.forEachIndexed { catIdx, (category, items) ->
                appendLine("""    "$category": [""")
                items.forEachIndexed { idx, item ->
                    val comma = if (idx < items.size - 1) "," else ""
                    appendLine("""      {"localPath": "${item.localPath.replace("\\", "/")}", "remotePath": "${item.remotePath}", "size": ${item.size}, "type": "${item.type}"}$comma""")
                }
                val catComma = if (catIdx < categoryEntries.size - 1) "," else ""
                appendLine("    ]$catComma")
            }
            
            appendLine("  }")
            appendLine("}")
        }
        
        manifestFile.writeText(json)
    }
    
    /**
     * Read manifest for restore
     */
    suspend fun readManifest(backupDir: File): BackupManifest? = withContext(Dispatchers.IO) {
        val manifestFile = File(backupDir, MANIFEST_FILE)
        if (!manifestFile.exists()) return@withContext null
        
        try {
            val content = manifestFile.readText()
            val manifest = parseManifest(content)
            
            // Validate sizes against local files to fix "0b" issues
            val updatedCategories = manifest.categories.mapValues { (_, items) ->
                items.map { item ->
                    val file = File(backupDir, item.localPath)
                    if (file.exists() && item.size == 0L) {
                        item.copy(size = file.length())
                    } else {
                        item
                    }
                }
            }
            
            manifest.copy(categories = updatedCategories)
        } catch (e: Exception) {
            println("Failed to read manifest: ${e.message}")
            null
        }
    }
    
    private fun parseManifest(json: String): BackupManifest {
        // Simple JSON parsing (no external library needed)
        val version = Regex(""""version":\s*"([^"]+)"""").find(json)?.groupValues?.get(1) ?: "1.0"
        val timestamp = Regex(""""timestamp":\s*"([^"]+)"""").find(json)?.groupValues?.get(1) ?: ""
        val deviceName = Regex(""""deviceName":\s*"([^"]+)"""").find(json)?.groupValues?.get(1) ?: ""
        
        val categories = mutableMapOf<String, List<ManifestItem>>()
        
        // Parse each category
        val categoryPattern = Regex(""""(\w+)":\s*\[([\s\S]*?)\]""")
        categoryPattern.findAll(json).forEach { match ->
            val categoryName = match.groupValues[1]
            if (categoryName !in listOf("version", "timestamp", "deviceName")) {
                val itemsJson = match.groupValues[2]
                val items = mutableListOf<ManifestItem>()
                
                val itemPattern = Regex("""\{\s*"localPath":\s*"([^"]*)",\s*"remotePath":\s*"([^"]*)",\s*"size":\s*(\d+),\s*"type":\s*"([^"]*)"\s*\}""")
                itemPattern.findAll(itemsJson).forEach { itemMatch ->
                    items.add(ManifestItem(
                        localPath = itemMatch.groupValues[1],
                        remotePath = itemMatch.groupValues[2],
                        size = itemMatch.groupValues[3].toLongOrNull() ?: 0,
                        type = itemMatch.groupValues[4]
                    ))
                }
                
                if (items.isNotEmpty()) {
                    categories[categoryName] = items
                }
            }
        }
        
        return BackupManifest(version, timestamp, deviceName, categories)
    }
    
    /**
     * Scan backup folder if no manifest exists
     */
    suspend fun scanBackupFolder(backupDir: File): BackupManifest = withContext(Dispatchers.IO) {
        val categories = mutableMapOf<String, MutableList<ManifestItem>>()
        
        // Scan APKS folder
        val apksDir = File(backupDir, "APKS")
        if (apksDir.exists()) {
            categories["InstalledApps"] = apksDir.listFiles { f -> f.extension == "apk" }
                ?.map { ManifestItem(it.relativeTo(backupDir).path, "", it.length(), "app") }
                ?.toMutableList() ?: mutableListOf()
        }
        
        // Scan sdcard folder
        val sdcardDir = File(backupDir, "sdcard")
        if (sdcardDir.exists()) {
            sdcardDir.walk().filter { it.isFile }.forEach { file ->
                val relativePath = file.relativeTo(backupDir).path.replace("\\", "/")
                val remotePath = "/" + relativePath
                val ext = file.extension.lowercase()
                
                val category = when (ext) {
                    in listOf("jpg", "jpeg", "png", "gif", "webp", "heic") -> "Images"
                    in listOf("mp4", "mkv", "avi", "mov", "3gp") -> "Videos"
                    in listOf("mp3", "wav", "m4a", "flac", "ogg") -> "Audio"
                    in listOf("zip", "rar", "7z", "apk") -> "Archives"
                    in listOf("pdf", "doc", "docx", "txt", "xls", "xlsx") -> "Documents"
                    else -> "Others"
                }
                
                categories.getOrPut(category) { mutableListOf() }.add(
                    ManifestItem(relativePath, remotePath, file.length(), "file")
                )
            }
        }
        
        // Check for contacts
        val contactsFile = File(backupDir, "Contacts/contacts.vcf")
        if (contactsFile.exists()) {
            categories["Contacts"] = mutableListOf(
                ManifestItem("Contacts/contacts.vcf", "", contactsFile.length(), "contact")
            )
        }
        
        // Check for call logs
        val callLogsFile = File(backupDir, "CallLog/call_logs.json")
        if (callLogsFile.exists()) {
            categories["CallLogs"] = mutableListOf(
                ManifestItem("CallLog/call_logs.json", "", callLogsFile.length(), "calllog")
            )
        }
        
        BackupManifest(
            version = "1.0",
            timestamp = "Unknown",
            deviceName = "Unknown",
            categories = categories
        )
    }
}
